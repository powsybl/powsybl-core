package com.powsybl.psse.model.io;

import com.univocity.parsers.annotations.Parsed;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeanListProcessor<T> {
    private final Class<T> clazz;
    private final String[] headers;
    private List<T> beans;
    private Field[] fields;

    public BeanListProcessor(Class<T> clazz, String[] headers) {
        this.clazz = clazz;
        this.headers = headers;
        beans = new ArrayList<>();

        initFields(clazz, headers);
    }

    private void initFields(Class<T> clazz, String[] headers) {
        fields = new Field[headers.length];
        for (Field field : clazz.getDeclaredFields()) {
            Parsed annotation = field.getAnnotation(Parsed.class);
            String[] fieldNames = annotation.field().length > 0 ? annotation.field() : new String[]{field.getName()};

            int colIndex = findHeaderIndex(headers, fieldNames);

            if (colIndex != -1) {
                field.setAccessible(true);
                fields[colIndex] = field;
            }
        }
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

    public void processLine(String[] row) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (row.length == 0) return;

        var count = Math.min(row.length, headers.length);
        T instance = clazz.getDeclaredConstructor().newInstance();

        for (int i = 0; i < count; i++) {
            setFieldValue(instance, fields[i], row[i]);
        }

        beans.add(instance);
    }

    private static void setFieldValue(Object instance, Field field, String value) throws IllegalAccessException {
        if (value == null || value.trim().isEmpty() || field == null) return;
        Class<?> type = field.getType();
        if (type == int.class || type == Integer.class) {
            field.set(instance, Integer.parseInt(value));
        } else if (type == double.class || type == Double.class) {
            field.set(instance, Double.parseDouble(value));
        } else if (type == String.class) {
            field.set(instance, value);
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + type);
        }
    }
}
