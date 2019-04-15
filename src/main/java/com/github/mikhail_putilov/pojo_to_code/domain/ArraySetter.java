package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ArraySetter extends AbstractSetter {
    @Getter
    private final String propertyValue;

    @SneakyThrows({InvocationTargetException.class, IllegalAccessException.class})
    public ArraySetter(Method getter, Object pojo) {
        super(getter);
        Object array = getter.invoke(pojo);
        if (array instanceof int[]) {
            propertyValue = "new int[]{" + replaceBrackets(Arrays.toString((int[]) array)) + "}";
        } else  if (array instanceof long[]) {
            propertyValue = "new long[]{" + replaceBrackets(Arrays.toString((long[]) array)) + "}";
        } else if (array instanceof short[]) {
            propertyValue = "new short[]{" + replaceBrackets(Arrays.toString((short[]) array)) + "}";
        } else if (array instanceof boolean[]) {
            propertyValue = "new boolean[]{" + replaceBrackets(Arrays.toString((boolean[]) array)) + "}";
        } else if (array instanceof byte[]) {
            propertyValue = "new byte[]{" + replaceBrackets(Arrays.toString((byte[]) array)) + "}";
        } else if (array instanceof char[]) {
            propertyValue = "new char[]{" + replaceBrackets(Arrays.toString((char[]) array)) + "}";
        } else if (array instanceof double[]) {
            propertyValue = "new double[]{" + replaceBrackets(Arrays.toString((double[]) array)) + "}";
        } else if (array instanceof float[]) {
            propertyValue = "new float[]{" + replaceBrackets(Arrays.toString((float[]) array)) + "}";
        } else {
            throw new IllegalStateException();
        }
    }

    private CharSequence replaceBrackets(String str) {
        return str.subSequence(1, str.length()-1);
    }
}
