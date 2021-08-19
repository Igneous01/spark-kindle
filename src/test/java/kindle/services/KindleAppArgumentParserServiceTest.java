package kindle.services;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class KindleAppArgumentParserServiceTest {

    private KindleAppArgumentParserService service = new KindleAppArgumentParserService();

    @Test
    public void isValidSuccess1() {
        Assert.assertTrue(service.isValid("${}"));
    }

    @Test
    public void isValidSuccess2() {
        Assert.assertTrue(service.isValid("${arg1 arg2}"));
    }

    @Test
    public void isValidSuccess3() {
        Assert.assertTrue(service.isValid("${${} arg}"));
    }

    @Test
    public void isValidFail1() {
        Assert.assertFalse(service.isValid("{}"));
    }

    @Test
    public void isValidFail2() {
        Assert.assertFalse(service.isValid("$}"));
    }

    @Test
    public void isValidFail3() {
        Assert.assertFalse(service.isValid("${a"));
    }

    @Test
    public void isValidFail4() {
        Assert.assertFalse(service.isValid("$ {a}"));
    }

    @Test
    public void isValidFail5() {
        Assert.assertFalse(service.isValid("_${a}"));
    }

    @Test
    public void extractSuccess1() {
        String[] expected = new String[] {"arg1","arg2"};
        assertArrayEquals(expected,service.extract("${arg1 arg2}"));
    }

    @Test
    public void extractSuccess2() {
        String[] expected = new String[] {"${","arg1","arg2"};
        assertArrayEquals(expected,service.extract("${${ arg1 arg2}"));
    }

    @Test
    public void extractSuccess3() {
        String[] expected = new String[]{};
        assertArrayEquals(expected,service.extract("${}"));
    }
}
