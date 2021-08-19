package kindle.services;

import kindle.Utils.ReflectionAnnotationUtils;
import kindle.annotations.ConfigValue;
import kindle.annotations.Kindle;
import kindle.annotations.KindleArgumentMatcher;
import kindle.domain.KindleFieldAnnotationPair;
import kindle.exceptions.KindleHttpException;
import kindle.exceptions.KindleLauncherException;
import kindle.exceptions.KindleReflectionException;
import okhttp3.*;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KindleReflectionService {

    private OkHttpClient httpClient;
    private String cloudConfigServerUrl;
    private String argumentMatcherValue;

    public KindleReflectionService(String url) {
        cloudConfigServerUrl = url;
        httpClient = new OkHttpClient();
    }

    public KindleReflectionService(String url, OkHttpClient httpClient) {
        cloudConfigServerUrl = url;
        this.httpClient = httpClient;
    }

    public void setArgumentMatcherValue(String argumentMatcherValue) {
        this.argumentMatcherValue = argumentMatcherValue;
    }

    public void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void processAnnotations(Object instance) throws IOException {
        Optional<Kindle> kindleAnnotation = ReflectionAnnotationUtils.findAnnotation(instance.getClass(), Kindle.class);
        if (kindleAnnotation.isPresent()) {
            System.out.println("Kindle Class Annotation Present");
            List<KindleFieldAnnotationPair<ConfigValue>> fieldAnnotations = ReflectionAnnotationUtils.findFieldsWithAnnotation(instance.getClass(), ConfigValue.class);
            if (!fieldAnnotations.isEmpty()) {
                System.out.println("Kindle Field Annotations Present");
                String springCloudConfigServerResponse = requestConfigFromServer(buildConfigHttpRequestUrl(kindleAnnotation.get()));
                setConfigProperties(instance, fieldAnnotations, springCloudConfigServerResponse);
            }
            else {
                System.out.println("Kindle Field Annotations Not Found");
            }
        }
        else {
            System.out.println("Kindle Class Annotation Not Found");
        }
    }

    private String buildConfigHttpRequestUrl(Kindle annotation) {
        boolean argumentMatcherAnnotationsPresent = annotation.matchers().length > 0;

        Optional<KindleArgumentMatcher> results = Arrays.stream(annotation.matchers())
                .filter(pair -> pair.argValue().equals(argumentMatcherValue))
                .findFirst();

        // defaulr to request value if no arg matcher annotations defined on class
        String request = annotation.request();

        if (results.isPresent()) {
            request = results.get().requestValue();
        }
        else if (argumentMatcherAnnotationsPresent) {
            throw new KindleLauncherException("Kindle Launch Error - Class is annotated with Argument Matchers, but no match could be made with the provided value '" + argumentMatcherValue + "'");
        }

        return cloudConfigServerUrl + "/" + request;
    }

    private String requestConfigFromServer(String url) throws IOException {
        System.out.println("Kindle: Calling " + url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = httpClient.newCall(request);
        Response response = call.execute();

        System.out.println("Kindle: Spring Cloud Config Server Response: " + response.message());

        int httpStatusCode = response.code();
        if (httpStatusCode != 200) {
            throw new KindleHttpException("Response returned from Config Server was " + httpStatusCode + " - " + response.message());
        }

        ResponseBody body = response.body();
        if (body != null) {
            return body.string();
        }
        else {
            throw new KindleHttpException("No response received from '" + url + "'");
        }
    }

    private void setConfigProperties(Object instance, List<KindleFieldAnnotationPair<ConfigValue>> fieldAnnotationMap, String configResponse) {
        Yaml yaml = new Yaml();
        Map<String, Object> props = (Map<String, Object>) yaml.load(configResponse);
        fieldAnnotationMap.forEach(pair -> {
            Object configValue = getYamlPropertyIncludingNested(props, pair.getAnnotation().value());
            try {
                if (configValue != null || !pair.getField().getType().isPrimitive()) {
                    pair.getField().set(instance, configValue);
                }
            } catch (IllegalAccessException e) {
                throw new KindleReflectionException(e);
            }
        });
    }

    private Object getYamlPropertyIncludingNested(Map<String, Object> props, String path) {
        String[] paths = path.split(":");
        if (paths.length > 1) {
            Object propValue = null;
            Map<String, Object> propertyMap = props;
            for (int i = 0; i < paths.length; i++) {
                propValue = propertyMap.get(paths[i]);
                if (propValue instanceof Map) {
                    propertyMap = (Map<String, Object>) propValue;
                }
                else if (propValue == null && i < (paths.length - 1)) {
                    return null;
                }
            }

            return propValue;
        }
        else {
            return props.get(path);
        }
    }
}
