/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.google.common.base.Joiner;
import com.powsybl.commons.exceptions.UncheckedClassNotFoundException;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MapModuleConfig implements ModuleConfig {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{(\\w+)\\}|\\$(\\w+)"); // match ${ENV_VAR_NAME} or $ENV_VAR_NAME

    private final Map<Object, Object> properties;

    private final FileSystem fs;

    public MapModuleConfig(FileSystem fs) {
        this(new HashMap<>(), fs);
    }

    public MapModuleConfig(Map<Object, Object> properties) {
        this(properties, FileSystems.getDefault());
    }

    public MapModuleConfig(Map<Object, Object> properties, FileSystem fs) {
        this.properties = properties;
        this.fs = fs;
    }

    @Override
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.entrySet().stream()
                .map(Map.Entry::getKey)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    /*
     * Environment variables substitution
     */
    private static String substitureEnvVar(String str) {
        if (str == null) {
            return null;
        }
        Matcher m = VAR_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String envVarName = m.group(1) == null ? m.group(2) : m.group(1);
            String envVarValue = System.getenv(envVarName);
            m.appendReplacement(sb, envVarValue == null ? "" : envVarValue);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @Override
    public String getStringProperty(String name) {
        String value = (String) properties.get(name);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Property " + name + " is not set");
        }
        return substitureEnvVar(value);
    }

    @Override
    public String getStringProperty(String name, String defaultValue) {
        Object value = properties.get(name);
        return substitureEnvVar(value == null ? defaultValue : (String) value);
    }

    public void setStringProperty(String name, String value) {
        properties.put(Objects.requireNonNull(name), Objects.requireNonNull(value));
    }

    @Override
    public List<String> getStringListProperty(String name) {
        return new ArrayList<>(Arrays.asList(getStringProperty(name).split("[:,]")));
    }

    @Override
    public List<String> getStringListProperty(String name, List<String> defaultValue) {
        String str = getStringProperty(name, null);
        if (str == null) {
            return defaultValue;
        }
        return str.isEmpty() ? Collections.emptyList() : new ArrayList<>(Arrays.asList(str.split("[:,]")));
    }

    public void setStringListProperty(String name, List<String> stringList) {
        properties.put(name, Joiner.on(",").join(stringList).toString());
    }

    @Override
    public <E extends Enum<E>> E getEnumProperty(String name, Class<E> clazz) {
        return Enum.valueOf(clazz, getStringProperty(name));
    }

    @Override
    public <E extends Enum<E>> E getEnumProperty(String name, Class<E> clazz, E defaultValue) {
        return Enum.valueOf(clazz, getStringProperty(name, Objects.requireNonNull(defaultValue).name()));
    }

    @Override
    public <E extends Enum<E>> Set<E> getEnumSetProperty(String name, Class<E> clazz) {
        List<String> strings = getStringListProperty(name);
        return strings.stream().map(s -> Enum.valueOf(clazz, s)).collect(Collectors.toSet());
    }

    @Override
    public <E extends Enum<E>> Set<E> getEnumSetProperty(String name, Class<E> clazz, Set<E> defaultValue) {
        List<String> strings = getStringListProperty(name, null);
        if (strings == null) {
            return defaultValue;
        }
        return strings.stream().map(s -> Enum.valueOf(clazz, s)).collect(Collectors.toSet());
    }

    @Override
    public int getIntProperty(String name) {
        return Integer.parseInt(getStringProperty(name));
    }

    @Override
    public Integer getOptionalIntProperty(String name) {
        String value = getStringProperty(name, null);
        return value != null ? Integer.parseInt(value) : null;
    }

    @Override
    public Optional<Integer> getOptionalIntegerProperty(String name) {
        String value = getStringProperty(name, null);
        return Optional.ofNullable(value != null ? Integer.parseInt(value) : null);
    }

    @Override
    public int getIntProperty(String name, int defaultValue) {
        return Integer.parseInt(getStringProperty(name, Integer.toString(defaultValue)));
    }

    @Override
    public float getFloatProperty(String name) {
        return Float.parseFloat(getStringProperty(name));
    }

    @Override
    public float getFloatProperty(String name, float defaultValue) {
        return Float.parseFloat(getStringProperty(name, Float.toString(defaultValue)));
    }

    @Override
    public Optional<Float> getOptionalFloatProperty(String name) {
        String value = getStringProperty(name, null);
        return Optional.ofNullable(value != null ? Float.parseFloat(value) : null);
    }

    @Override
    public double getDoubleProperty(String name) {
        return Double.parseDouble(getStringProperty(name));
    }

    @Override
    public double getDoubleProperty(String name, double defaultValue) {
        return Double.parseDouble(getStringProperty(name, Double.toString(defaultValue)));
    }

    @Override
    public boolean getBooleanProperty(String name) {
        return Boolean.parseBoolean(getStringProperty(name));
    }

    @Override
    public boolean getBooleanProperty(String name, boolean defaultValue) {
        return Boolean.parseBoolean(getStringProperty(name, Boolean.toString(defaultValue)));
    }

    @Override
    public Boolean getOptinalBooleanProperty(String name) {
        String value = getStringProperty(name, null);
        return value != null ? Boolean.parseBoolean(value) : null;
    }

    @Override
    public Optional<Boolean> getOptionalBooleanProperty(String name) {
        String value = getStringProperty(name, null);
        return Optional.ofNullable(value != null ? Boolean.parseBoolean(value) : null);
    }

    @Override
    public Path getPathProperty(String name) {
        return fs.getPath(getStringProperty(name));
    }

    @Override
    public Path getPathProperty(String name, Path defaultValue) {
        String value = getStringProperty(name, defaultValue != null ? defaultValue.toString() : null);
        return value != null ? fs.getPath(value) : null;
    }

    public void setPathProperty(String name, Path path) {
        properties.put(name, path.toAbsolutePath().toString());
    }

    @Override
    public List<Path> getPathListProperty(String name) {
        List<String> strings = getStringListProperty(name);
        List<Path> paths = new ArrayList<>(strings.size());
        for (String s : strings) {
            paths.add(fs.getPath(s));
        }
        return paths;
    }

    @Override
    public <T> Class<? extends T> getClassProperty(String name, Class<T> subClass) {
        try {
            return Class.forName(getStringProperty(name)).asSubclass(subClass);
        } catch (ClassNotFoundException e) {
            throw new UncheckedClassNotFoundException(e);
        }
    }

    @Override
    public <T> Class<? extends T> getClassProperty(String name, Class<T> subClass, Class<? extends T> defaultValue) {
        try {
            Object value = properties.get(name);
            if (value == null) {
                return defaultValue;
            } else {
                return Class.forName((String) value).asSubclass(subClass);
            }
        } catch (ClassNotFoundException e) {
            throw new UncheckedClassNotFoundException(e);
        }
    }

    public <T> void setClassProperty(String name, Class<T> subClass) {
        Objects.requireNonNull(subClass);
        setStringProperty(name, subClass.getName());
    }

    @Override
    public DateTime getDateTimeProperty(String name) {
        return DateTime.parse(getStringProperty(name));
    }

    @Override
    public Interval getIntervalProperty(String name) {
        return Interval.parse(getStringProperty(name));
    }

}
