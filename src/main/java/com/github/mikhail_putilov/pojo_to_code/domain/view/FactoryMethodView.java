package com.github.mikhail_putilov.pojo_to_code.domain.view;

import lombok.Data;

import java.util.List;

@Data
public class FactoryMethodView {
    private String returnType;
    private String localVariableName;
    private List<SetterView> setters;
    private String factoryMethodName;
}
