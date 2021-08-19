package kindle;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import kindle.domain.KindleArguments;
import kindle.exceptions.KindleLauncherException;
import kindle.services.KindleReflectionService;
import kindle.services.KindleRuntimeArgumentsService;
import okhttp3.OkHttpClient;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.yaml.snakeyaml.Yaml;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

// TODO: support runtime request as argument
// TODO: support resource path as annotation argument?
// TODO: client decryption
// TODO: logging
// TODO: remote upload and launching of spark jar
// TODO: support structured yaml
// TODO: support JSON
// TODO: support jackson?
public class CarfaxSparkKindleEngine {

    // USED FOR TESTING ONLY
    public static OkHttpClient httpClient;
    public static SparkSession sparkSession;

    protected static JavaSparkContext sc;
    private static final String EXECUTE_METHOD = "execute";
    private static KindleRuntimeArgumentsService kindleRuntimeArgumentsService = new KindleRuntimeArgumentsService();

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        Logger.getLogger("shadow.org.reflections").setLevel(Level.ERROR);
        Class<?> cls = Class.forName(args[0]);
        String kindleResource = findKindleResource();
        String configServerBaseUrl;

        if (kindleResource != null) {
            configServerBaseUrl = configureCloudConfigServerFromYaml(cls, kindleResource);
        }
        else {
            throw new KindleLauncherException("kindle.properties could not be found");
        }

        KindleArguments kindleArguments = kindleRuntimeArgumentsService.processArgs(args);
        logKindleStartup(kindleArguments);
        KindleReflectionService service = getKindleReflectionService(kindleArguments, configServerBaseUrl);

        if (sparkSession == null) {
            sparkSession = SparkSession.builder().getOrCreate();
            sparkSession.sparkContext().setLogLevel("WARN");
            sc = new JavaSparkContext(sparkSession.sparkContext());
        }

        Object sparkJobInstance = cls.newInstance();
        service.processAnnotations(sparkJobInstance);

        //if there are arguments to pass to the class, pass them here
        String[] appArgs = kindleArguments.getAppArgs();
        if (appArgs.length > 1) {
            Method method = cls.getDeclaredMethod(EXECUTE_METHOD, SparkSession.class, String[].class);
            method.invoke(sparkJobInstance, sparkSession, appArgs);
        } else {
            Method method = cls.getDeclaredMethod(EXECUTE_METHOD, SparkSession.class);
            method.invoke(sparkJobInstance, sparkSession);
        }
    }

    private static void logKindleStartup(KindleArguments kindleArguments) {
        System.out.println("Kindle Arguments");
        System.out.println("--kindleArgumentMatcher: " + kindleArguments.getMatcher());
        System.out.println("--kindleRequest: " + kindleArguments.getRequest());
        System.out.print("--appArgs: { ");
        for (String appArg : kindleArguments.getAppArgs()) {
            System.out.print(appArg + " ");
        }
        System.out.print("}\n");
        System.out.flush();
    }

    //TODO - handle no resource file for config server url
    private static KindleReflectionService getKindleReflectionService(KindleArguments kindleArguments, String configServerBaseUrl) {
        KindleReflectionService service = new KindleReflectionService(configServerBaseUrl);
        service.setArgumentMatcherValue(kindleArguments.getMatcher());

        if (httpClient != null) {
            service.setHttpClient(httpClient);
        }

        return service;
    }

    private static String configureCloudConfigServerFromYaml(Class<?> clazz, String resourcePath) {
        Yaml yaml = new Yaml();
        InputStream is = clazz.getClassLoader().getResourceAsStream(resourcePath);
        Map<String, Object> yamlParsers = (Map<String, Object>) yaml.load(is);
        return (String) yamlParsers.get("spring.cloud.config.server");
    }

    private static String findKindleResource() {
        Set<String> resources = new Reflections(new ResourcesScanner()).getResources(Pattern.compile("kindle.properties"));
        if (resources.isEmpty())
            return null;
        else
            return resources.iterator().next();
    }
}
