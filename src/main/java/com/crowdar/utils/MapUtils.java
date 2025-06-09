package com.crowdar.utils;

import java.util.*;

/**
 * Utility class for safe casting of dynamic or untyped objects (e.g., from JSON-parsed structures).
 */
public class MapUtils {

    /**
     * Safely casts an object to a List of elements of a given type.
     *
     * @param obj   the object to cast (expected to be a List)
     * @param clazz the expected class of the list elements
     * @param <T>   the target type
     * @return an Optional containing the cast list, or empty if the cast is not valid
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<List<T>> getListOf(Object obj, Class<T> clazz) {
        if (!(obj instanceof List<?> rawList)) return Optional.empty();

        for (Object item : rawList) {
            if (item != null && !clazz.isInstance(item)) return Optional.empty();
        }

        return Optional.of((List<T>) rawList);
    }

    /**
     * Safely casts an object to a Map with specific key and value types.
     *
     * @param obj       the object to cast (expected to be a Map)
     * @param keyClass   the expected class of the keys
     * @param valueClass the expected class of the values
     * @param <K>        key type
     * @param <V>        value type
     * @return an Optional containing the cast map, or empty if the cast is not valid
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Optional<Map<K, V>> getMap(Object obj, Class<K> keyClass, Class<V> valueClass) {
        if (!(obj instanceof Map<?, ?> rawMap)) return Optional.empty();

        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (!keyClass.isInstance(entry.getKey()) || !valueClass.isInstance(entry.getValue())) {
                return Optional.empty();
            }
        }

        return Optional.of((Map<K, V>) rawMap);
    }
}

