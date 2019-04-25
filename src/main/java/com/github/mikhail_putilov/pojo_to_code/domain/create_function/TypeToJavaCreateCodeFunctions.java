package com.github.mikhail_putilov.pojo_to_code.domain.create_function;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A collection of predefined {@link TypeToJavaCreateCodeFunction} functions
 */
@Component
public class TypeToJavaCreateCodeFunctions {
    /**
     * Mappers that are not dependent on identity of created object, like numbers, strings, dates. Not pojos!
     */
    private final Map<Class<?>, Object> notIdentityMappers = new HashMap<>();
    /**
     * For each class type we need to track number of instances so that we can generate unique method names for them
     */
    private final Map<Class<?>, Integer> identityCounters = new HashMap<>();

    public TypeToJavaCreateCodeFunctions() {
        put(Long.class, String::valueOf);
        put(long.class, String::valueOf);
        put(long[].class, arr -> "new long[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        put(Integer.class, String::valueOf);
        put(int.class, String::valueOf);
        put(int[].class, arr -> "new int[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        put(Short.class, String::valueOf);
        put(short.class, String::valueOf);
        put(short[].class, arr -> "new short[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        put(Byte.class, String::valueOf);
        put(byte.class, String::valueOf);
        put(byte[].class, arr -> "new byte[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        put(Double.class, String::valueOf);
        put(double.class, String::valueOf);
        put(double[].class, arr -> "new double[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        put(Float.class, String::valueOf);
        put(float.class, String::valueOf);
        put(float[].class, arr -> "new float[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        put(Character.class, String::valueOf);
        put(char.class, String::valueOf);
        put(char[].class, arr -> "new char[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        put(String.class, str -> "\"" + StringEscapeUtils.escapeJava(str) + "\"");
        put(String[].class, arr -> "new String[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        put(LocalDate.class, date -> "LocalDate.parse(\"" + date + "\")");
    }

    private <T> void put(Class<T> clazz, TypeToJavaCreateCodeFunction<T> mapper) {
        notIdentityMappers.put(Objects.requireNonNull(clazz), mapper);
    }

    public TypeToJavaCreateCodeFunction get(Method getter) {
        Class<?> returnType = getter.getReturnType();
        if (returnType.isEnum()) {
            return enumObject -> returnType.getSimpleName() + "." + enumObject.toString();
        }
        if (notIdentityMappers.containsKey(returnType)) {
            return (TypeToJavaCreateCodeFunction) notIdentityMappers.get(returnType);
        }
        if (identityCounters.get(returnType).equals(0)) {
            return pojoObject -> "create" + getter.getReturnType().getSimpleName() + "()";
        }
        return pojoObject -> {
            Integer discriminator = identityCounters.compute(returnType, (aClass, counter) -> counter - 1);
            return "create" + getter.getName().replaceFirst("get", "") + discriminator + "()";
        };
    }

    public void learnClass(Class<?> clazz) {
        if (!notIdentityMappers.containsKey(clazz)) {
            identityCounters.putIfAbsent(clazz, -1);
            identityCounters.compute(clazz, (aClass, counter) -> counter + 1);
        }
    }

    public boolean doesNotNeedIdentityMapper(Method method) {
        return notIdentityMappers.containsKey(method.getReturnType());
    }

    private static String replaceBrackets(String arrayStr) {
        return arrayStr.substring(1, arrayStr.length() - 1);
    }
}
