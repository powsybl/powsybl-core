/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.PowsyblException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Models a group of variables.
 * A list of weighted variables is used in general to model an injection increase of a group of generators and loads
 * through shift keys, also called GLSK (for Generation and Load shift keys).
 * Note that weights are not normalized.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SensitivityVariableSet {

    private final String id;

    private final Map<String, WeightedSensitivityVariable> variables;

    /**
     * Constructor
     * @param id ID of this complex variable. It should not correspond to an id present in the network.
     * @param variables the list of weighted variables, see {@link com.powsybl.sensitivity.WeightedSensitivityVariable}
     */
    public SensitivityVariableSet(String id, List<WeightedSensitivityVariable> variables) {
        this.id = Objects.requireNonNull(id);
        //Use LinkedHashMap to preserve insertion order
        this.variables = Collections.unmodifiableMap(
                Objects.requireNonNull(variables).stream().collect(LinkedHashMap::new, (map, item) -> map.put(item.getId(), item), Map::putAll));
    }

    public String getId() {
        return id;
    }

    public Collection<WeightedSensitivityVariable> getVariables() {
        return variables.values();
    }

    public Map<String, WeightedSensitivityVariable> getVariablesById() {
        return variables;
    }

    public WeightedSensitivityVariable getVariable(String key) {
        return variables.get(key);
    }

    @Override
    public String toString() {
        return "SensitivityVariableSet(" +
                "id='" + id + '\'' +
                ", variables=" + variables +
                ')';
    }

    public static void writeJson(JsonGenerator jsonGenerator, SensitivityVariableSet variableSet) {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringProperty("id", variableSet.getId());
        jsonGenerator.writeName("variables");
        jsonGenerator.writeStartArray();
        for (WeightedSensitivityVariable variable : variableSet.getVariables()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringProperty("id", variable.getId());
            jsonGenerator.writeNumberProperty("weight", variable.getWeight());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }

    public static SensitivityVariableSet parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);
        String id = null;
        List<WeightedSensitivityVariable> variables = null;
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            if (token == JsonToken.PROPERTY_NAME) {
                String fieldName = parser.currentName();
                switch (fieldName) {
                    case "id" -> id = parser.nextStringValue();
                    case "variables" -> variables = WeightedSensitivityVariable.parseJson(parser);
                    default -> throw new PowsyblException("Unexpected field: " + fieldName);
                }
            } else if (token == JsonToken.END_OBJECT) {
                return new SensitivityVariableSet(id, variables);
            }
        }
        throw new PowsyblException("Parsing error");
    }
}
