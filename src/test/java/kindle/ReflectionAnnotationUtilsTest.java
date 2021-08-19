package kindle;

import kindle.Utils.ReflectionAnnotationUtils;
import kindle.annotations.ConfigValue;
import kindle.annotations.Kindle;
import kindle.domain.KindleFieldAnnotationPair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RunWith(Enclosed.class)
public class ReflectionAnnotationUtilsTest {

    @Kindle
    private class Stub {

        @ConfigValue("A")
        private String fieldA;

        @ConfigValue("B")
        public String fieldB;
    }

    @RunWith(JUnit4.class)
    public static class ReflectionAnnotationTest {
        private String fieldName = "fieldA";

        @Test
        public void findAnnotationOnClassPresentTest() {
            Optional<Kindle> result = ReflectionAnnotationUtils.findAnnotation(Stub.class, Kindle.class);
            Assert.assertTrue("Annotation should be present on class", result.isPresent());
        }

        @Test
        public void findAnnotationOnClassNotPresentTest() {
            Optional<Test> result = ReflectionAnnotationUtils.findAnnotation(Stub.class, Test.class);
            Assert.assertFalse("Annotation should not be present on class", result.isPresent());
        }

        @Test
        public void findAnnotationOnClassEqualsExpectedTest() {
            Optional<Kindle> result = ReflectionAnnotationUtils.findAnnotation(Stub.class, Kindle.class);
            Assert.assertEquals(Kindle.class, result.get().annotationType());
        }

        @Test
        public void findAnnotationOnFieldPresentTest() throws NoSuchFieldException {
            Field field = Stub.class.getDeclaredField(fieldName);
            Optional<ConfigValue> result = ReflectionAnnotationUtils.findAnnotation(field, ConfigValue.class);
            Assert.assertTrue("Annotation should be present on class", result.isPresent());
        }

        @Test
        public void findAnnotationOnFieldNotPresentTest() throws NoSuchFieldException {
            Field field = Stub.class.getDeclaredField(fieldName);
            Optional<Test> result = ReflectionAnnotationUtils.findAnnotation(field, Test.class);
            Assert.assertFalse("Annotation should not be present on class", result.isPresent());
        }

        @Test
        public void findAnnotationOnFieldEqualsExpectedTest() throws NoSuchFieldException {
            Field field = Stub.class.getDeclaredField(fieldName);
            Optional<ConfigValue> result = ReflectionAnnotationUtils.findAnnotation(field, ConfigValue.class);
            Assert.assertEquals(ConfigValue.class, result.get().annotationType());
        }

        @Test
        public void findFieldsWithAnnotationResultSizeMatchesTest() {
            List<KindleFieldAnnotationPair<ConfigValue>> results = ReflectionAnnotationUtils.findFieldsWithAnnotation(Stub.class, ConfigValue.class);
            Assert.assertEquals("Expected 2 results for fields annotated", 2, results.size());
        }
    }

    @RunWith(Parameterized.class)
    public static class FindFieldsWithAnnotationValues {

        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    { "fieldA", "A" },
                    { "fieldB", "B" }
            });
        }

        private String fieldName;
        private String annotationValue;

        public FindFieldsWithAnnotationValues(String fieldName, String annotationValue) {
            this.fieldName = fieldName;
            this.annotationValue = annotationValue;
        }

        @Test
        public void findFieldsWithAnnotationTest() {
            List<KindleFieldAnnotationPair<ConfigValue>> results = ReflectionAnnotationUtils.findFieldsWithAnnotation(Stub.class, ConfigValue.class);
            KindleFieldAnnotationPair<ConfigValue> pair = findFieldAnnotationPair(results, fieldName);
            Assert.assertEquals("Expected field name to be '" + fieldName + "'", fieldName, pair.getField().getName());
            Assert.assertEquals("Expected annotation value to be '" + annotationValue + "'", annotationValue, pair.getAnnotation().value());
        }

        private static KindleFieldAnnotationPair<ConfigValue> findFieldAnnotationPair(List<KindleFieldAnnotationPair<ConfigValue>> results, String fieldName) {
            return results
                    .stream()
                    .filter(pair -> pair.getField().getName().equals(fieldName))
                    .findFirst()
                    .get();
        }
    }


}
