package com.github.mikhail_putilov.pojo_to_code.domain;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NameResolver {

    /**
     * For each class type we need to track number of instances so that we can generate unique method names for them
     */
    private final Map<Class<?>, Integer> classToCounter = new HashMap<>();
    private final Set<Class<?>> allClasses = classToCounter.keySet();
    private final Map<Object, Integer> objectToCounter = new IdentityHashMap<>();
    private Map<String, Long> simpleNameToCounter;

    public void learnClass(Object object) {
        Class<?> clazz = object.getClass();
        classToCounter.putIfAbsent(clazz, -1);
        //noinspection ConstantConditions
        classToCounter.compute(clazz, (aClass, counter) -> ++counter);
        objectToCounter.put(object, classToCounter.get(clazz));
    }

    public void afterLearningAllClasses() {
        simpleNameToCounter = allClasses.stream()
            .collect(Collectors.groupingBy(Class::getSimpleName, Collectors.counting()));
    }

    public String resolveFactoryMethodName(Object pojo) {
        Integer discriminator = objectToCounter.get(pojo);
        return "create" + pojo.getClass().getSimpleName() + (discriminator.equals(0) ? "" : discriminator.toString());
    }

    public String resolveReturnType(Class<?> returnType) {
        long counter = simpleNameToCounter.get(returnType.getSimpleName());
        return counter == 1 ? returnType.getSimpleName() : returnType.getCanonicalName();
    }

    public Collection<String> resolveImports(Predicate<String> additionalFilter) {
        return allClasses.stream()
            .map(Class::getCanonicalName)
            .filter(a -> !a.startsWith("java.lang"))
            .filter(additionalFilter)
            .collect(Collectors.toCollection(TreeSet::new));
    }
}
