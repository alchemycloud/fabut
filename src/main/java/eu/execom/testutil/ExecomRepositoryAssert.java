package eu.execom.testutil;

import eu.execom.testutil.property.ISingleProperty;

/**
 * The Interface ExecomRepositoryAssert.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * @param <EntityType>
 *            - entity type
 * @param <EntityIdType>
 *            - type of entity id -
 */
// TODO interface can be deleted.
public interface ExecomRepositoryAssert<EntityType, EntityIdType> extends EntityAssert<EntityType> {

    /**
     * Marks entity as deleted in current snapshot.
     * 
     * @param actual
     *            - actual entity
     */
    void assertEntityAsDeleted(final EntityType actual);

    /**
     * Sets entity to ignored when asserting snapshots.
     * 
     * @param actual
     *            - actual entity
     */
    void ignoreEntity(final EntityType actual);

    /**
     * Assert entity with its state from snapshot using specified array of properties.
     * 
     * @param <X>
     *            the generic type
     * @param expected
     *            the expected
     * @param properties
     *            the properties
     */
    <X extends EntityType> void assertEntityWithSnapshot(final X expected, final ISingleProperty... properties);

    /**
     * Assert entity with its state from snapshot using specified array of properties.
     * 
     * @param <X>
     *            the generic type
     * @param message
     *            the message
     * @param actual
     *            the actual
     * @param properties
     *            the properties
     */
    <X extends EntityType> void assertEntityWithSnapshot(String message, final X actual,
            final ISingleProperty... properties);

}
