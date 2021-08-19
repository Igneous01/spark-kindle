package kindle;

import kindle.services.KindleResourceService;
import kindle.services.KindleSparkConfigurationService;
import org.apache.spark.launcher.SparkLauncher;

import java.io.IOException;
import java.util.Arrays;

public class CarfaxSparkKindleLauncher {

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String runnerClassName = args[0];
        Class cls = Class.forName(runnerClassName);
        String[] sparkAppArgs = Arrays.copyOfRange(args, 1, args.length);
        KindleSparkConfigurationService kindleSparkConfigurationService = new KindleSparkConfigurationService(new KindleResourceService());
        SparkLauncher sparkLauncher = kindleSparkConfigurationService.getSparkLauncher(cls, sparkAppArgs);
        sparkLauncher.launch();
    }
}
