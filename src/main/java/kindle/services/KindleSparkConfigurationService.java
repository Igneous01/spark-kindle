package kindle.services;

import kindle.exceptions.KindleLauncherException;
import org.apache.spark.launcher.SparkLauncher;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class KindleSparkConfigurationService {
    private static final String SPARK_KEY = "spark";
    private static final String CONF_KEY = "conf";
    private static final String APP_ARGS_KEY = "appArgs";
    private KindleResourceService resourceService;

    public KindleSparkConfigurationService(KindleResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public SparkLauncher getSparkLauncher(Class<?> clazz, String[] args) {
        String resourcePath = resourceService.findSparkResource(clazz);
        InputStream resourceStream = resourceService.getResource(clazz, resourcePath);
        return configureSparkLauncher(resourceStream, args);
    }

    private SparkLauncher configureSparkLauncher(InputStream resourceStream, String[] args) {
        Yaml yaml = new Yaml();
        Object yamlObject = yaml.load(resourceStream);
        if (!(yamlObject instanceof Map)) {
            throw new KindleLauncherException("YAML is in an invalid format. Must be in dictionary format");
        }
        Map<String, Object> yamlParsers = (Map<String, Object>) yamlObject;
        SparkLauncher sparkLauncher = new SparkLauncher();

        if (yamlParsers.containsKey(SPARK_KEY) && yamlParsers.get(SPARK_KEY) instanceof Map) {
            Map<String,Object> sparkConfigMap = (Map<String,Object>) yamlParsers.get(SPARK_KEY);;
            sparkLauncher
                    .setSparkHome(safeMapGet(sparkConfigMap, "sparkHome", String.class))
                    .setJavaHome(safeMapGet(sparkConfigMap, "javaHome", String.class))
                    .setMaster(safeMapGet(sparkConfigMap, "master", String.class))
                    .setMainClass(safeMapGet(sparkConfigMap, "class", String.class))
                    .setAppName(safeMapGet(sparkConfigMap, "appName", String.class))
                    .setDeployMode(safeMapGet(sparkConfigMap, "deployMode", String.class))
                    .setAppResource(safeMapGet(sparkConfigMap, "queue", String.class))
                    .setVerbose(safeMapGet(sparkConfigMap, "verbose", Boolean.class))
                    .addAppArgs(args);

            if (sparkConfigMap.containsKey(CONF_KEY) && sparkConfigMap.get(CONF_KEY) instanceof Map) {
                Map<String,String> sparkConfConfig = (Map<String,String>) sparkConfigMap.get(CONF_KEY);
                for (Map.Entry<String, String> entry : sparkConfConfig.entrySet()) {
                    sparkLauncher.setConf(entry.getKey(), entry.getValue());
                }
            }
        }
        else {
            throw new KindleLauncherException("Error launching spark job: spark.properties resource does not contain definition for '" + SPARK_KEY + "', or the definition is not a map type in YAML");
        }

        return sparkLauncher;
    }

    private <T> T safeMapGet(Map<String,Object> map, String key, Class<T> clazz) {
        Object val = map.get(key);
        if (val != null) {
            return (T) val;
        }
        else {
            return null;
        }
    }
}
