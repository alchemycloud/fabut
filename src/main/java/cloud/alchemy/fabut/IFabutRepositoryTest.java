package cloud.alchemy.fabut;

import java.util.List;

/**
 * TODO add comments.
 * 
 * @author Dusko Vesin
 * 
 */
public interface IFabutRepositoryTest extends IFabutTest {

    /**
     * Find all objects of specified type.
     * 
     * @param clazz
     *            class of objects that are requested.
     * @return {@link List} of objects of clazz type
     */
    List<?> findAll(Class<?> clazz);

    /**
     * Find object of requested class by id.
     * 
     * @param entityClass
     *            of the object
     * @param id
     *            of the object
     * @return matching object if it exist, else return <code>null</code>
     */
    Object findById(final Class<?> entityClass, final Object id);

    /**
     * Get {@link List} of entity types that that can be persisted.
     * 
     * @return {@link List} with existing entity types.
     */
    List<Class<?>> getEntityTypes();

}
