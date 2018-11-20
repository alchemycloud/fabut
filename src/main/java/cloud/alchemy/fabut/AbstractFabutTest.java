package cloud.alchemy.fabut;

import org.junit.After;
import org.junit.Before;

/**
 * TODO add comments.
 * 
 * @author Dusko Vesin
 * 
 */
public abstract class AbstractFabutTest extends Fabut implements IFabutTest {

    public AbstractFabutTest() {
        super();
    }

    @Before
    @Override
    public void fabutBeforeTest() {
        Fabut.beforeTest(this);
    }

    @Override
    @After
    public void fabutAfterTest() {
        Fabut.afterTest();
    }

}
