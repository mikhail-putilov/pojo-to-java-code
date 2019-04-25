package com.github.mikhail_putilov.pojo_to_code.domain;


import com.github.mikhail_putilov.pojo_to_code.domain.view.FactoryMethodView;
import com.samskivert.mustache.Template;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Serializer {
    private final Template bootstrap;
    private final ObjectProvider<SerializationContext> serializationContext;

    @SneakyThrows(Exception.class)
    public String writePojoToCode(Object pojo) {
        List<FactoryMethodView> factories = serializationContext.getObject(pojo).getFactoryMethodsForPojo();
        return bootstrap.execute(Map.of("factories", factories));
    }
}
