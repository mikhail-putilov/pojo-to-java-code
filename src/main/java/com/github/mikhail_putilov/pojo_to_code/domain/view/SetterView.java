package com.github.mikhail_putilov.pojo_to_code.domain.view;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class SetterView {
    private String propertyName;
    private String propertyValue;

    public void setPropertyNameFromGetter(Method getter) {
        String name = getter.getName();
        if (!name.startsWith("get")) {
            throw new RuntimeException("not a getter");
        }
        setPropertyName(name.substring(3));
    }
}
