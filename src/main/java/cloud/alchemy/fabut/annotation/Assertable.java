package cloud.alchemy.fabut.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class for assertion builder generation.
 * The annotation processor will generate a type-safe builder class with methods like:
 * <ul>
 *   <li>{@code fieldNameIs(Type value)} - assert exact value</li>
 *   <li>{@code fieldNameIsNull()} - assert null</li>
 *   <li>{@code fieldNameIsNotNull()} - assert not null</li>
 *   <li>{@code fieldNameIsEmpty()} - assert Optional.empty()</li>
 *   <li>{@code fieldNameIsNotEmpty()} - assert Optional has value</li>
 *   <li>{@code fieldNameIgnored()} - ignore field in assertion</li>
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
 * FieldValueAssert.assertThat(fieldValue)
 *     .nameIs("test")
 *     .descriptionIsEmpty()
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
     * OrderAssert.assertCreateKey(order, 1L, "name").verify();
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
     * OrderAssert.assertUpdateStatus(order, "SHIPPED").verify();
     * </pre>
     */
    AssertGroup[] update() default {};
}
