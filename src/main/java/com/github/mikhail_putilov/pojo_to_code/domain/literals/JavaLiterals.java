package com.github.mikhail_putilov.pojo_to_code.domain.literals;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class JavaLiterals {
    private final Map<Class<?>, ObjectToLiteral<?>> clazzToMapper = new HashMap<>();

    public JavaLiterals() {
        put(String.class, str -> "\"" + StringEscapeUtils.escapeJava(str) + "\"");
        put(Long.class, String::valueOf);
        put(Integer.class, String::valueOf);
        put(Short.class, String::valueOf);
        put(Byte.class, String::valueOf);
        put(Double.class, String::valueOf);
        put(Float.class, String::valueOf);
        put(Character.class, String::valueOf);
        put(int[].class, arr -> "new int[]{" + replaceBrackets(Arrays.toString(arr)) + "}");
        put(long[].class, arr -> "new long[]{" + replaceBrackets(Arrays.toString(arr)) + "}");
        put(short[].class, arr -> "new short[]{" + replaceBrackets(Arrays.toString(arr)) + "}");
        put(byte[].class, arr -> "new byte[]{" + replaceBrackets(Arrays.toString(arr)) + "}");
        put(double[].class, arr -> "new double[]{" + replaceBrackets(Arrays.toString(arr)) + "}");
        put(float[].class, arr -> "new float[]{" + replaceBrackets(Arrays.toString(arr)) + "}");
        put(char[].class, arr -> "new char[]{" + replaceBrackets(Arrays.toString(arr)) + "}");
        put(String[].class, arr -> "new String[]{" + replaceBrackets(Arrays.toString(arr)) + "}");
        put(LocalDate.class, date -> "LocalDate.parse(\"" + date + "\")");
    }

    private <T> void put(Class<T> clazz, ObjectToLiteral<T> mapper) {
        clazzToMapper.put(clazz, mapper);
    }

    public ObjectToLiteral getLiteralMapperUnsafe(Class clazz) {
        return clazzToMapper.get(clazz);
    }

    private String replaceBrackets(String arrayStr) {
        return arrayStr.substring(1, arrayStr.length()-1);
    }
}
