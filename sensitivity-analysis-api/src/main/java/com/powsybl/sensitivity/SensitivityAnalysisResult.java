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
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Sensitivity analysis result
 *
 * <p>
 *     Composed of a list of sensitivity values in pre-contingency and post-contingency states.
 * </p>
 *
 * A single sensitivity analysis should return, besides its status and some stats on the
 * analysis itself, all the sensitivity values for each factor (combination of a monitored equipment or bus and a specific
 * equipment or group of equipments). The chosen sensitivity provider offers the possibility to calculate the sensitivity
 * on a set of contingencies besides the pre-contingency state.
 * Note that the analysis is launched only once, but the solver itself modifies the matrix for each state of the network
 * to output a full set of results. In the sensitivity API, it has been allowed to provide a list of contingencies as an input,
 * which then triggers such a sensitivity analysis.
 * The full set of results consists of :
 *  - the list of sensitivity values in pre-contingency and post-contingency states.
 *  - some metadata (status, stats, logs)
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see SensitivityValue
 */
public class SensitivityAnalysisResult {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensitivityAnalysisResult.class);

    private final List<SensitivityValue> values;

    private final Map<String, List<SensitivityValue>> valuesByContingencyId = new HashMap<>();

    private final Map<Triple<String, String, String>, SensitivityValue> valuesByContingencyIdAndFunctionIdAndVariableId = new HashMap<>();

    private final Map<Pair<String, String>, Double> functionReferenceByContingencyAndFunctionId = new HashMap<>();

    /**
     * Sensitivity analysis result
     * @param values result values of the sensitivity analysis in pre-contingency state and post-contingency states.
     */
    public SensitivityAnalysisResult(List<SensitivityValue> values) {
        this.values = Objects.requireNonNull(values);
        for (SensitivityValue value : values) {
            SensitivityFactor factor = value.getFactor();
            valuesByContingencyId.computeIfAbsent(value.getContingencyId(), k -> new ArrayList<>())
                    .add(value);
            valuesByContingencyIdAndFunctionIdAndVariableId.put(Triple.of(value.getContingencyId(), factor.getFunctionId(), factor.getVariableId()), value);
            functionReferenceByContingencyAndFunctionId.put(Pair.of(value.getContingencyId(), factor.getFunctionId()), value.getFunctionReference());
        }
    }

    /**
     * Get a collection of all the sensitivity values.
     *
     * @return a collection of all the sensitivity values.
     */
    public List<SensitivityValue> getValues() {
        return values;
    }

    /**
     * Get a collection of sensitivity value associated with given contingency id
     *
     * @param contingencyId the ID of the considered contingency
     * @return the sensitivity value associated with a given contingency ID.
     */
    public List<SensitivityValue> getValues(String contingencyId) {
        return valuesByContingencyId.getOrDefault(contingencyId, Collections.emptyList());
    }

    /**
     * Get a collection of pre-contingency sensitivity value
     *
     * @return pre-contingency sensitivity values.
     */
    public List<SensitivityValue> getPreContingencyValues() {
        return valuesByContingencyId.getOrDefault(null, Collections.emptyList());
    }

    /**
     * Get the sensitivity value associated with a given function and a given variable for a specific contingency.
     *
     * @param contingencyId the id of the considered contingency
     * @param functionId sensitivity function id
     * @param variableId sensitivity variable id
     * @return the sensitivity value associated with a given function and a given variable for a given contingency
     */
    public SensitivityValue getValue(String contingencyId, String functionId, String variableId) {
        return valuesByContingencyIdAndFunctionIdAndVariableId.get(Triple.of(contingencyId, functionId, variableId));
    }

    /**
     * Get the function reference associated with a given contingency Id and a given function.
     *
     * @param contingencyId the id of the considered contingency
     * @param functionId sensitivity function id
     * @return the function reference value
     */
    public double getFunctionReferenceValue(String contingencyId, String functionId) {
        return functionReferenceByContingencyAndFunctionId.get(Pair.of(contingencyId, functionId));
    }

    /**
     * Get the status of the presence of contingencies
     *
     * @return true if the analysis contains contingencies, false otherwise
     */
    public boolean contingenciesArePresent() {
        return !valuesByContingencyId.isEmpty();
    }

    /**
     * Get a collection of all the sensitivity values for all contingencies.
     *
     * @return a collection of all the sensitivity values for all contingencies.
     */
    public Map<String, List<SensitivityValue>> getValuesByContingencyId() {
        return valuesByContingencyId;
    }

    public static SensitivityAnalysisResult empty() {
        return new SensitivityAnalysisResult(Collections.emptyList());
    }

    static final class ParsingContext {
        private List<SensitivityValue> values;
    }

    public static SensitivityAnalysisResult parseJson(JsonParser jsonParser) {
        Objects.requireNonNull(jsonParser);

        var stopwatch = Stopwatch.createStarted();

        var context = new ParsingContext();
        try {
            JsonToken token;
            while ((token = jsonParser.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME) {
                    String fieldName = jsonParser.getCurrentName();
                    switch (fieldName) {
                        case "values":
                            jsonParser.nextToken();
                            context.values = SensitivityValue.parseJsonArray(jsonParser);
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
        LOGGER.info("result read in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return new SensitivityAnalysisResult(context.values);
    }

    public static SensitivityAnalysisResult readJson(Reader reader) {
        return JsonUtil.parseJson(reader, SensitivityAnalysisResult::parseJson);
    }

    public static void writeJson(Writer writer, SensitivityAnalysisResult result) {
        JsonUtil.writeJson(writer, generator -> writeJson(generator, result));
    }

    public static void writeJson(JsonGenerator jsonGenerator, SensitivityAnalysisResult result) {
        try {
            jsonGenerator.writeStartObject();

            jsonGenerator.writeFieldName("values");
            SensitivityValue.writeJson(jsonGenerator, result.getValues());

            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
