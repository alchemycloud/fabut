package cloud.alchemy.fabut;

import cloud.alchemy.fabut.assertt.FabutObjectAssert;
import cloud.alchemy.fabut.model.*;
import cloud.alchemy.fabut.model.test.Address;
import cloud.alchemy.fabut.model.test.Faculty;
import cloud.alchemy.fabut.model.test.Student;
import cloud.alchemy.fabut.model.test.Teacher;
import org.junit.Assert;
import org.junit.Before;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * TODO add comments
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * @author Andrej Miletic
 */
public abstract class AbstractFabutObjectAssertTest extends Assert implements IFabutTest {

    private FabutObjectAssert fabutObjectAssert;

    /**
     * Default constructor.
     */
    public AbstractFabutObjectAssertTest() {
        super();
    }

    @Override
    @Before
    public void fabutBeforeTest() {
        fabutObjectAssert = new FabutObjectAssert(this);
    }

    @Override
    public void fabutAfterTest() {
    }

    @Override
    public List<Class<?>> getComplexTypes() {
        final List<Class<?>> complexTypes = new LinkedList<>();
        complexTypes.add(A.class);
        complexTypes.add(B.class);
        complexTypes.add(C.class);
        complexTypes.add(TierOneType.class);
        complexTypes.add(TierTwoType.class);
        complexTypes.add(TierThreeType.class);
        complexTypes.add(TierFourType.class);
        complexTypes.add(TierFiveType.class);
        complexTypes.add(TierSixType.class);
        complexTypes.add(NoGetMethodsType.class);
        complexTypes.add(IgnoredMethodsType.class);
        complexTypes.add(TierTwoTypeWithIgnoreProperty.class);
        complexTypes.add(TierTwoTypeWithListProperty.class);
        complexTypes.add(TierTwoTypeWithPrimitiveProperty.class);
        complexTypes.add(DoubleLink.class);
        complexTypes.add(Start.class);
        complexTypes.add(Address.class);
        complexTypes.add(Faculty.class);
        complexTypes.add(Student.class);
        complexTypes.add(Teacher.class);
        return complexTypes;
    }

    @Override
    public List<Class<?>> getIgnoredTypes() {
        final List<Class<?>> ignoredTypes = new LinkedList<>();
        ignoredTypes.add(IgnoredType.class);
        return ignoredTypes;
    }

    @Override
    public Map<Class<?>, List<String>> getIgnoredFields() {

        return new HashMap<>();
    }

    @Override
    public void customAssertEquals(final Object expected, final Object actual) {
        Assert.assertEquals(expected, actual);

    }

    public FabutObjectAssert getFabutObjectAssert() {
        return fabutObjectAssert;
    }

}
