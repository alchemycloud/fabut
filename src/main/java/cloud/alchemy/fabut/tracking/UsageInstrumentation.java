package cloud.alchemy.fabut.tracking;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * ByteBuddy-based class instrumentation for usage tracking.
 * Instruments constructor exit (to register objects) and getter methods
 * (to record field access) using retransformation, which is compatible
 * with other agents like JaCoCo.
 *
 * Classes are instrumented once and cached. The tracking is controlled by
 * {@link UsageTracker}'s active flag — instrumented code only records
 * when tracking is active, keeping overhead minimal.
 */
public class UsageInstrumentation {

    private static final Logger LOGGER = Logger.getLogger(UsageInstrumentation.class.getName());
    private static final Set<Class<?>> instrumentedClasses = ConcurrentHashMap.newKeySet();
    private static volatile boolean agentInstalled = false;
    private static Instrumentation instrumentation;

    private UsageInstrumentation() {}

    /**
     * Installs the ByteBuddy agent for class retransformation.
     * Safe to call multiple times — only installs once.
     *
     * @return true if agent is installed (or was already installed)
     */
    public static synchronized boolean install() {
        if (agentInstalled) {
            return true;
        }
        try {
            instrumentation = ByteBuddyAgent.install();
            agentInstalled = true;
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to install ByteBuddy agent. "
                    + "Usage tracking will not be available. "
                    + "Add -XX:+EnableDynamicAgentLoading to JVM args.", e);
            return false;
        }
    }

    /**
     * Checks if the ByteBuddy agent has been installed.
     */
    public static boolean isInstalled() {
        return agentInstalled;
    }

    /**
     * Instruments a class to track constructor calls and getter access.
     * Uses retransformation to be compatible with other bytecode agents (JaCoCo, etc.).
     * Idempotent — already-instrumented classes are skipped.
     *
     * @param clazz the class to instrument
     * @return true if instrumented successfully (or was already instrumented)
     */
    public static boolean instrumentClass(Class<?> clazz) {
        if (instrumentedClasses.contains(clazz)) {
            return true;
        }
        if (!agentInstalled) {
            if (!install()) {
                return false;
            }
        }
        try {
            ElementMatcher.Junction<MethodDescription> getterMatcher = buildGetterMatcher(clazz);

            new AgentBuilder.Default()
                    .disableClassFormatChanges()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .type(is(clazz))
                    .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                            builder
                                    .visit(Advice.to(ConstructorAdvice.class).on(isConstructor()))
                                    .visit(Advice.to(GetterAdvice.class).on(getterMatcher))
                    )
                    .installOn(instrumentation);

            // Force retransformation of already-loaded class
            instrumentation.retransformClasses(clazz);

            instrumentedClasses.add(clazz);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to instrument class: " + clazz.getName(), e);
            return false;
        }
    }

    /**
     * Checks if a class has been instrumented.
     */
    public static boolean isInstrumented(Class<?> clazz) {
        return instrumentedClasses.contains(clazz);
    }

    /**
     * Resets instrumentation state. Primarily used for testing.
     */
    static void resetForTesting() {
        instrumentedClasses.clear();
    }

    /**
     * Builds an ElementMatcher that matches getter methods backed by actual fields.
     */
    static ElementMatcher.Junction<MethodDescription> buildGetterMatcher(Class<?> clazz) {
        ElementMatcher.Junction<MethodDescription> matcher = none();

        for (Method method : clazz.getMethods()) {
            if (method.getParameterCount() != 0) continue;
            if (method.getReturnType() == void.class) continue;
            if (method.getDeclaringClass() == Object.class) continue;

            String name = method.getName();
            String fieldName = null;

            if (name.startsWith("get") && name.length() > 3) {
                fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
            } else if (name.startsWith("is") && name.length() > 2
                    && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
                fieldName = Character.toLowerCase(name.charAt(2)) + name.substring(3);
            }

            if (fieldName != null && hasField(clazz, fieldName)) {
                matcher = matcher.or(named(name).and(takesNoArguments()).and(not(isConstructor())));
            }
        }

        return matcher;
    }

    private static boolean hasField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                current.getDeclaredField(fieldName);
                return true;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return false;
    }

    /**
     * Extracts the field name from a getter method name.
     */
    public static String fieldNameFromGetter(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        if (methodName.startsWith("is") && methodName.length() > 2) {
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        }
        return methodName;
    }

    /**
     * ByteBuddy Advice applied to constructors.
     * After constructor completes, registers the object with UsageTracker.
     */
    public static class ConstructorAdvice {
        @Advice.OnMethodExit
        public static void afterConstructor(@Advice.This Object self) {
            UsageTracker.registerIfActive(self);
        }
    }

    /**
     * ByteBuddy Advice applied to getter methods.
     * Before the getter executes, records the field access with UsageTracker.
     */
    public static class GetterAdvice {
        @Advice.OnMethodEnter
        public static void beforeGetter(@Advice.This Object self, @Advice.Origin("#m") String methodName) {
            UsageTracker.recordAccessIfActive(self, UsageInstrumentation.fieldNameFromGetter(methodName));
        }
    }
}
