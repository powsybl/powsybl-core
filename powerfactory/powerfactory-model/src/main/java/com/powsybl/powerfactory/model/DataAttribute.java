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
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DataAttribute {

    public static final String LOC_NAME = "loc_name";
    public static final String FOLD_ID = "fold_id";
    public static final String FOR_NAME = "for_name";

    private final String name;

    private final DataAttributeType type;

    private final String description;

    public DataAttribute(String name, DataAttributeType type) {
        this(name, type, "");
    }

    public DataAttribute(String name, DataAttributeType type, String description) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.description = Objects.requireNonNull(description);
    }

    public String getName() {
        return name;
    }

    public DataAttributeType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    static class ParsingContext {
        String name;

        DataAttributeType type;

        String description = "";
    }

    static DataAttribute parseJson(JsonParser parser) {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "name":
                    context.name = parser.nextTextValue();
                    return true;
                case "type":
                    context.type = DataAttributeType.valueOf(parser.nextTextValue());
                    return true;
                case "description":
                    context.description = parser.nextTextValue();
                    return true;
                default:
                    return false;
            }
        });
        return new DataAttribute(context.name, context.type, context.description);
    }

    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("name", name);
        generator.writeStringField("type", type.name());
        if (description != null && !description.isBlank()) {
            generator.writeStringField("description", description);
        }
        generator.writeEndObject();
    }

    @Override
    public String toString() {
        return "DataAttribute(name=" + name + ", type=" + type + ", description=" + description + ")";
    }
}
