package cloud.alchemy.fabut.tracking;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ThreadLocal-based tracker that records which objects are created and which fields
 * are accessed during a test's tracked segment (between takeSnapshot and @AfterEach).
 *
 * The tracker is activated by takeSnapshot() and deactivated in @AfterEach.
 * ByteBuddy-instrumented constructors call registerIfActive() and
 * instrumented getters call recordAccessIfActive().
 */
public class UsageTracker {

    private static final ThreadLocal<UsageTracker> CURRENT = new ThreadLocal<>();

    private boolean active;
    private final Map<Integer, TrackedObject> trackedObjects = new ConcurrentHashMap<>();
    private final Map<Class<?>, Set<String>> fieldNamesCache = new ConcurrentHashMap<>();
    private Map<Class<?>, List<String>> ignoredFields = Collections.emptyMap();

    /**
     * Sets the current UsageTracker for this thread.
     */
    public static void setCurrent(UsageTracker tracker) {
        CURRENT.set(tracker);
    }

    /**
     * Removes the current UsageTracker from this thread.
     */
    public static void removeCurrent() {
        CURRENT.remove();
    }

    /**
     * Returns the current UsageTracker for this thread, or null if none set.
     */
    public static UsageTracker getCurrent() {
        return CURRENT.get();
    }

    /**
     * Called from ByteBuddy-instrumented constructors.
     * Registers the object for tracking if a tracker is active.
     */
    public static void registerIfActive(Object obj) {
        if (obj == null) return;
        UsageTracker tracker = CURRENT.get();
        if (tracker != null && tracker.active) {
            tracker.register(obj);
        }
    }

    /**
     * Called from ByteBuddy-instrumented getters.
     * Records the field access if a tracker is active.
     */
    public static void recordAccessIfActive(Object obj, String fieldName) {
        if (obj == null) return;
        UsageTracker tracker = CURRENT.get();
        if (tracker != null && tracker.active) {
            tracker.recordAccess(obj, fieldName);
        }
    }

    /**
     * Checks if a tracker is currently active on this thread.
     */
    public static boolean isCurrentActive() {
        UsageTracker tracker = CURRENT.get();
        return tracker != null && tracker.active;
    }

    public void setIgnoredFields(Map<Class<?>, List<String>> ignoredFields) {
        this.ignoredFields = ignoredFields;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void reset() {
        active = false;
        trackedObjects.clear();
    }

    /**
     * Registers an object for tracking. Computes field names from getter methods.
     */
    public void register(Object obj) {
        int hash = System.identityHashCode(obj);
        if (trackedObjects.containsKey(hash)) {
            return;
        }
        Set<String> fields = getFieldNames(obj.getClass());
        trackedObjects.put(hash, new TrackedObject(hash, obj.getClass(), fields));
    }

    /**
     * Records a field access on a tracked object.
     */
    public void recordAccess(Object obj, String fieldName) {
        TrackedObject tracked = trackedObjects.get(System.identityHashCode(obj));
        if (tracked != null) {
            tracked.recordAccess(fieldName);
        }
    }

    public Collection<TrackedObject> getTrackedObjects() {
        return Collections.unmodifiableCollection(trackedObjects.values());
    }

    public boolean hasTrackedObjects() {
        return !trackedObjects.isEmpty();
    }

    public UsageReport getReport() {
        return new UsageReport(new ArrayList<>(trackedObjects.values()));
    }

    /**
     * Computes and caches the set of field names for a class based on its getter methods.
     */
    Set<String> getFieldNames(Class<?> clazz) {
        return fieldNamesCache.computeIfAbsent(clazz, c -> {
            var fields = new LinkedHashSet<String>();
            for (Method method : c.getMethods()) {
                String name = method.getName();
                if (method.getParameterCount() != 0) continue;
                if (method.getReturnType() == void.class) continue;
                if (method.getDeclaringClass() == Object.class) continue;

                String fieldName = null;
                if (name.startsWith("get") && name.length() > 3) {
                    fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                } else if (name.startsWith("is") && name.length() > 2
                        && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
                    fieldName = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                }
                if (fieldName != null) {
                    // Verify field exists on the class hierarchy
                    if (hasField(c, fieldName)) {
                        fields.add(fieldName);
                    }
                }
            }
            List<String> ignored = ignoredFields.get(c);
            if (ignored != null) {
                fields.removeAll(ignored);
            }
            return fields;
        });
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
}
