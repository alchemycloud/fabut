package cloud.alchemy.fabut.enums;

import java.util.Map;
import java.util.List;
import java.util.Optional;

/**
 * Types assertable by FABUT.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * 
 */
public enum AssertableType {

    /** Type that can be split by sub fields and asserted by those fields. */
    COMPLEX_TYPE,

    /** Type that should be ignored during Fabut assert. */
    IGNORED_TYPE,

    /**
     * Type used for storing in external repositories, main difference to {@link AssertableType#COMPLEX_TYPE} is that
     * this type requires id field.
     */
    ENTITY_TYPE,

    /** Type asserted using custom user assert. */
    PRIMITIVE_TYPE,

    /** Type that implements {@link List}, its asserted iterating over its elements and asserting those. */
    LIST_TYPE,

    /** Type that implements {@link Map} */
    MAP_TYPE,

    /** Type that implemens {@link Optional}**/
    OPTIONAL_TYPE;

}
