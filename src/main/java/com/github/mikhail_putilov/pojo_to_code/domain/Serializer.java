package com.github.mikhail_putilov.pojo_to_code.domain;


import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Serializer {
    private final Mustache.Compiler mustache;
    private final Mustache.TemplateLoader templateLoader;

    @SneakyThrows(Exception.class)
    public String writePojoToCode(Object pojo) {
        final Template bootstrapTemplate = mustache.compile(templateLoader.getTemplate("bootstrap"));
        return bootstrapTemplate.execute(createModel(pojo));
    }

    @Lookup
    Model createModel(Object pojo) {
        return null;
    }
}
