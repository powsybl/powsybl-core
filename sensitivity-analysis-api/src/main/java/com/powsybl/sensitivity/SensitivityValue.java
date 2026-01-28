/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
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

import java.util.Objects;

/**
 * Elementary result value of a sensitivity analysis, given the sensitivity factor and a contingency id (use null to get
 * a pre-contingency value). The value is the impact of the variable change on the monitored equipment. The function
 * reference gives the level of the function in the network pre-contingency state.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @see SensitivityFactor
 */
public class SensitivityValue {

    private final int factorIndex;

    private final int contingencyIndex;

    private final double value;

    private final double functionReference;

    /**
     * Constructor.
     *
     * @param factorIndex the sensitivity factor index
     * @param contingencyIndex the contingency index, -1 for pre-contingency state.
     * @param value the sensitivity value, as a result of the computation.
     * @param functionReference the value of the sensitivity function in the pre-contingency state.
     */
    public SensitivityValue(int factorIndex, int contingencyIndex, double value, double functionReference) {
        this.factorIndex = factorIndex;
        if (contingencyIndex < -1) {
            throw new IllegalArgumentException("Invalid contingency index: " + contingencyIndex);
        }
        this.contingencyIndex = contingencyIndex;
        this.value = value;
        this.functionReference = functionReference;
    }

    public int getFactorIndex() {
        return factorIndex;
    }

    public int getContingencyIndex() {
        return contingencyIndex;
    }

    public double getValue() {
        return value;
    }

    public double getFunctionReference() {
        return functionReference;
    }

    @Override
    public String toString() {
        return "SensitivityValue(" +
                "factorIndex=" + factorIndex +
                ", contingencyIndex='" + contingencyIndex + '\'' +
                ", value=" + value +
                ", functionReference=" + functionReference +
                ')';
    }

    static final class ParsingContext {
        private int factorIndex;
        private int contingencyIndex = -1;
        private double value;
        private double functionReference;
    }

    public static SensitivityValue parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);

        var context = new ParsingContext();
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            if (token == JsonToken.PROPERTY_NAME) {
                parseJson(parser, context);
            } else if (token == JsonToken.END_OBJECT) {
                return new SensitivityValue(context.factorIndex, context.contingencyIndex, context.value, context.functionReference);
            }
        }
        throw new PowsyblException("Parsing error");
    }

    private static void parseJson(JsonParser parser, ParsingContext context) {
        String fieldName = parser.currentName();
        switch (fieldName) {
            case "factorIndex":
                parser.nextToken();
                context.factorIndex = parser.getIntValue();
                break;
            case "contingencyIndex":
                parser.nextToken();
                context.contingencyIndex = parser.getIntValue();
                break;
            case "value":
                parser.nextToken();
                context.value = parser.getDoubleValue();
                break;
            case "functionReference":
                parser.nextToken();
                context.functionReference = parser.getDoubleValue();
                break;
            default:
                throw new PowsyblException("Unexpected field: " + fieldName);
        }
    }

    public static void writeJson(JsonGenerator generator, SensitivityValue value) {
        writeJson(generator, value.factorIndex, value.contingencyIndex, value.value, value.functionReference);
    }

    static void writeJson(JsonGenerator jsonGenerator,
                          int factorIndex,
                          int contingencyIndex,
                          double value,
                          double functionReference) {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberProperty("factorIndex", factorIndex);
        if (contingencyIndex != -1) {
            jsonGenerator.writeNumberProperty("contingencyIndex", contingencyIndex);
        }
        jsonGenerator.writeNumberProperty("value", value);
        jsonGenerator.writeNumberProperty("functionReference", functionReference);

        jsonGenerator.writeEndObject();
    }
}
