package cloud.alchemy.fabut.processor;

import cloud.alchemy.fabut.annotation.AssertGroup;
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

        // Get ignored fields and assert groups from annotation
        Assertable annotation = typeElement.getAnnotation(Assertable.class);
        Set<String> ignoredFields = new HashSet<>(Arrays.asList(annotation.ignoredFields()));
        AssertGroup[] create = annotation.create();
        AssertGroup[] update = annotation.update();

        // Collect fields with getters
        List<FieldInfo> fields = collectFields(typeElement, ignoredFields);

        // Generate the builder class
        JavaFileObject builderFile = filer.createSourceFile(qualifiedBuilderName, typeElement);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            generateBuilderClass(out, packageName, className, builderClassName, fields, ignoredFields, create, update);
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
                                       String builderClassName, List<FieldInfo> fields, Set<String> ignoredFields,
                                       AssertGroup[] create, AssertGroup[] update) {
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
        out.println("    private final boolean isDelete;");
        out.println("    private final List<IProperty> properties = new ArrayList<>();");
        out.println();

        // Constructor - auto-adds ignored() for fields specified in @Assertable(ignoredFields)
        out.println("    private " + builderClassName + "(Fabut fabut, " + className + " object, boolean isSnapshot) {");
        out.println("        this(fabut, object, isSnapshot, false);");
        out.println("    }");
        out.println();

        out.println("    private " + builderClassName + "(Fabut fabut, " + className + " object, boolean isSnapshot, boolean isDelete) {");
        out.println("        this.fabut = fabut;");
        out.println("        this.object = object;");
        out.println("        this.isSnapshot = isSnapshot;");
        out.println("        this.isDelete = isDelete;");
        // Auto-add ignored for fields specified in annotation
        for (String ignoredField : ignoredFields) {
            out.println("        properties.add(fabut.ignored(\"" + ignoredField + "\"));");
        }
        out.println("    }");
        out.println();

        // Static factory methods - simplified (uses ThreadLocal)
        out.println("    /**");
        out.println("     * Assert a newly created object. Use this after creating an entity.");
        out.println("     * Uses the current Fabut instance from ThreadLocal (set automatically in @BeforeEach).");
        out.println("     */");
        out.println("    public static " + builderClassName + " assertCreate(" + className + " object) {");
        out.println("        return new " + builderClassName + "(Fabut.current(), object, false);");
        out.println("    }");
        out.println();

        out.println("    /**");
        out.println("     * Assert an updated entity against its snapshot. Use this after modifying an entity.");
        out.println("     * Uses the current Fabut instance from ThreadLocal (set automatically in @BeforeEach).");
        out.println("     */");
        out.println("    public static " + builderClassName + " assertUpdate(" + className + " entity) {");
        out.println("        return new " + builderClassName + "(Fabut.current(), entity, true);");
        out.println("    }");
        out.println();

        out.println("    /**");
        out.println("     * Assert that an entity was deleted. Call verify() to execute the assertion.");
        out.println("     * Uses the current Fabut instance from ThreadLocal (set automatically in @BeforeEach).");
        out.println("     */");
        out.println("    public static " + builderClassName + " assertDelete(" + className + " entity) {");
        out.println("        return new " + builderClassName + "(Fabut.current(), entity, false, true);");
        out.println("    }");
        out.println();

        // Overloads with explicit Fabut (for advanced use cases)
        out.println("    /**");
        out.println("     * Assert a newly created object with explicit Fabut instance.");
        out.println("     */");
        out.println("    public static " + builderClassName + " assertCreate(Fabut fabut, " + className + " object) {");
        out.println("        return new " + builderClassName + "(fabut, object, false);");
        out.println("    }");
        out.println();

        out.println("    /**");
        out.println("     * Assert an updated entity against its snapshot with explicit Fabut instance.");
        out.println("     */");
        out.println("    public static " + builderClassName + " assertUpdate(Fabut fabut, " + className + " entity) {");
        out.println("        return new " + builderClassName + "(fabut, entity, true);");
        out.println("    }");
        out.println();

        out.println("    /**");
        out.println("     * Assert that an entity was deleted with explicit Fabut instance.");
        out.println("     */");
        out.println("    public static " + builderClassName + " assertDelete(Fabut fabut, " + className + " entity) {");
        out.println("        return new " + builderClassName + "(fabut, entity, false, true);");
        out.println("    }");
        out.println();

        // Generate assertCreate with all mandatory fields as parameters
        List<FieldInfo> mandatoryFields = fields.stream()
                .filter(f -> !f.isOptional)
                .toList();

        if (!mandatoryFields.isEmpty()) {
            // Build parameter list
            StringBuilder params = new StringBuilder(className + " object");
            for (FieldInfo field : mandatoryFields) {
                params.append(", ").append(field.type).append(" ").append(field.name);
            }

            out.println("    /**");
            out.println("     * Assert a newly created object with all mandatory (non-Optional) field values.");
            out.println("     * Optional fields can still be asserted using the fluent builder methods.");
            out.println("     */");
            out.println("    public static " + builderClassName + " assertCreate(" + params + ") {");
            out.println("        " + builderClassName + " builder = new " + builderClassName + "(Fabut.current(), object, false);");
            for (FieldInfo field : mandatoryFields) {
                out.println("        builder.properties.add(builder.fabut.value(\"" + field.name + "\", " + field.name + "));");
            }
            out.println("        return builder;");
            out.println("    }");
            out.println();

            // Also with explicit Fabut
            StringBuilder paramsWithFabut = new StringBuilder("Fabut fabut, " + className + " object");
            for (FieldInfo field : mandatoryFields) {
                paramsWithFabut.append(", ").append(field.type).append(" ").append(field.name);
            }

            out.println("    /**");
            out.println("     * Assert a newly created object with all mandatory (non-Optional) field values and explicit Fabut instance.");
            out.println("     */");
            out.println("    public static " + builderClassName + " assertCreate(" + paramsWithFabut + ") {");
            out.println("        " + builderClassName + " builder = new " + builderClassName + "(fabut, object, false);");
            for (FieldInfo field : mandatoryFields) {
                out.println("        builder.properties.add(builder.fabut.value(\"" + field.name + "\", " + field.name + "));");
            }
            out.println("        return builder;");
            out.println("    }");
            out.println();

            // assertUpdate with mandatory fields
            out.println("    /**");
            out.println("     * Assert an updated entity with specified field values against its snapshot.");
            out.println("     * Only specify fields that have changed.");
            out.println("     */");
            out.println("    public static " + builderClassName + " assertUpdate(" + params.toString().replace("object", "entity") + ") {");
            out.println("        " + builderClassName + " builder = new " + builderClassName + "(Fabut.current(), entity, true);");
            for (FieldInfo field : mandatoryFields) {
                out.println("        builder.properties.add(builder.fabut.value(\"" + field.name + "\", " + field.name + "));");
            }
            out.println("        return builder;");
            out.println("    }");
            out.println();

            out.println("    /**");
            out.println("     * Assert an updated entity with specified field values against its snapshot with explicit Fabut instance.");
            out.println("     */");
            out.println("    public static " + builderClassName + " assertUpdate(" + paramsWithFabut.toString().replace("object", "entity") + ") {");
            out.println("        " + builderClassName + " builder = new " + builderClassName + "(fabut, entity, true);");
            for (FieldInfo field : mandatoryFields) {
                out.println("        builder.properties.add(builder.fabut.value(\"" + field.name + "\", " + field.name + "));");
            }
            out.println("        return builder;");
            out.println("    }");
            out.println();
        }

        // Build field map for group lookups
        Map<String, FieldInfo> fieldMap = new HashMap<>();
        for (FieldInfo field : fields) {
            fieldMap.put(field.name, field);
        }

        // Generate assertCreate methods for each newGroup
        for (AssertGroup group : create) {
            generateGroupMethods(out, className, builderClassName, group, fieldMap, "assertCreate", "object", false);
        }

        // Generate assertUpdate methods for each updateGroup
        for (AssertGroup group : update) {
            generateGroupMethods(out, className, builderClassName, group, fieldMap, "assertUpdate", "entity", true);
        }

        // Generate methods for each field
        for (FieldInfo field : fields) {
            generateFieldMethods(out, builderClassName, field);
        }

        // Verify method
        out.println("    /**");
        out.println("     * Execute the assertion.");
        out.println("     */");
        out.println("    public " + className + " verify() {");
        out.println("        if (isDelete) {");
        out.println("            fabut.assertEntityAsDeleted(object);");
        out.println("            return object;");
        out.println("        }");
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

    private void generateGroupMethods(PrintWriter out, String className, String builderClassName,
                                       AssertGroup group, Map<String, FieldInfo> fieldMap,
                                       String methodPrefix, String paramName, boolean isSnapshot) {
        String groupName = group.name();
        String[] groupFields = group.fields();

        // Build parameter list for this group
        StringBuilder groupParams = new StringBuilder(className + " " + paramName);
        List<FieldInfo> groupFieldInfos = new ArrayList<>();

        for (String fieldName : groupFields) {
            FieldInfo fieldInfo = fieldMap.get(fieldName);
            if (fieldInfo != null) {
                groupFieldInfos.add(fieldInfo);
                // Use inner type for Optional fields
                String paramType = fieldInfo.isOptional ? fieldInfo.innerType : fieldInfo.type;
                groupParams.append(", ").append(paramType).append(" ").append(fieldName);
            }
        }

        if (!groupFieldInfos.isEmpty()) {
            // Generate method without Fabut
            String description = isSnapshot
                    ? "Assert an updated entity with the '" + groupName + "' field group against its snapshot."
                    : "Assert a newly created object with the '" + groupName + "' field group.";
            out.println("    /**");
            out.println("     * " + description);
            out.println("     * Fields: " + String.join(", ", groupFields));
            out.println("     */");
            out.println("    public static " + builderClassName + " " + methodPrefix + groupName + "(" + groupParams + ") {");
            out.println("        " + builderClassName + " builder = new " + builderClassName + "(Fabut.current(), " + paramName + ", " + isSnapshot + ");");
            for (int i = 0; i < groupFieldInfos.size(); i++) {
                FieldInfo fieldInfo = groupFieldInfos.get(i);
                String fieldName = groupFields[i];
                if (fieldInfo.isOptional) {
                    out.println("        builder.properties.add(builder.fabut.value(\"" + fieldName + "\", Optional.ofNullable(" + fieldName + ")));");
                } else {
                    out.println("        builder.properties.add(builder.fabut.value(\"" + fieldName + "\", " + fieldName + "));");
                }
            }
            out.println("        return builder;");
            out.println("    }");
            out.println();

            // Generate method with explicit Fabut
            StringBuilder groupParamsWithFabut = new StringBuilder("Fabut fabut, " + className + " " + paramName);
            for (String fieldName : groupFields) {
                FieldInfo fieldInfo = fieldMap.get(fieldName);
                if (fieldInfo != null) {
                    String paramType = fieldInfo.isOptional ? fieldInfo.innerType : fieldInfo.type;
                    groupParamsWithFabut.append(", ").append(paramType).append(" ").append(fieldName);
                }
            }

            out.println("    /**");
            out.println("     * " + description.replace(".", " with explicit Fabut instance."));
            out.println("     */");
            out.println("    public static " + builderClassName + " " + methodPrefix + groupName + "(" + groupParamsWithFabut + ") {");
            out.println("        " + builderClassName + " builder = new " + builderClassName + "(fabut, " + paramName + ", " + isSnapshot + ");");
            for (int i = 0; i < groupFieldInfos.size(); i++) {
                FieldInfo fieldInfo = groupFieldInfos.get(i);
                String fieldName = groupFields[i];
                if (fieldInfo.isOptional) {
                    out.println("        builder.properties.add(builder.fabut.value(\"" + fieldName + "\", Optional.ofNullable(" + fieldName + ")));");
                } else {
                    out.println("        builder.properties.add(builder.fabut.value(\"" + fieldName + "\", " + fieldName + "));");
                }
            }
            out.println("        return builder;");
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
