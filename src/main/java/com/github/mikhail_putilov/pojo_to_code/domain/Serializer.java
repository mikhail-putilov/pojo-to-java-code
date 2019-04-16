package com.github.mikhail_putilov.pojo_to_code.domain;


import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Serializer {
    private final Mustache.Compiler mustache;
    private final Mustache.TemplateLoader templateLoader;

    @SneakyThrows(Exception.class)
    public String writePojoToCode(Object pojo) {
        final Template bootstrapTemplate = mustache.compile(templateLoader.getTemplate("bootstrap"));
        List<FactoryMethod> factories = new SerializationContext(pojo).getFactoryMethodsForPojo();
        return bootstrapTemplate.execute(new Dto(factories));
    }

    @Data
    static class Dto {
        final List<FactoryMethod> factories;
    }
}
