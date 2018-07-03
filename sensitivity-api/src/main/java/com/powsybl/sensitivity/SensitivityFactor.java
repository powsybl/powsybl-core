/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

/**
 * Basic abstract implementation of sensitivity factors
 *
 * <p>
 *     Must be subclassed to get a business meaning, helping for
 *     computation of sensitivity factors in computation engines.
 * </p>
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
public class SensitivityFactor<F extends SensitivityFunction, V extends SensitivityVariable> {

    @JsonProperty("function")
    private final F sensitivityFunction;

    @JsonProperty("variable")
    private final V sensitivityVariable;

    /**
     * Sensitivity factor standard implementation constructor.
     *
     * @param sensitivityFunction sensitivity function
     * @param sensitivityVariable sensitivity variable
     */
    @JsonCreator
    protected SensitivityFactor(@JsonProperty("function") F sensitivityFunction,
                                @JsonProperty("variable") V sensitivityVariable) {
        this.sensitivityFunction = Objects.requireNonNull(sensitivityFunction);
        this.sensitivityVariable = Objects.requireNonNull(sensitivityVariable);
    }

    /**
     * Get the function of the sensitivity factor
     *
     * @return the function of the sensitivity factor
     */
    public F getFunction() {
        return sensitivityFunction;
    }

    /**
     * Get the variable of the sensitivity factor
     *
     * @return the variable of the sensitivity factor
     */
    public V getVariable() {
        return sensitivityVariable;
    }
}
