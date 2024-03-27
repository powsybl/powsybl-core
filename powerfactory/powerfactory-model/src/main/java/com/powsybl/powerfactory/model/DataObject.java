/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.powsybl.commons.json.JsonUtil;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DataObject {

    private final long id;

    private DataObject parent;

    private final List<DataObject> children = new ArrayList<>();

    private final DataClass dataClass;

    private final DataObjectIndex index;

    private final Map<String, Object> attributeValues;

    public DataObject(long id, DataClass dataClass, DataObjectIndex index) {
        this(id, dataClass, index, new LinkedHashMap<>());
    }

    public DataObject(long id, DataClass dataClass, DataObjectIndex index, Map<String, Object> attributeValues) {
        this.id = id;
        this.dataClass = Objects.requireNonNull(dataClass);
        this.index = Objects.requireNonNull(index);
        this.attributeValues = attributeValues;
        index.addDataObject(this);
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

    public DataObjectIndex getIndex() {
        return index;
    }

    public List<String> getAttributeNames() {
        return dataClass.getAttributes().stream().map(DataAttribute::getName).collect(Collectors.toList());
    }

    public Map<String, Object> getAttributeValues() {
        return attributeValues;
    }

    public Optional<Object> findAttributeValue(String name) {
        Objects.requireNonNull(name);
        return Optional.ofNullable(attributeValues.get(name));
    }

    public Object getAttributeValue(String name) {
        return findAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException(name));
    }

    private static void checkAttributeType(DataAttribute attribute, DataAttributeType type) {
        if (attribute.getType() != type
                // hack to handle float attributes considered as double in C++ API
                && !(attribute.getType() == DataAttributeType.DOUBLE && type == DataAttributeType.FLOAT)
                && !(attribute.getType() == DataAttributeType.INTEGER && type == DataAttributeType.INTEGER64)) {
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

    public Optional<DataObjectRef> findObjectAttributeValue(String name) {
        return findGenericAttributeValue(name, DataAttributeType.OBJECT);
    }

    public DataObjectRef getObjectAttributeValue(String name) {
        return findObjectAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Object", name));
    }

    public DataObject setObjectAttributeValue(String name, long id) {
        setGenericAttributeValue(name, DataAttributeType.OBJECT, new DataObjectRef(id, index));
        return this;
    }

    public Optional<List<DataObjectRef>> findObjectVectorAttributeValue(String name) {
        return findGenericAttributeValue(name, DataAttributeType.OBJECT_VECTOR);
    }

    public List<DataObjectRef> getObjectVectorAttributeValue(String name) {
        return findObjectVectorAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Object vector", name));
    }

    public DataObject setObjectVectorAttributeValue(String name, List<Long> ids) {
        List<DataObjectRef> value = Objects.requireNonNull(ids).stream()
                .map(objId -> new DataObjectRef(objId, index))
                .collect(Collectors.toList());
        setGenericAttributeValue(name, DataAttributeType.OBJECT_VECTOR, value);
        return this;
    }

    public Optional<Float> findFloatAttributeValue(String name) {
        return this.<Number>findGenericAttributeValue(name, DataAttributeType.FLOAT)
                .map(Number::floatValue);
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

    public Optional<List<Long>> findLongVectorAttributeValue(String name) {
        return findGenericAttributeValue(name, DataAttributeType.INTEGER64_VECTOR);
    }

    public List<Long> getLongVectorAttributeValue(String name) {
        return findLongVectorAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Long vector", name));
    }

    public DataObject setLongVectorAttributeValue(String name, List<Long> value) {
        setGenericAttributeValue(name, DataAttributeType.INTEGER64_VECTOR, value);
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

    public Optional<List<String>> findStringVectorAttributeValue(String name) {
        return findGenericAttributeValue(name, DataAttributeType.STRING_VECTOR);
    }

    public List<String> getStringVectorAttributeValue(String name) {
        return findStringVectorAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("String vector", name));
    }

    public DataObject setStringVectorAttributeValue(String name, List<String> value) {
        setGenericAttributeValue(name, DataAttributeType.STRING_VECTOR, value);
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
        Number value = (Number) attributeValues.get(name);
        if (value == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(value.longValue());
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

    public Optional<RealMatrix> findDoubleMatrixAttributeValue(String name) {
        return findGenericAttributeValue(name, DataAttributeType.DOUBLE_MATRIX);
    }

    public RealMatrix getDoubleMatrixAttributeValue(String name) {
        return findDoubleMatrixAttributeValue(name).orElseThrow(() -> createAttributeNotFoundException("Matrix", name));
    }

    public DataObject setDoubleMatrixAttributeValue(String name, RealMatrix value) {
        setGenericAttributeValue(name, DataAttributeType.DOUBLE_MATRIX, value);
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

    static class ParsingContext {
        long id = -1;

        String className;

        final List<DataObject> children = new ArrayList<>();

        final Map<String, Object> attributeValues = new LinkedHashMap<>();
    }

    private static RealMatrix parseMatrixJson(JsonParser parser) throws IOException {
        Mutable<RealMatrix> result = new MutableObject<>();

        parser.nextToken();
        JsonUtil.parseObject(parser, new JsonUtil.FieldHandler() {

            private Integer rowCount;
            private Integer columnCount;

            @Override
            public boolean onField(String fieldName3) throws IOException {
                switch (fieldName3) {
                    case "rowCount":
                        parser.nextToken();
                        rowCount = parser.getValueAsInt();
                        return true;
                    case "columnCount":
                        parser.nextToken();
                        columnCount = parser.getValueAsInt();
                        return true;
                    case "data":
                        if (rowCount == null || columnCount == null) {
                            throw new PowerFactoryException("Data has to be specified after row and column count");
                        }
                        List<Double> data = JsonUtil.parseDoubleArray(parser);
                        BlockRealMatrix matrix = new BlockRealMatrix(rowCount, columnCount);
                        for (int row = 0; row < rowCount; row++) {
                            for (int col = 0; col < columnCount; col++) {
                                matrix.setEntry(row, col, data.get(row * columnCount + col));
                            }
                        }
                        result.setValue(matrix);
                        return true;
                    default:
                        return false;
                }
            }
        });
        return result.getValue();
    }

    private static void parseValueJson(JsonParser parser, DataObjectIndex index, ParsingContext context, DataClass dataClass) throws IOException {
        parser.nextToken();
        JsonUtil.parseObject(parser, fieldName -> {
            DataAttribute attribute = dataClass.getAttributeByName(fieldName);
            switch (attribute.getType()) {
                case INTEGER:
                    parser.nextToken();
                    context.attributeValues.put(fieldName, parser.getValueAsInt());
                    return true;
                case INTEGER64:
                    parser.nextToken();
                    context.attributeValues.put(fieldName, parser.getValueAsLong());
                    return true;
                case FLOAT:
                    parser.nextToken();
                    context.attributeValues.put(fieldName, parser.getFloatValue());
                    return true;
                case DOUBLE:
                    parser.nextToken();
                    context.attributeValues.put(fieldName, parser.getValueAsDouble());
                    return true;
                case STRING:
                    context.attributeValues.put(fieldName, parser.nextTextValue());
                    return true;
                case OBJECT:
                    parser.nextToken();
                    context.attributeValues.put(fieldName, new DataObjectRef(parser.getValueAsLong(), index));
                    return true;
                case INTEGER_VECTOR:
                    context.attributeValues.put(fieldName, JsonUtil.parseIntegerArray(parser));
                    return true;
                case INTEGER64_VECTOR:
                    context.attributeValues.put(fieldName, JsonUtil.parseLongArray(parser));
                    return true;
                case FLOAT_VECTOR:
                    context.attributeValues.put(fieldName, JsonUtil.parseFloatArray(parser));
                    return true;
                case DOUBLE_VECTOR:
                    context.attributeValues.put(fieldName, JsonUtil.parseDoubleArray(parser));
                    return true;
                case OBJECT_VECTOR:
                    context.attributeValues.put(fieldName, JsonUtil.parseLongArray(parser).stream()
                            .map(id -> new DataObjectRef(id, index))
                            .collect(Collectors.toList()));
                    return true;
                case STRING_VECTOR:
                    context.attributeValues.put(fieldName, JsonUtil.parseStringArray(parser));
                    return true;
                case DOUBLE_MATRIX:
                    context.attributeValues.put(fieldName, parseMatrixJson(parser));
                    return true;
            }
            return false;
        });
    }

    static DataObject parseJson(JsonParser parser, DataObjectIndex index, DataScheme scheme) {
        Objects.requireNonNull(parser);
        Objects.requireNonNull(index);
        Objects.requireNonNull(scheme);
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "id":
                    parser.nextToken();
                    context.id = parser.getValueAsLong();
                    return true;
                case "className":
                    context.className = parser.nextTextValue();
                    return true;
                case "values":
                    DataClass dataClass = scheme.getClassByName(context.className);
                    parseValueJson(parser, index, context, dataClass);
                    return true;
                case "children":
                    JsonUtil.parseObjectArray(parser, context.children::add, parser2 -> DataObject.parseJson(parser2, index, scheme));
                    return true;
                default:
                    return false;
            }
        });
        DataObject object = new DataObject(context.id, scheme.getClassByName(context.className), index, context.attributeValues);
        for (DataObject child : context.children) {
            child.setParent(object);
        }
        return object;
    }

    private static boolean writeValue(JsonGenerator generator, Object value) throws IOException {
        if (value instanceof String stringValue) {
            generator.writeString(stringValue);
            return true;
        } else if (value instanceof Integer integerValue) {
            generator.writeNumber(integerValue);
            return true;
        } else if (value instanceof Long longValue) {
            generator.writeNumber(longValue);
            return true;
        } else if (value instanceof Float floatValue) {
            generator.writeNumber(floatValue);
            return true;
        } else if (value instanceof Double doubleValue) {
            generator.writeNumber(doubleValue);
            return true;
        } else if (value instanceof DataObjectRef dataObjectRef) {
            generator.writeNumber(dataObjectRef.getId());
            return true;
        }
        return false;
    }

    private void writeListJson(JsonGenerator generator, Map.Entry<String, Object> e) throws IOException {
        generator.writeStartArray();
        for (Object value : (List) e.getValue()) {
            writeValue(generator, value);
        }
        generator.writeEndArray();
    }

    private void writeMatrixJson(JsonGenerator generator, Map.Entry<String, Object> e) throws IOException {
        RealMatrix matrix = (RealMatrix) e.getValue();
        generator.writeStartObject();
        generator.writeNumberField("rowCount", matrix.getRowDimension());
        generator.writeNumberField("columnCount", matrix.getColumnDimension());
        generator.writeFieldName("data");
        generator.writeStartArray();
        for (int row = 0; row < matrix.getRowDimension(); row++) {
            double[] rowValues = matrix.getRow(row);
            for (double rowValue : rowValues) {
                generator.writeNumber(rowValue);
            }
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        generator.writeNumberField("id", id);
        generator.writeStringField("className", dataClass.getName());

        generator.writeFieldName("values");
        generator.writeStartObject();
        for (var e : attributeValues.entrySet()) {
            generator.writeFieldName(e.getKey());
            if (writeValue(generator, e.getValue())) {
                // nothing
            } else if (e.getValue() instanceof List) {
                writeListJson(generator, e);
            } else if (e.getValue() instanceof RealMatrix) {
                writeMatrixJson(generator, e);
            } else {
                throw new PowerFactoryException("Unsupported value type: " + e.getValue().getClass());
            }
        }
        generator.writeEndObject();

        if (!children.isEmpty()) {
            generator.writeFieldName("children");
            generator.writeStartArray();
            for (DataObject child : children) {
                child.writeJson(generator);
            }
            generator.writeEndArray();
        }

        generator.writeEndObject();
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
