package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.Getter;

import java.lang.reflect.Method;

public abstract class AbstractSetter implements Setter {
    @Getter
    protected final String propertyName;

    public AbstractSetter(Method getter) {
        propertyName = getter.getName().replaceFirst("^get", "set");
    }
}
