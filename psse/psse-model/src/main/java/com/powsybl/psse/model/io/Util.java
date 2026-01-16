/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import com.powsybl.psse.model.PsseException;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class Util {

    private static final BinaryOperator<String> DEFAULT_SUFFIX_ADDER = (header, suffix) -> header + suffix;

    private Util() {
    }

    public static <T, V> PsseFieldDefinition<T, V> createNewField(String header, Class<V> classType,
                                                                  Function<T, V> getter, BiConsumer<T, V> setter) {
        return new PsseFieldDefinition<>(header, classType, getter, setter, null, false, null, DEFAULT_SUFFIX_ADDER);
    }

    public static <T, V> PsseFieldDefinition<T, V> createNewField(String header, Class<V> classType,
                                                                  Function<V, String> formatter, Function<T, V> getter, BiConsumer<T, V> setter) {
        return new PsseFieldDefinition<>(header, classType, getter, setter, null, false, formatter, DEFAULT_SUFFIX_ADDER);
    }

    public static <T, V> PsseFieldDefinition<T, V> createNewField(String header, Class<V> classType,
                                                                  Function<T, V> getter, BiConsumer<T, V> setter, V defaultValue) {
        return new PsseFieldDefinition<>(header, classType, getter, setter, defaultValue, true, null, DEFAULT_SUFFIX_ADDER);
    }

    public static <T, V> PsseFieldDefinition<T, V> createNewField(String header, Class<V> classType,
                                                                  Function<T, V> getter, BiConsumer<T, V> setter, V defaultValue,
                                                                  BinaryOperator<String> suffixAdder) {
        return new PsseFieldDefinition<>(header, classType, getter, setter, defaultValue, true, null, suffixAdder);
    }

    public static <T, V> PsseFieldDefinition<T, V> createNewField(String header, Class<V> classType,
                                                                  Function<V, String> formatter, Function<T, V> getter, BiConsumer<T, V> setter,
                                                                  V defaultValue) {
        return new PsseFieldDefinition<>(header, classType, getter, setter, defaultValue, true, formatter, DEFAULT_SUFFIX_ADDER);
    }

    public static <T, V> PsseFieldDefinition<T, V> createNewField(String header, Class<V> classType,
                                                                  Function<V, String> formatter, Function<T, V> getter,
                                                                  BiConsumer<T, V> setter, V defaultValue,
                                                                  BinaryOperator<String> suffixAdder) {
        return new PsseFieldDefinition<>(header, classType, getter, setter, defaultValue, true, formatter, suffixAdder);
    }

    public static <T, V> void addField(Map<String, PsseFieldDefinition<T, ?>> fields, PsseFieldDefinition<T, V> fieldDefinition) {
        fields.put(fieldDefinition.fieldName(), fieldDefinition);
    }

    public static <T> String[] stringHeaders(Map<String, PsseFieldDefinition<T, ?>> fields) {
        return fields.entrySet()
                .stream()
                .filter(header -> header.getValue().classType() == String.class)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }

    public static <T> Double defaultDoubleFor(String header, Map<String, PsseFieldDefinition<T, ?>> fields) {
        return defaultValueFor(header, fields, Double.class);
    }

    public static <T> Float defaultFloatFor(String header, Map<String, PsseFieldDefinition<T, ?>> fields) {
        return defaultValueFor(header, fields, Float.class);
    }

    public static <T> Integer defaultIntegerFor(String header, Map<String, PsseFieldDefinition<T, ?>> fields) {
        return defaultValueFor(header, fields, Integer.class);
    }

    public static <T> String defaultStringFor(String header, Map<String, PsseFieldDefinition<T, ?>> fields) {
        return defaultValueFor(header, fields, String.class);
    }

    private static <T, V> V defaultValueFor(String header, Map<String, PsseFieldDefinition<T, ?>> fields, Class<V> expectedType) {
        PsseFieldDefinition<T, ?> field = fields.get(header);
        if (field == null) {
            throw new PsseException("Header not defined in fields map: " + header);
        }

        Object defaultValue = field.defaultValue();
        if (defaultValue == null) {
            throw new PsseException("Default value is null for header: " + header);
        }

        if (!expectedType.isInstance(defaultValue)) {
            throw new PsseException(
                    "Invalid default value type for field '" + header
                            + "'. Expected: " + expectedType.getSimpleName()
                            + ", but was: " + defaultValue.getClass().getSimpleName());
        }

        return expectedType.cast(defaultValue);
    }

    public static String[] retainAll(String[] strings, String[] stringsToKeep) {
        if (strings == null || stringsToKeep == null) {
            return new String[0];
        }
        Set<String> setStringsToKeep = new HashSet<>(Arrays.asList(stringsToKeep));
        List<String> kept = new ArrayList<>();
        for (String s : strings) {
            if (setStringsToKeep.contains(s)) {
                kept.add(s);
            }
        }
        return kept.toArray(new String[0]);
    }

    public static <T> T fromRecord(List<String> rec, String[] headers, Map<String, PsseFieldDefinition<T, ?>> fields,
                                   Supplier<T> instanceSupplier, String headerSuffix) {
        try {
            T obj = instanceSupplier.get();

            Map<String, PsseFieldDefinition<T, ?>> fieldsWithSuffix = fields.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getValue().suffixAdder().apply(entry.getKey(), headerSuffix),
                    Map.Entry::getValue));

            for (String header : headers) {
                PsseFieldDefinition<T, ?> fieldDefinition = fieldsWithSuffix.get(header);
                if (fieldDefinition != null) {
                    Object value = parseValue(fieldDefinition.defaultValue(), fieldDefinition.hasDefaultValue(),
                        fieldDefinition.classType(), rec, headers, header);
                    applySetter(obj, fieldDefinition, value);
                }
            }

            return obj;

        } catch (Exception e) {
            throw new PsseException("Unexpected instance creation", e);
        }
    }

    public static <T> T fromRecord(List<String> rec, String[] headers, Map<String, PsseFieldDefinition<T, ?>> fields,
                                   Supplier<T> instanceSupplier) {
        return fromRecord(rec, headers, fields, instanceSupplier, "");
    }

    public static Double parseDoubleFromRecord(List<String> rec, Double defaultValue, String[] headers, String header,
                                               boolean hasDefaultValue) {
        // Parse the value from the record
        String value = manageNumericalNullValues(parseValueFromRecord(rec, headers, header));

        // Manage null values
        if (value != null) {
            return Double.parseDouble(value);
        } else {
            if (!hasDefaultValue) {
                throwIfHeaderNotFound(rec, header);
            }
            return defaultValue;
        }
    }

    public static Float parseFloatFromRecord(List<String> rec, Float defaultValue, String[] headers, String header,
                                             boolean hasDefaultValue) {
        // Parse the value from the record
        String value = manageNumericalNullValues(parseValueFromRecord(rec, headers, header));

        // Manage null values
        if (value != null) {
            return Float.parseFloat(value);
        } else {
            if (!hasDefaultValue) {
                throwIfHeaderNotFound(rec, header);
            }
            return defaultValue;
        }
    }

    public static Integer parseIntFromRecord(List<String> rec, Integer defaultValue, String[] headers, String header,
                                             boolean hasDefaultValue) {
        // Parse the value from the record
        String value = manageNumericalNullValues(parseValueFromRecord(rec, headers, header));

        // Manage null values
        if (value != null) {
            return Integer.parseInt(value);
        } else {
            if (!hasDefaultValue) {
                throwIfHeaderNotFound(rec, header);
            }
            return defaultValue;
        }
    }

    public static String parseStringFromRecord(List<String> rec, String defaultValue, String[] headers, String header,
                                               boolean hasDefaultValue) {
        // Parse the value from the record
        String value = parseValueFromRecord(rec, headers, header);

        // Manage null values
        if (value != null && (!value.isEmpty() || !hasDefaultValue)) {
            return value;
        } else {
            if (!hasDefaultValue) {
                throwIfHeaderNotFound(rec, header);
            }
            return defaultValue;
        }
    }

    public static <T> void toRecord(T obj, String[] headers, Map<String, PsseFieldDefinition<T, ?>> fields,
                                        String[] row, Set<String> unexpectedHeaders, String headerSuffix) {
        Map<String, PsseFieldDefinition<T, ?>> fieldsWithSuffix = fields.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getValue().suffixAdder().apply(entry.getKey(), headerSuffix),
                Map.Entry::getValue));

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            PsseFieldDefinition<T, ?> fieldDefinition = fieldsWithSuffix.get(header);
            if (fieldDefinition != null) {
                row[i] = formatField(fieldDefinition, obj);
                unexpectedHeaders.remove(header);
            }
        }
    }

    public static <T> void toRecord(T obj, String[] headers, Map<String, PsseFieldDefinition<T, ?>> fields,
                                    String[] row, Set<String> unexpectedHeaders) {
        toRecord(obj, headers, fields, row, unexpectedHeaders, "");
    }

    public static <T> String[] toRecord(T obj, String[] headers, Map<String, PsseFieldDefinition<T, ?>> fields,
                                        Set<String> unexpectedHeaders) {
        String[] row = new String[headers.length];
        toRecord(obj, headers, fields, row, unexpectedHeaders);
        return row;
    }

    public static <T> String[] toRecord(T obj, String[] headers, Map<String, PsseFieldDefinition<T, ?>> fields) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] rec = toRecord(obj, headers, fields, unexpectedHeaders);
        checkForUnexpectedHeader(unexpectedHeaders);
        return rec;
    }

    public static void checkForUnexpectedHeader(Set<String> unexpectedHeaders) {
        if (!unexpectedHeaders.isEmpty()) {
            throw new PsseException("Unsupported headers: " + unexpectedHeaders);
        }
    }

    private static String parseValueFromRecord(List<String> rec, String[] headers, String header) {
        // Parse the value from the record
        int index = ArrayUtils.indexOf(headers, header);
        return index >= 0 && index < rec.size() ? rec.get(ArrayUtils.indexOf(headers, header)) : null;
    }

    private static String manageNumericalNullValues(String value) {
        return value == null || value.isEmpty() || value.equalsIgnoreCase("null") ? null : value;
    }

    private static void throwIfHeaderNotFound(List<String> rec, String header) {
        throw new PsseException(String.format("Column [%s] not found in record: %s", header, rec.toString()));
    }

    public static String[] concatStringArrays(String[]... listOfArrays) {
        int size = 0;
        for (String[] a : listOfArrays) {
            size += a.length;
        }

        String[] result = new String[size];
        int pos = 0;
        for (String[] a : listOfArrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }

    public static String[] addSuffixToHeaders(String[] headers, String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            return headers;
        }
        String[] newHeaders = new String[headers.length];
        Arrays.setAll(newHeaders, i -> headers[i] + suffix);
        return newHeaders;
    }

    public static String[] addPrefixToHeaders(String[] headers, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return headers;
        }
        String[] newHeaders = new String[headers.length];
        Arrays.setAll(newHeaders, i -> prefix + headers[i]);
        return newHeaders;
    }

    @SuppressWarnings("unchecked")
    private static <T, V> void applySetter(T obj, PsseFieldDefinition<T, V> def, Object value) {
        def.setter().accept(obj, (V) value);
    }

    private static Object parseValue(Object defaultValue, boolean hasDefaultValue, Class<?> type, List<String> rec,
                                     String[] headers, String header) {
        return switch (type.getSimpleName()) {
            case "Double" -> parseDoubleFromRecord(rec, (Double) defaultValue, headers, header, hasDefaultValue);
            case "Float" -> parseFloatFromRecord(rec, (Float) defaultValue, headers, header, hasDefaultValue);
            case "Integer" -> parseIntFromRecord(rec, (Integer) defaultValue, headers, header, hasDefaultValue);
            case "String" -> parseStringFromRecord(rec, (String) defaultValue, headers, header, hasDefaultValue);
            default -> throw new PsseException("Unexpected Type: " + type);
        };
    }

    private static <T, V> String formatField(PsseFieldDefinition<T, V> fieldDefinition, T obj) {
        V value = fieldDefinition.getter().apply(obj);
        if (fieldDefinition.formatter() != null) {
            return fieldDefinition.formatter().apply(value);
        }
        return String.valueOf(value);
    }
}
