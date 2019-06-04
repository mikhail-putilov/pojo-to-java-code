package com.github.mikhail_putilov.pojo_to_code.domain.view;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Collection;

@Data
@Builder
public class FactoryClassView {
    private String packageName;
    private Collection<String> imports;
    private String className;
    @Singular
    private Collection<FactoryMethodView> factories;
}
