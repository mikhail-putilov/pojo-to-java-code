package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.text.StringEscapeUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StringSetter extends AbstractSetter {
    @Getter
    private final String propertyValue;

    @SneakyThrows({InvocationTargetException.class, IllegalAccessException.class})
    public StringSetter(Method getter, Object pojo) {
        super(getter);
        propertyValue = "\"" + StringEscapeUtils.escapeJava(getter.invoke(pojo).toString()) + "\"";
    }
}
