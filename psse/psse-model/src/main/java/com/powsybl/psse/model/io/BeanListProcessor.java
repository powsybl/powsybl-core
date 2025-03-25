package com.powsybl.psse.model.io;

import com.powsybl.psse.model.PsseException;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;

import java.lang.reflect.Field;
import java.util.*;

public class BeanListProcessor<T> {
    private final Class<T> clazz;
    private final String[] headers;
    private List<T> beans;
    private Field[] fields;
    private Map<String, Field> nestedMappings = new HashMap<>();
    private Map<Field, Map<String, Field>> nestedFieldMap = new HashMap<>();
    private List<Field> setToDefault = new ArrayList<>();

    public BeanListProcessor(Class<T> clazz, String[] headers) {
        this.clazz = clazz;
        this.headers = headers;
        beans = new ArrayList<>();

        initFields(headers);
    }

    private void initFields(String[] headers) {
        fields = new Field[headers.length];
        for (Field field : clazz.getDeclaredFields()) {
            Parsed parsedAnnotation = field.getAnnotation(Parsed.class);
            Nested nestedAnnotation = field.getAnnotation(Nested.class);
            if (parsedAnnotation != null) {
                String[] fieldNames = parsedAnnotation.field().length > 0 ? parsedAnnotation.field() : new String[]{field.getName()};

                int colIndex = findHeaderIndex(headers, fieldNames);

                if (colIndex != -1) {
                    field.setAccessible(true);
                    fields[colIndex] = field;
                } else {
                    if (!parsedAnnotation.defaultNullRead().equals("null")) {
                        field.setAccessible(true);
                        setToDefault.add(field);
                    }
                }
            } else if (nestedAnnotation != null) {
                processNestedMapping(field, nestedAnnotation);
            }
        }
    }

    private void processNestedMapping(Field nestedField, Nested nestedAnnotation) {
        Map<String, Field> mappings = new HashMap<>();
        var suffix = nestedAnnotation.args().length > 0 ? nestedAnnotation.args()[0] : "";
        for (Field subField : nestedField.getType().getDeclaredFields()) {
            Parsed annotation = subField.getAnnotation(Parsed.class);
            if (annotation != null) {
                String[] mappedHeaders = annotation.field().length > 0 ? annotation.field() : new String[]{subField.getName()};
                for (String header : mappedHeaders) {
                    var finalHeader = header.startsWith("wdg") ? "wdg" + suffix + header.substring(3) : header + suffix;
                    mappings.put(finalHeader, subField);
                }
            }
        }
        nestedFieldMap.put(nestedField, mappings);
    }

    private static int findHeaderIndex(String[] headers, String[] fieldNames) {
        int colIndex = -1;
        for (String fieldName : fieldNames) {
            colIndex = Arrays.asList(headers).indexOf(fieldName);
            if (colIndex != -1) break;
        }
        return colIndex;
    }

    public List<T> getBeans() {
        return beans;
    }

    public void processLine(String[] row) {
        try {
            if (row.length == 0) return;

            var count = Math.min(row.length, headers.length);
            T instance = clazz.getDeclaredConstructor().newInstance();

            for (int i = 0; i < count; i++) {
                if (fields[i] != null) {
                    setFieldValue(instance, fields[i], row[i]);
                } else {
                    String header = headers[i];
                    for (Map.Entry<Field, Map<String, Field>> entry : nestedFieldMap.entrySet()) {
                        Field nestedField = entry.getKey();
                        Map<String, Field> mappings = entry.getValue();

                        if (mappings.containsKey(header)) {
                            processNestedField(instance, row[i], nestedField, mappings.get(header));
                            break;
                        }
                    }
                }
                for (Field field : setToDefault) {
                    setFieldValue(instance, field, null);
                }
            }

            for (int i = count; i < fields.length; i++) {
                var field = fields[i];
                if (field != null) {
                    setFieldValue(instance, field, null);
                }
            }

            beans.add(instance);
        } catch (Exception e) {
            throw new PsseException("Parsing error:" + e.getMessage());
        }
    }

    private void processNestedField(Object instance, String value, Field nestedField, Field subField) throws Exception {
        Object nestedInstance = getOrCreateNestedInstance(instance, nestedField);

        subField.setAccessible(true);
        setFieldValue(nestedInstance, subField, value);
    }

    private Object getOrCreateNestedInstance(Object instance, Field nestedField) throws Exception {
        nestedField.setAccessible(true);
        Object nestedInstance = nestedField.get(instance);
        if (nestedInstance == null) {
            nestedInstance = nestedField.getType().getDeclaredConstructor().newInstance();
            nestedField.setAccessible(true);
            nestedField.set(instance, nestedInstance);
        }
        return nestedInstance;
    }

    private static void setFieldValue(Object instance, Field field, String _value) throws IllegalAccessException {
        var value = _value;
        if (value == null || value.isEmpty() || value.equals("null")) {
            value = field.getAnnotation(Parsed.class).defaultNullRead();
            if (value.equals("null"))
                return;
        }
        Class<?> type = field.getType();
        if (type == int.class || type == Integer.class) {
            int i = value.contains(".") ? Double.valueOf(value).intValue() : Integer.parseInt(value);
            field.set(instance, i);
        } else if (type == double.class || type == Double.class) {
            field.set(instance, Double.parseDouble(value));
        } else if (type == float.class || type == Float.class) {
            field.set(instance, Float.parseFloat(value));
        } else if (type == String.class) {
            field.set(instance, value);
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + type);
        }
    }
}
