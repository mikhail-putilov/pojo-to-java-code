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
public class CodeCreationNameResolver {
    private final NameResolver nameResolver;
    private final Map<Class<?>, Object> builtinTypes = new HashMap<>();

    public static CodeCreationNameResolver createDefault(NameResolver nameResolver) {
        var local = new CodeCreationNameResolver(nameResolver);
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

    public boolean isJavaLang(Class<?> clazz) {
        return clazz.isArray() ? clazz.getComponentType().getPackageName().startsWith("java.lang") : clazz.getPackageName().startsWith("java.lang");
    }

    private <T> void put(Class<T> clazz, TypeToJavaCreateCodeFunction<T> mapper) {
        builtinTypes.put(Objects.requireNonNull(clazz), mapper);
    }

    private TypeToJavaCreateCodeFunction getRaw(Class<?> clazz) {
        return (TypeToJavaCreateCodeFunction) builtinTypes.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public String get(Object object) {
        TypeToJavaCreateCodeFunction mapper = getRaw(object.getClass());
        if (mapper != null) {
            return mapper.typeToJavaCreateCode(object);
        } else if (object.getClass().isEnum()) {
            var enums = object.getClass().getEnumConstants();
            int i = Arrays.asList(enums).indexOf(object);
            return nameResolver.resolveReturnType(object.getClass()) + "." + enums[i];
        }
        return nameResolver.resolveFactoryMethodName(object) + "()";
    }

    public boolean isBuiltinType(Class<?> clazz) {
        return builtinTypes.containsKey(clazz);
    }

    private static String replaceBrackets(String arrayStr) {
        return arrayStr.substring(1, arrayStr.length() - 1);
    }
}
