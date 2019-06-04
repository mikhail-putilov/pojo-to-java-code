package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.mikhail_putilov.pojo_to_code.domain.ExceptionUtils.invokeGetterOnPojo;

@Slf4j
class Dfs {
    private final Set<Object> visitedPojos = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Class<?>> doNotTraverseTheseTypes = Set.of(Class.class, ClassLoader.class);
    private final Function<Class<?>, Boolean> doNotTraverseThoseClassesToo;
    @Setter
    @Getter
    private boolean skipEnums = true;

    Dfs(Function<Class<?>, Boolean> doNotTraverseThoseClassesToo) {
        this.doNotTraverseThoseClassesToo = doNotTraverseThoseClassesToo;
    }

    void reset() {
        visitedPojos.clear();
        setSkipEnums(true);
    }

    void dfsTraverse(Object pojo, Consumer<Object> postOrderVisitFunc) {
        visitedPojos.add(pojo);
        if (!isStopNeeded(pojo.getClass())) {
            // if pojo is not in a stop list, we traverse its adjacent pojos
            for (Object adjacentPojo : getAdjacentPojos(pojo)) {
                if (!visitedPojos.contains(adjacentPojo)) {
                    dfsTraverse(adjacentPojo, postOrderVisitFunc);
                }
            }
        }
        postOrderVisitFunc.accept(pojo);
    }

    /**
     * during DFS java object traversal, we don't need to go deep into Class, ClassLoader and Enum objects.
     */
    private boolean isStopNeeded(Class<?> aClass) {
        return doNotTraverseTheseTypes.contains(aClass) || aClass.isEnum();
    }


    private List<Object> getAdjacentPojos(Object pojo) {
        log.trace("getAdjacentPojos {}", pojo);
        List<Object> pojos = new ArrayList<>();
        ReflectionUtils.doWithMethods(pojo.getClass(),
            getter -> pojos.add(invokeGetterOnPojo(pojo, getter)),
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
        if (returnType.isEnum() && isSkipEnums()) {
            log.trace("skipping \"{}\" as it is a enum property", method.getName());
            return false;
        }
        boolean isNotTraversable = doNotTraverseThoseClassesToo.apply(method.getReturnType());
        if (isNotTraversable) {
            log.trace("skipping \"{}\" as it can be created in-place", method.getName());
            return false;
        }
        return true;
    }
}
