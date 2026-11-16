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

import java.io.IOException;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DataClass {

    private final String name;

    private final List<DataAttribute> attributes = new ArrayList<>();

    private final Map<String, DataAttribute> attributesByName = new HashMap<>();

    public DataClass(String name) {
        this(name, Collections.emptyList());
    }

    public DataClass(String name, List<DataAttribute> attributes) {
        this.name = Objects.requireNonNull(name);
        for (var attribute : attributes) {
            addAttribute(attribute);
        }
    }

    public String getName() {
        return name;
    }

    public DataClass addAttribute(DataAttribute attribute) {
        Objects.requireNonNull(attribute);
        if (attributesByName.containsKey(attribute.getName())) {
            throw new PowerFactoryException("Class '" + name + "' already has an attribute named '" + attribute.getName() + "'");
        }
        attributes.add(attribute);
        attributesByName.put(attribute.getName(), attribute);
        return this;
    }

    public List<DataAttribute> getAttributes() {
        return attributes;
    }

    public DataAttribute getAttributeByName(String name) {
        Objects.requireNonNull(name);
        return attributesByName.get(name);
    }

    static class ParsingContext {
        String name;

        final List<DataAttribute> attributes = new ArrayList<>();
    }

    static DataClass parseJson(JsonParser parser) {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "name":
                    context.name = parser.nextTextValue();
                    return true;
                case "attributes":
                    JsonUtil.parseObjectArray(parser, context.attributes::add, DataAttribute::parseJson);
                    return true;
                default:
                    return false;
            }
        });
        return new DataClass(context.name, context.attributes);
    }

    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("name", name);
        generator.writeFieldName("attributes");
        generator.writeStartArray();
        for (DataAttribute attribute : attributes) {
            attribute.writeJson(generator);
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    @Override
    public String toString() {
        return "DataClass(name=" + name + ")";
    }

    public static DataClass init(String name) {
        return new DataClass(name)
                .addAttribute(new DataAttribute(DataAttribute.LOC_NAME, DataAttributeType.STRING));
    }
}
