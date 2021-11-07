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

    private final List<SensitivityFactor> factors;

    private final List<Contingency> contingencies;

    private final List<SensitivityValue> values;

    private final Map<String, List<SensitivityValue>> valuesByContingencyId = new HashMap<>();

    private final Map<Triple<String, String, String>, SensitivityValue> valuesByContingencyIdAndFunctionIdAndVariableId = new HashMap<>();

    private final Map<Pair<String, String>, Double> functionReferenceByContingencyAndFunctionId = new HashMap<>();

    /**
     * Sensitivity analysis result
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

    public List<SensitivityFactor> getFactors() {
        return factors;
    }

    public List<Contingency> getContingencies() {
        return contingencies;
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
     * @param variableId sensitivity variable id
     * @param functionId sensitivity function id
     * @return the sensitivity value associated with a given function and a given variable for a given contingency
     */
    public double getSensitivityValue(String contingencyId, String variableId, String functionId) {
        SensitivityValue value = valuesByContingencyIdAndFunctionIdAndVariableId.get(Triple.of(contingencyId, functionId, variableId));
        if (value == null) {
            throw new PowsyblException("Sensitivity value not found for contingency '" + contingencyId + "', function '"
                    + functionId + "', variable '" + variableId + "'");
        }
        return value.getValue();
    }

    public double getSensitivityValue(String variableId, String functionId) {
        return getSensitivityValue(null, variableId, functionId);
    }

    /**
     * Get the function reference associated with a given contingency Id and a given function.
     *
     * @param contingencyId the id of the considered contingency
     * @param functionId sensitivity function id
     * @return the function reference value
     */
    public double getFunctionReferenceValue(String contingencyId, String functionId) {
        Double value = functionReferenceByContingencyAndFunctionId.get(Pair.of(contingencyId, functionId));
        if (value == null) {
            throw new PowsyblException("Reference flow value not found for contingency '" + contingencyId + "', function '" + functionId + "'");
        }
        return value;
    }

    public double getFunctionReferenceValue(String functionId) {
        return getFunctionReferenceValue(null, functionId);
    }
}
