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
}
