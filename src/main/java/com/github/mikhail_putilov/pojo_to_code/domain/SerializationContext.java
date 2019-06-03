package com.github.mikhail_putilov.pojo_to_code.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mikhail_putilov.pojo_to_code.domain.create_function.FactoryCodeCreationContext;
import com.github.mikhail_putilov.pojo_to_code.domain.view.FactoryClassView;
import com.github.mikhail_putilov.pojo_to_code.domain.view.FactoryMethodView;
import com.github.mikhail_putilov.pojo_to_code.domain.view.SetterView;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class SerializationContext {
    private final Object pojo;
    private final ObjectMapper objectMapper;
    private final Set<Object> visitedPojos = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<FactoryMethodView> factoryMethodViews = new ArrayList<>();
    private final Set<Class<?>> doNotTraverseTheseTypes = Set.of(Class.class, ClassLoader.class);
    private final FactoryCodeCreationContext factoryCodeCreationContext;
    private final NameResolver nameResolver;

    /**
     * Serialization context is coupled to the only one given pojo.
     *
     * @param pojo which needs to be converted to java code
     */
    public SerializationContext(Object pojo, ObjectMapper objectMapper, FactoryCodeCreationContext factoryCodeCreationContext, NameResolver nameResolver) {
        this.pojo = pojo;
        this.objectMapper = objectMapper;
        this.factoryCodeCreationContext = factoryCodeCreationContext;
        this.nameResolver = nameResolver;
    }

    public FactoryClassView generateFactoryClass() {
        dfs(pojo, this::firstPostOrderVisit);
        visitedPojos.clear();
        nameResolver.prepareImports();
        dfs(pojo, this::postOrderVisit);
        return buildFactoryClassView();
    }

    private FactoryClassView buildFactoryClassView() {
        FactoryClassView factoryClass = new FactoryClassView();
        factoryClass.setClassName("Create" + pojo.getClass().getSimpleName());
        factoryClass.setFactories(factoryMethodViews);
        factoryClass.setImports(nameResolver.resolveImports());
        factoryClass.setPackageName(pojo.getClass().getPackageName());
        return factoryClass;
    }

    /**
     * Gather information about all types, so that we can resolve name clashes effectively
     */
    private void firstPostOrderVisit(Object pojo) {
        nameResolver.learnClass(pojo);
    }

    private void dfs(Object pojo, Consumer<Object> postOrderVisitFunc) {
        visitedPojos.add(pojo);
        if (!isStopNeeded(pojo.getClass())) {
            // if pojo is not in a stop list, we traverse its adjacent pojos
            for (Object adjacentPojo : getAdjacentPojos(pojo)) {
                if (!visitedPojos.contains(adjacentPojo)) {
                    dfs(adjacentPojo, postOrderVisitFunc);
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

    private void postOrderVisit(Object pojo) {
        FactoryMethodView factory = buildFactoryMethodView(pojo);
        factoryMethodViews.add(factory);
    }

    private FactoryMethodView buildFactoryMethodView(Object pojo) {
        List<Method> getters = new ArrayList<>();
        ReflectionUtils.doWithMethods(pojo.getClass(),
            getters::add,
            this::filterAccessibleGetters);

        var setterViews = getters.stream()
            .sequential()
            .map(getter -> buildSetter(getter, pojo))
            .collect(Collectors.toList());

        FactoryMethodView factoryMethod = new FactoryMethodView();
        factoryMethod.setSetters(setterViews);
        factoryMethod.setFactoryMethodName(nameResolver.resolveFactoryMethodName(pojo));
        factoryMethod.setLocalVariableName("bean");
        factoryMethod.setReturnType(nameResolver.resolveReturnType(pojo.getClass()));
        return factoryMethod;
    }

    private SetterView buildSetter(Method getter, Object pojo) {
        SetterView setter = new SetterView();
        setter.setPropertyNameFromGetter(getter);
        Object propertyValue = invokeGetterOnPojo(pojo, getter);
        setter.setPropertyValue(factoryCodeCreationContext.get(propertyValue));
        return setter;
    }

    private List<Object> getAdjacentPojos(Object pojo) {
        log.trace("getAdjacentPojos {}", pojo);
        List<Object> pojos = new ArrayList<>();
        ReflectionUtils.doWithMethods(pojo.getClass(),
            getter -> pojos.add(invokeGetterOnPojo(pojo, getter)),
            this::filterNonPrimitiveGetters);
        return pojos;
    }

    @SneakyThrows
    private Object invokeGetterOnPojo(Object pojo, Method getter) {
        return getter.invoke(pojo);
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
        if (returnType.isEnum()) {
            log.trace("skipping \"{}\" as it is a enum property", method.getName());
            return false;
        }
        boolean isBuiltinType = factoryCodeCreationContext.isBuiltinType(method.getReturnType());
        if (isBuiltinType) {
            log.trace("skipping \"{}\" as it can be created in-place", method.getName());
            return false;
        }
        return true;
    }
}
