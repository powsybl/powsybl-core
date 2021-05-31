/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Stopwatch;
import com.powsybl.commons.json.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityFactor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensitivityFactor.class);

    private final SensitivityFunctionType functionType;

    private final String functionId;

    private final SensitivityVariableType variableType;

    private final String variableId;

    private final boolean variableSet;

    private final ContingencyContext contingencyContext;

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

    static void writeJson(JsonGenerator generator, List<? extends SensitivityFactor> factorList) {
        Objects.requireNonNull(factorList);
        try {
            generator.writeStartArray();
            for (SensitivityFactor factor : factorList) {
                SensitivityFactor.writeJson(generator, factor.getFunctionType(), factor.getFunctionId(), factor.getVariableType(),
                        factor.getVariableId(), factor.isVariableSet(), factor.getContingencyContext());
            }
            generator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void writeJson(Writer writer, List<? extends SensitivityFactor> factorList) {
        JsonUtil.writeJson(writer, generator -> writeJson(generator, factorList));
    }

    static void writeJson(JsonGenerator jsonGenerator, SensitivityFunctionType functionType, String functionId, SensitivityVariableType variableType,
                          String variableId, boolean variableSet, ContingencyContext contingencyContext) {
        try {
            jsonGenerator.writeStartObject();

            jsonGenerator.writeStringField("functionType", functionType.name());
            jsonGenerator.writeStringField("functionId", functionId);
            jsonGenerator.writeStringField("variableType", variableType.name());
            jsonGenerator.writeStringField("variableId", variableId);
            jsonGenerator.writeBooleanField("variableSet", variableSet);
            jsonGenerator.writeStringField("contingencyContextType", contingencyContext.getContextType().name());
            if (contingencyContext.getContingencyId() != null) {
                jsonGenerator.writeStringField("contingencyId", contingencyContext.getContingencyId());
            }

            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static final class ParsingContext {
        private SensitivityFunctionType functionType;
        private String functionId;
        private SensitivityVariableType variableType;
        private String variableId;
        private Boolean variableSet;
        private ContingencyContextType contingencyContextType;
        private String contingencyId;

        private void reset() {
            functionType = null;
            functionId = null;
            variableType = null;
            variableId = null;
            variableSet = null;
            contingencyContextType = null;
            contingencyId = null;
        }
    }

    public static List<SensitivityFactor> parseMultipleJson(JsonParser parser) {
        Objects.requireNonNull(parser);

        Stopwatch stopwatch = Stopwatch.createStarted();

        List<SensitivityFactor> factors = new ArrayList<>();
        try {
            ParsingContext context = new ParsingContext();
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME) {
                    String fieldName = parser.getCurrentName();
                    switch (fieldName) {
                        case "functionType":
                            context.functionType = SensitivityFunctionType.valueOf(parser.nextTextValue());
                            break;
                        case "functionId":
                            context.functionId = parser.nextTextValue();
                            break;
                        case "variableType":
                            context.variableType = SensitivityVariableType.valueOf(parser.nextTextValue());
                            break;
                        case "variableId":
                            context.variableId = parser.nextTextValue();
                            break;
                        case "variableSet":
                            context.variableSet = parser.nextBooleanValue();
                            break;
                        case "contingencyContextType":
                            context.contingencyContextType = ContingencyContextType.valueOf(parser.nextTextValue());
                            break;
                        case "contingencyId":
                            context.contingencyId = parser.nextTextValue();
                            break;
                        default:
                            break;
                    }
                } else if (token == JsonToken.END_OBJECT) {
                    factors.add(new SensitivityFactor(context.functionType, context.functionId, context.variableType, context.variableId, context.variableSet,
                            new ContingencyContext(context.contingencyContextType, context.contingencyId)));
                    context.reset();
                } else if (token == JsonToken.END_ARRAY) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        stopwatch.stop();
        LOGGER.info("{} factors read in {} ms", factors.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return factors;
    }

    public static SensitivityFactor parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);

        Stopwatch stopwatch = Stopwatch.createStarted();

        ParsingContext context = new ParsingContext();
        try {
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME) {
                    String fieldName = parser.getCurrentName();
                    switch (fieldName) {
                        case "functionType":
                            context.functionType = SensitivityFunctionType.valueOf(parser.nextTextValue());
                            break;
                        case "functionId":
                            context.functionId = parser.nextTextValue();
                            break;
                        case "variableType":
                            context.variableType = SensitivityVariableType.valueOf(parser.nextTextValue());
                            break;
                        case "variableId":
                            context.variableId = parser.nextTextValue();
                            break;
                        case "variableSet":
                            context.variableSet = parser.nextBooleanValue();
                            break;
                        case "contingencyContextType":
                            context.contingencyContextType = ContingencyContextType.valueOf(parser.nextTextValue());
                            break;
                        case "contingencyId":
                            context.contingencyId = parser.nextTextValue();
                            break;
                        default:
                            break;
                    }
                } else if (token == JsonToken.END_OBJECT) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        stopwatch.stop();
        LOGGER.info("factor read in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return new SensitivityFactor(context.functionType, context.functionId, context.variableType, context.variableId, context.variableSet,
                new ContingencyContext(context.contingencyContextType, context.contingencyId));
    }
}
