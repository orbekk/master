package com.orbekk.same;

import org.codehaus.jackson.type.TypeReference;

public class Types {
    public final static TypeReference<String> STRING =
            new TypeReference<String>() {};
    public final static TypeReference<Integer> INTEGER =
            new TypeReference<Integer>() {};
    public static <T> TypeReference<T> fromType(Class<T> clazz) {
        return new TypeReference<T>() {};
    }
}
