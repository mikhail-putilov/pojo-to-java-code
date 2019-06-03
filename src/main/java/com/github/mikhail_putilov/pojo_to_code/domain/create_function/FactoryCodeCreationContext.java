package com.github.mikhail_putilov.pojo_to_code.domain.create_function;

import com.github.mikhail_putilov.pojo_to_code.domain.NameResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A collection of predefined {@link TypeToJavaCreateCodeFunction} functions
 */
@RequiredArgsConstructor
public class FactoryCodeCreationContext {
    private final NameResolver nameResolver;
    private final Map<Class<?>, Object> builtinTypes = new HashMap<>();

    public static FactoryCodeCreationContext createDefault(NameResolver nameResolver) {
        var local = new FactoryCodeCreationContext(nameResolver);
        local.put(Long.class, String::valueOf);
        local.put(long.class, String::valueOf);
        local.put(long[].class, arr -> "new long[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        local.put(Integer.class, String::valueOf);
        local.put(int.class, String::valueOf);
        local.put(int[].class, arr -> "new int[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        local.put(Short.class, String::valueOf);
        local.put(short.class, String::valueOf);
        local.put(short[].class, arr -> "new short[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        local.put(Byte.class, String::valueOf);
        local.put(byte.class, String::valueOf);
        local.put(byte[].class, arr -> "new byte[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        local.put(Double.class, String::valueOf);
        local.put(double.class, String::valueOf);
        local.put(double[].class, arr -> "new double[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        local.put(Float.class, String::valueOf);
        local.put(float.class, String::valueOf);
        local.put(float[].class, arr -> "new float[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        local.put(Character.class, String::valueOf);
        local.put(char.class, String::valueOf);
        local.put(char[].class, arr -> "new char[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        local.put(String.class, str -> "\"" + StringEscapeUtils.escapeJava(str) + "\"");
        local.put(String[].class, arr -> "new String[]{" + replaceBrackets(Arrays.toString(arr)) + "}");

        local.put(LocalDate.class, date -> "LocalDate.parse(\"" + date + "\")");
        return local;
    }

    private <T> void put(Class<T> clazz, TypeToJavaCreateCodeFunction<T> mapper) {
        builtinTypes.put(Objects.requireNonNull(clazz), mapper);
    }

    public String get(Object object) {
        Object mapper = builtinTypes.get(object.getClass());
        if (mapper != null) {
            //noinspection unchecked
            return ((TypeToJavaCreateCodeFunction) mapper).typeToJavaCreateCode(object);
        }
        return nameResolver.resolveFactoryMethodName(object);
    }


    public boolean isBuiltinType(Class<?> clazz) {
        return builtinTypes.containsKey(clazz);
    }

    private static String replaceBrackets(String arrayStr) {
        return arrayStr.substring(1, arrayStr.length() - 1);
    }
}
