package com.github.mikhail_putilov.pojo_to_code.domain;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NameResolver {

    /**
     * For each class type we need to track number of instances so that we can generate unique method names for them
     */
    private final Map<Class<?>, Integer> identityCounters = new HashMap<>();
    private final Set<Class<?>> allClasses = identityCounters.keySet();
    private final Map<Object, Integer> objectToCounter = new IdentityHashMap<>();
    private Map<String, Set<Class<?>>> simpleNameToClazzes;

    public void learnClass(Object object) {
        Class<?> clazz = object.getClass();
        identityCounters.putIfAbsent(clazz, -1);
        identityCounters.compute(clazz, (aClass, counter) -> counter + 1);
        objectToCounter.put(object, identityCounters.get(clazz));
    }

    public void prepareImports() {
        simpleNameToClazzes = allClasses.stream()
            .collect(Collectors.groupingBy(Class::getSimpleName, Collectors.toSet()));

    }

    public String resolveFactoryMethodName(Object pojo) {
        Integer discriminator = objectToCounter.get(pojo);
        return "create" + pojo.getClass().getSimpleName() + (discriminator.equals(0) ? "" : discriminator.toString());
    }

    public String resolveReturnType(Class<?> returnType) {
        Set<Class<?>> classes = simpleNameToClazzes.get(returnType.getSimpleName());
        return classes.size() == 1 ? returnType.getSimpleName() : returnType.getCanonicalName();
    }

    public Set<String> resolveImports() {
        return allClasses.stream().map(Class::getCanonicalName).collect(Collectors.toUnmodifiableSet());
    }
}
