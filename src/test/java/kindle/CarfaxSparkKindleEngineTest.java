package kindle;


import kindle.annotations.ConfigValue;
import kindle.annotations.Kindle;
import kindle.annotations.KindleArgumentMatcher;
import okhttp3.*;
import org.apache.spark.sql.SparkSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CarfaxSparkKindleEngineTest {

    @Kindle(request = "default.yaml", matchers = {
            @KindleArgumentMatcher(argValue = "PROD", requestValue = "production.yaml"),
            @KindleArgumentMatcher(argValue = "TEST", requestValue = "test.yaml"),
    })
    public static class TestSparkJob {

        public static String[] applicationArgs;

        @ConfigValue("config.value")
        public static String configurationValue1;

        public void execute(SparkSession spark, String[] args) {
            applicationArgs = args;
        }
    }

    @Before
    public void setUp() throws IOException {
        CarfaxSparkKindleEngine.httpClient = createMockHttpClientArgumentMatcher();
        CarfaxSparkKindleEngine.sparkSession = mock(SparkSession.class);
    }

    @Test
    public void testKindleEngine1() throws NoSuchMethodException, InstantiationException, IOException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        String[] args = new String[] {
                "kindle.CarfaxSparkKindleEngineTest$TestSparkJob",
                "--kindleArgumentMatcher",
                "PROD",
                "--appArgs",
                "${appArg1 appArg2 appArg3 appArg4}"
        };

        CarfaxSparkKindleEngine.main(args);

        String[] appArgs = new String[] {"appArg1","appArg2","appArg3","appArg4"};

        Assert.assertArrayEquals(appArgs,TestSparkJob.applicationArgs);
        Assert.assertEquals("PRODVALUE",TestSparkJob.configurationValue1);
    }

    @Test
    public void testKindleEngine2() throws NoSuchMethodException, InstantiationException, IOException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        String[] args = new String[] {
                "kindle.CarfaxSparkKindleEngineTest$TestSparkJob",
                "--kindleArgumentMatcher",
                "TEST",
                "--appArgs",
                "${appArg1 appArg2 appArg3 appArg4}"
        };

        CarfaxSparkKindleEngine.main(args);

        String[] appArgs = new String[] {"appArg1","appArg2","appArg3","appArg4"};

        Assert.assertArrayEquals(appArgs,TestSparkJob.applicationArgs);
        Assert.assertEquals("TESTVALUE",TestSparkJob.configurationValue1);
    }

    @Test
    public void testKindleEngine3() throws NoSuchMethodException, InstantiationException, IOException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        String[] args = new String[] {
                "kindle.CarfaxSparkKindleEngineTest$TestSparkJob",
                "--appArgs",
                "${appArg1 appArg2 appArg3 appArg4}"
        };

        CarfaxSparkKindleEngine.main(args);

        String[] appArgs = new String[] {"appArg1","appArg2","appArg3","appArg4"};

        Assert.assertArrayEquals(appArgs,TestSparkJob.applicationArgs);
        Assert.assertEquals("DEFAULTVALUE",TestSparkJob.configurationValue1);
    }

    private OkHttpClient createMockHttpClientArgumentMatcher() throws IOException {
        String prodResponse = "config.value: PRODVALUE";
        String testResponse = "config.value: TESTVALUE";
        String defaultResponse = "config.value: DEFAULTVALUE";
        OkHttpClient httpClient = mock(OkHttpClient.class);
        Call prodCall = mock(Call.class);
        when(prodCall.execute()).thenReturn(createStubResponse(200, "200 OK", prodResponse));
        Call testCall = mock(Call.class);
        when(testCall.execute()).thenReturn(createStubResponse(200, "200 OK", testResponse));
        Call defaultCall = mock(Call.class);
        when(defaultCall.execute()).thenReturn(createStubResponse(200, "200 OK", defaultResponse));
        when(httpClient.newCall(any())).thenAnswer(request -> {
            Request req = request.getArgument(0);
            if (req.url().toString().contains("production.yaml")) {
                return prodCall;
            }
            else if (req.url().toString().contains("test.yaml")) {
                return testCall;
            }
            else {
                return defaultCall;
            }
        });
        return httpClient;
    }

    private Response createStubResponse(int code, String message, String body) {
        Request mockRequest = new Request.Builder()
                .url("https://some-url.com")
                .build();
        return new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_2)
                .code(code)
                .message(message)
                .body(ResponseBody.create(
                        MediaType.get("application/json; charset=utf-8"),
                        body
                ))
                .build();
    }
}
