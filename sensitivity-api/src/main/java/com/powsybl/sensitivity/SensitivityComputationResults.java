/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sensitivity computation results
 *
 * <p>
 *     Mainly composed of the list of sensitivity values
 * </p>
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 * @see SensitivityValue
 */
public class SensitivityComputationResults {

    @JsonProperty("ok")
    private final boolean ok;

    @JsonProperty("metrics")
    private final Map<String, String> metrics;

    @JsonProperty("logs")
    private final String logs;

    @JsonProperty("values")
    private final List<SensitivityValue> sensitivityValues;

    /**
     * Hades2 sensitivity computation results
     *
     * @param ok true if the computation succeeded, false otherwise
     * @param metrics map of metrics about the computation
     * @param logs computation logs
     * @param values result values of the senstivity computation. Must be non null.
     */
    @JsonCreator
    public SensitivityComputationResults(@JsonProperty("ok") boolean ok,
                                         @JsonProperty("metrics") Map<String, String> metrics,
                                         @JsonProperty("logs") String logs,
                                         @JsonProperty("values") List<SensitivityValue> values) {
        this.ok = ok;
        this.metrics = Objects.requireNonNull(metrics);
        this.logs = Objects.requireNonNull(logs);
        this.sensitivityValues = Objects.requireNonNull(values);
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
     * Get a collection of all the sensitivity values.
     *
     * @return a collection of all the sensitivity values.
     */
    public Collection<SensitivityValue> getSensitivityValues() {
        return Collections.unmodifiableCollection(sensitivityValues);
    }

    /**
     * Get a collection of all the sensitivity values associated with given function.
     *
     * @param function sensitivity function
     * @return a collection of all the sensitivity values associated with given function
     */
    public Collection<SensitivityValue> getSensitivityValuesByFunction(SensitivityFunction function) {
        return sensitivityValues.stream().filter(sensitivityValue -> sensitivityValue.getFactor().getFunction().equals(function)).collect(Collectors.toList());
    }

    /**
     * Get a collection of all the sensitivity values associated with given variable
     *
     * @param variable sensitivity variable
     * @return a collection of all the sensitivity values associated with given variable
     */
    public Collection<SensitivityValue> getSensitivityValuesByVariable(SensitivityVariable variable) {
        return sensitivityValues.stream().filter(sensitivityValue -> sensitivityValue.getFactor().getVariable().equals(variable)).collect(Collectors.toList());
    }

    /**
     * Get the sensitivity value associated with given function and given variable
     *
     * @param function sensitivity function
     * @param variable sensitivity variable
     * @return the sensitivity value associated with given function and given variable
     */
    public SensitivityValue getSensitivityValue(SensitivityFunction function, SensitivityVariable variable) {
        Optional<SensitivityValue> returnValue = sensitivityValues.stream().filter(sensitivityValue -> sensitivityValue.getFactor().getFunction().equals(function)
            && sensitivityValue.getFactor().getVariable().equals(variable)).findAny();
        if (!returnValue.isPresent()) {
            throw new NoSuchElementException(String.format("Sensitivity value not found for function %s and variable %s.", function.getId(), variable.getId()));
        }
        return returnValue.get();
    }

    /**
     * Get the sensitivity value associated with given factor
     *
     * @param factor sensitivity factor
     * @return the sensitivity value associated with given function and given variable
     */
    public SensitivityValue getSensitivityValue(SensitivityFactor factor) {
        Optional<SensitivityValue> returnValue = sensitivityValues.stream().filter(sensitivityValue -> sensitivityValue.getFactor().equals(factor)).findAny();
        if (!returnValue.isPresent()) {
            throw new NoSuchElementException(String.format("Sensitivity value not found for function %s and variable %s.", factor.getFunction().getId(), factor.getVariable().getId()));
        }
        return returnValue.get();
    }
}
