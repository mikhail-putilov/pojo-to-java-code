package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Setter {
    @Getter
    private final String propertyName;
    @Getter
    private final String propertyValue;

    @SneakyThrows({InvocationTargetException.class, IllegalAccessException.class})
    public Setter(Method getter, Object pojo) {
        if (getter.getReturnType().isPrimitive()) {
            propertyValue = getter.invoke(pojo).toString();
        } else if (getter.getReturnType().equals(String.class)) {
            propertyValue = "\"" + getter.invoke(pojo).toString() + "\"";
        } else {
            throw new IllegalStateException();
        }
        propertyName = getter.getName().replaceFirst("^get", "set");
    }
}
