/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.Contingency;
import org.jgrapht.alg.util.Triple;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * Sensitivity analysis result
 *
 * <p>
 *     Composed of a list of sensitivity values in pre-contingency and post-contingency states.
 * </p>
 *
 * Sensitivity analysis is used to assess the impact of a small modification of a network variables on the value of
 * network functions. A combination of a variable and a function is called a sensitivity factor. It returns
 * the sensitivity values for each factor, and the reference values for each function. The sensitivity analysis API
 * offers the possibility to calculate the sensitivities on a set of contingencies besides the pre-contingency state.
 * The full set of results consists of:
 *  - the list of factors
 *  - the list of contingencies and their associated computation status
 *  - the list of sensitivity values in pre-contingency and post-contingency states
 *  - the list of function reference values in pre-contingency and post-contingency states.
 *  A sensitivity analysis result offers a set of methods to retrieve sensitivity values or function reference values.
 *  For example, you can retrieve a sensitivity value as a double given the ID of a contingency, the ID of a variable
 *  and the ID of a function.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @see SensitivityValue
 */
public class SensitivityAnalysisResult {

    private final List<SensitivityFactor> factors;

    private final List<SensitivityContingencyStatus> contingencyStatuses;

    private final List<SensitivityValue> values;

    private final Map<String, List<SensitivityValue>> valuesByContingencyId = new HashMap<>();

    private final Map<SensitivityValueKey, SensitivityValue> valuesByContingencyIdAndFunctionAndVariable = new HashMap<>();

    private final Map<Triple<SensitivityFunctionType, String, String>, Double> functionReferenceByContingencyAndFunction = new HashMap<>();

    private final Map<String, SensitivityContingencyStatus> statusByContingencyId = new HashMap<>();

    public enum Status {
        SUCCESS,
        FAILURE,
        NO_IMPACT
    }

    public static class SensitivityContingencyStatus {

        private final String contingencyId;

        private final Status status;

        public String getContingencyId() {
            return contingencyId;
        }

        public Status getStatus() {
            return status;
        }

        public SensitivityContingencyStatus(String contingencyId, Status status) {
            this.contingencyId = Objects.requireNonNull(contingencyId);
            this.status = Objects.requireNonNull(status);
        }

        public static void writeJson(JsonGenerator jsonGenerator, SensitivityContingencyStatus contingencyStatus) {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("contingencyId", contingencyStatus.getContingencyId());
                jsonGenerator.writeStringField("contingencyStatus", contingencyStatus.status.name());
                jsonGenerator.writeEndObject();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        static final class ParsingContext {
            private Contingency contingency;
            private Status status;
        }

        public static SensitivityContingencyStatus parseJson(JsonParser parser) {
            Objects.requireNonNull(parser);

            var context = new SensitivityContingencyStatus.ParsingContext();
            try {
                JsonToken token;
                while ((token = parser.nextToken()) != null) {
                    if (token == JsonToken.FIELD_NAME) {
                        parseJson(parser, context);
                    } else if (token == JsonToken.END_OBJECT) {
                        return new SensitivityContingencyStatus(context.contingency != null ? context.contingency.getId() : "", context.status);
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            throw new PowsyblException("Parsing error");
        }

        private static void parseJson(JsonParser parser, SensitivityContingencyStatus.ParsingContext context) throws IOException {
            String fieldName = parser.currentName();
            switch (fieldName) {
                case "contingencyId":
                    parser.nextToken();
                    context.contingency = new Contingency(parser.getValueAsString());
                    break;
                case "contingencyStatus":
                    parser.nextToken();
                    context.status = Status.valueOf(parser.getValueAsString());
                    break;
                default:
                    throw new PowsyblException("Unexpected field: " + fieldName);
            }
        }
    }

    /**
     * Sensitivity analysis result
     * @param factors the list of sensitivity factors that have been computed.
     * @param contingencyStatuses the list of contingencies and their associated computation status.
     * @param values result values of the sensitivity analysis in pre-contingency state and post-contingency states.
     */
    public SensitivityAnalysisResult(List<SensitivityFactor> factors, List<SensitivityContingencyStatus> contingencyStatuses, List<SensitivityValue> values) {
        this.factors = Collections.unmodifiableList(Objects.requireNonNull(factors));
        this.contingencyStatuses = Collections.unmodifiableList(Objects.requireNonNull(contingencyStatuses));
        this.values = Collections.unmodifiableList(Objects.requireNonNull(values));
        for (SensitivityValue value : values) {
            SensitivityFactor factor = factors.get(value.getFactorIndex());
            String contingencyId = value.getContingencyIndex() != -1 ? contingencyStatuses.get(value.getContingencyIndex()).getContingencyId() : null;
            valuesByContingencyId.computeIfAbsent(contingencyId, k -> new ArrayList<>())
                    .add(value);
            valuesByContingencyIdAndFunctionAndVariable.put(new SensitivityValueKey(contingencyId, factor.getVariableId(), factor.getFunctionId(), factor.getFunctionType(), factor.getVariableType()), value);
            functionReferenceByContingencyAndFunction.put(Triple.of(factor.getFunctionType(), contingencyId, factor.getFunctionId()), value.getFunctionReference());
        }

        for (SensitivityContingencyStatus status : contingencyStatuses) {
            this.statusByContingencyId.put(status.getContingencyId(), status);
        }
    }

    /**
     * Get a list of all the sensitivity factors.
     *
     * @return a list of all the sensitivity factors.
     */
    public List<SensitivityFactor> getFactors() {
        return factors;
    }

    /**
     * Get a list of all the contingency statuses.
     *
     * @return a list of all the contingency statuses.
     */
    public List<SensitivityContingencyStatus> getContingencyStatuses() {
        return contingencyStatuses;
    }

    /**
     * Get a list of all the sensitivity values.
     *
     * @return a list of all the sensitivity values.
     */
    public List<SensitivityValue> getValues() {
        return values;
    }

    /**
     * Get a list of sensitivity value associated to a given contingency id
     *
     * @param contingencyId the ID of the considered contingency. Use null to get pre-contingency sensitivity values.
     * @return the sensitivity value associated to a given contingency ID.
     */
    public List<SensitivityValue> getValues(String contingencyId) {
        return valuesByContingencyId.getOrDefault(contingencyId, Collections.emptyList());
    }

    /**
     * Get a list of all the pre-contingency sensitivity values.
     *
     * @return a list of all the pre-contingency sensitivity values.
     */
    public List<SensitivityValue> getPreContingencyValues() {
        return valuesByContingencyId.getOrDefault(null, Collections.emptyList());
    }

    /**
     * Get the sensitivity value associated to a given function id and type and a given variable and for a specific contingency.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency sensitivity value.
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @param functionType the sensitivity function type.
     * @return the sensitivity value associated with a given function and a given variable for a given contingency.
     */
    public double getSensitivityValue(String contingencyId, String variableId, String functionId, SensitivityFunctionType functionType, SensitivityVariableType variableType) {
        SensitivityValue value = valuesByContingencyIdAndFunctionAndVariable.get(new SensitivityValueKey(contingencyId, variableId, functionId, functionType, variableType));
        if (value != null) {
            return value.getValue();
        }
        throw new PowsyblException("Sensitivity value not found for contingency '" + contingencyId + "', function '"
                                   + functionId + "', variable '" + variableId + "'" + "', functionType '" + functionType);
    }

    /**
     * Get the sensitivity value associated to a given function id and a given variable and for a specific contingency for function type BRANCH_ACTIVE_POWER_1.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency sensitivity value.
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable for a given contingency.
     */
    public double getBranchFlow1SensitivityValue(String contingencyId, String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(contingencyId, variableId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function id and a given variable and for a specific contingency for function type BRANCH_ACTIVE_POWER_2.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency sensitivity value.
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable for a given contingency.
     */
    public double getBranchFlow2SensitivityValue(String contingencyId, String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(contingencyId, variableId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_2, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function id and a given variable and for a specific contingency for function type BRANCH_ACTIVE_POWER_3.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency sensitivity value.
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable for a given contingency.
     */
    public double getBranchFlow3SensitivityValue(String contingencyId, String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(contingencyId, variableId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_3, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function id and a given variable and for a specific contingency for function type BRANCH_CURRENT_1.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency sensitivity value.
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable for a given contingency.
     */
    public double getBranchCurrent1SensitivityValue(String contingencyId, String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(contingencyId, variableId, functionId, SensitivityFunctionType.BRANCH_CURRENT_1, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function id and a given variable and for a specific contingency for function type BRANCH_CURRENT_2.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency sensitivity value.
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable for a given contingency.
     */
    public double getBranchCurrent2SensitivityValue(String contingencyId, String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(contingencyId, variableId, functionId, SensitivityFunctionType.BRANCH_CURRENT_2, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function id and a given variable and for a specific contingency for function type BRANCH_CURRENT_3.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency sensitivity value.
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable for a given contingency.
     */
    public double getBranchCurrent3SensitivityValue(String contingencyId, String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(contingencyId, variableId, functionId, SensitivityFunctionType.BRANCH_CURRENT_3, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function id and a given variable and for a specific contingency for function type BUS_VOLTAGE.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency sensitivity value.
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable for a given contingency.
     */
    public double getBusVoltageSensitivityValue(String contingencyId, String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(contingencyId, variableId, functionId, SensitivityFunctionType.BUS_VOLTAGE, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function id and type and a given variable in pre-contingency state.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @param functionType sensitivity function type
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getSensitivityValue(String variableId, String functionId, SensitivityFunctionType functionType, SensitivityVariableType variableType) {
        return getSensitivityValue(null, variableId, functionId, functionType, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BRANCH_ACTIVE_POWER_1.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBranchFlow1SensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(null, variableId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BRANCH_ACTIVE_POWER_2.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBranchFlow2SensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(null, variableId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_2, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BRANCH_ACTIVE_POWER_3.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBranchFlow3SensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(null, variableId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_3, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BRANCH_CURRENT_1.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBranchCurrent1SensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(null, variableId, functionId, SensitivityFunctionType.BRANCH_CURRENT_1, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BRANCH_CURRENT_2.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBranchCurrent2SensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(null, variableId, functionId, SensitivityFunctionType.BRANCH_CURRENT_2, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BRANCH_CURRENT_3.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBranchCurrent3SensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(null, variableId, functionId, SensitivityFunctionType.BRANCH_CURRENT_3, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BUS_VOLTAGE.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBusVoltageSensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(null, variableId, functionId, SensitivityFunctionType.BUS_VOLTAGE, variableType);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and type.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @param functionType sensitivity function type
     * @return the function reference value
     */
    public double getFunctionReferenceValue(String contingencyId, String functionId, SensitivityFunctionType functionType) {
        Double value = functionReferenceByContingencyAndFunction.get(Triple.of(functionType, contingencyId, functionId));
        if (value == null) {
            throw new PowsyblException("Reference flow value not found for contingency '" + contingencyId + "', function '" + functionId + "'"
                                       + "', functionType '" + functionType);
        }
        return value;
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and function type BRANCH_ACTIVE_POWER_1.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getBranchFlow1FunctionReferenceValue(String contingencyId, String functionId) {
        return getFunctionReferenceValue(contingencyId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_1);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and function type BRANCH_ACTIVE_POWER_2.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getBranchFlow2FunctionReferenceValue(String contingencyId, String functionId) {
        return getFunctionReferenceValue(contingencyId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_2);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and function type BRANCH_ACTIVE_POWER_3.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getBranchFlow3FunctionReferenceValue(String contingencyId, String functionId) {
        return getFunctionReferenceValue(contingencyId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_3);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and function type BRANCH_CURRENT_1.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getBranchCurrent1FunctionReferenceValue(String contingencyId, String functionId) {
        return getFunctionReferenceValue(contingencyId, functionId, SensitivityFunctionType.BRANCH_CURRENT_1);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and function type BRANCH_CURRENT_2.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getBranchCurrent2FunctionReferenceValue(String contingencyId, String functionId) {
        return getFunctionReferenceValue(contingencyId, functionId, SensitivityFunctionType.BRANCH_CURRENT_2);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and function type BRANCH_CURRENT_3.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getBranchCurrent3FunctionReferenceValue(String contingencyId, String functionId) {
        return getFunctionReferenceValue(contingencyId, functionId, SensitivityFunctionType.BRANCH_CURRENT_3);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and function type BUS_VOLTAGE.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getBusVoltageFunctionReferenceValue(String contingencyId, String functionId) {
        return getFunctionReferenceValue(contingencyId, functionId, SensitivityFunctionType.BUS_VOLTAGE);
    }

    /**
     * Get the function reference associated to a given function id and type in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @param functionType sensitivity function type
     * @return the function reference value.
     */
    public double getFunctionReferenceValue(String functionId, SensitivityFunctionType functionType) {
        return getFunctionReferenceValue(null, functionId, functionType);
    }

    /**
     * Get the function reference associated to a given function and function type BRANCH_ACTIVE_POWER_1 in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBranchFlow1FunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(null, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_1);
    }

    /**
     * Get the function reference associated to a given function and function type BRANCH_ACTIVE_POWER_2 in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBranchFlow2FunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(null, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_2);
    }

    /**
     * Get the function reference associated to a given function and function type BRANCH_ACTIVE_POWER_3 in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBranchFlow3FunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(null, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_3);
    }

    /**
     * Get the function reference associated to a given function and function type BRANCH_CURRENT_1 in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBranchCurrent1FunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(null, functionId, SensitivityFunctionType.BRANCH_CURRENT_1);
    }

    /**
     * Get the function reference associated to a given function and function type BRANCH_CURRENT_2 in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBranchCurrent2FunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(null, functionId, SensitivityFunctionType.BRANCH_CURRENT_2);
    }

    /**
     * Get the function reference associated to a given function and function type BRANCH_CURRENT_3 in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBranchCurrent3FunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(null, functionId, SensitivityFunctionType.BRANCH_CURRENT_3);
    }

    /**
     * Get the function reference associated to a given function and function type BUS_VOLTAGE in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBusVoltageFunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(null, functionId, SensitivityFunctionType.BUS_VOLTAGE);
    }

    /**
     * Get the status associated to a contingency id
     *
     * @param contingencyId The contingency id
     * @return The associated status.
     */
    public Status getContingencyStatus(String contingencyId) {
        return statusByContingencyId.get(contingencyId).getStatus();
    }
}
