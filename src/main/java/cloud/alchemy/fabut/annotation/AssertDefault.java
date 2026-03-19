package cloud.alchemy.fabut.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the default expected value for a field in generated assertion builders.
 * When {@code verify()} is called on a {@code created()} assertion and this field
 * has not been explicitly asserted, the default value is automatically asserted.
 *
 * <p>Supported values:</p>
 * <ul>
 *   <li>{@code "true"}, {@code "false"} — Boolean fields</li>
 *   <li>{@code "empty"} — Optional.empty()</li>
 *   <li>{@code "null"} — null value</li>
 * </ul>
 *
 * <p>For Optional fields, non-special values are wrapped in {@code Optional.of()}.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * {@literal @}Assertable
 * public class PropertyValue {
 *     {@literal @}AssertDefault("false")
 *     private Boolean usedInCoa;
 * }
 *
 * // Before: PropertyValueAssert.created(pv).usedInCoa_is(false).verify();
 * // After:  PropertyValueAssert.created(pv).verify();  // usedInCoa=false is automatic
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface AssertDefault {

    /**
     * The default expected value as a string literal.
     * Interpreted based on the field's type.
     */
    String value();
}
