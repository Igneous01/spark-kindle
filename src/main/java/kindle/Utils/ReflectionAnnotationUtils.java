package kindle.Utils;

import kindle.domain.KindleFieldAnnotationPair;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ReflectionAnnotationUtils {

    private ReflectionAnnotationUtils() {
    }

    // gotta love java generics
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> Optional<A> findAnnotation(Class clazz, Class<A> annotationType) {
        return Arrays
                .stream(clazz.getDeclaredAnnotationsByType(annotationType))
                .map(annotation -> (A) annotation)
                .findFirst();
    }

    public static <A extends Annotation> Optional<A> findAnnotation(Field field, Class<A> annotationType) {
        return Arrays
                .stream(field.getDeclaredAnnotationsByType(annotationType))
                .findFirst();
    }

    public static <A extends Annotation> List<KindleFieldAnnotationPair<A>> findFieldsWithAnnotation(Class clazz, Class<A> annotationType) {
        List<KindleFieldAnnotationPair<A>> results = new ArrayList<>();
        for(Field field : clazz.getDeclaredFields()){
            Optional<A> annotation = ReflectionAnnotationUtils.findAnnotation(field, annotationType);
            annotation.ifPresent(value -> results.add(new KindleFieldAnnotationPair<>(field, value)));
        }
        return results;
    }
}
