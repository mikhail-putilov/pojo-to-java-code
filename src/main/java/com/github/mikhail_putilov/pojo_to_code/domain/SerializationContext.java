package com.github.mikhail_putilov.pojo_to_code.domain;

import com.github.mikhail_putilov.pojo_to_code.domain.create_function.TypeToJavaCreateCodeFunction;
import com.github.mikhail_putilov.pojo_to_code.domain.create_function.TypeToJavaCreateCodeFunctions;
import com.github.mikhail_putilov.pojo_to_code.domain.view.FactoryMethodView;
import com.github.mikhail_putilov.pojo_to_code.domain.view.SetterView;
import com.github.mikhail_putilov.pojo_to_code.domain.view.SetterViewImpl;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Slf4j
@Component
@Scope(SCOPE_PROTOTYPE)
public class SerializationContext {
    private final Object pojo;
    private Set<Object> visitedPojos = Collections.newSetFromMap(new IdentityHashMap<>());
    private List<FactoryMethodView> factoryMethodViews = new ArrayList<>();
    private Set<Class<?>> doNotTraverseTheseTypes = Set.of(Class.class, ClassLoader.class);
    @Setter(onMethod_ = @Autowired)
    private TypeToJavaCreateCodeFunctions typeToJavaCreateCodeFunctions;

    /**
     * Serialization context is coupled to the only one given pojo.
     *
     * @param pojo which needs to be converted to java code
     */
    public SerializationContext(Object pojo) {
        this.pojo = pojo;
    }

    public List<FactoryMethodView> getFactoryMethodsForPojo() {
        dfs(pojo);
        return factoryMethodViews;
    }

    private void dfs(Object pojo) {
        visitedPojos.add(pojo);
        if (!isStopNeeded(pojo.getClass())) {
            // if pojo is not in a stop list, we traverse its adjacent pojos
            for (Object adjacentPojo : getAdjacentPojos(pojo)) {
                if (!visitedPojos.contains(adjacentPojo)) {
                    dfs(adjacentPojo);
                }
            }
        }
        postOrderVisit(pojo);
    }

    /**
     * during DFS java object traversal, we don't need to go deep into Class, ClassLoader and Enum objects.
     */
    private boolean isStopNeeded(Class<?> aClass) {
        return doNotTraverseTheseTypes.contains(aClass) || aClass.isEnum();
    }

    private void postOrderVisit(Object pojo) {
        List<Method> getters = new ArrayList<>();
        ReflectionUtils.doWithMethods(pojo.getClass(),
            getters::add,
            this::filterAccessibleGetters);

        List<? extends SetterView> setterViews = getters.stream()
            .sequential()
            .filter(getter -> typeToJavaCreateCodeFunctions.get(getter.getReturnType()).isPresent())
            // which means we can create a setter view right away!
            .map(getter -> {
                Class<?> returnType = getter.getReturnType();
                TypeToJavaCreateCodeFunction mapFunc = typeToJavaCreateCodeFunctions.get(returnType).orElseThrow();
                return new SetterViewImpl(getter, pojo, mapFunc);
            })
            .collect(Collectors.toList());

        FactoryMethodView factory = new FactoryMethodView(pojo, setterViews);
        factoryMethodViews.add(factory);
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
        Optional<TypeToJavaCreateCodeFunction> typeToJavaCreateCodeFunction = typeToJavaCreateCodeFunctions.get(returnType);
        if (typeToJavaCreateCodeFunction.isPresent()) {
            log.trace("skipping \"{}\" as it can be created in-place", method.getName());
            return false;
        }
        return true;
    }
}
