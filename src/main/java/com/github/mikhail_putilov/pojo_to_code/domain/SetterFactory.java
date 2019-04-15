package com.github.mikhail_putilov.pojo_to_code.domain;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDate;

@Component
public class SetterFactory {
    public Setter createSetter(Method getter, Object pojo) {
        if (getter.getReturnType().isPrimitive()) {
            return new PrimitiveSetter(getter, pojo);
        } else if (getter.getReturnType().equals(String.class)) {
            return new StringSetter(getter, pojo);
        } else if (getter.getReturnType().equals(LocalDate.class)){
            return new LocalDateSetter(getter, pojo);
        } else if (getter.getReturnType().isArray()){
            return new ArraySetter(getter, pojo);
        } else {
            throw new IllegalStateException();
        }
    }
}
