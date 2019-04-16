package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

public class FactoryMethod {
    private final Class<?> clazz;
    @Getter(lazy = true)
    private final String returnType = clazz.getSimpleName();
    @Getter(lazy = true)
    private final String pojoName = capitalize(getReturnType());
    @Getter(lazy = true)
    private final String localVariableName = uncapitalize(getReturnType());
    @Getter
    private final List<Setter> setters = new ArrayList<>();

    public FactoryMethod(Object pojo) {
        clazz = pojo.getClass();
    }

    public void addSetter(Method getter, Object value) {
        setters.add(new SetterImpl(getter, value));
    }
}
