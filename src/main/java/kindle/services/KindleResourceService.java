package kindle.services;

import kindle.exceptions.KindleLauncherException;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.InputStream;
import java.util.Set;
import java.util.regex.Pattern;

public class KindleResourceService {
    public InputStream getResource(Class<?> clazz, String resourcePath) {
        return clazz.getClassLoader().getResourceAsStream(resourcePath);
    }

    public String findSparkResource(Class<?> clazz) {
        Package packageName = clazz.getPackage();
        Set<String> resources = new Reflections(new ResourcesScanner()).getResources(Pattern.compile("spark.properties"));
        if (resources.isEmpty())
            throw new KindleLauncherException("Error launching spark job: could not find spark.properties resource in package " + packageName.getName());
        else
            return resources.iterator().next();
    }
}
