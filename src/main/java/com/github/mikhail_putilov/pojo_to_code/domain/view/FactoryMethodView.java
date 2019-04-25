package com.github.mikhail_putilov.pojo_to_code.domain.view;

import lombok.Getter;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

public class FactoryMethodView {
    private final Class<?> clazz;
    @Getter(lazy = true)
    private final String returnType = clazz.getSimpleName();
    @Getter(lazy = true)
    private final String pojoName = capitalize(getReturnType());
    @Getter(lazy = true)
    private final String localVariableName = uncapitalize(getReturnType());
    @Getter
    private final List<? extends SetterView> setterViews;

    public FactoryMethodView(Object pojo, List<? extends SetterView> setterViews) {
        clazz = pojo.getClass();
        this.setterViews = setterViews;
    }
}
