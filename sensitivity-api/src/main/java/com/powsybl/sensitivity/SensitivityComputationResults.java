/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sensitivity computation results
 *
 * <p>
 *     Mainly composed of the lists of sensitivity values in N, and optionally in N-1
 * </p>
 *
 * A single sensitivity computation should return, besides its status and some stats on the
 * computation itself, all the sensitivity values for each factor (combination of a monitoredBranch and a specific
 * equipment or group of equipments). The HADES2 sensitivity provider used with Powsybl offers the
 * possibility to calculate the sensitivity on a set of contingencies besides the N state.
 * The computation is launched only once, but the solver itself
 * modifies the matrix for each state of the network to output a full set of results.
 * In the sensitivity API, it has been allowed to provide a list of contingencies as an optional input,
 * which then triggers such a sensitivity analysis.
 * The full set of results consists of :
 *  - the list of sensitivity values in N
 *  - the lists of sensitivity values for each N-1 situation
 *  - some metadata (status, stats, logs)
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 * @see SensitivityValue
 */
public class SensitivityComputationResults {

    private static final String VALUE_NOT_FOUND = "Sensitivity value not found for function %s and variable %s.";
    private static final String VALUE_NOT_FOUND_CONTINGENCY = "Sensitivity value not found for function %s and variable %s at contingencyId %s.";

    @JsonProperty("ok")
    private final boolean ok;

    @JsonProperty("metrics")
    private final Map<String, String> metrics;

    @JsonProperty("logs")
    private final String logs;

    @JsonProperty("values")
    private final List<SensitivityValue> sensitivityValues;

    @JsonProperty("contingenciesValues")
    private final Map<String, List<SensitivityValue>> sensitivityValuesContingencies;

    /**
     * Hades2 sensitivity computation results
     *
     * @param ok true if the computation succeeded, false otherwise
     * @param metrics map of metrics about the computation
     * @param logs computation logs
     * @param sensitivityValues result values of the senstivity computation in N
     * @param sensitivityValuesContingencies result values of the senstivity computation on contingencies
     */
    @JsonCreator
    public SensitivityComputationResults(@JsonProperty("ok") boolean ok,
                                         @JsonProperty("metrics") Map<String, String> metrics,
                                         @JsonProperty("logs") String logs,
                                         @JsonProperty("values") List<SensitivityValue> sensitivityValues,
                                         @JsonProperty("contingenciesValues") Map<String, List<SensitivityValue>> sensitivityValuesContingencies) {
        this.ok = ok;
        this.metrics = Objects.requireNonNull(metrics);
        this.logs = Objects.requireNonNull(logs);
        this.sensitivityValues = Collections.unmodifiableList(Objects.requireNonNull(sensitivityValues));
        this.sensitivityValuesContingencies = Optional.ofNullable(sensitivityValuesContingencies).map(Collections::unmodifiableMap).orElse(Collections.emptyMap());
    }

    /**
     * Get the status of the sensitivity computation
     *
     * @return true if the computation is ok, false otherwise
     */
    public boolean isOk() {
        return ok;
    }

    /**
     * Get some metrics about computation execution.
     * Content may vary a lot depending of the implementation
     *
     * @return the metrics of the execution
     */
    public Map<String, String> getMetrics() {
        return metrics;
    }

    /**
     * Get computation logs.
     *
     * @return the computation logs
     */
    public String getLogs() {
        return logs;
    }

    /**
     * Get a collection of all the sensitivity values in state N.
     *
     * @return a collection of all the sensitivity values in state N.
     */
    public Collection<SensitivityValue> getSensitivityValues() {
        return Collections.unmodifiableCollection(sensitivityValues);
    }


    /**
     * Get a collection of all the sensitivity values associated with given function in state N.
     *
     * @param function sensitivity function
     * @return a collection of all the sensitivity values associated with given function in state N.
     */
    public Collection<SensitivityValue> getSensitivityValuesByFunction(SensitivityFunction function) {
        return sensitivityValues.stream().filter(sensitivityValue -> sensitivityValue.getFactor().getFunction().equals(function))
                .collect(Collectors.toList());
    }

    /**
     * Get a collection of all the sensitivity values associated with given variable in state N.
     *
     * @param variable sensitivity variable
     * @return a collection of all the sensitivity values associated with given variable in state N.
     */
    public Collection<SensitivityValue> getSensitivityValuesByVariable(SensitivityVariable variable) {
        return sensitivityValues.stream().filter(sensitivityValue -> sensitivityValue.getFactor().getVariable().equals(variable))
                .collect(Collectors.toList());
    }

    /**
     * Get the sensitivity value associated with given function and given variable in state N.
     *
     * @param function sensitivity function
     * @param variable sensitivity variable
     * @return the sensitivity value associated with given function and given variable in state N.
     */
    public SensitivityValue getSensitivityValue(SensitivityFunction function, SensitivityVariable variable) {
        Optional<SensitivityValue> returnValue = sensitivityValues.stream().filter(sensitivityValue -> sensitivityValue.getFactor().getFunction().equals(function)
                && sensitivityValue.getFactor().getVariable().equals(variable)).findAny();
        if (!returnValue.isPresent()) {
            throw new NoSuchElementException(String.format(VALUE_NOT_FOUND, function.getId(), variable.getId()));
        }
        return returnValue.get();
    }

    /**
     * Get the sensitivity value associated with given factor in state N.
     *
     * @param factor sensitivity factor
     * @return the sensitivity value associated with given function and given variable in state N.
     */
    public SensitivityValue getSensitivityValue(SensitivityFactor factor) {
        Optional<SensitivityValue> returnValue = sensitivityValues.stream().filter(sensitivityValue -> sensitivityValue.getFactor().equals(factor)).findAny();
        if (!returnValue.isPresent()) {
            throw new NoSuchElementException(String.format(VALUE_NOT_FOUND, factor.getFunction().getId(), factor.getVariable().getId()));
        }
        return returnValue.get();
    }

    /**
     * Get the status of the presence of contingencies
     *
     * @return true if the computation contains contingencies, false otherwise
     */
    public boolean contingenciesArePresent() {
        return !sensitivityValuesContingencies.isEmpty();
    }

    /**
     * Get a collection of all the sensitivity values for all contingencies.
     *
     * @return a collection of all the sensitivity values for all contingencies.
     */
    public Map<String, List<SensitivityValue>> getSensitivityValuesContingencies() {
        return Collections.unmodifiableMap(sensitivityValuesContingencies);
    }

    /**
     * Get a collection of all the sensitivity values associated with given function
     * for a specific contingency.
     *
     * @param function sensitivity function
     * @param contingencyId the ID of the considered contingency
     * @return a collection of all the sensitivity values associated with given function for the given contingencyId
     */
    public Collection<SensitivityValue> getSensitivityValuesByFunction(SensitivityFunction function, String contingencyId) {
        return sensitivityValuesContingencies.get(contingencyId).stream()
                .filter(sensitivityValue -> sensitivityValue.getFactor().getFunction().equals(function))
                .collect(Collectors.toList());
    }

    /**
     * Get a collection of all the sensitivity values associated with given variable
     * for a specific contingency.
     *
     * @param variable sensitivity variable
     * @param contingencyId the ID of the considered contingency
     * @return a collection of all the sensitivity values associated with given variable
     */
    public Collection<SensitivityValue> getSensitivityValuesByVariable(SensitivityVariable variable, String contingencyId) {
        return sensitivityValuesContingencies.get(contingencyId).stream()
                .filter(sensitivityValue -> sensitivityValue.getFactor().getVariable().equals(variable))
                .collect(Collectors.toList());
    }

    /**
     * Get the sensitivity value associated with given function and given variable for a specific contingency.
     *
     * @param function sensitivity function
     * @param variable sensitivity variable
     * @param contingencyId the ID of the considered contingency
     * @return the sensitivity value associated with given function and given variable
     */
    public SensitivityValue getSensitivityValue(SensitivityFunction function, SensitivityVariable variable, String contingencyId) {
        return sensitivityValuesContingencies.get(contingencyId).stream()
                .filter(sensitivityValue -> sensitivityValue.getFactor().getFunction().equals(function)
                        && sensitivityValue.getFactor().getVariable().equals(variable))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format(VALUE_NOT_FOUND_CONTINGENCY, function.getId(), variable.getId(), contingencyId)));
    }

    /**
     * Get the sensitivity value associated with given factor for a specific contingency
     *
     * @param factor sensitivity factor
     * @param contingencyId the ID of the considered contingency
     * @return the sensitivity value associated with given function and given variable
     */
    public SensitivityValue getSensitivityValue(SensitivityFactor factor, String contingencyId) {
        return sensitivityValuesContingencies.get(contingencyId).stream()
                .filter(sensitivityValue -> sensitivityValue.getFactor().equals(factor))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format(VALUE_NOT_FOUND, factor.getFunction().getId(), factor.getVariable().getId())));
    }

    public static SensitivityComputationResults empty() {
        return new SensitivityComputationResults(false, Collections.emptyMap(), "", Collections.emptyList(), Collections.emptyMap());
    }
}
