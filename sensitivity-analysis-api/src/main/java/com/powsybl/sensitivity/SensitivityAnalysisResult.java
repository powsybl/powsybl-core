/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.Contingency;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.Triple;

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
 *  - the list of contingencies
 *  - the list of sensitivity values in pre-contingency and post-contingency states
 *  - the list of function reference values in pre-contingency and post-contingency states.
 *  A sensitivity analysis result offers a set of methods to retrieve sensitivity values or function reference values.
 *  For example, you can retrieve a sensitivity value as a double given the ID of a contingency, the ID of a variable
 *  and the ID of a function.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see SensitivityValue
 */
public class SensitivityAnalysisResult {

    private final List<SensitivityFactor> factors;

    private final List<Contingency> contingencies;

    private final List<SensitivityValue> values;

    private final Map<String, List<SensitivityValue>> valuesByContingencyId = new HashMap<>();

    private final Map<Triple<String, String, String>, SensitivityValue> valuesByContingencyIdAndFunctionIdAndVariableId = new HashMap<>();

    private final Map<Pair<String, String>, Double> functionReferenceByContingencyAndFunctionId = new HashMap<>();

    /**
     * Sensitivity analysis result
     * @param factors the list of sensitivity factors that have been computed.
     * @param contingencies the list of contingencies that have been simulated.
     * @param values result values of the sensitivity analysis in pre-contingency state and post-contingency states.
     */
    public SensitivityAnalysisResult(List<SensitivityFactor> factors, List<Contingency> contingencies, List<SensitivityValue> values) {
        this.factors = Collections.unmodifiableList(Objects.requireNonNull(factors));
        this.contingencies = Collections.unmodifiableList(Objects.requireNonNull(contingencies));
        this.values = Collections.unmodifiableList(Objects.requireNonNull(values));
        for (SensitivityValue value : values) {
            SensitivityFactor factor = factors.get(value.getFactorIndex());
            Contingency contingency = value.getContingencyIndex() != -1 ? contingencies.get(value.getContingencyIndex()) : null;
            String contingencyId = contingency != null ? contingency.getId() : null;
            valuesByContingencyId.computeIfAbsent(contingencyId, k -> new ArrayList<>())
                    .add(value);
            valuesByContingencyIdAndFunctionIdAndVariableId.put(Triple.of(contingencyId, factor.getFunctionId(), factor.getVariableId()), value);
            functionReferenceByContingencyAndFunctionId.put(Pair.of(contingencyId, factor.getFunctionId()), value.getFunctionReference());
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
     * Get a list of all the contingencies.
     *
     * @return a list of all the contingencies.
     */
    public List<Contingency> getContingencies() {
        return contingencies;
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
     * Get the sensitivity value associated to a given function and a given variable and for a specific contingency.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency sensitivity value.
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable for a given contingency.
     */
    public double getSensitivityValue(String contingencyId, String variableId, String functionId) {
        SensitivityValue value = valuesByContingencyIdAndFunctionIdAndVariableId.get(Triple.of(contingencyId, functionId, variableId));
        if (value == null) {
            throw new PowsyblException("Sensitivity value not found for contingency '" + contingencyId + "', function '"
                    + functionId + "', variable '" + variableId + "'");
        }
        return value.getValue();
    }

    /**
     * Get the sensitivity value associated to a given function and a given variable in pre-contingency state.
     *
     * @param variableId the sensitivity variable id.
     * @param functionId the sensitivity function id.
     * @return the sensitivity value associated with a given function and a given variable in pre-contingency state.
     */
    public double getSensitivityValue(String variableId, String functionId) {
        return getSensitivityValue(null, variableId, functionId);
    }

    /**
     * Get the function reference associated to a given contingency Id and a given function.
     *
     * @param contingencyId the id of the considered contingency. Use null to get a pre-contingency function reference value.
     * @param functionId sensitivity function id.
     * @return the function reference value
     */
    public double getFunctionReferenceValue(String contingencyId, String functionId) {
        Double value = functionReferenceByContingencyAndFunctionId.get(Pair.of(contingencyId, functionId));
        if (value == null) {
            throw new PowsyblException("Reference flow value not found for contingency '" + contingencyId + "', function '" + functionId + "'");
        }
        return value;
    }

    /**
     * Get the function reference associated to a given function in a pre-contingency state.
     *
     * @param functionId sensitivity function id.
     * @return the function reference value.
     */
    public double getFunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(null, functionId);
    }
}
