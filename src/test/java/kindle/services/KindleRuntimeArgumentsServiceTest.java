package kindle.services;

import kindle.domain.KindleArguments;
import kindle.exceptions.KindleLauncherException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class KindleRuntimeArgumentsServiceTest {

    private KindleRuntimeArgumentsService service = new KindleRuntimeArgumentsService();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void kindleRequestUrlSuccess() {
        String[] args = new String[] {"blah","--kindleRequest","requestUrl"};
        KindleArguments kindleArguments = service.processArgs(args);
        Assert.assertEquals("requestUrl", kindleArguments.getRequest());
    }

    @Test(expected = KindleLauncherException.class)
    public void kindleRequestUrlException() {
        String[] args = new String[] {"blah","--kindleRequest"};
        service.processArgs(args);
    }

    @Test
    public void kindleArgMatcherSuccess() {
        String[] args = new String[] {"--kindleArgumentMatcher","PROD"};
        KindleArguments kindleArguments = service.processArgs(args);
        Assert.assertEquals("PROD", kindleArguments.getMatcher());
    }

    @Test(expected = KindleLauncherException.class)
    public void kindleArgMatcherException() {
        String[] args = new String[] {"--kindleArgumentMatcher"};
        service.processArgs(args);
    }

    @Test
    public void kindleAppArgsMultipleSuccess() {
        String[] args = new String[] {"blah","--appArgs","${arg1 arg2 arg3}"};
        KindleArguments kindleArguments = service.processArgs(args);
        assertEquals(3, kindleArguments.getAppArgs().length);
        assertEquals("arg1",kindleArguments.getAppArgs()[0]);
        assertEquals("arg2",kindleArguments.getAppArgs()[1]);
        assertEquals("arg3",kindleArguments.getAppArgs()[2]);
    }

    @Test
    public void kindleAppArgsSingleSuccess() {
        String[] args = new String[] {"blah","--appArgs","${arg1}"};
        KindleArguments kindleArguments = service.processArgs(args);
        assertEquals(1, kindleArguments.getAppArgs().length);
        assertEquals("arg1",kindleArguments.getAppArgs()[0]);
    }

    @Test
    public void kindleAppArgsEmptySuccess() {
        String[] args = new String[] {"blah","--appArgs","${}"};
        KindleArguments kindleArguments = service.processArgs(args);
        assertEquals(0, kindleArguments.getAppArgs().length);
    }

    @Test(expected = KindleLauncherException.class)
    public void kindleAppArgsParseException1() {
        String[] args = new String[] {"blah","--appArgs","${arg1"};
        service.processArgs(args);
    }

    @Test(expected = KindleLauncherException.class)
    public void kindleAppArgsParseException2() {
        String[] args = new String[] {"blah","--appArgs","arg1"};
        service.processArgs(args);
    }

    @Test(expected = KindleLauncherException.class)
    public void kindleAppArgsParseException3() {
        String[] args = new String[] {"blah","--appArgs","$arg1"};
        service.processArgs(args);
    }

    @Test(expected = KindleLauncherException.class)
    public void kindleAppArgsParseException4() {
        String[] args = new String[] {"blah","--appArgs","arg1}"};
        service.processArgs(args);
    }

    @Test
    public void kindleNoKnownArgumentsSuccess() {
        String[] args = new String[] {"blah","arg2","requestUrl"};
        KindleArguments kindleArguments = service.processArgs(args);
        assertNull(kindleArguments.getRequest());
        assertNull(kindleArguments.getAppArgs());
        assertNull(kindleArguments.getMatcher());
    }
}
