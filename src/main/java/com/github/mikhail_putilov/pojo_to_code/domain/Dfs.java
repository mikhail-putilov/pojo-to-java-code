package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.mikhail_putilov.pojo_to_code.domain.ExceptionUtils.invokeGetterOnPojo;

@Slf4j
@Builder
class Dfs {
    private final Set<Object> visitedPojos = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Class<?>> skipAllPropertiesForTheseTypes = Set.of(Class.class, ClassLoader.class);
    @SuppressWarnings("UnusedAssignment")
    @Builder.Default
    private Predicate<Class<?>> skipAllPropertiesForThisType = a -> false;
    @SuppressWarnings("UnusedAssignment")
    @Builder.Default
    private Consumer<Object> postOrderVisitFunc = a -> {
    };
    @SuppressWarnings("UnusedAssignment")
    @Builder.Default
    private Consumer<Object> preOrderVisitFunc = a -> {
    };
    @SuppressWarnings("UnusedAssignment")
    @Builder.Default
    private Predicate<Class<?>> skipPropertyWithType = clazz -> false;
    private Object target;

    void dfsTraverse() {
        _dfsTraverse(target);
    }

    private void _dfsTraverse(Object pojo) {
        visitedPojos.add(pojo);
        preOrderVisitFunc.accept(pojo);
        for (Object adjacentPojo : getAdjacentPojos(pojo)) {
            if (!visitedPojos.contains(adjacentPojo)) {
                _dfsTraverse(adjacentPojo);
            }
        }
        postOrderVisitFunc.accept(pojo);
    }

    private List<Object> getAdjacentPojos(Object pojo) {
        log.trace("getAdjacentPojos {}", pojo);
        if (skipAllPropertiesForThisType.test(pojo.getClass()) || skipAllPropertiesForTheseTypes.contains(pojo.getClass())) {
            return List.of();
        }
        List<Object> pojos = new ArrayList<>();
        ReflectionUtils.doWithMethods(pojo.getClass(),
            getter -> {
                if (!skipPropertyWithType.test(getter.getReturnType())) {
                    pojos.add(invokeGetterOnPojo(pojo, getter));
                }
            },
            this::filterNonPrimitiveGetters);
        return pojos;
    }

    List<Method> getAccessibleGetters(Object pojo) {
        List<Method> getters = new ArrayList<>();
        ReflectionUtils.doWithMethods(pojo.getClass(),
            getters::add,
            this::filterAccessibleGetters);
        return getters;
    }

    private boolean filterAccessibleGetters(Method method) {
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            log.trace("skipping static method \"{}\"", method.getName());
            return false;
        }
        if (method.isSynthetic()) {
            log.trace("skipping \"{}\" as it is a synthetic method", method.getName());
            return false;
        }
        if (Modifier.isNative(method.getModifiers())) {
            log.trace("skipping \"{}\" as it is a native method", method.getName());
            return false;
        }
        if (Modifier.isPrivate(method.getModifiers())) {
            log.trace("skipping \"{}\" as it is a private method", method.getName());
            return false;
        }
        if (method.getParameterCount() != 0) {
            log.trace("getter \"{}\" with parameter count > 0 is not a property", method.getName());
            return false;
        }
        if (!method.getName().startsWith("get")) {
            log.trace("method \"{}\" is not a getter", method.getName());
            return false;
        }
        if ("getClass".equals(method.getName()) && method.getReturnType().isAssignableFrom(Class.class)) {
            log.trace("skipping \"getClass\" as it is not a property");
            return false;
        }
        if ("getClassLoader".equals(method.getName()) && method.getReturnType().isAssignableFrom(ClassLoader.class)) {
            log.trace("skipping \"getClassLoader\" as it is not a property");
            return false;
        }
        if ("getClassLoader0".equals(method.getName()) && method.getReturnType().isAssignableFrom(ClassLoader.class)) {
            log.trace("skipping \"getClassLoader0\" as it is not a property");
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
            log.trace("skipping \"{}\" as it is a primitive property", method.getName());
            return false;
        }
//        boolean isNotTraversable = skipAllPropertiesForThisType.test(method.getReturnType());
//        if (isNotTraversable) {
//            log.trace("skipping \"{}\" as it can be created in-place", method.getName());
//            return false;
//        }
        return true;
    }
}
