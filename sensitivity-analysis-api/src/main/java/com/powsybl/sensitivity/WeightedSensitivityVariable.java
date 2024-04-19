/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Associates a classical sensitivity variable and a weight.
 * The weight is not normalized.
 * A list of weighted variables can be used to model an injection increase of a group of generators and loads
 * trough shift keys, also called GLSK (for Generation and Load shift keys), see {@link com.powsybl.sensitivity.SensitivityVariableSet}
 * for more details.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class WeightedSensitivityVariable {

    private final String id;

    private final double weight;

    /**
     * Constructor
     * @param id id of the equipment affected by the injection increase (a generator id or a load id in general).
     * @param weight the shift key of the injection.
     */
    public WeightedSensitivityVariable(String id, double weight) {
        this.id = Objects.requireNonNull(id);
        if (Double.isNaN(weight)) {
            throw new IllegalArgumentException("Invalid weigth: " + weight);
        }
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "WeightedSensitivityVariable(" +
                "id='" + id + '\'' +
                ", weight=" + weight +
                ')';
    }

    private static final class ParsingContext {

        private String id;

        private double weight = Double.NaN;

        private void reset() {
            id = null;
            weight = Double.NaN;
        }
    }

    public static List<WeightedSensitivityVariable> parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);
        List<WeightedSensitivityVariable> variables = new ArrayList<>();
        try {
            ParsingContext context = new ParsingContext();
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME) {
                    String fieldName = parser.getCurrentName();
                    switch (fieldName) {
                        case "id" -> context.id = parser.nextTextValue();
                        case "weight" -> {
                            parser.nextToken();
                            context.weight = parser.getDoubleValue();
                        }
                        default -> {
                            // Do nothing
                        }
                    }
                } else if (token == JsonToken.END_ARRAY) {
                    break;
                } else if (token == JsonToken.END_OBJECT) {
                    variables.add(new WeightedSensitivityVariable(context.id, context.weight));
                    context.reset();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return variables;
    }
}
