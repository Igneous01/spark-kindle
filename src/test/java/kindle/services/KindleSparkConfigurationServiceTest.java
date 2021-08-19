package kindle.services;

import kindle.exceptions.KindleLauncherException;
import kindle.services.KindleResourceService;
import kindle.services.KindleSparkConfigurationService;
import org.apache.spark.launcher.SparkLauncher;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KindleSparkConfigurationServiceTest {

    @Test
    public void getSparkLauncherSuccessTest() {
        String sparkConfig =
                "spark:\n" +
                "  sparkHome: /etc/spark2\n" +
                "  javaHome: /etc/java\n" +
                "  master: yarn\n" +
                "  appName: Spark Kindle\n" +
                "  class: com.carfax.sparkTools.kindle.CarfaxSparkKindleEngine\n" +
                "  deployMode: cluster\n" +
                "  queue: root.Production.Ask_Price_Pool\n" +
                "  verbose: false\n" +
                "  conf:\n" +
                "    spark.executor.memory: '8g'\n" +
                "    spark.network.timeout: '420'\n";

        InputStream sparkConfigStream = new ByteArrayInputStream(sparkConfig.getBytes(StandardCharsets.UTF_8));
        KindleResourceService mockService = mock(KindleResourceService.class);
        when(mockService.getResource(any(),any())).thenReturn(sparkConfigStream);
        when(mockService.findSparkResource(any())).thenReturn("MockPath");

        KindleSparkConfigurationService service = new KindleSparkConfigurationService(mockService);
        SparkLauncher sparkLauncher = service.getSparkLauncher(Class.class, new String[]{});
        Assert.assertNotNull(sparkLauncher);
    }

    @Test(expected = KindleLauncherException.class)
    public void getSparkLauncherThrowsException1Test() {
        String sparkConfig = "nospark=24";
        InputStream sparkConfigStream = new ByteArrayInputStream(sparkConfig.getBytes(StandardCharsets.UTF_8));
        KindleResourceService mockService = mock(KindleResourceService.class);
        when(mockService.getResource(any(),any())).thenReturn(sparkConfigStream);
        when(mockService.findSparkResource(any())).thenReturn("MockPath");

        KindleSparkConfigurationService service = new KindleSparkConfigurationService(mockService);
        service.getSparkLauncher(Class.class, new String[]{});
    }

    @Test(expected = KindleLauncherException.class)
    public void getSparkLauncherThrowsException2Test() {
        String sparkConfig =
                "notspark:\n" +
                        "  master: yarn\n" +
                        "  appName: Spark Kindle\n" +
                        "  class: com.carfax.sparkTools.kindle.CarfaxSparkKindleEngine\n" +
                        "  deployMode: cluster\n" +
                        "  queue: root.Production.Ask_Price_Pool\n" +
                        "  conf:\n" +
                        "    spark.executor.memory: '8g'\n" +
                        "    spark.network.timeout: '420'\n";
        InputStream sparkConfigStream = new ByteArrayInputStream(sparkConfig.getBytes(StandardCharsets.UTF_8));
        KindleResourceService mockService = mock(KindleResourceService.class);
        when(mockService.getResource(any(),any())).thenReturn(sparkConfigStream);
        when(mockService.findSparkResource(any())).thenReturn("MockPath");

        KindleSparkConfigurationService service = new KindleSparkConfigurationService(mockService);
        service.getSparkLauncher(Class.class, new String[]{});
    }

    @Test(expected = KindleLauncherException.class)
    public void getSparkLauncherThrowsExceptionNoResourceTest() {
        KindleResourceService mockService = mock(KindleResourceService.class);
        when(mockService.findSparkResource(any())).thenThrow(new KindleLauncherException("Error"));
        KindleSparkConfigurationService service = new KindleSparkConfigurationService(mockService);
        service.getSparkLauncher(Class.class, new String[]{});
    }
}
