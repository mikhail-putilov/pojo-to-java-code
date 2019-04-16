package com.github.mikhail_putilov.pojo_to_code.domain.literals;

@FunctionalInterface
public interface ObjectToLiteral<T> {
    String objToLiteral(T o);
}
