package com.github.mikhail_putilov.pojo_to_code.domain;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mikhail_putilov.pojo_to_code.domain.create_function.FactoryCodeCreationContext;
import com.github.mikhail_putilov.pojo_to_code.domain.view.FactoryClassView;
import com.samskivert.mustache.Template;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Serializer {
    private final Template bootstrap;
    private final ObjectMapper objectMapper;

    @SneakyThrows(Exception.class)
    public String writePojoToCode(Object pojo) {
        NameResolver nameResolver = new NameResolver();
        FactoryClassView factoryClassView = new SerializationContext(pojo, objectMapper, FactoryCodeCreationContext.createDefault(nameResolver), nameResolver).generateFactoryClass();
        return bootstrap.execute(factoryClassView);
    }
}
