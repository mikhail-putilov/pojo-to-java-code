package com.github.mikhail_putilov.pojo_to_code.domain;

import com.github.mikhail_putilov.pojo_to_code.domain.literals.JavaLiterals;
import com.github.mikhail_putilov.pojo_to_code.domain.literals.ObjectToLiteral;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

public class SetterImpl implements Setter {
    private static final JavaLiterals javaLiterals = new JavaLiterals();

    @Getter
    private final String propertyName;
    @Getter
    private final String propertyValue;

    @SneakyThrows
    public SetterImpl(Method getter, Object pojo) {
        propertyName = getter.getName().replaceFirst("^get", "");
        Object prop = getter.invoke(pojo);
        ObjectToLiteral literalMapperUnsafe = javaLiterals.getLiteralMapperUnsafe(prop.getClass());
        if (literalMapperUnsafe != null) {
            propertyValue = literalMapperUnsafe.objToLiteral(prop);
        } else {
            propertyValue = "create" + propertyName + "()";
        }
    }
}
