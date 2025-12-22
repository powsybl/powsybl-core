/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.ContingencyContextType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Sensitivity factor to be computed in the sensitivity analysis.
 * It regroups in a single object a description of the variable to modify, a description of the function to monitor
 * and a contingency context. A factor corresponds to the definition of a partial derivative to be extracted from the
 * network in a given contingency context. Usually we compute the impact of an injection increase on a branch flow or current,
 * the impact of a shift of a phase tap changer on a branch flow or current or the impact of a voltage target increase on a bus voltage.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SensitivityFactor {

    private final SensitivityFunctionType functionType;

    private final String functionId;

    private final SensitivityVariableType variableType;

    private final String variableId;

    private final boolean variableSet;

    private final ContingencyContext contingencyContext;

    /**
     * Constructor.
     *
     * @param functionType see {@link com.powsybl.sensitivity.SensitivityFunctionType}
     * @param functionId the id of the equipment to monitor (in general the id of a branch). For BUS_VOLTAGE type, see
     * {@link com.powsybl.iidm.network.IdBasedBusRef}
     * @param variableType see {@link com.powsybl.sensitivity.SensitivityVariableType}
     * @param variableId id of the equipment affected by the injection increase, the angle sift, the voltage target
     *                   increase or the active power set point increase.
     * @param variableSet boolean to says if the variable is a variable set or not
     * @param contingencyContext see {@link com.powsybl.contingency.ContingencyContext}
     */
    public SensitivityFactor(SensitivityFunctionType functionType, String functionId, SensitivityVariableType variableType,
                              String variableId, boolean variableSet, ContingencyContext contingencyContext) {
        this.functionType = Objects.requireNonNull(functionType);
        this.functionId = Objects.requireNonNull(functionId);
        this.variableType = Objects.requireNonNull(variableType);
        this.variableId = Objects.requireNonNull(variableId);
        this.variableSet = variableSet;
        this.contingencyContext = Objects.requireNonNull(contingencyContext);
    }

    public SensitivityFunctionType getFunctionType() {
        return functionType;
    }

    public String getFunctionId() {
        return functionId;
    }

    public SensitivityVariableType getVariableType() {
        return variableType;
    }

    public String getVariableId() {
        return variableId;
    }

    public boolean isVariableSet() {
        return variableSet;
    }

    public ContingencyContext getContingencyContext() {
        return contingencyContext;
    }

    @Override
    public String toString() {
        return "SensitivityFactor(" +
                "functionType=" + functionType +
                ", functionId='" + functionId + '\'' +
                ", variableType=" + variableType +
                ", variableId='" + variableId + '\'' +
                ", variableSet=" + variableSet +
                ", contingencyContext=" + contingencyContext +
                ')';
    }

    public static void writeJson(JsonGenerator jsonGenerator, SensitivityFactor factor) {
        writeJson(jsonGenerator, factor.getFunctionType(), factor.getFunctionId(), factor.getVariableType(),
                factor.getVariableId(), factor.isVariableSet(), factor.getContingencyContext());
    }

    public static void writeJson(JsonGenerator jsonGenerator, SensitivityFunctionType functionType, String functionId, SensitivityVariableType variableType,
                          String variableId, boolean variableSet, ContingencyContext contingencyContext) {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("functionType", functionType.name());
        jsonGenerator.writeStringProperty("functionId", functionId);
        jsonGenerator.writeStringProperty("variableType", variableType.name());
        jsonGenerator.writeStringProperty("variableId", variableId);
        jsonGenerator.writeBooleanProperty("variableSet", variableSet);
        jsonGenerator.writeStringProperty("contingencyContextType", contingencyContext.getContextType().name());
        if (contingencyContext.getContingencyId() != null) {
            jsonGenerator.writeStringProperty("contingencyId", contingencyContext.getContingencyId());
        }

        jsonGenerator.writeEndObject();
    }

    static final class ParsingContext {
        SensitivityFunctionType functionType;
        String functionId;
        SensitivityVariableType variableType;
        String variableId;
        Boolean variableSet;
        ContingencyContextType contingencyContextType;
        String contingencyId;

        void reset() {
            functionType = null;
            functionId = null;
            variableType = null;
            variableId = null;
            variableSet = null;
            contingencyContextType = null;
            contingencyId = null;
        }
    }

    public static SensitivityFactor parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);

        var context = new ParsingContext();
        try {
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.PROPERTY_NAME) {
                    parseJson(parser, context);
                } else if (token == JsonToken.END_OBJECT) {
                    boolean variableSet = Objects.requireNonNull(context.variableSet, "Parameter variableSet is missing");
                    return new SensitivityFactor(context.functionType, context.functionId, context.variableType, context.variableId, variableSet,
                            new ContingencyContext(context.contingencyId, context.contingencyContextType));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        throw new PowsyblException("Parsing error");
    }

    static void parseJson(JsonParser parser, ParsingContext context) throws IOException {
        String fieldName = parser.currentName();
        switch (fieldName) {
            case "functionType":
                context.functionType = SensitivityFunctionType.valueOf(parser.nextStringValue());
                break;
            case "functionId":
                context.functionId = parser.nextStringValue();
                break;
            case "variableType":
                context.variableType = SensitivityVariableType.valueOf(parser.nextStringValue());
                break;
            case "variableId":
                context.variableId = parser.nextStringValue();
                break;
            case "variableSet":
                context.variableSet = parser.nextBooleanValue();
                break;
            case "contingencyContextType":
                context.contingencyContextType = ContingencyContextType.valueOf(parser.nextStringValue());
                break;
            case "contingencyId":
                context.contingencyId = parser.nextStringValue();
                break;
            default:
                throw new PowsyblException("Unexpected field: " + fieldName);
        }
    }

    public static List<SensitivityFactor> createMatrix(SensitivityFunctionType functionType, Collection<String> functionIds,
                                                       SensitivityVariableType variableType, Collection<String> variableIds,
                                                       boolean variableSet, ContingencyContext contingencyContext) {
        return functionIds.stream().flatMap(functionId -> variableIds.stream().map(variableId -> new SensitivityFactor(functionType, functionId, variableType, variableId, variableSet, contingencyContext)))
                .collect(Collectors.toList());
    }
}
