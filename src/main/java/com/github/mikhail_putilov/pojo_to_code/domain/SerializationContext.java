package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class SerializationContext {
    private Set<Object> visited = new HashSet<>();
    private static List<FactoryMethod> builtinFactories = List.of(new PrimitiveFactoryMethod());
    private List<FactoryMethod> factories = new ArrayList<>(builtinFactories);
    private Object pojo;
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

    public FactoryMethod getFactoryMethodForModel() {
        dfs(pojo);
        return factories.get(0);
    }

    private void dfs(Object pojo) {
        visit(pojo);
        visited.add(pojo);
        for (Object adjacentPojo : getAdjacentPojos(pojo)) {
            if (!visited.contains(adjacentPojo)) {
                dfs(adjacentPojo);
            }
        }
        postOrder(pojo);
    }

    private void postOrder(Object pojo) {
        FactoryMethod factory = new FactoryMethod(pojo);
        ReflectionUtils.doWithMethods(pojo.getClass(),
            getter -> pojos.add(invoke(pojo, getter)),
            this::filterPropertiesWhichNeedsToBeVisited);
        factories.add(factory);
    }

    private List<Object> getAdjacentPojos(Object pojo) {
        List<Object> pojos = new ArrayList<>();
        ReflectionUtils.doWithMethods(pojo.getClass(),
            getter -> pojos.add(invoke(pojo, getter)),
            this::filterPropertiesWhichNeedsToBeVisited);
        return pojos;
    }

    @SneakyThrows
    private Object invoke(Object target, Method getter) {
        return getter.invoke(target);
    }

    private boolean filterPropertiesWhichNeedsToBeVisited(Method method) {
        if (!method.getName().startsWith("get")) {
            return false;
        }
        if ("getClass".equals(method.getName())) {
            return false;
        }
        Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            log.info("need visit? {} -> {}", returnType, false);
            return false;
        }
        log.info("need visit? {} -> {}", returnType.getSimpleName(), !knownClasses.contains(returnType));
        return !knownClasses.contains(returnType);
    }

    private void visit(Object object) {
        log.info("creating factory method for {}", object);
        log.info("foreach all props and create primitive setters");
    }

    public Setter createSetter(Method getter, Object pojo) {
        if (getter.getReturnType().isPrimitive()) {
            return new PrimitiveSetter(getter, pojo);
        } else if (getter.getReturnType().equals(String.class)) {
            return new StringSetter(getter, pojo);
        } else if (getter.getReturnType().equals(LocalDate.class)) {
            return new LocalDateSetter(getter, pojo);
        } else if (getter.getReturnType().isArray()) {
            return new ArraySetter(getter, pojo);
        } else {
            throw new IllegalStateException();
        }
    }
}
