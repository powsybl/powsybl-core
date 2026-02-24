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

    private final List<SensitivityPreContingencyStatus> preContingencyStatuses;

    private final List<SensitivityValue> values;

    private final Map<String, List<SensitivityValue>> valuesByContingencyId = new HashMap<>();

    private final Map<SensitivityValueKey, SensitivityValue> valuesByContingencyIdAndFunctionAndVariable = new HashMap<>();

    private final Map<Triple<SensitivityFunctionType, String, String>, Double> functionReferenceByContingencyAndFunction = new HashMap<>();

    private final Map<String, SensitivityContingencyStatus> statusByContingencyId = new HashMap<>();

    /**
     * The load flow status for the component
     * @param status
     * @param statusText
     */
    public record LoadFlowStatus(LoadFlowResult.ComponentResult.Status status, String statusText) {
    }

    public enum Status {
        SUCCESS,
        FAILURE,
        NO_IMPACT
    }

    public static class SensitivityContingencyStatus {

        static final String COMPONENTS_LOADFLOW_STATUSES = "componentsLoadFlowStatuses";
        static final String LOAD_FLOW_STATUS = "loadFlowStatus";
        static final String LOAD_FLOW_STATUS_DESCRIPTION = "loadFlowStatusDescription";
        static final String NUM_CC = "numCC";
        static final String NUM_CS = "numCS";
        static final String CONTINGENCY_ID = "contingencyId";
        static final String CONTINGENCY_STATUS = "contingencyStatus";

        private final String contingencyId;

        private final Status status;
        /**
         * Load flow status for all the components
         */
        private final List<Triple<LoadFlowStatus, Integer, Integer>> componentsLoadFlowStatusList;

        public String getContingencyId() {
            return contingencyId;
        }

        public Status getStatus() {
            return status;
        }

        public Collection<Triple<LoadFlowStatus, Integer, Integer>> getComponentsLoadFlowStatusList() {
            return componentsLoadFlowStatusList;
        }

        public SensitivityContingencyStatus(String contingencyId, Status status) {
            this.contingencyId = Objects.requireNonNull(contingencyId);
            this.status = Objects.requireNonNull(status);
            this.componentsLoadFlowStatusList = new ArrayList<>();
        }

        public void addComponentLoadFlowStatus(LoadFlowStatus loadFlowStatus, int numCC, int numCs) {
            componentsLoadFlowStatusList.add(Triple.of(loadFlowStatus, numCC, numCs));
        }

        public static void writeJson(JsonGenerator jsonGenerator, SensitivityContingencyStatus contingencyStatus) {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(CONTINGENCY_ID, contingencyStatus.getContingencyId());
                jsonGenerator.writeStringField(CONTINGENCY_STATUS, contingencyStatus.status.name());
                if (!contingencyStatus.getComponentsLoadFlowStatusList().isEmpty()) {
                    jsonGenerator.writeArrayFieldStart(COMPONENTS_LOADFLOW_STATUSES);
                    for (Triple<LoadFlowStatus, Integer, Integer> componentLoadflowStatus : contingencyStatus.getComponentsLoadFlowStatusList()) {
                        jsonGenerator.writeStartObject();
                        jsonGenerator.writeStringField(LOAD_FLOW_STATUS, componentLoadflowStatus.getFirst().status.toString());
                        jsonGenerator.writeStringField(LOAD_FLOW_STATUS_DESCRIPTION, componentLoadflowStatus.getFirst().statusText);
                        jsonGenerator.writeNumberField(NUM_CC, componentLoadflowStatus.getSecond());
                        jsonGenerator.writeNumberField(NUM_CS, componentLoadflowStatus.getThird());
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
            protected Contingency contingency;
            private Status status;
            protected List<Triple<LoadFlowStatus, Integer, Integer>> componentLoadflowStatuses;
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
                        return getSensitivityContingencyStatus(context);
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            throw new PowsyblException("Parsing error");
        }

        private static SensitivityContingencyStatus getSensitivityContingencyStatus(ParsingContext context) {
            SensitivityContingencyStatus sensitivityContingencyStatus = new SensitivityContingencyStatus(context.contingency != null ? context.contingency.getId() : "", context.status);
            if (context.componentLoadflowStatuses != null) {
                for (var triple : context.componentLoadflowStatuses) {
                    sensitivityContingencyStatus.addComponentLoadFlowStatus(triple.getFirst(), triple.getSecond(), triple.getThird());
                }
            }
            return sensitivityContingencyStatus;
        }

        static void parseJson(JsonParser parser, SensitivityContingencyStatus.ParsingContext context) throws IOException {
            String fieldName = parser.currentName();

            switch (fieldName) {
                case CONTINGENCY_ID -> {
                    parser.nextToken();
                    context.contingency = new Contingency(parser.getValueAsString());
                }
                case CONTINGENCY_STATUS -> {
                    parser.nextToken();
                    context.status = Status.valueOf(parser.getValueAsString());
                }
                case COMPONENTS_LOADFLOW_STATUSES -> {
                    context.componentLoadflowStatuses = parseComponentLoadflowStatuses(parser);
                }
                default -> throw new PowsyblException("Unexpected field: " + fieldName);
            }
        }

        private static List<Triple<LoadFlowStatus, Integer, Integer>> parseComponentLoadflowStatuses(JsonParser parser) throws IOException {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new PowsyblException("Expected start of array for component loadflow statuses");
            }

            List<Triple<LoadFlowStatus, Integer, Integer>> statuses = new ArrayList<>();
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.currentToken() == JsonToken.START_OBJECT) {
                    statuses.add(parseSingleComponentStatus(parser));
                }
            }
            return statuses;
        }

        private static Triple<LoadFlowStatus, Integer, Integer> parseSingleComponentStatus(JsonParser parser) throws IOException {
            String statusStr = null;
            String descStr = null;
            int numCC = 0;
            int numCS = 0;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                parser.nextToken(); // Move to value

                switch (fieldName) {
                    case LOAD_FLOW_STATUS -> statusStr = parser.getText();
                    case LOAD_FLOW_STATUS_DESCRIPTION -> descStr = parser.getText();
                    case NUM_CC -> numCC = parser.getIntValue();
                    case NUM_CS -> numCS = parser.getIntValue();
                    default -> parser.skipChildren();
                }
            }

            LoadFlowStatus lfs = new LoadFlowStatus(com.powsybl.loadflow.LoadFlowResult.ComponentResult.Status.valueOf(statusStr), descStr);
            return new Triple<>(lfs, numCC, numCS);
        }

    }

    public static class SensitivityPreContingencyStatus {

        public static final String LOAD_FLOW_STATUS = "loadFlowStatus";
        public static final String STATUS = "status";
        public static final String STATUS_TEXT = "statusText";
        public static final String NUM_CC = "numCC";
        public static final String NUM_CS = "numCS";

        private LoadFlowStatus loadFlowStatus;
        private Integer numCC;
        private Integer numCS;

        public SensitivityPreContingencyStatus(LoadFlowStatus loadFlowStatus, int numCC, int numCS) {
            this.loadFlowStatus = loadFlowStatus;
            this.numCC = numCC;
            this.numCS = numCS;
        }

        public SensitivityPreContingencyStatus() {
        }

        public void setLoadFlowStatus(LoadFlowStatus loadFlowStatus) {
            this.loadFlowStatus = loadFlowStatus;
        }

        public void setNumCC(Integer numCC) {
            this.numCC = numCC;
        }

        public void setNumCS(Integer numCS) {
            this.numCS = numCS;
        }

        public LoadFlowStatus getLoadFlowStatus() {
            return loadFlowStatus;
        }

        public Integer getNumCC() {
            return numCC;
        }

        public Integer getNumCS() {
            return numCS;
        }

        public static void writeJson(JsonGenerator jsonGenerator, SensitivityPreContingencyStatus precontingencyStatus) {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeNumberField(NUM_CC, precontingencyStatus.getNumCC());
                jsonGenerator.writeNumberField(NUM_CS, precontingencyStatus.getNumCS());
                jsonGenerator.writeObjectFieldStart(LOAD_FLOW_STATUS);
                jsonGenerator.writeStringField(STATUS, precontingencyStatus.getLoadFlowStatus().status.toString());
                jsonGenerator.writeStringField(STATUS_TEXT, precontingencyStatus.getLoadFlowStatus().statusText);
                jsonGenerator.writeEndObject();
                jsonGenerator.writeEndObject();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        static final class ParsingContext {
            protected List<SensitivityPreContingencyStatus> preContingencyloadflowStatuses;
        }

        public static List<SensitivityPreContingencyStatus> parseJson(JsonParser parser) {
            Objects.requireNonNull(parser);

            var context = new SensitivityPreContingencyStatus.ParsingContext();
            try {
                JsonToken token;
                while ((token = parser.nextToken()) != null) {
                    if (token == JsonToken.FIELD_NAME) {
                        parseJson(parser, context);
                    } else if (token == JsonToken.END_OBJECT) {
                        return getSensitivityPreContingencyStatus(context);
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            throw new PowsyblException("Parsing error");
        }

        private static List<SensitivityPreContingencyStatus> getSensitivityPreContingencyStatus(ParsingContext context) {
            return context.preContingencyloadflowStatuses;
        }

        protected static void parseJson(JsonParser parser, SensitivityPreContingencyStatus.ParsingContext context) throws IOException {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new PowsyblException("Expected an array of objects");
            }

            context.preContingencyloadflowStatuses = new ArrayList<>();

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    context.preContingencyloadflowStatuses.add(parseComponentStatus(parser));
                }
            }
        }

        private static SensitivityPreContingencyStatus parseComponentStatus(JsonParser parser) throws IOException {
            SensitivityPreContingencyStatus componentStatus = new SensitivityPreContingencyStatus();
            LoadFlowStatusHolder holder = new LoadFlowStatusHolder();

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                parser.nextToken(); // Move to value

                switch (fieldName) {
                    case NUM_CC -> componentStatus.setNumCC(parser.getIntValue());
                    case NUM_CS -> componentStatus.setNumCS(parser.getIntValue());
                    case LOAD_FLOW_STATUS -> parseLoadFlowStatus(parser, holder);
                    default -> parser.skipChildren();
                }
            }

            if (holder.value != null) {
                var status = com.powsybl.loadflow.LoadFlowResult.ComponentResult.Status.valueOf(holder.value);
                componentStatus.setLoadFlowStatus(new LoadFlowStatus(status, holder.text));
            }

            return componentStatus;
        }

        private static void parseLoadFlowStatus(JsonParser parser, LoadFlowStatusHolder holder) throws IOException {
            if (parser.currentToken() != JsonToken.START_OBJECT) {
                throw new PowsyblException(LOAD_FLOW_STATUS + " should be an object");
            }

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                parser.nextToken();

                if (STATUS.equals(fieldName)) {
                    holder.value = parser.getText();
                } else if (STATUS_TEXT.equals(fieldName)) {
                    holder.text = parser.getText();
                } else {
                    parser.skipChildren();
                }
            }
        }

        private static final class LoadFlowStatusHolder {
            String value;
            String text;
        }
    }

    /**
     * Sensitivity analysis result
     * @param factors the list of sensitivity factors that have been computed.
     * @param contingencyStatuses the list of contingencies and their associated computation status.
     * @param preContingencyStatuses the list of pre contingencies and their associated loadflow status.
     * @param values result values of the sensitivity analysis in pre-contingency state and post-contingency states.
     */
    public SensitivityAnalysisResult(List<SensitivityFactor> factors, List<SensitivityContingencyStatus> contingencyStatuses, List<SensitivityPreContingencyStatus> preContingencyStatuses, List<SensitivityValue> values) {
        this.factors = Collections.unmodifiableList(Objects.requireNonNull(factors));
        this.contingencyStatuses = Collections.unmodifiableList(Objects.requireNonNull(contingencyStatuses));
        this.preContingencyStatuses = Collections.unmodifiableList(Objects.requireNonNull(preContingencyStatuses));
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

    public List<SensitivityPreContingencyStatus> getPreContingencyStatuses() {
        return preContingencyStatuses;
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
