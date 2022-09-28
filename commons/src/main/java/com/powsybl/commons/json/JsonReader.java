/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.json;

import com.fasterxml.jackson.core.JsonParser;
import com.powsybl.commons.io.TreeDataReader;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class JsonReader implements TreeDataReader {

    private final JsonParser jsonParser;

    public JsonReader(JsonParser jsonParser) {
        this.jsonParser = Objects.requireNonNull(jsonParser);
    }

    @Override
    public double readDoubleAttribute(String name) {
        return 0;
    }

    @Override
    public double readDoubleAttribute(String name, double defaultValue) {
        return 0;
    }

    @Override
    public float readFloatAttribute(String name) {
        return 0;
    }

    @Override
    public float readFloatAttribute(String name, float defaultValue) {
        return 0;
    }

    @Override
    public String readStringAttribute(String name) {
        return null;
    }

    @Override
    public Integer readIntAttribute(String name) {
        return null;
    }

    @Override
    public int readIntAttribute(String name, int defaultValue) {
        return 0;
    }

    @Override
    public Boolean readBooleanAttribute(String name) {
        return null;
    }

    @Override
    public boolean readBooleanAttribute(String name, boolean defaultValue) {
        return false;
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz) {
        return null;
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz, T defaultValue) {
        return null;
    }

    @Override
    public String getNodeName() {
        return null;
    }

    @Override
    public String readContent() {
        return null;
    }

    @Override
    public String readUntilEndNode(String endElementName, EventHandler eventHandler) {
        return null;
    }

    @Override
    public String readUntilEndNodeWithDepth(String endElementName, EventHandlerWithDepth eventHandler) {
        return null;
    }
}
