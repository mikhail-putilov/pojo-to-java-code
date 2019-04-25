package com.github.mikhail_putilov.pojo_to_code.domain.view;

import com.github.mikhail_putilov.pojo_to_code.domain.create_function.TypeToJavaCreateCodeFunction;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Getter
public class SetterViewImpl implements SetterView {
    private final String propertyName;
    private final String propertyValue;

    @SneakyThrows({IllegalAccessException.class, InvocationTargetException.class})
    public SetterViewImpl(Method getter, Object pojo, TypeToJavaCreateCodeFunction typeToJavaCreateCodeFunction) {
        propertyName = getter.getName().replaceFirst("^get", "");
        Object prop = invokeAccessibly(getter, pojo);
        //noinspection unchecked
        propertyValue = typeToJavaCreateCodeFunction.typeToJavaCreateCode(prop);
    }

    private Object invokeAccessibly(Method getter, Object pojo) throws InvocationTargetException, IllegalAccessException {
        boolean changeAccessBack = false;
        if (!getter.canAccess(pojo)) {
            changeAccessBack = getter.trySetAccessible();
        }
        Object prop = getter.invoke(pojo);
        if (changeAccessBack) {
            getter.setAccessible(false);
        }
        return prop;
    }
}
