package com.common.utils.assemble;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class Assemble {
    public static final String MAP_TEMPLATE = "#this.?[%s != null].![%s]";
    public static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
    public static final Expression TO_MAP_EXPRESSION = EXPRESSION_PARSER.parseExpression("id");

    @SuppressWarnings("unchecked")
    public static <T> List<T> map(Collection<?> data, String property) {
        final Expression expression = EXPRESSION_PARSER.parseExpression(String.format(MAP_TEMPLATE, property, property));
        return (List<T>) expression.getValue(data);
    }

    public static List<Long> map(Collection<?> data) {
        return map(data, "id");
    }

    private static <K, V> Map<K, V> toMap(Collection<V> data, Function<V, K> keyFunction, Supplier<Map<K, V>> mapSupplier) {
        return data.stream().collect(Collectors.toMap(keyFunction, UnaryOperator.identity(), (v, v2) -> v2, mapSupplier));
    }

    public static <K, V> Map<K, V> toMap(Collection<V> data, Function<V, K> keyFunction) {
        return toMap(data, keyFunction, HashMap::new);
    }

    public static <V> Map<Long, V> toMap(Collection<V> data) {
        return toMap(data, v -> TO_MAP_EXPRESSION.getValue(v, Long.class));
    }

    public static <K, V> Map<K, V> toLinkedMap(Collection<V> data, Function<V, K> keyFunction) {
        return toMap(data, keyFunction, LinkedHashMap::new);
    }

    public static <V> Map<Long, V> toLinkedMap(Collection<V> data) {
        return toLinkedMap(data, v -> TO_MAP_EXPRESSION.getValue(v, Long.class));
    }

    public static <K, V> Map<K, List<V>> toGroup(Collection<V> data, Function<V, K> keyFunction) {
        return data.stream().collect(Collectors.groupingBy(keyFunction));
    }
}
