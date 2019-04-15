package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

@Component
@Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
public class Model {
    private final Class<?> clazz;
    private final Object pojo;
    @lombok.Setter(onMethod_=@Autowired)
    private SetterFactory setterFactory;
    @Getter(lazy = true)
    private final String returnType = clazz.getSimpleName();
    @Getter(lazy = true)
    private final String pojoName = capitalize(getReturnType());
    @Getter(lazy = true)
    private final String localVariableName = uncapitalize(getReturnType());
    @Getter
    private final List<Setter> setters = new ArrayList<>();

    public Model(Object pojo) {
        clazz = pojo.getClass();
        this.pojo = pojo;
    }

    @PostConstruct
    private void setSetters() {
        ReflectionUtils.doWithMethods(clazz,
                method1 -> setters.add(setterFactory.createSetter(method1, pojo)),
                method -> method.getName().startsWith("get") && !method.getName().equals("getClass"));
    }
}
