package cloud.alchemy.fabut;

import java.util.List;
import java.util.Map;

/**
 * TODO add comments.
 * 
 * @author Dusko Vesin
 * @author Andrej Miletic
 *
 */
public interface IFabutTest {

    void fabutBeforeTest();

    void fabutAfterTest();

    List<Class<?>> getComplexTypes();

    List<Class<?>> getIgnoredTypes();

    Map<Class<?>, List<String>> getIgnoredFields();

    void customAssertEquals(Object expected, Object actual);
}
