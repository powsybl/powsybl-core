/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import com.google.common.primitives.Ints;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.PrintStream;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DataObject {

    private final long id;

    private DataObject parent;

    private final List<DataObject> children = new ArrayList<>();

    private final DataClass dataClass;

    private StudyCase studyCase;

    private Map<String, Object> attributeValues = new HashMap<>();

    private Map<String, Object> initialAttributeValues;

    public DataObject(long id, DataClass dataClass) {
        this.id = id;
        this.dataClass = Objects.requireNonNull(dataClass);
    }

    public long getId() {
        return id;
    }

    public String getLocName() {
        return findStringAttributeValue(DataAttribute.LOC_NAME).orElseThrow(() -> new PowerFactoryException("Attribute 'loc_name' not found in class " + dataClass.getName()));
    }

    public DataObject getParent() {
        return parent;
    }

    public DataObject setParent(DataObject parent) {
        if (this.parent != null) {
            this.parent.getChildren().remove(this);
        }
        if (parent != null) {
            parent.getChildren().add(this);
        }
        this.parent = parent;
        return this;
    }

    public List<DataObject> getChildren() {
        return children;
    }

    public List<DataObject> getChildrenByClass(String className) {
        Objects.requireNonNull(className);
        return children.stream()
                .filter(child -> child.getDataClass().getName().equals(className))
                .collect(Collectors.toList());
    }

    public Optional<DataObject> getChild(String name) {
        Objects.requireNonNull(name);
        return children.stream().filter(child -> child.getLocName().equals(name)).findFirst();
    }

    public Optional<DataObject> getChild(String... names) {
        return getChild(0, names);
    }

    private Optional<DataObject> getChild(int i, String... names) {
        Optional<DataObject> child = getChild(names[i]);
        if (i == names.length - 1) {
            return child;
        }
        return child.flatMap(littleChild -> littleChild.getChild(i + 1, names));
    }

    public Optional<DataObject> findFirstChildByClass(String className) {
        Objects.requireNonNull(className);
        return children.stream()
                .filter(child -> child.getDataClass().getName().equals(className))
                .findFirst();
    }

    public DataClass getDataClass() {
        return dataClass;
    }

    public String getDataClassName() {
        return dataClass.getName();
    }

    public StudyCase getStudyCase() {
        return studyCase;
    }

    public void setStudyCase(StudyCase studyCase) {
        if (this.studyCase != null) {
            throw new PowerFactoryException("Data object is already assigned to a study case");
        }
        this.studyCase = Objects.requireNonNull(studyCase);
    }

    public List<String> getAttributeNames() {
        return dataClass.getAttributes().stream().map(DataAttribute::getName).collect(Collectors.toList());
    }

    public void copyAttributeValues(DataObject other) {
        Objects.requireNonNull(other);
        if (!getDataClassName().equals(other.getDataClassName())) {
            throw new PowerFactoryException("It is forbidden to copy attributes from an object with a different class: "
                    + getDataClassName() + " and " + other.getDataClassName());
        }
        initialAttributeValues = new HashMap<>(attributeValues);
        for (Map.Entry<String, Object> e : other.attributeValues.entrySet()) {
            String attributeName = e.getKey();
            Object attributeValue = e.getValue();
            if (!attributeName.equals(DataAttribute.LOC_NAME)
                    && !attributeName.equals(DataAttribute.FOLD_ID)
                    && !attributeName.equals(DataAttribute.FOR_NAME)) {
                attributeValues.put(attributeName, attributeValue);
            }
        }
    }

    public void restoreAttributeValues() {
        if (initialAttributeValues != null) {
            attributeValues = initialAttributeValues;
        }
    }

    private static void checkAttributeType(DataAttribute attribute, DataAttributeType type) {
        if (attribute.getType() != type) {
            throw new PowerFactoryException("Incorrect attribute type: " + attribute.getType());
        }
    }

    private static PowerFactoryException createAttributeNotFoundException(String name) {
        return new PowerFactoryException("Attribute '" + name + "' not found");
    }

    private static PowerFactoryException createAttributeNotFoundException(String type, String name) {
        return new PowerFactoryException(type + " attribute '" + name + "' not found");
    }

    private <T> void setGenericAttributeValue(String name, DataAttributeType type, T value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        DataAttribute attribute = dataClass.getAttributeByName(name);
        if (attribute == null) {
            throw createAttributeNotFoundException(name);
        }
        checkAttributeType(attribute, type);
        attributeValues.put(name, value);
    }

    public Optional<Object> findAttributeValue(String name) {
        Objects.requireNonNull(name);
        return Optional.ofNullable(attributeValues.get(name));
    }

    public Object getAttributeValue(String name) {
        return findAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException(name));
    }

    private <T> Optional<T> findGenericAttributeValue(String name, DataAttributeType type) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        DataAttribute attribute = dataClass.getAttributeByName(name);
        if (attribute == null) {
            return Optional.empty();
        }
        checkAttributeType(attribute, type);
        T value = (T) attributeValues.get(name);
        return Optional.ofNullable(value);
    }

    public Optional<String> findStringAttributeValue(String name) {
        return findGenericAttributeValue(name, DataAttributeType.STRING);
    }

    public String getStringAttributeValue(String name) {
        return findStringAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("String", name));
    }

    public DataObject setStringAttributeValue(String name, String value) {
        setGenericAttributeValue(name, DataAttributeType.STRING, value);
        return this;
    }

    public DataObject setLocName(String locName) {
        return setStringAttributeValue(DataAttribute.LOC_NAME, locName);
    }

    public Optional<DataObject> findObjectAttributeValue(String name) {
        return findGenericAttributeValue(name, DataAttributeType.OBJECT);
    }

    public DataObject getObjectAttributeValue(String name) {
        return findObjectAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Object", name));
    }

    public DataObject setObjectAttributeValue(String name, DataObject value) {
        setGenericAttributeValue(name, DataAttributeType.OBJECT, value);
        return this;
    }

    public Optional<List<DataObject>> findObjectVectorAttributeValue(String name) {
        return findGenericAttributeValue(name, DataAttributeType.OBJECT_VECTOR);
    }

    public List<DataObject> getObjectVectorAttributeValue(String name) {
        return findObjectVectorAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Object vector", name));
    }

    public DataObject setObjectVectorAttributeValue(String name, List<DataObject> value) {
        setGenericAttributeValue(name, DataAttributeType.OBJECT_VECTOR, value);
        return this;
    }

    public Optional<Float> findFloatAttributeValue(String name) {
        return findGenericAttributeValue(name, DataAttributeType.FLOAT);
    }

    public float getFloatAttributeValue(String name) {
        return findFloatAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Float", name));
    }

    public DataObject setFloatAttributeValue(String name, float value) {
        setGenericAttributeValue(name, DataAttributeType.FLOAT, value);
        return this;
    }

    public Optional<List<Integer>> findIntVectorAttributeValue(String name) {
        return findGenericAttributeValue(name, DataAttributeType.INTEGER_VECTOR);
    }

    public List<Integer> getIntVectorAttributeValue(String name) {
        return findIntVectorAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Int vector", name));
    }

    public DataObject setIntVectorAttributeValue(String name, List<Integer> value) {
        setGenericAttributeValue(name, DataAttributeType.INTEGER_VECTOR, value);
        return this;
    }

    public Optional<List<Float>> findFloatVectorAttributeValue(String name) {
        return findGenericAttributeValue(name, DataAttributeType.FLOAT_VECTOR);
    }

    public List<Float> getFloatVectorAttributeValue(String name) {
        return findFloatVectorAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Float vector", name));
    }

    public DataObject setFloatVectorAttributeValue(String name, List<Float> value) {
        setGenericAttributeValue(name, DataAttributeType.FLOAT_VECTOR, value);
        return this;
    }

    public Optional<List<Double>> findDoubleVectorAttributeValue(String name) {
        return findGenericAttributeValue(name, DataAttributeType.DOUBLE_VECTOR);
    }

    public List<Double> getDoubleVectorAttributeValue(String name) {
        return findDoubleVectorAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Double vector", name));
    }

    public DataObject setDoubleVectorAttributeValue(String name, List<Double> value) {
        setGenericAttributeValue(name, DataAttributeType.DOUBLE_VECTOR, value);
        return this;
    }

    public OptionalInt findIntAttributeValue(String name) {
        Objects.requireNonNull(name);
        DataAttribute attribute = dataClass.getAttributeByName(name);
        if (attribute == null) {
            return OptionalInt.empty();
        }
        checkAttributeType(attribute, DataAttributeType.INTEGER);
        Integer value = (Integer) attributeValues.get(name);
        if (value == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(value);
    }

    public int getIntAttributeValue(String name) {
        return findIntAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Integer", name));
    }

    public DataObject setIntAttributeValue(String name, int value) {
        setGenericAttributeValue(name, DataAttributeType.INTEGER, value);
        return this;
    }

    public OptionalLong findLongAttributeValue(String name) {
        Objects.requireNonNull(name);
        DataAttribute attribute = dataClass.getAttributeByName(name);
        if (attribute == null) {
            return OptionalLong.empty();
        }
        checkAttributeType(attribute, DataAttributeType.INTEGER64);
        Long value = (Long) attributeValues.get(name);
        if (value == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(value);
    }

    public long getLongAttributeValue(String name) {
        return findLongAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Long", name));
    }

    public DataObject setLongAttributeValue(String name, long value) {
        setGenericAttributeValue(name, DataAttributeType.INTEGER64, value);
        return this;
    }

    public OptionalDouble findDoubleAttributeValue(String name) {
        Objects.requireNonNull(name);
        DataAttribute attribute = dataClass.getAttributeByName(name);
        if (attribute == null) {
            return OptionalDouble.empty();
        }
        checkAttributeType(attribute, DataAttributeType.DOUBLE);
        Double value = (Double) attributeValues.get(name);
        if (value == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(value);
    }

    public double getDoubleAttributeValue(String name) {
        return findDoubleAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Double", name));
    }

    public DataObject setDoubleAttributeValue(String name, double value) {
        setGenericAttributeValue(name, DataAttributeType.DOUBLE, value);
        return this;
    }

    public Optional<RealMatrix> findMatrixAttributeValue(String name) {
        return findGenericAttributeValue(name, DataAttributeType.MATRIX);
    }

    public RealMatrix getMatrixAttributeValue(String name) {
        return findMatrixAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Matrix", name));
    }

    public DataObject setMatrixAttributeValue(String name, RealMatrix value) {
        setGenericAttributeValue(name, DataAttributeType.MATRIX, value);
        return this;
    }

    public Optional<Instant> findInstantAttributeValue(String name) {
        OptionalInt i = findIntAttributeValue(name);
        if (i.isPresent()) {
            return Optional.of(Instant.ofEpochSecond(i.getAsInt()));
        }
        return Optional.empty();
    }

    public Instant getInstantAttributeValue(String name) {
        return findInstantAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Instant", name));
    }

    public DataObject setInstantAttributeValue(String name, Instant value) {
        setIntAttributeValue(name, Ints.checkedCast(value.getEpochSecond()));
        return this;
    }

    private static void indent(PrintStream out, int depth) {
        for (int i = 0; i < depth; i++) {
            out.print("    ");
        }
    }

    public void print(PrintStream out, Function<DataObject, String> toString) {
        Objects.requireNonNull(out);
        Objects.requireNonNull(toString);
        print(out, toString, 0);
    }

    private void print(PrintStream out, Function<DataObject, String> toString, int depth) {
        indent(out, depth);
        out.println(toString.apply(this));
        for (DataObject child : children) {
            child.print(out, toString, depth + 1);
        }
    }

    public List<DataObject> getPath() {
        List<DataObject> path = new ArrayList<>();
        DataObject obj = this;
        while (obj != null) {
            path.add(0, obj);
            obj = obj.getParent();
        }
        return path;
    }

    public void traverse(Consumer<DataObject> handler) {
        Objects.requireNonNull(handler);
        handler.accept(this);
        for (DataObject child : children) {
            child.traverse(handler);
        }
    }

    public List<DataObject> search(String regex) {
        List<DataObject> result = new ArrayList<>();
        traverse(object -> {
            if (object.getFullName().matches(regex)) {
                result.add(object);
            }
        });
        return result;
    }

    public String getFullName() {
        return getPath().stream().map(DataObject::getLocName).collect(Collectors.joining("\\"))
                + '.' + dataClass.getName();
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
