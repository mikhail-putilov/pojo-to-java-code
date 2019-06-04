package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.SneakyThrows;

import java.lang.reflect.Method;

final class ExceptionUtils {
    private ExceptionUtils() {
        // nothing
    }

    @SneakyThrows
    static Object invokeGetterOnPojo(Object pojo, Method getter) {
        return getter.invoke(pojo);
    }
}
