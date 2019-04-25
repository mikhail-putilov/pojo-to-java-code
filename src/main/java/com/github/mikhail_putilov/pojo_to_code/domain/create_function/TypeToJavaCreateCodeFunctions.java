package com.github.mikhail_putilov.pojo_to_code.domain.create_function;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

/**
 * A collection of predefined {@link TypeToJavaCreateCodeFunction} functions
 */
@Component
public class TypeToJavaCreateCodeFunctions {
    private final Map<Class<?>, Object> clazzToMapper = new HashMap<>();

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
        clazzToMapper.put(Objects.requireNonNull(clazz), mapper);
    }

    private <T> void putIfAbsent(Class<T> clazz, TypeToJavaCreateCodeFunction<T> mapper) {
        clazzToMapper.putIfAbsent(Objects.requireNonNull(clazz), mapper);
    }

    public Optional<TypeToJavaCreateCodeFunction> get(Class clazz) {
        if (clazz.isEnum()) {
            //noinspection unchecked
            putIfAbsent(clazz, enumFunctionOf(clazz));
        }
        return Optional.ofNullable((TypeToJavaCreateCodeFunction) clazzToMapper.get(clazz));
    }

    private TypeToJavaCreateCodeFunction enumFunctionOf(Class enumType) {
        return o -> enumType.getSimpleName() + "." + o.toString();
    }

    private static String replaceBrackets(String arrayStr) {
        return arrayStr.substring(1, arrayStr.length() - 1);
    }
}
