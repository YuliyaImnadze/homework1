package org.example.runner;


import org.example.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TestRunner {

    private final static String ERROR_MESSAGE_FOR_BEFORE_SUITE = "@BeforeSuite annotation can only be used once";
    private final static String ERROR_MESSAGE_FOR_AFTER_SUITE = "@AfterSuite annotation can only be used once";

    public static void runTests(Class<?> с) throws Exception {
        Method[] allMethods = с.getDeclaredMethods();

        List<Method> beforeSuiteMethods = findAndValidateSuiteMethods(allMethods, BeforeSuite.class,
                ERROR_MESSAGE_FOR_BEFORE_SUITE);
        List<Method> afterSuiteMethods = findAndValidateSuiteMethods(allMethods, AfterSuite.class,
                ERROR_MESSAGE_FOR_AFTER_SUITE);
        List<Method> testMethods = findAndValidateTestMethods(allMethods);
        List<Method> beforeTests = findMethodsByAnnotation(allMethods, BeforeTest.class);
        List<Method> afterTests = findMethodsByAnnotation(allMethods, AfterTest.class);

        Object instance = с.getDeclaredConstructor().newInstance();

        invokeStaticIfExists(beforeSuiteMethods);

        for (Method testMethod : testMethods) {
            for (Method beforeTest : beforeTests) {
                invokeMethod(beforeTest, instance);
            }

            invokeTestMethod(testMethod, instance);

            for (Method afterTest : afterTests) {
                invokeMethod(afterTest, instance);
            }
        }

        invokeStaticIfExists(afterSuiteMethods);
    }

    private static <A extends Annotation> List<Method> findAndValidateSuiteMethods(Method[] methods,
                                                                                   Class<A> annotationClass,
                                                                                   String errorMessage
    ) {
        List<Method> result = Stream.of(methods)
                .filter(m -> {
                    if (m.isAnnotationPresent(annotationClass)) {
                        if (!Modifier.isStatic(m.getModifiers())) {
                            throw new RuntimeException(("@%s should only be used on static methods. "
                                    + "Incorrect usage in the method %s ")
                                    .formatted(annotationClass.getSimpleName(), m.getName()));
                        }
                        return true;
                    }
                    return false;
                })
                .toList();

        if (result.size() > 1) {
            throw new RuntimeException(errorMessage);
        }

        return result;
    }


    private static List<Method> findAndValidateTestMethods(Method[] methods) {
        return Stream.of(methods)
                .filter(m -> {
                    if (!m.isAnnotationPresent(Test.class)) return false;

                    int priority = m.getAnnotation(Test.class).priority();
                    if (priority < 1 || priority > 10) {
                        throw new RuntimeException("Priority in method %s must be between 1 and 10: "
                                .formatted(m.getName()));
                    }

                    return true;
                })
                .sorted((m1, m2) -> Integer.compare(
                        m2.getAnnotation(Test.class).priority(),
                        m1.getAnnotation(Test.class).priority()
                ))
                .toList();
    }

    private static <A extends Annotation> List<Method> findMethodsByAnnotation(Method[] methods, Class<A> annotation) {
        return Stream.of(methods)
                .filter(m -> m.isAnnotationPresent(annotation))
                .toList();
    }

    private static void invokeStaticIfExists(List<Method> methods) throws Exception {
        if (!methods.isEmpty()) {
            Method method = methods.getFirst();
            method.setAccessible(true);
            method.invoke(null);
        }
    }

    private static void invokeMethod(Method method, Object instance) throws Exception {
        method.setAccessible(true);
        method.invoke(instance);
    }

    private static void invokeTestMethod(Method method, Object instance) throws Exception {
        method.setAccessible(true);

        if (method.isAnnotationPresent(CsvSource.class)) {
            String csv = method.getAnnotation(CsvSource.class).value();
            String[] parts = csv.split(",\\s*");
            Parameter[] params = method.getParameters();

            if (parts.length != params.length) {
                throw new IllegalArgumentException("Mismatch between CSV values and method parameters for: %s"
                        .formatted(method.getName()));
            }

            Object[] args = IntStream.range(0, params.length)
                    .mapToObj(i -> convert(parts[i], params[i].getType()))
                    .toArray();

            method.invoke(instance, args);
        } else {
            method.invoke(instance);
        }
    }

    private static Object convert(String str, Class<?> type) {
        if (type == int.class) return Integer.parseInt(str);
        if (type == boolean.class) return Boolean.parseBoolean(str);
        if (type == double.class) return Double.parseDouble(str);
        if (type == long.class) return Long.parseLong(str);
        if (type == String.class) return str;
        throw new IllegalArgumentException("Unsupported parameter type: " + type);
    }
}
