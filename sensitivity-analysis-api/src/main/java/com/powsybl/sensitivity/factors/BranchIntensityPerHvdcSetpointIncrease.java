/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.sensitivity.SensitivityFactor;
import com.powsybl.sensitivity.factors.functions.BranchIntensity;
import com.powsybl.sensitivity.factors.variables.HvdcSetpointIncrease;

/**
 * Sensitivity factor for an impact of HVDC setpoint variation on a line intensity
 * Only available in AC computation
 *
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 * @see BranchIntensity
 * @see HvdcSetpointIncrease
 */
public class BranchIntensityPerHvdcSetpointIncrease extends SensitivityFactor<BranchIntensity, HvdcSetpointIncrease> {

    /**
     * Constructor
     *
     * @param sensitivityFunction network line which intensity is used as sensitivity function
     * @param sensitivityVariable HVDC which active power setpoint is used as sensitivity variable
     * @throws NullPointerException if sensitivityFunction or sensitivityVariable is null
     */
    @JsonCreator
    public BranchIntensityPerHvdcSetpointIncrease(@JsonProperty("function") BranchIntensity sensitivityFunction,
                                                     @JsonProperty("variable") HvdcSetpointIncrease sensitivityVariable) {
        super(sensitivityFunction, sensitivityVariable);
    }
}
