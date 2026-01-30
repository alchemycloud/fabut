package cloud.alchemy.fabut.processor;

import cloud.alchemy.fabut.annotation.Assertable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Annotation processor that generates type-safe assertion builders for classes annotated with @Assertable.
 */
@SupportedAnnotationTypes("cloud.alchemy.fabut.annotation.Assertable")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class AssertableProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Assertable.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "@Assertable can only be applied to classes", element);
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            try {
                generateAssertBuilder(typeElement);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Failed to generate assertion builder: " + e.getMessage(), element);
            }
        }
        return true;
    }

    private void generateAssertBuilder(TypeElement typeElement) throws IOException {
        String className = typeElement.getSimpleName().toString();
        String packageName = getPackageName(typeElement);
        String builderClassName = className + "Assert";
        String diffClassName = className + "Diff";
        String qualifiedBuilderName = packageName.isEmpty() ? builderClassName : packageName + "." + builderClassName;
        String qualifiedDiffName = packageName.isEmpty() ? diffClassName : packageName + "." + diffClassName;

        // Get ignored fields from annotation
        Assertable annotation = typeElement.getAnnotation(Assertable.class);
        Set<String> ignoredFields = new HashSet<>(Arrays.asList(annotation.ignoredFields()));

        // Collect fields with getters
        List<FieldInfo> fields = collectFields(typeElement, ignoredFields);

        // Generate the builder class
        JavaFileObject builderFile = filer.createSourceFile(qualifiedBuilderName, typeElement);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            generateBuilderClass(out, packageName, className, builderClassName, fields, ignoredFields);
        }

        // Generate the diff class (compile-time comparison, zero reflection)
        JavaFileObject diffFile = filer.createSourceFile(qualifiedDiffName, typeElement);
        try (PrintWriter out = new PrintWriter(diffFile.openWriter())) {
            generateDiffClass(out, packageName, className, diffClassName, fields, ignoredFields);
        }
    }

    private String getPackageName(TypeElement typeElement) {
        PackageElement packageElement = elementUtils.getPackageOf(typeElement);
        return packageElement.isUnnamed() ? "" : packageElement.getQualifiedName().toString();
    }

    private List<FieldInfo> collectFields(TypeElement typeElement, Set<String> ignoredFields) {
        List<FieldInfo> fields = new ArrayList<>();

        // Get all methods (including inherited)
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) enclosed;
                String methodName = method.getSimpleName().toString();

                // Check if it's a getter
                if (isGetter(method)) {
                    String fieldName = extractFieldName(methodName);

                    if (!ignoredFields.contains(fieldName)) {
                        TypeMirror returnType = method.getReturnType();
                        boolean isOptional = isOptionalType(returnType);
                        String typeString = getTypeString(returnType);
                        String innerTypeString = isOptional ? getOptionalInnerType(returnType) : typeString;

                        fields.add(new FieldInfo(fieldName, typeString, innerTypeString, isOptional));
                    }
                }
            }
        }

        // Also check superclass
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass.getKind() != TypeKind.NONE) {
            Element superElement = typeUtils.asElement(superclass);
            if (superElement instanceof TypeElement superTypeElement) {
                if (!superTypeElement.getQualifiedName().toString().equals("java.lang.Object")) {
                    fields.addAll(collectFields(superTypeElement, ignoredFields));
                }
            }
        }

        return fields;
    }

    private boolean isGetter(ExecutableElement method) {
        String name = method.getSimpleName().toString();
        return method.getParameters().isEmpty()
                && method.getReturnType().getKind() != TypeKind.VOID
                && (name.startsWith("get") || name.startsWith("is"))
                && !name.equals("getClass");
    }

    private String extractFieldName(String methodName) {
        String fieldName;
        if (methodName.startsWith("get")) {
            fieldName = methodName.substring(3);
        } else if (methodName.startsWith("is")) {
            fieldName = methodName.substring(2);
        } else {
            fieldName = methodName;
        }
        // Lowercase first character
        return Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    private boolean isOptionalType(TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) type;
            Element element = declaredType.asElement();
            return element.toString().equals("java.util.Optional");
        }
        return false;
    }

    private String getTypeString(TypeMirror type) {
        return type.toString();
    }

    private String getOptionalInnerType(TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) type;
            List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
            if (!typeArgs.isEmpty()) {
                return typeArgs.getFirst().toString();
            }
        }
        return "Object";
    }

    private void generateBuilderClass(PrintWriter out, String packageName, String className,
                                       String builderClassName, List<FieldInfo> fields, Set<String> ignoredFields) {
        // Package
        if (!packageName.isEmpty()) {
            out.println("package " + packageName + ";");
            out.println();
        }

        // Imports
        out.println("import cloud.alchemy.fabut.Fabut;");
        out.println("import cloud.alchemy.fabut.property.IProperty;");
        out.println("import java.util.ArrayList;");
        out.println("import java.util.List;");
        out.println("import java.util.Optional;");
        out.println();

        // Class javadoc
        out.println("/**");
        out.println(" * Generated assertion builder for {@link " + className + "}.");
        out.println(" * Provides type-safe fluent assertion methods.");
        out.println(" */");
        out.println("@javax.annotation.processing.Generated(\"cloud.alchemy.fabut.processor.AssertableProcessor\")");
        out.println("public class " + builderClassName + " {");
        out.println();

        // Fields
        out.println("    private final Fabut fabut;");
        out.println("    private final " + className + " object;");
        out.println("    private final boolean isSnapshot;");
        out.println("    private final List<IProperty> properties = new ArrayList<>();");
        out.println();

        // Constructor - auto-adds ignored() for fields specified in @Assertable(ignoredFields)
        out.println("    private " + builderClassName + "(Fabut fabut, " + className + " object, boolean isSnapshot) {");
        out.println("        this.fabut = fabut;");
        out.println("        this.object = object;");
        out.println("        this.isSnapshot = isSnapshot;");
        // Auto-add ignored for fields specified in annotation
        for (String ignoredField : ignoredFields) {
            out.println("        properties.add(fabut.ignored(\"" + ignoredField + "\"));");
        }
        out.println("    }");
        out.println();

        // Static factory methods - simplified (uses ThreadLocal)
        out.println("    /**");
        out.println("     * Start asserting the given object.");
        out.println("     * Uses the current Fabut instance from ThreadLocal (set automatically in @BeforeEach).");
        out.println("     */");
        out.println("    public static " + builderClassName + " assertThat(" + className + " object) {");
        out.println("        return new " + builderClassName + "(Fabut.current(), object, false);");
        out.println("    }");
        out.println();

        out.println("    /**");
        out.println("     * Start asserting the given entity against its snapshot.");
        out.println("     * Uses the current Fabut instance from ThreadLocal (set automatically in @BeforeEach).");
        out.println("     */");
        out.println("    public static " + builderClassName + " assertSnapshot(" + className + " entity) {");
        out.println("        return new " + builderClassName + "(Fabut.current(), entity, true);");
        out.println("    }");
        out.println();

        // Overloads with explicit Fabut (for advanced use cases)
        out.println("    /**");
        out.println("     * Start asserting the given object with explicit Fabut instance.");
        out.println("     */");
        out.println("    public static " + builderClassName + " assertThat(Fabut fabut, " + className + " object) {");
        out.println("        return new " + builderClassName + "(fabut, object, false);");
        out.println("    }");
        out.println();

        out.println("    /**");
        out.println("     * Start asserting the given entity against its snapshot with explicit Fabut instance.");
        out.println("     */");
        out.println("    public static " + builderClassName + " assertSnapshot(Fabut fabut, " + className + " entity) {");
        out.println("        return new " + builderClassName + "(fabut, entity, true);");
        out.println("    }");
        out.println();

        // Generate methods for each field
        for (FieldInfo field : fields) {
            generateFieldMethods(out, builderClassName, field);
        }

        // Verify method
        out.println("    /**");
        out.println("     * Execute the assertion.");
        out.println("     */");
        out.println("    public " + className + " verify() {");
        out.println("        IProperty[] props = properties.toArray(new IProperty[0]);");
        out.println("        if (isSnapshot) {");
        out.println("            return fabut.assertEntityWithSnapshot(object, props);");
        out.println("        } else {");
        out.println("            fabut.assertObject(object, props);");
        out.println("            return object;");
        out.println("        }");
        out.println("    }");

        out.println("}");
    }

    private void generateFieldMethods(PrintWriter out, String builderClassName, FieldInfo field) {
        String fieldPath = "\"" + field.name + "\"";

        // fieldIs(value) - exact value match
        out.println("    /** Assert " + field.name + " equals the expected value. */");
        out.println("    public " + builderClassName + " " + field.name + "Is(" + field.type + " expected) {");
        out.println("        properties.add(fabut.value(" + fieldPath + ", expected));");
        out.println("        return this;");
        out.println("    }");
        out.println();

        // fieldIsNull()
        out.println("    /** Assert " + field.name + " is null. */");
        out.println("    public " + builderClassName + " " + field.name + "IsNull() {");
        out.println("        properties.add(fabut.isNull(" + fieldPath + "));");
        out.println("        return this;");
        out.println("    }");
        out.println();

        // fieldIsNotNull()
        out.println("    /** Assert " + field.name + " is not null. */");
        out.println("    public " + builderClassName + " " + field.name + "IsNotNull() {");
        out.println("        properties.add(fabut.notNull(" + fieldPath + "));");
        out.println("        return this;");
        out.println("    }");
        out.println();

        // fieldIgnored()
        out.println("    /** Ignore " + field.name + " in this assertion. */");
        out.println("    public " + builderClassName + " " + field.name + "Ignored() {");
        out.println("        properties.add(fabut.ignored(" + fieldPath + "));");
        out.println("        return this;");
        out.println("    }");
        out.println();

        // For Optional fields, add isEmpty/isNotEmpty
        if (field.isOptional) {
            out.println("    /** Assert " + field.name + " is empty (Optional.empty()). */");
            out.println("    public " + builderClassName + " " + field.name + "IsEmpty() {");
            out.println("        properties.add(fabut.isEmpty(" + fieldPath + "));");
            out.println("        return this;");
            out.println("    }");
            out.println();

            out.println("    /** Assert " + field.name + " is not empty (Optional has value). */");
            out.println("    public " + builderClassName + " " + field.name + "IsNotEmpty() {");
            out.println("        properties.add(fabut.notEmpty(" + fieldPath + "));");
            out.println("        return this;");
            out.println("    }");
            out.println();

            // Convenience method to assert Optional contains specific value
            out.println("    /** Assert " + field.name + " contains the expected value. */");
            out.println("    public " + builderClassName + " " + field.name + "HasValue(" + field.innerType + " expected) {");
            out.println("        properties.add(fabut.value(" + fieldPath + ", Optional.of(expected)));");
            out.println("        return this;");
            out.println("    }");
            out.println();
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ==================== Diff Class Generation ====================

    private void generateDiffClass(PrintWriter out, String packageName, String className,
                                    String diffClassName, List<FieldInfo> fields, Set<String> ignoredFields) {
        // Package
        if (!packageName.isEmpty()) {
            out.println("package " + packageName + ";");
            out.println();
        }

        // Imports
        out.println("import cloud.alchemy.fabut.diff.Diff;");
        out.println("import java.util.Objects;");
        out.println();

        // Class javadoc
        out.println("/**");
        out.println(" * Generated compile-time diff comparator for {@link " + className + "}.");
        out.println(" * Zero reflection - all comparisons are statically generated.");
        out.println(" *");
        out.println(" * <p>Usage:");
        out.println(" * <pre>{@code");
        out.println(" * " + className + " before = ...;");
        out.println(" * " + className + " after = ...;");
        out.println(" * Diff<" + className + "> diff = " + diffClassName + ".compare(before, after);");
        out.println(" * if (diff.hasChanges()) {");
        out.println(" *     System.out.println(diff.toConsoleReport());");
        out.println(" * }");
        out.println(" * }</pre>");
        out.println(" */");
        out.println("@javax.annotation.processing.Generated(\"cloud.alchemy.fabut.processor.AssertableProcessor\")");
        out.println("public final class " + diffClassName + " {");
        out.println();

        // Private constructor
        out.println("    private " + diffClassName + "() {}");
        out.println();

        // Static compare method
        out.println("    /**");
        out.println("     * Compare two " + className + " instances and return a detailed diff.");
        out.println("     * Uses compile-time generated comparisons - no reflection.");
        out.println("     *");
        out.println("     * @param before the state before the change");
        out.println("     * @param after the state after the change");
        out.println("     * @return Diff containing all field comparisons");
        out.println("     */");
        out.println("    public static Diff<" + className + "> compare(" + className + " before, " + className + " after) {");
        out.println("        String identifier = \"" + className + "\" + getIdentifier(after);");
        out.println("        Diff<" + className + "> diff = new Diff<>(" + className + ".class, identifier);");
        out.println();

        // Generate comparison for each field
        for (FieldInfo field : fields) {
            String getter = "get" + capitalize(field.name) + "()";
            if (field.type.equals("boolean")) {
                getter = "is" + capitalize(field.name) + "()";
            }
            out.println("        diff.field(\"" + field.name + "\", ");
            out.println("            before == null ? null : before." + getter + ",");
            out.println("            after == null ? null : after." + getter + ");");
        }

        out.println();
        out.println("        return diff;");
        out.println("    }");
        out.println();

        // Static compare method with custom identifier
        out.println("    /**");
        out.println("     * Compare two " + className + " instances with a custom identifier.");
        out.println("     *");
        out.println("     * @param before the state before the change");
        out.println("     * @param after the state after the change");
        out.println("     * @param identifier custom identifier for the diff report");
        out.println("     * @return Diff containing all field comparisons");
        out.println("     */");
        out.println("    public static Diff<" + className + "> compare(" + className + " before, " + className + " after, String identifier) {");
        out.println("        Diff<" + className + "> diff = new Diff<>(" + className + ".class, identifier);");
        out.println();

        // Generate comparison for each field (again)
        for (FieldInfo field : fields) {
            String getter = "get" + capitalize(field.name) + "()";
            if (field.type.equals("boolean")) {
                getter = "is" + capitalize(field.name) + "()";
            }
            out.println("        diff.field(\"" + field.name + "\", ");
            out.println("            before == null ? null : before." + getter + ",");
            out.println("            after == null ? null : after." + getter + ");");
        }

        out.println();
        out.println("        return diff;");
        out.println("    }");
        out.println();

        // Helper method to get identifier (tries getId())
        out.println("    private static String getIdentifier(" + className + " obj) {");
        out.println("        if (obj == null) return \"[null]\";");
        out.println("        try {");
        out.println("            var idMethod = obj.getClass().getMethod(\"getId\");");
        out.println("            var id = idMethod.invoke(obj);");
        out.println("            return id != null ? \"#\" + id : \"[new]\";");
        out.println("        } catch (Exception e) {");
        out.println("            return \"@\" + Integer.toHexString(System.identityHashCode(obj));");
        out.println("        }");
        out.println("    }");

        out.println("}");
    }

    private record FieldInfo(String name, String type, String innerType, boolean isOptional) {}
}
