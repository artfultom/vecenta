package io.github.artfultom.vecenta.util;

import io.github.artfultom.vecenta.matcher.annotations.Model;
import io.github.artfultom.vecenta.matcher.annotations.RpcMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectionUtils {

    private static final Logger log = LoggerFactory.getLogger(ReflectionUtils.class);

    private ReflectionUtils() {
    }

    public static List<Method> getPublicGetters(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(item -> item.getName().startsWith("get") && item.getParameterTypes().length == 0)
                .filter(item -> Modifier.isPublic(item.getModifiers()))
                .collect(Collectors.toList());
    }

    public static List<Method> getPublicSetters(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(item -> item.getName().startsWith("set") && item.getParameterTypes().length == 1)
                .filter(item -> Modifier.isPublic(item.getModifiers()))
                .collect(Collectors.toList());
    }

    public static Field getField(Class<?> clazz, Method method) {
        Optional<Field> fieldOptional = Arrays.stream(clazz.getDeclaredFields())
                .filter(item -> item.getName().equalsIgnoreCase(method.getName().substring(3)))
                .findFirst();

        return fieldOptional.orElse(null);
    }

    public static Set<Class<?>> findModelClasses() throws IOException {
        Set<Class<?>> result = new HashSet<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        List<String> packNames = Arrays.stream(classLoader.getDefinedPackages())
                .map(item -> item.getName().split("\\.")[0])
                .distinct()
                .collect(Collectors.toList());
        for (String pack : packNames) {
            Enumeration<URL> resources = classLoader.getResources(pack.replace('.', '/'));

            List<File> dirs = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                dirs.add(new File(resource.getFile()));
            }

            for (File directory : dirs) {
                List<Class<?>> classes = findClasses(directory, pack);

                classes = classes.stream()
                        .filter(item -> item.isAnnotationPresent(Model.class))
                        .collect(Collectors.toList());

                result.addAll(classes);
            }
        }

        return result;
    }

    public static Set<Class<?>> findServerClasses(String packageName) throws IOException {
        Set<Class<?>> result = new HashSet<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Enumeration<URL> resources = classLoader.getResources(packageName.replace('.', '/'));

        List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            dirs.add(new File(resource.getFile()));
        }

        for (File directory : dirs) {
            List<Class<?>> classes = findClasses(directory, packageName);

            classes = classes.stream()
                    .filter(item -> {
                        if (item.isInterface()) {
                            return false;
                        }

                        Class<?>[] inters = item.getInterfaces();
                        for (Class<?> inter : inters) {
                            for (Method method : inter.getMethods()) {
                                for (Annotation annotation : method.getAnnotations()) {
                                    if (annotation.annotationType() == RpcMethod.class) {
                                        return true;
                                    }
                                }
                            }
                        }

                        return false;
                    })
                    .collect(Collectors.toList());

            result.addAll(classes);
        }

        return result;
    }

    private static List<Class<?>> findClasses(File directory, String packageName) {
        List<Class<?>> classes = new ArrayList<>();

        if (directory.exists()) {
            File[] files = directory.listFiles();

            if (files == null) {
                return classes;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    classes.addAll(findClasses(file, packageName + "." + file.getName()));
                } else {
                    if (file.getName().endsWith(".class")) {
                        String name = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);

                        try {
                            classes.add(Class.forName(name));
                        } catch (ClassNotFoundException e) {
                            log.error(String.format("Cannot find class %s.", name), e);
                        }
                    }
                }
            }
        }

        return classes;
    }
}
