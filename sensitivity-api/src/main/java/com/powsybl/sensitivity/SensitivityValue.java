/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Elementary result value of sensitivity computation.
 * Associates a value to a sensitivity factor.
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 * @see SensitivityFactor
 */
public class SensitivityValue {

    @JsonProperty("factor")
    private final SensitivityFactor sensitivityFactor;

    @JsonProperty("value")
    private final double value;

    @JsonProperty("functionReference")
    private final double functionReference;

    @JsonProperty("variableReference")
    private final double variableReference;

    /**
     * Constructor
     *
     * @param sensitivityFactor sensitivity factor input
     * @param value value of the sensitivity
     * @param functionReference reference value of the function
     * @param variableReference reference value of the variable
     */
    @JsonCreator
    public SensitivityValue(@JsonProperty("factor") SensitivityFactor sensitivityFactor,
                            @JsonProperty("value") double value,
                            @JsonProperty("functionReference") double functionReference,
                            @JsonProperty("variableReference") double variableReference) {
        this.sensitivityFactor = Objects.requireNonNull(sensitivityFactor);
        this.value = value;
        this.functionReference = functionReference;
        this.variableReference = variableReference;
    }


    /**
     * Get the sensitivity factor
     *
     * @return the sensitivity factor
     */
    public SensitivityFactor getFactor() {
        return sensitivityFactor;
    }

    /**
     * Get the value of the sensitivity
     *
     * @return the value of the sensitivity, and NaN if not computed
     */
    public double getValue() {
        return value;
    }

    /**
     * Get the value of the function in the reference network state
     *
     * @return the reference value of the function, and NaN if not computed
     */
    public double getFunctionReference() {
        return functionReference;
    }

    /**
     * Get the value of the variable in the reference network state
     *
     * @return the reference value of the variable, and NaN if not computed
     */
    public double getVariableReference() {
        return variableReference;
    }
}
