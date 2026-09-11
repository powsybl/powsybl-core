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
import com.powsybl.loadflow.LoadFlowResult;
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
 *  - the list of states (contingency + optional operator strategy) and their associated computation status
 *  - the list of sensitivity values in pre-contingency and post-contingency states
 *  - the list of function reference values in pre-contingency and post-contingency states.
 *  A sensitivity analysis result offers a set of methods to retrieve sensitivity values or function reference values.
 *  For example, you can retrieve a sensitivity value as a double given the ID of a contingency, the ID of a variable
 *  and the ID of a function.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 * @see SensitivityValue
 */
public class SensitivityAnalysisResult {

    public static final String VERSION = "1.2";

    public static final String CONTEXT_NAME = "SensitivityAnalysisResult";

    private final List<SensitivityFactor> factors;

    private final List<SensitivityStateStatus> stateStatuses;

    private final List<String> contingencyIds; // mapping index -> ID

    private final List<String> operatorStrategyIds; // mapping index -> ID

    private final List<SensitivityValue> values;

    private final Map<SensitivityState, List<SensitivityValue>> valuesByState = new HashMap<>();

    private final Map<SensitivityValueKey, SensitivityValue> valuesByKey = new HashMap<>();

    private final Map<Triple<SensitivityFunctionType, SensitivityState, String>, Double> functionReferenceByContingencyAndFunction = new HashMap<>();

    private final Map<SensitivityState, SensitivityStateStatus> statusByState = new HashMap<>();

    private final boolean computationComplete;

    /**
     * The load flow status reported for a given component.
     * @param status the load flow component status
     * @param statusText the human-readable description of the status
     */
    public record LoadFlowStatus(LoadFlowResult.ComponentResult.Status status, String statusText) {
    }

    public enum Status {
        SUCCESS,
        FAILURE,
        NO_IMPACT
    }

    public static class SensitivityStateStatus {

        public record ComponentStatus(LoadFlowStatus status, int numCC, int numSC) { }

        static final String COMPONENTS_LOADFLOW_STATUSES = "componentsLoadFlowStatuses";
        static final String LOAD_FLOW_STATUS = "loadFlowStatus";
        static final String LOAD_FLOW_STATUS_DESCRIPTION = "loadFlowStatusDescription";
        static final String NUM_CC = "numCC";
        static final String NUM_CS = "numCS";

        private final SensitivityState state;

        /**
         * Per-component load flow status (one entry per (numCC, numCS) for which a load flow has run).
         */
        private final List<ComponentStatus> componentsLoadFlowStatusList;

        public SensitivityState getState() {
            return state;
        }

        @Deprecated(since = "7.4.0")
        public Status getStatus() {
            if (!getComponentsLoadFlowStatusList().isEmpty()) {
                switch (getComponentsLoadFlowStatusList().get(0).status().status) {
                    case CONVERGED -> {
                        return Status.SUCCESS;
                    }
                    case NO_CALCULATION -> {
                        return Status.NO_IMPACT;
                    }
                    default -> {
                        return Status.FAILURE;
                    }
                }
            } else {
                return Status.FAILURE;
            }
        }

        public List<ComponentStatus> getComponentsLoadFlowStatusList() {
            return componentsLoadFlowStatusList;
        }

        public SensitivityStateStatus(SensitivityState state, List<ComponentStatus> statusList) {
            this.state = Objects.requireNonNull(state);
            this.componentsLoadFlowStatusList = new ArrayList<>(statusList);
        }

        public SensitivityStateStatus(SensitivityState state) {
            this(state, Collections.emptyList());
        }

        @Deprecated(since = "7.4.0")
        public SensitivityStateStatus(SensitivityState state, Status status) {
            this(state, List.of(new ComponentStatus(
                    new LoadFlowStatus(toLoadFlowStatus(status), ""), -1, -1)));
        }

        private static LoadFlowResult.ComponentResult.Status toLoadFlowStatus(Status status) {
            switch (status) {
                case SUCCESS -> {
                    return LoadFlowResult.ComponentResult.Status.CONVERGED;
                }
                case NO_IMPACT -> {
                    return LoadFlowResult.ComponentResult.Status.NO_CALCULATION;
                }
                default -> {
                    return LoadFlowResult.ComponentResult.Status.FAILED;
                }
            }
        }

        public SensitivityStateStatus addComponentLoadFlowStatus(LoadFlowStatus loadFlowStatus, int numCC, int numCS) {
            componentsLoadFlowStatusList.add(new ComponentStatus(loadFlowStatus, numCC, numCS));
            return this;
        }

        public SensitivityStateStatus addComponentLoadFlowStatus(LoadFlowResult.ComponentResult.Status status, int numCC, int numCS) {
            componentsLoadFlowStatusList.add(new ComponentStatus(new LoadFlowStatus(status, ""), numCC, numCS));
            return this;
        }

        public static void writeJson(JsonGenerator jsonGenerator, SensitivityStateStatus stateStatus) {
            writeJson(jsonGenerator, stateStatus.state, stateStatus.componentsLoadFlowStatusList);
        }

        public static void writeJson(JsonGenerator jsonGenerator, SensitivityState state,
                                     List<ComponentStatus> componentsLoadFlowStatusList) {
            try {
                jsonGenerator.writeStartObject();
                if (state.contingencyId() != null) {
                    jsonGenerator.writeStringField("contingencyId", state.contingencyId());
                }
                if (state.operatorStrategyId() != null) {
                    jsonGenerator.writeStringField("operatorStrategyId", state.operatorStrategyId());
                }
                if (componentsLoadFlowStatusList != null && !componentsLoadFlowStatusList.isEmpty()) {
                    jsonGenerator.writeArrayFieldStart(COMPONENTS_LOADFLOW_STATUSES);
                    for (ComponentStatus componentStatus : componentsLoadFlowStatusList) {
                        jsonGenerator.writeStartObject();
                        jsonGenerator.writeStringField(LOAD_FLOW_STATUS, componentStatus.status().status().toString());
                        jsonGenerator.writeStringField(LOAD_FLOW_STATUS_DESCRIPTION, componentStatus.status().statusText());
                        jsonGenerator.writeNumberField(NUM_CC, componentStatus.numCC());
                        jsonGenerator.writeNumberField(NUM_CS, componentStatus.numSC());
                        jsonGenerator.writeEndObject();
                    }
                    jsonGenerator.writeEndArray();
                }
                jsonGenerator.writeEndObject();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        static final class ParsingContext {
            private String contingencyId;
            private String operatorStrategyId;
            private Status status;
            private List<ComponentStatus> componentsLoadFlowStatusList;
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
                        if (version != null && version.compareTo("1.1") <= 0) {
                            return new SensitivityStateStatus(
                                    new SensitivityState(context.contingencyId, context.operatorStrategyId), context.status);
                        } else {
                            return new SensitivityStateStatus(
                                    new SensitivityState(context.contingencyId, context.operatorStrategyId),
                                    context.componentsLoadFlowStatusList != null ? context.componentsLoadFlowStatusList : Collections.emptyList());
                        }
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
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: status", version, "1.1");
                    parser.nextToken();
                    context.status = Status.valueOf(parser.getValueAsString());
                    break;
                case COMPONENTS_LOADFLOW_STATUSES:
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: " + COMPONENTS_LOADFLOW_STATUSES, version, "1.2");
                    context.componentsLoadFlowStatusList = parseComponentLoadFlowStatuses(parser);
                    break;
                default:
                    throw new PowsyblException("Unexpected field: " + fieldName);
            }
        }

        private static List<ComponentStatus> parseComponentLoadFlowStatuses(JsonParser parser) throws IOException {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new PowsyblException("Expected start of array for component loadflow statuses");
            }
            List<ComponentStatus> statuses = new ArrayList<>();
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.currentToken() == JsonToken.START_OBJECT) {
                    statuses.add(parseSingleComponentStatus(parser));
                }
            }
            return statuses;
        }

        private static ComponentStatus parseSingleComponentStatus(JsonParser parser) throws IOException {
            String statusStr = null;
            String descStr = null;
            int numCC = 0;
            int numCS = 0;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                parser.nextToken();
                switch (fieldName) {
                    case LOAD_FLOW_STATUS -> statusStr = parser.getText();
                    case LOAD_FLOW_STATUS_DESCRIPTION -> descStr = parser.getText();
                    case NUM_CC -> numCC = parser.getIntValue();
                    case NUM_CS -> numCS = parser.getIntValue();
                    default -> parser.skipChildren();
                }
            }
            LoadFlowStatus lfs = new LoadFlowStatus(LoadFlowResult.ComponentResult.Status.valueOf(statusStr), descStr);
            return new ComponentStatus(lfs, numCC, numCS);
        }
    }

    /**
     * Sensitivity analysis result
     * @param factors the list of sensitivity factors that have been computed.
     * @param stateStatuses the list of states and their associated computation status.
     * @param contingencyIds the list of contingency IDs that have been considered during the sensitivity analysis.
     * @param operatorStrategyIds the list of operator strategy IDs that have been considered during the sensitivity analysis.
     * @param values result values of the sensitivity analysis in pre-contingency state and post-contingency states.
     */
    public SensitivityAnalysisResult(List<SensitivityFactor> factors, List<SensitivityStateStatus> stateStatuses, List<String> contingencyIds,
                                     List<String> operatorStrategyIds, List<SensitivityValue> values) {
        this(factors, stateStatuses, contingencyIds, operatorStrategyIds, values, true);
    }

    /**
     * Sensitivity analysis result
     * @param factors the list of sensitivity factors that have been computed.
     * @param stateStatuses the list of states and their associated computation status.
     * @param contingencyIds the list of contingency IDs that have been considered during the sensitivity analysis.
     * @param operatorStrategyIds the list of operator strategy IDs that have been considered during the sensitivity analysis.
     * @param values result values of the sensitivity analysis in pre-contingency state and post-contingency states.
     * @param computationComplete whether the sensitivity computation fully or partially completed
     */
    public SensitivityAnalysisResult(List<SensitivityFactor> factors, List<SensitivityStateStatus> stateStatuses, List<String> contingencyIds,
                                     List<String> operatorStrategyIds, List<SensitivityValue> values, boolean computationComplete) {
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
        this.computationComplete = computationComplete;
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
     * Get a list of all the state statuses.
     *
     * @return a list of all the state statuses.
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
     * @param state the considered state.
     * @return the sensitivity value associated to a given contingency ID.
     */
    public List<SensitivityValue> getValues(SensitivityState state) {
        Objects.requireNonNull(state);
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
     * @param state the considered state.
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
     * @param state the considered state.
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
     * Get the status associated to a state.
     *
     * @param state the considered state.
     * @return the associated status.
     * @deprecated Use {@link SensitivityAnalysisResult#getStateComponentStatus(SensitivityState)} instead.
     */
    @Deprecated(since = "7.4.0")
    public Status getStateStatus(SensitivityState state) {
        Objects.requireNonNull(state);
        return statusByState.get(state).getStatus();
    }

    /**
     * Get the status associated to a state for all components
     *
     * @param state the considered state.
     * @return the components' status.
     */
    public List<SensitivityStateStatus.ComponentStatus> getStateComponentStatus(SensitivityState state) {
        Objects.requireNonNull(state);
        return statusByState.get(state).getComponentsLoadFlowStatusList();
    }

    /**
     * Return true if the computation was fully completed, false if it was partially completed
     * @return the computation complete boolean
     */
    public boolean isComputationComplete() {
        return computationComplete;
    }
}
