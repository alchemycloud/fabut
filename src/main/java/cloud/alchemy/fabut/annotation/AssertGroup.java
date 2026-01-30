package cloud.alchemy.fabut.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a named group of fields for generating a custom assertThat method.
 *
 * <p>Usage:</p>
 * <pre>
 * {@literal @}Assertable(
 *     ignoredFields = {"version"},
 *     assertGroups = {
 *         {@literal @}AssertGroup(name = "key", fields = {"id", "name"}),
 *         {@literal @}AssertGroup(name = "status", fields = {"id", "status", "updatedAt"})
 *     }
 * )
 * public class Order { ... }
 *
 * // Generates:
 * // OrderAssert.assertThatKey(order, id, name)
 * // OrderAssert.assertThatStatus(order, id, status, updatedAt)
 * </pre>
 */
@Target({})
@Retention(RetentionPolicy.SOURCE)
public @interface AssertGroup {

    /**
     * Name of the group. Used as suffix for the generated method: assertThat{Name}().
     * Use PascalCase (e.g., "Key", "Status", "BasicInfo").
     */
    String name();

    /**
     * List of field names to include in this group's assertThat method.
     * Fields are used as parameters in the order specified.
     */
    String[] fields();
}
