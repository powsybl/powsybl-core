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
 * Elementary result value of sensitivity analysis.
 * Associates a value to a sensitivity factor.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see SensitivityFactor
 */
public class SensitivityValue {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensitivityValue.class);

    private final SensitivityFactor factorContext;

    private final String contingencyId;

    private final double value;

    private final double functionReference;

    public SensitivityValue(SensitivityFactor factorContext, String contingencyId, double value, double functionReference) {
        this.factorContext = factorContext;
        this.contingencyId = contingencyId;
        this.value = value;
        this.functionReference = functionReference;
    }

    public SensitivityFactor getFactor() {
        return factorContext;
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
                "factorContext=" + factorContext +
                ", contingencyId='" + contingencyId + '\'' +
                ", value=" + value +
                ", functionReference=" + functionReference +
                ')';
    }

    static final class ParsingContext {
        private SensitivityFactor factorContext;
        private String contingencyId;
        private double value;
        private double functionReference;

        private void reset() {
            factorContext = null;
            contingencyId = null;
            value = Double.NaN;
            functionReference = Double.NaN;
        }
    }

    public static List<SensitivityValue> parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);

        Stopwatch stopwatch = Stopwatch.createStarted();

        List<SensitivityValue> values = new ArrayList<>();
        try {
            ParsingContext context = new ParsingContext();
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME) {
                    String fieldName = parser.getCurrentName();
                    switch (fieldName) {
                        case "factorContext":
                            parser.nextToken();
                            context.factorContext = SensitivityFactor.parseJson(parser);
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
                } else if (token == JsonToken.END_OBJECT) {
                    values.add(new SensitivityValue(context.factorContext, context.contingencyId, context.value, context.functionReference));
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

    static void writeJson(JsonGenerator generator, Collection<? extends SensitivityValue> valueList) {
        Objects.requireNonNull(valueList);
        try {
            generator.writeStartArray();
            for (SensitivityValue value : valueList) {
                SensitivityValue.writeJson(generator, value.factorContext, value.contingencyId, value.value, value.functionReference);
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
                          SensitivityFactor factorContext,
                          String contingencyId,
                          double value,
                          double functionReference) {
        try {
            jsonGenerator.writeStartObject();

            jsonGenerator.writeFieldName("factorContext");
            SensitivityFactor.writeJson(jsonGenerator, factorContext.getFunctionType(), factorContext.getFunctionId(), factorContext.getVariableType(),
                    factorContext.getVariableId(), factorContext.isVariableSet(), factorContext.getContingencyContext());
            jsonGenerator.writeStringField("contingencyId", contingencyId);
            jsonGenerator.writeNumberField("value", value);
            jsonGenerator.writeNumberField("functionReference", functionReference);

            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
