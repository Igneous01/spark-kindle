package kindle.services;

import kindle.annotations.ConfigValue;
import kindle.annotations.Kindle;
import kindle.annotations.KindleArgumentMatcher;
import kindle.exceptions.KindleHttpException;
import kindle.exceptions.KindleLauncherException;
import kindle.services.KindleReflectionService;
import okhttp3.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KindleReflectionServiceTest {
    private KindleReflectionService service;
    private final String SERVER_URL = "http://localhost:8000";

    @Kindle(request = "test-config.properties")
    public class SparkCloudConfigStub1 {
        @ConfigValue("test:config:option1")
        public String option1;
        @ConfigValue("test:config:option2")
        public double option2;
        @ConfigValue("different.path.location")
        public int option3;
        @ConfigValue("static.field")
        public Double option4 = 0.0;

        public String getOption1() {
            return option1;
        }
        public double getOption2() {
            return option2;
        }
        public int getOption3() {
            return option3;
        }
        public final Double getOption4() {
            return option4;
        }
    }

    @Kindle(matchers = {
        @KindleArgumentMatcher(argValue = "PROD", requestValue = "PROD.Config"),
        @KindleArgumentMatcher(argValue = "TEST", requestValue = "TEST.Config")
    })
    public class SparkCloudConfigStub2 {
        @ConfigValue("test:config:option1")
        public String option1;

        public String getOption1() {
            return option1;
        }
    }

    @Kindle(request = "PROD.Config",
        matchers = {
            @KindleArgumentMatcher(argValue = "PROD", requestValue = "PROD.Config"),
            @KindleArgumentMatcher(argValue = "TEST", requestValue = "TEST.Config")
    })
    public class SparkCloudConfigStub3 {
        @ConfigValue("test:config:option1")
        public String option1;

        public String getOption1() {
            return option1;
        }
    }

    @Test
    public void processAnnotationsArgumentMatcherSuccessTest1() throws IOException {
        service = new KindleReflectionService(SERVER_URL, createMockHttpClientArgumentMatcher());
        service.setArgumentMatcherValue("PROD");
        SparkCloudConfigStub2 stub = new SparkCloudConfigStub2();
        service.processAnnotations(stub);
        Assert.assertEquals("PRODUCTION", stub.getOption1());
    }

    @Test
    public void processAnnotationsArgumentMatcherSuccessTest2() throws IOException {
        service = new KindleReflectionService(SERVER_URL, createMockHttpClientArgumentMatcher());
        service.setArgumentMatcherValue("TEST");
        SparkCloudConfigStub2 stub = new SparkCloudConfigStub2();
        service.processAnnotations(stub);
        Assert.assertEquals("TEST", stub.getOption1());
    }

    @Test(expected = KindleLauncherException.class)
    public void processAnnotationsArgumentMatcherExceptionTest() throws IOException {
        service = new KindleReflectionService(SERVER_URL, createMockHttpClientArgumentMatcher());
        service.setArgumentMatcherValue("NONE");
        SparkCloudConfigStub3 stub = new SparkCloudConfigStub3();
        service.processAnnotations(stub);
        Assert.assertEquals("PRODUCTION", stub.getOption1());
    }

    @Test
    public void processAnnotationsSuccessTest() throws IOException {
        service = new KindleReflectionService(SERVER_URL, createMockHttpClientSuccess());
        SparkCloudConfigStub1 stub = new SparkCloudConfigStub1();
        service.processAnnotations(stub);
        Assert.assertEquals("Option1", stub.getOption1());
        Assert.assertEquals(3.14, stub.getOption2(), 0.01);
        Assert.assertEquals(3, stub.getOption3());
        Assert.assertEquals(new Double(4.2), stub.getOption4());
    }

    @Test(expected = KindleHttpException.class)
    public void processAnnotationsInternalServerErrorTest() throws IOException {
        service = new KindleReflectionService(SERVER_URL, createMockHttpClientInternalServerError());
        SparkCloudConfigStub1 stub = new SparkCloudConfigStub1();
        service.processAnnotations(stub);
    }

    @Test(expected = SocketTimeoutException.class)
    public void processAnnotationsTimeoutExceptionTest() throws IOException {
        service = new KindleReflectionService(SERVER_URL, createMockHttpClientTimeoutException());
        SparkCloudConfigStub1 stub = new SparkCloudConfigStub1();
        service.processAnnotations(stub);
    }

    private OkHttpClient createMockHttpClientSuccess() throws IOException {
        String configServerResponse = "test:\n"
                + "  config:\n"
                + "    option1: Option1\n"
                + "    option2: 3.14\n"
                + "different.path.location: 3\n"
                + "static.field: 4.2\n";
        OkHttpClient httpClient = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        when(call.execute()).thenReturn(createStubResponse(200, "200 OK", configServerResponse));
        when(httpClient.newCall(any())).thenReturn(call);
        return httpClient;
    }

    private OkHttpClient createMockHttpClientInternalServerError() throws IOException {
        String configServerResponse = "Internal Server Error";
        OkHttpClient httpClient = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        when(call.execute()).thenReturn(createStubResponse(500, "500 Internal Server Error", configServerResponse));
        when(httpClient.newCall(any())).thenReturn(call);
        return httpClient;
    }

    private OkHttpClient createMockHttpClientTimeoutException() throws IOException {
        OkHttpClient httpClient = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        when(call.execute()).thenThrow(new SocketTimeoutException());
        when(httpClient.newCall(any())).thenReturn(call);
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

    private OkHttpClient createMockHttpClientArgumentMatcher() throws IOException {
        String prodResponse = "test:\n"
                + "  config:\n"
                + "    option1: PRODUCTION\n";
        String testResponse = "test:\n"
                + "  config:\n"
                + "    option1: TEST\n";
        OkHttpClient httpClient = mock(OkHttpClient.class);
        Call prodCall = mock(Call.class);
        when(prodCall.execute()).thenReturn(createStubResponse(200, "200 OK", prodResponse));
        Call testCall = mock(Call.class);
        when(testCall.execute()).thenReturn(createStubResponse(200, "200 OK", testResponse));
        when(httpClient.newCall(any())).thenAnswer(request -> {
            Request req = request.getArgument(0);
            if (req.url().toString().contains("PROD.Config")) {
                return prodCall;
            }
            else if (req.url().toString().contains("TEST.Config")) {
                return testCall;
            }
            else {
                return null;
            }
        });
        return httpClient;
    }
}
