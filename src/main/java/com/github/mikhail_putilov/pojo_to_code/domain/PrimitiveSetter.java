package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PrimitiveSetter extends AbstractSetter {

    @Getter
    private final String propertyValue;

    @SneakyThrows({InvocationTargetException.class, IllegalAccessException.class})
    public PrimitiveSetter(Method getter, Object pojo) {
        super(getter);
        propertyValue = getter.invoke(pojo).toString();
    }
}
