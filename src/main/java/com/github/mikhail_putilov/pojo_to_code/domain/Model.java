package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.Getter;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

public class Model {
    private final Class<?> clazz;
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
        setSetters(pojo);
    }

    private void setSetters(Object pojo) {
        ReflectionUtils.doWithMethods(clazz,
                method1 -> setters.add(new Setter(method1, pojo)),
                method -> method.getName().startsWith("get") && !method.getName().equals("getClass"));
    }
}
