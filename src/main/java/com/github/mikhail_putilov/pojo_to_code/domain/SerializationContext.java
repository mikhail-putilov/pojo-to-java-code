package com.github.mikhail_putilov.pojo_to_code.domain;

import com.github.mikhail_putilov.pojo_to_code.domain.create_function.CodeCreationNameResolver;
import com.github.mikhail_putilov.pojo_to_code.domain.view.FactoryClassView;
import com.github.mikhail_putilov.pojo_to_code.domain.view.FactoryMethodView;
import com.github.mikhail_putilov.pojo_to_code.domain.view.SetterView;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.mikhail_putilov.pojo_to_code.domain.ExceptionUtils.invokeGetterOnPojo;

@Slf4j
public class SerializationContext {
    private final Object pojo;

    private final FactoryClassView.FactoryClassViewBuilder result = FactoryClassView.builder();
    private final CodeCreationNameResolver codeCreationNameResolver;
    private final NameResolver nameResolver;
    private Dfs dfs;

    /**
     * Serialization context is coupled to the only one given pojo.
     *
     * @param pojo which needs to be converted to java code
     */
    public SerializationContext(Object pojo, CodeCreationNameResolver factoryCodeCreationContext, NameResolver nameResolver) {
        this.pojo = pojo;
        this.codeCreationNameResolver = factoryCodeCreationContext;
        this.nameResolver = nameResolver;
    }

    public FactoryClassView generateFactoryClass() {
        dfs = Dfs.builder()
            .target(pojo)
            .preOrderVisitFunc(nameResolver::learnClass)
            .skipPropertyWithType(codeCreationNameResolver::isJavaLang)
            .skipAllPropertiesForThisType(codeCreationNameResolver::isBuiltinType)
            .build();
        dfs.dfsTraverse();

        afterLearningAllClassesBeforeSecondTraverse();

        dfs = Dfs.builder()
            .target(pojo)
            .postOrderVisitFunc(this::postOrderVisit)
            .skipPropertyWithType(clazz -> codeCreationNameResolver.isBuiltinType(clazz) || clazz.isEnum())
            .build();
        dfs.dfsTraverse();
        return buildFactoryClassView();
    }

    private void afterLearningAllClassesBeforeSecondTraverse() {
        nameResolver.afterLearningAllClasses();
    }

    private FactoryClassView buildFactoryClassView() {
        String packageName = pojo.getClass().getPackageName();
        Predicate<String> dropTheseImports = importStr -> !importStr.startsWith(packageName);

        result.className("Create" + pojo.getClass().getSimpleName())
            .imports(nameResolver.resolveImports(dropTheseImports))
            .packageName(packageName);
        return result.build();
    }

    private void postOrderVisit(Object pojo) {
        FactoryMethodView factory = buildFactoryMethodView(pojo);
        result.factory(factory);
    }

    private FactoryMethodView buildFactoryMethodView(Object pojo) {
        List<Method> getters = dfs.getAccessibleGetters(pojo);

        List<SetterView> setterViews = getters.stream()
            .map(getter -> buildSetter(getter, pojo))
            .sorted(Comparator.comparing(SetterView::getPropertyName))
            .collect(Collectors.toList());

        var builder = FactoryMethodView.builder()
            .setters(setterViews)
            .factoryMethodName(nameResolver.resolveFactoryMethodName(pojo))
            .localVariableName("bean")
            .returnType(nameResolver.resolveReturnType(pojo.getClass()));
        return builder.build();
    }

    private SetterView buildSetter(Method getter, Object pojo) {
        SetterView setter = new SetterView();
        setter.setPropertyNameFromGetter(getter);
        Object propertyValue = invokeGetterOnPojo(pojo, getter);
        setter.setPropertyValue(codeCreationNameResolver.get(propertyValue));
        return setter;
    }
}
