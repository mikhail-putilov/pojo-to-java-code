package com.github.mikhail_putilov.pojo_to_code.domain;


import com.github.mikhail_putilov.pojo_to_code.domain.create_function.CodeCreationNameResolver;
import com.github.mikhail_putilov.pojo_to_code.domain.view.FactoryClassView;
import com.samskivert.mustache.Template;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Serializer {
    private final Template bootstrap;

    @SneakyThrows(Exception.class)
    public String writePojoToCode(Object pojo) {
        NameResolver nameResolver = new NameResolver();
        CodeCreationNameResolver codeCreationNameResolver = CodeCreationNameResolver.createDefault(nameResolver);
        FactoryClassView factoryClassView = new SerializationContext(pojo, codeCreationNameResolver, nameResolver).generateFactoryClass();
        return bootstrap.execute(factoryClassView);
    }
}
