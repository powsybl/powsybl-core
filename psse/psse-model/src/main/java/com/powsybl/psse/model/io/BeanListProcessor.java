/**
 * Copyright (c) 2025, University of West Bohemia (https://www.zcu.cz)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import com.powsybl.psse.model.PsseException;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Petr Janecek {@literal <pjanecek at ntis.zcu.cz>}
 */
public class BeanListProcessor<T> {
    private static final String NULL_VALUE = "null";

    private final Class<T> clazz;
    private final String[] headers;
    private List<T> beans = new ArrayList<>();
    private Field[] fields;
    private Map<Field, Map<String, Field>> nestedFieldMap = new HashMap<>();
    private List<Field> defaultedFields = new ArrayList<>();

    public BeanListProcessor(Class<T> clazz, String[] headers) {
        if (clazz == null || headers == null || headers.length == 0) {
            throw new IllegalArgumentException("Class type and headers must be provided");
        }
        this.clazz = clazz;
        this.headers = headers;
        initFields(headers);
    }

    private void initFields(String[] headers) {
        fields = new Field[headers.length];
        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> processFieldAnnotations(headers, field));
    }

    private void processFieldAnnotations(String[] headers, Field field) {
        Parsed parsedAnnotation = field.getAnnotation(Parsed.class);
        Nested nestedAnnotation = field.getAnnotation(Nested.class);

        if (parsedAnnotation != null) {
            initializeParsedField(headers, field, parsedAnnotation);
        } else if (nestedAnnotation != null) {
            initializeNestedMapping(field, nestedAnnotation);
        }
    }

    private void initializeParsedField(String[] headers, Field field, Parsed parsedAnnotation) {
        String[] fieldNames = parsedAnnotation.field().length > 0 ? parsedAnnotation.field() : new String[]{field.getName()};
        int columnIndex = findHeaderIndex(headers, fieldNames);

        if (columnIndex != -1) {
            makeAccessible(field);
            fields[columnIndex] = field;
        } else if (!NULL_VALUE.equals(parsedAnnotation.defaultNullRead())) {
            makeAccessible(field);
            defaultedFields.add(field);
        }
    }

    private void initializeNestedMapping(Field nestedField, Nested nestedAnnotation) {
        Map<String, Field> headerMappings = new HashMap<>();
        String suffix = nestedAnnotation.args().length > 0 ? nestedAnnotation.args()[0] : "";

        for (Field subField : nestedField.getType().getDeclaredFields()) {
            Parsed parsed = subField.getAnnotation(Parsed.class);
            if (parsed != null) {
                String[] mappedHeaders = parsed.field().length > 0 ? parsed.field() : new String[]{subField.getName()};
                for (String header : mappedHeaders) {
                    String finalHeader = header.startsWith("wdg") ? "wdg" + suffix + header.substring(3) : header + suffix;
                    headerMappings.put(finalHeader, subField);
                }
            }
        }
        nestedFieldMap.put(nestedField, headerMappings);
    }

    private static int findHeaderIndex(String[] headers, String[] fieldNames) {
        return Arrays.stream(fieldNames)
                .mapToInt(fieldName -> Arrays.asList(headers).indexOf(fieldName))
                .filter(index -> index != -1)
                .findFirst()
                .orElse(-1);
    }

    private void makeAccessible(Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    public List<T> getBeans() {
        return beans;
    }

    public void processLine(String[] row) {
        if (row == null || row.length == 0) {
            return;
        }

        try {
            int maxCount = Math.min(row.length, headers.length);
            T instance = createNewInstance();
            applyDefaultedFields(instance);
            processDataFields(row, maxCount, instance);
            handleRemainingFields(maxCount, instance);
            beans.add(instance);
        } catch (Exception e) {
            throw new PsseException("Parsing error: " + e.getMessage(), e);
        }
    }

    private T createNewInstance() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return clazz.getDeclaredConstructor().newInstance();
    }

    private void applyDefaultedFields(T instance) throws IllegalAccessException {
        for (Field field : defaultedFields) {
            setFieldValue(instance, field, null);
        }
    }

    private void processDataFields(String[] row, int maxCount, T instance) throws Exception {
        for (int i = 0; i < maxCount; i++) {
            if (fields[i] != null) {
                setFieldValue(instance, fields[i], row[i]);
            } else {
                handleNestedFieldMapping(instance, row[i], headers[i]);
            }
        }
    }

    private void handleNestedFieldMapping(Object instance, String value, String header) throws Exception {
        for (Map.Entry<Field, Map<String, Field>> entry : nestedFieldMap.entrySet()) {
            Field parentField = entry.getKey();
            Map<String, Field> subFieldMapping = entry.getValue();

            if (subFieldMapping.containsKey(header)) {
                handleNestedField(instance, value, parentField, subFieldMapping.get(header));
                return;
            }
        }
    }

    private void handleRemainingFields(int start, T instance) throws IllegalAccessException {
        for (int i = start; i < fields.length; i++) {
            if (fields[i] != null) {
                setFieldValue(instance, fields[i], null);
            }
        }
    }

    private void handleNestedField(Object parentInstance, String value, Field parentField, Field childField) throws Exception {
        Object nestedInstance = getOrCreateNestedInstance(parentInstance, parentField);
        makeAccessible(childField);
        setFieldValue(nestedInstance, childField, value);
    }

    private Object getOrCreateNestedInstance(Object parentInstance, Field parentField) throws Exception {
        makeAccessible(parentField);
        Object nestedInstance = parentField.get(parentInstance);

        if (nestedInstance == null) {
            nestedInstance = parentField.getType().getDeclaredConstructor().newInstance();
            makeAccessible(parentField);
            parentField.set(parentInstance, nestedInstance);
        }
        return nestedInstance;
    }

    private static void setFieldValue(Object instance, Field field, String value) throws IllegalAccessException {
        String finalValue = (value == null || value.isEmpty() || NULL_VALUE.equals(value))
                ? getDefaultValue(field)
                : value;

        if (finalValue != null) {
            field.set(instance, parseValue(field.getType(), finalValue));
        }
    }

    private static String getDefaultValue(Field field) {
        Parsed annotation = field.getAnnotation(Parsed.class);
        return annotation != null && !NULL_VALUE.equals(annotation.defaultNullRead()) ? annotation.defaultNullRead() : null;
    }

    private static Object parseValue(Class<?> type, String value) {
        if (type == int.class || type == Integer.class) {
            return Double.valueOf(value).intValue();
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        } else if (type == String.class) {
            return value;
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + type);
        }
    }
}
