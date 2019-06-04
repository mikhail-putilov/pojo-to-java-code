package com.github.mikhail_putilov.pojo_to_code.domain.view;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class FactoryMethodView {
    private String returnType;
    private String localVariableName;
    private Collection<SetterView> setters;
    private String factoryMethodName;
}
