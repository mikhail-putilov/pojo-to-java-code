package com.github.mikhail_putilov.pojo_to_code.domain.view;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class FactoryClassView {
    private String packageName;
    private Collection<String> imports = new ArrayList<>();
    private String className;
    private List<FactoryMethodView> factories = new ArrayList<>();
}
