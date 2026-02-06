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
import com.powsybl.commons.json.JsonUtil;
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

    public static final String VERSION = "1.1";

    public static final String CONTEXT_NAME = "SensitivityAnalysisResult";

    private final List<SensitivityFactor> factors;

    private final List<SensitivityStateStatus> stateStatuses;

    private final List<String> contingencyIds; // to have mapping index -> ID

    private final List<String> operatorStrategyIds; // to have mapping index -> ID

    private final List<SensitivityValue> values;

    private final Map<SensitivityState, List<SensitivityValue>> valuesByState = new HashMap<>();

    private final Map<SensitivityValueKey, SensitivityValue> valuesByKey = new HashMap<>();

    private final Map<Triple<SensitivityFunctionType, SensitivityState, String>, Double> functionReferenceByContingencyAndFunction = new HashMap<>();

    private final Map<SensitivityState, SensitivityStateStatus> statusByState = new HashMap<>();

    public enum Status {
        SUCCESS,
        FAILURE,
        NO_IMPACT
    }

    public static class SensitivityStateStatus {

        private final SensitivityState state;

        private final Status status;

        public SensitivityState getState() {
            return state;
        }

        public Status getStatus() {
            return status;
        }

        public SensitivityStateStatus(SensitivityState state, Status status) {
            this.state = Objects.requireNonNull(state);
            this.status = Objects.requireNonNull(status);
        }

        public static void writeJson(JsonGenerator jsonGenerator, SensitivityStateStatus stateStatus) {
            try {
                jsonGenerator.writeStartObject();
                if (stateStatus.state.contingencyId() != null) {
                    jsonGenerator.writeStringField("contingencyId", stateStatus.state.contingencyId());
                }
                if (stateStatus.state.operatorStrategyId() != null) {
                    jsonGenerator.writeStringField("operatorStrategyId", stateStatus.state.operatorStrategyId());
                }
                jsonGenerator.writeStringField("status", stateStatus.status.name());
                jsonGenerator.writeEndObject();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        static final class ParsingContext {
            private String contingencyId;
            private String operatorStrategyId;
            private Status status;
        }

        public static SensitivityStateStatus parseJson(JsonParser parser, String version) {
            Objects.requireNonNull(parser);

            var context = new SensitivityStateStatus.ParsingContext();
            try {
                JsonToken token;
                while ((token = parser.nextToken()) != null) {
                    if (token == JsonToken.FIELD_NAME) {
                        parseJson(parser, context, version == null ? VERSION : version);
                    } else if (token == JsonToken.END_OBJECT) {
                        return new SensitivityStateStatus(new SensitivityState(context.contingencyId, context.operatorStrategyId),
                                                          context.status);
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            throw new PowsyblException("Parsing error");
        }

        private static void parseJson(JsonParser parser, SensitivityStateStatus.ParsingContext context, String version) throws IOException {
            String fieldName = parser.currentName();
            switch (fieldName) {
                case "contingencyId":
                    parser.nextToken();
                    context.contingencyId = parser.getValueAsString();
                    break;
                case "operatorStrategyId":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: operatorStrategyId", version, "1.1");
                    parser.nextToken();
                    context.operatorStrategyId = parser.getValueAsString();
                    break;
                case "contingencyStatus":
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: contingencyStatus", version, "1.0");
                    parser.nextToken();
                    context.status = Status.valueOf(parser.getValueAsString());
                    break;
                case "status":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: status", version, "1.1");
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
     * @param stateStatuses the list of contingencies and their associated computation status.
     * @param contingencyIds the list of contingency IDs that have been considered during the sensitivity analysis.
     * @param operatorStrategyIds the list of operator strategy IDs that have been considered during the sensitivity analysis.
     * @param values result values of the sensitivity analysis in pre-contingency state and post-contingency states.
     */
    public SensitivityAnalysisResult(List<SensitivityFactor> factors, List<SensitivityStateStatus> stateStatuses, List<String> contingencyIds,
                                     List<String> operatorStrategyIds, List<SensitivityValue> values) {
        this.factors = Collections.unmodifiableList(Objects.requireNonNull(factors));
        this.stateStatuses = Collections.unmodifiableList(Objects.requireNonNull(stateStatuses));
        this.contingencyIds = Collections.unmodifiableList(Objects.requireNonNull(contingencyIds));
        this.operatorStrategyIds = Collections.unmodifiableList(Objects.requireNonNull(operatorStrategyIds));
        this.values = Collections.unmodifiableList(Objects.requireNonNull(values));
        for (SensitivityValue value : values) {
            SensitivityFactor factor = factors.get(value.getFactorIndex());
            String contingencyId = value.getContingencyIndex() != -1 ? contingencyIds.get(value.getContingencyIndex()) : null;
            String operatorStrategyId = value.getOperatorStrategyIndex() != -1 ? operatorStrategyIds.get(value.getOperatorStrategyIndex()) : null;
            SensitivityState state = new SensitivityState(contingencyId, operatorStrategyId);
            valuesByState.computeIfAbsent(state, k -> new ArrayList<>())
                    .add(value);
            valuesByKey.put(new SensitivityValueKey(state, factor.getVariableId(), factor.getFunctionId(), factor.getFunctionType(), factor.getVariableType()), value);
            functionReferenceByContingencyAndFunction.put(Triple.of(factor.getFunctionType(), state, factor.getFunctionId()), value.getFunctionReference());
        }

        for (SensitivityStateStatus stateStatus : stateStatuses) {
            this.statusByState.put(stateStatus.getState(), stateStatus);
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
    public List<SensitivityStateStatus> getStateStatuses() {
        return stateStatuses;
    }

    /**
     * Retrieves the list of contingency IDs.
     *
     * @return a list of contingency IDs as strings.
     */
    public List<String> getContingencyIds() {
        return contingencyIds;
    }

    /**
     * Retrieves the list of operator strategy IDs.
     *
     * @return a list of strings representing the operator strategy IDs.
     */
    public List<String> getOperatorStrategyIds() {
        return operatorStrategyIds;
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
     * @param state the considered state. Use null to get pre-contingency sensitivity values.
     * @return the sensitivity value associated to a given contingency ID.
     */
    public List<SensitivityValue> getValues(SensitivityState state) {
        return valuesByState.getOrDefault(state, Collections.emptyList());
    }

    /**
     * Get a list of all the pre-contingency sensitivity values.
     *
     * @return a list of all the pre-contingency sensitivity values.
     */
    public List<SensitivityValue> getPreContingencyValues() {
        return valuesByState.getOrDefault(SensitivityState.PRE_CONTINGENCY, Collections.emptyList());
    }

    /**
     * Get the sensitivity value associated to a given function id and type and a given variable and for a specific contingency.
     *
     * @param state the considered state. Use null to get a pre-contingency sensitivity value.
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @param functionType the sensitivity function type.
     * @return the sensitivity value associated with a given function and a given variable for a given contingency.
     */
    public double getSensitivityValue(SensitivityState state, String variableId, String functionId, SensitivityFunctionType functionType, SensitivityVariableType variableType) {
        Objects.requireNonNull(state);
        Objects.requireNonNull(variableId);
        Objects.requireNonNull(functionId);
        Objects.requireNonNull(functionType);
        Objects.requireNonNull(variableType);
        SensitivityValue value = valuesByKey.get(new SensitivityValueKey(state, variableId, functionId, functionType, variableType));
        if (value != null) {
            return value.getValue();
        }
        throw new PowsyblException("Sensitivity value not found for contingency '" + state.contingencyId() + "', operator strategy '" + state.operatorStrategyId() + "', function '"
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
        return getSensitivityValue(SensitivityState.postContingency(contingencyId), variableId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, variableType);
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
        return getSensitivityValue(SensitivityState.postContingency(contingencyId), variableId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_2, variableType);
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
        return getSensitivityValue(SensitivityState.postContingency(contingencyId), variableId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_3, variableType);
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
        return getSensitivityValue(SensitivityState.postContingency(contingencyId), variableId, functionId, SensitivityFunctionType.BRANCH_CURRENT_1, variableType);
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
        return getSensitivityValue(SensitivityState.postContingency(contingencyId), variableId, functionId, SensitivityFunctionType.BRANCH_CURRENT_2, variableType);
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
        return getSensitivityValue(SensitivityState.postContingency(contingencyId), variableId, functionId, SensitivityFunctionType.BRANCH_CURRENT_3, variableType);
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
        return getSensitivityValue(SensitivityState.postContingency(contingencyId), variableId, functionId, SensitivityFunctionType.BUS_VOLTAGE, variableType);
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
        return getSensitivityValue(SensitivityState.PRE_CONTINGENCY, variableId, functionId, functionType, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BRANCH_ACTIVE_POWER_1.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBranchFlow1SensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(SensitivityState.PRE_CONTINGENCY, variableId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BRANCH_ACTIVE_POWER_2.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBranchFlow2SensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(SensitivityState.PRE_CONTINGENCY, variableId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_2, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BRANCH_ACTIVE_POWER_3.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBranchFlow3SensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(SensitivityState.PRE_CONTINGENCY, variableId, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_3, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BRANCH_CURRENT_1.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBranchCurrent1SensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(SensitivityState.PRE_CONTINGENCY, variableId, functionId, SensitivityFunctionType.BRANCH_CURRENT_1, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BRANCH_CURRENT_2.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBranchCurrent2SensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(SensitivityState.PRE_CONTINGENCY, variableId, functionId, SensitivityFunctionType.BRANCH_CURRENT_2, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BRANCH_CURRENT_3.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBranchCurrent3SensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(SensitivityState.PRE_CONTINGENCY, variableId, functionId, SensitivityFunctionType.BRANCH_CURRENT_3, variableType);
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state for function type BUS_VOLTAGE.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getBusVoltageSensitivityValue(String variableId, String functionId, SensitivityVariableType variableType) {
        return getSensitivityValue(SensitivityState.PRE_CONTINGENCY, variableId, functionId, SensitivityFunctionType.BUS_VOLTAGE, variableType);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and type.
     *
     * @param state the considered state. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @param functionType sensitivity function type
     * @return the function reference value
     */
    public double getFunctionReferenceValue(SensitivityState state, String functionId, SensitivityFunctionType functionType) {
        Objects.requireNonNull(state);
        Objects.requireNonNull(functionId);
        Objects.requireNonNull(functionType);
        Double value = functionReferenceByContingencyAndFunction.get(Triple.of(functionType, state, functionId));
        if (value == null) {
            throw new PowsyblException("Reference flow value not found for contingency '" + state.contingencyId() + "' and operator strategy '"
                    + state.operatorStrategyId() + "', function '" + functionId + "'" + "', functionType '" + functionType);
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
        return getFunctionReferenceValue(SensitivityState.postContingency(contingencyId), functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_1);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and function type BRANCH_ACTIVE_POWER_2.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getBranchFlow2FunctionReferenceValue(String contingencyId, String functionId) {
        return getFunctionReferenceValue(SensitivityState.postContingency(contingencyId), functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_2);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and function type BRANCH_ACTIVE_POWER_3.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getBranchFlow3FunctionReferenceValue(String contingencyId, String functionId) {
        return getFunctionReferenceValue(SensitivityState.postContingency(contingencyId), functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_3);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and function type BRANCH_CURRENT_1.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getBranchCurrent1FunctionReferenceValue(String contingencyId, String functionId) {
        return getFunctionReferenceValue(SensitivityState.postContingency(contingencyId), functionId, SensitivityFunctionType.BRANCH_CURRENT_1);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and function type BRANCH_CURRENT_2.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getBranchCurrent2FunctionReferenceValue(String contingencyId, String functionId) {
        return getFunctionReferenceValue(SensitivityState.postContingency(contingencyId), functionId, SensitivityFunctionType.BRANCH_CURRENT_2);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and function type BRANCH_CURRENT_3.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getBranchCurrent3FunctionReferenceValue(String contingencyId, String functionId) {
        return getFunctionReferenceValue(SensitivityState.postContingency(contingencyId), functionId, SensitivityFunctionType.BRANCH_CURRENT_3);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function id and function type BUS_VOLTAGE.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getBusVoltageFunctionReferenceValue(String contingencyId, String functionId) {
        return getFunctionReferenceValue(SensitivityState.postContingency(contingencyId), functionId, SensitivityFunctionType.BUS_VOLTAGE);
    }

    /**
     * Get the function reference associated to a given function id and type in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @param functionType sensitivity function type
     * @return the function reference value.
     */
    public double getFunctionReferenceValue(String functionId, SensitivityFunctionType functionType) {
        return getFunctionReferenceValue(SensitivityState.PRE_CONTINGENCY, functionId, functionType);
    }

    /**
     * Get the function reference associated to a given function and function type BRANCH_ACTIVE_POWER_1 in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBranchFlow1FunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(SensitivityState.PRE_CONTINGENCY, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_1);
    }

    /**
     * Get the function reference associated to a given function and function type BRANCH_ACTIVE_POWER_2 in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBranchFlow2FunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(SensitivityState.PRE_CONTINGENCY, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_2);
    }

    /**
     * Get the function reference associated to a given function and function type BRANCH_ACTIVE_POWER_3 in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBranchFlow3FunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(SensitivityState.PRE_CONTINGENCY, functionId, SensitivityFunctionType.BRANCH_ACTIVE_POWER_3);
    }

    /**
     * Get the function reference associated to a given function and function type BRANCH_CURRENT_1 in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBranchCurrent1FunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(SensitivityState.PRE_CONTINGENCY, functionId, SensitivityFunctionType.BRANCH_CURRENT_1);
    }

    /**
     * Get the function reference associated to a given function and function type BRANCH_CURRENT_2 in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBranchCurrent2FunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(SensitivityState.PRE_CONTINGENCY, functionId, SensitivityFunctionType.BRANCH_CURRENT_2);
    }

    /**
     * Get the function reference associated to a given function and function type BRANCH_CURRENT_3 in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBranchCurrent3FunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(SensitivityState.PRE_CONTINGENCY, functionId, SensitivityFunctionType.BRANCH_CURRENT_3);
    }

    /**
     * Get the function reference associated to a given function and function type BUS_VOLTAGE in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getBusVoltageFunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(SensitivityState.PRE_CONTINGENCY, functionId, SensitivityFunctionType.BUS_VOLTAGE);
    }

    /**
     * Get the status associated to a state
     *
     * @param state The state
     * @return The associated status.
     */
    public Status getStateStatus(SensitivityState state) {
        Objects.requireNonNull(state);
        return statusByState.get(state).getStatus();
    }
}
