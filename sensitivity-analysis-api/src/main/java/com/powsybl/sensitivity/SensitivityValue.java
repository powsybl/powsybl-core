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
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Elementary result value of a sensitivity analysis, given the sensitivity factor and a contingency id (use null to get
 * a pre-contingency value). The value is the impact of the variable change on the monitored equipment. The function
 * reference gives the level of the function in the network pre-contingency state.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see SensitivityFactor
 */
public class SensitivityValue {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensitivityValue.class);

    private final SensitivityFactor factor;

    private final String contingencyId;

    private final double value;

    private final double functionReference;

    /**
     * Constructor
     * @param factor the sensitivity factor {@link com.powsybl.sensitivity.SensitivityFactor}
     * @param contingencyId the id of the contingency. Use null for pre-contingency state.
     * @param value the sensitivity value, as a result of the computation.
     * @param functionReference the value of the sensitivity function in the pre-contingency state.
     */
    public SensitivityValue(SensitivityFactor factor, String contingencyId, double value, double functionReference) {
        this.factor = factor;
        this.contingencyId = contingencyId;
        this.value = value;
        this.functionReference = functionReference;
    }

    public SensitivityFactor getFactor() {
        return factor;
    }

    public String getContingencyId() {
        return contingencyId;
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
                "factor=" + factor +
                ", contingencyId='" + contingencyId + '\'' +
                ", value=" + value +
                ", functionReference=" + functionReference +
                ')';
    }

    static final class ParsingContext {
        private SensitivityFactor factor;
        private String contingencyId;
        private double value;
        private double functionReference;

        private void reset() {
            factor = null;
            contingencyId = null;
            value = Double.NaN;
            functionReference = Double.NaN;
        }
    }

    public static SensitivityValue parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);

        var context = new ParsingContext();
        try {
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME) {
                    parseJson(parser, context);
                } else if (token == JsonToken.END_OBJECT) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return new SensitivityValue(context.factor, context.contingencyId, context.value, context.functionReference);
    }

    public static List<SensitivityValue> parseJsonArray(JsonParser parser) {
        Objects.requireNonNull(parser);

        Stopwatch stopwatch = Stopwatch.createStarted();

        List<SensitivityValue> values = new ArrayList<>();
        try {
            ParsingContext context = new ParsingContext();
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME) {
                    parseJson(parser, context);
                } else if (token == JsonToken.END_OBJECT) {
                    values.add(new SensitivityValue(context.factor, context.contingencyId, context.value, context.functionReference));
                    context.reset();
                } else if (token == JsonToken.END_ARRAY) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        stopwatch.stop();
        LOGGER.info("{} values read in {} ms", values.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return values;
    }

    private static void parseJson(JsonParser parser, ParsingContext context) throws IOException {
        String fieldName = parser.getCurrentName();
        switch (fieldName) {
            case "factor":
                parser.nextToken();
                context.factor = SensitivityFactor.parseJson(parser);
                break;
            case "contingencyId":
                context.contingencyId = parser.nextTextValue();
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
                break;
        }
    }

    public static void writeJson(JsonGenerator generator, SensitivityValue value) {
        writeJson(generator, value.contingencyId, value.getFactor().getVariableId(), value.getFactor().getFunctionId(), value.value, value.functionReference);
    }

    static void writeJson(JsonGenerator generator, Collection<? extends SensitivityValue> valueList) {
        Objects.requireNonNull(valueList);
        try {
            generator.writeStartArray();
            for (SensitivityValue value : valueList) {
                writeJson(generator, value);
            }
            generator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void writeJson(Writer writer, List<? extends SensitivityValue> valueList) {
        JsonUtil.writeJson(writer, generator -> writeJson(generator, valueList));
    }

    static void writeJson(JsonGenerator jsonGenerator,
                          String contingencyId,
                          String variableId,
                          String functionId,
                          double value,
                          double functionReference) {
        try {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("contingencyId", contingencyId);
            jsonGenerator.writeStringField("variableId", variableId);
            jsonGenerator.writeStringField("functionId", functionId);
            jsonGenerator.writeNumberField("value", value);
            jsonGenerator.writeNumberField("functionReference", functionReference);

            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
