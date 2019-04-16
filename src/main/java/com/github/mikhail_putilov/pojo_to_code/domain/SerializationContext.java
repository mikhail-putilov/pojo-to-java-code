package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class SerializationContext {
    private Set<Object> visited = new HashSet<>();
    private List<FactoryMethod> factories = new ArrayList<>();
    private Object pojo;
    private Set<Class<?>> stopList = Set.of(Class.class, ClassLoader.class);

    private Set<Class<?>> knownClasses = Set.of(
        Long.class,
        long[].class,
        Integer.class,
        int[].class,
        Short.class,
        short[].class,
        Character.class,
        char[].class,
        Byte.class,
        byte[].class,
        Boolean.class,
        boolean[].class,
        Float.class,
        float[].class,
        Double.class,
        double[].class,
        LocalDate.class,
        String.class);

    public SerializationContext(Object pojo) {
        this.pojo = pojo;
    }

    public List<FactoryMethod> getFactoryMethodsForPojo() {
        dfs(pojo);
        return factories;
    }

    private void dfs(Object pojo) {
        visited.add(pojo);
        if (isInStopList(pojo.getClass())) {
            return;
        }
        for (Object adjacentPojo : getAdjacentPojos(pojo)) {
            if (!visited.contains(adjacentPojo)) {
                dfs(adjacentPojo);
            }
        }
        postOrder(pojo);
    }

    private boolean isInStopList(Class<?> aClass) {
        return stopList.contains(aClass) || aClass.isEnum();
    }

    private void postOrder(Object pojo) {
        FactoryMethod factory = new FactoryMethod(pojo);
        ReflectionUtils.doWithMethods(pojo.getClass(),
            getter -> factory.addSetter(getter, pojo),
            this::filterAccessibleGetters);
        factories.add(factory);
    }

    private List<Object> getAdjacentPojos(Object pojo) {
        log.trace("getAdjacentPojos {}", pojo);
        List<Object> pojos = new ArrayList<>();
        ReflectionUtils.doWithMethods(pojo.getClass(),
            getter -> pojos.add(invoke(pojo, getter)),
            this::filterNonPrimitiveGetters);
        return pojos;
    }

    @SneakyThrows
    private Object invoke(Object target, Method getter) {
        return getter.invoke(target);
    }

    private boolean filterAccessibleGetters(Method method) {
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            log.trace("skipping static method {}", method.getName());
            return false;
        }
        if (method.isSynthetic()) {
            log.trace("skipping {} as it is a synthetic method", method.getName());
            return false;
        }
        if (Modifier.isNative(method.getModifiers())) {
            log.trace("skipping {} as it is a native method", method.getName());
            return false;
        }
        if (Modifier.isPrivate(method.getModifiers())) {
            log.trace("skipping {} as it is a private method", method.getName());
            return false;
        }
        if (method.getParameterCount() != 0) {
            log.trace("getter {} with parameter count > 0 is not a property", method.getName());
            return false;
        }
        if (!method.getName().startsWith("get")) {
            log.trace("{} is not a getter", method.getName());
            return false;
        }
        if ("getClass".equals(method.getName())) {
            log.trace("skipping getClass as it is not a property");
            return false;
        }
        if ("getClassLoader".equals(method.getName())) {
            log.trace("skipping getClassLoader as it is not a property");
            return false;
        }
        if ("getClassLoader0".equals(method.getName())) {
            log.trace("skipping getClassLoader0 as it is not a property");
            return false;
        }
        return true;
    }

    private boolean filterNonPrimitiveGetters(Method method) {
        if (!filterAccessibleGetters(method)) {
            return false;
        }
        Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            log.trace("skipping {} as it is a primitive property", method.getName());
            return false;
        }
        if (returnType.isEnum()) {
            log.trace("skipping {} as it is a enum property", method.getName());
            return false;
        }
        boolean isNeededAnotherFactoryMethod = !knownClasses.contains(returnType);
        if (isNeededAnotherFactoryMethod) {
            log.trace("{} needs another factory method", method.getName());
        } else {
            log.trace("{} doesn't need factory method", method.getName());
        }
        return isNeededAnotherFactoryMethod;
    }
}
