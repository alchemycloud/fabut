package cloud.alchemy.fabut.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class for assertion builder generation.
 * The annotation processor will generate a type-safe builder class with methods like:
 * <ul>
 *   <li>{@code field_name_is(Type value)} - assert exact value</li>
 *   <li>{@code field_name_is_null()} - assert null</li>
 *   <li>{@code field_name_is_not_null()} - assert not null</li>
 *   <li>{@code field_name_is_empty()} - assert Optional.empty()</li>
 *   <li>{@code field_name_is_not_empty()} - assert Optional has value</li>
 *   <li>{@code field_name_is_ignored()} - ignore field in assertion</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <pre>
 * {@literal @}Assertable
 * public class FieldValue {
 *     private String name;
 *     private Optional&lt;String&gt; description;
 *     // getters/setters...
 * }
 *
 * // Generated: FieldValueAssert.java
 * // Usage in tests:
 * FieldValueAssert.created(fieldValue)
 *     .name_is("test")
 *     .description_is_empty()
 *     .verify();
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Assertable {

    /**
     * Fields to always ignore in assertions (e.g., "version", "lastModified").
     */
    String[] ignoredFields() default {};

    /**
     * Field groups for assertCreate methods (newly created objects).
     * Each group generates an assertCreate{Name}(object, field1, field2, ...) method.
     *
     * <p>Example:</p>
     * <pre>
     * {@literal @}Assertable(
     *     create = {
     *         {@literal @}AssertGroup(name = "Key", fields = {"id", "name"}),
     *         {@literal @}AssertGroup(name = "Full", fields = {"id", "name", "status"})
     *     }
     * )
     * public class Order { ... }
     *
     * // Usage:
     * OrderAssert.createdKey(order, 1L, "name").verify();
     * </pre>
     */
    AssertGroup[] create() default {};

    /**
     * Field groups for assertUpdate methods (updated entities against snapshot).
     * Each group generates an assertUpdate{Name}(entity, field1, field2, ...) method.
     *
     * <p>Example:</p>
     * <pre>
     * {@literal @}Assertable(
     *     update = {
     *         {@literal @}AssertGroup(name = "Status", fields = {"status"}),
     *         {@literal @}AssertGroup(name = "Audit", fields = {"updatedAt", "updatedBy"})
     *     }
     * )
     * public class Order { ... }
     *
     * // Usage:
     * OrderAssert.updatedStatus(order, "SHIPPED").verify();
     * </pre>
     */
    AssertGroup[] update() default {};
}
