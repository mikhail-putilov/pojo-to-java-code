package com.github.mikhail_putilov.pojo_to_code.domain.create_function;

/**
 * Map a java object to java code that spawn the given java object
 *
 * @param <T> type of given java object
 */
@FunctionalInterface
public interface TypeToJavaCreateCodeFunction<T> {
    String typeToJavaCreateCode(T o);
}
