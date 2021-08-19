package kindle.domain;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class KindleFieldAnnotationPair<A extends Annotation> {

    private Field field;
    private A annotation;

    public KindleFieldAnnotationPair(Field field, A annotation) {
        this.setField(field);
        this.setAnnotation(annotation);
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public A getAnnotation() {
        return annotation;
    }

    public void setAnnotation(A annotation) {
        this.annotation = annotation;
    }
}
