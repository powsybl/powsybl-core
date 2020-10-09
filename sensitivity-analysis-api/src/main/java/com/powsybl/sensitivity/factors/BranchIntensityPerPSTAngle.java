/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.sensitivity.SensitivityFactor;
import com.powsybl.sensitivity.factors.functions.BranchIntensity;
import com.powsybl.sensitivity.factors.variables.PhaseTapChangerAngle;

/**
 * Sensitivity factor for an impact of phase shift transformer angle on a line intensity
 * Only available in AC computation
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 * @see BranchIntensity
 * @see PhaseTapChangerAngle
 */
public class BranchIntensityPerPSTAngle extends SensitivityFactor<BranchIntensity, PhaseTapChangerAngle> {

    /**
     * Constructor
     *
     * @param sensitivityFunction network line which intensity is used as sensitivity function
     * @param sensitivityVariable network phase tap changer holder which angle is used as sensitivity variable
     * @throws NullPointerException if sensitivityFunction or sensitivityVariable is null
     */
    @JsonCreator
    public BranchIntensityPerPSTAngle(@JsonProperty("function") BranchIntensity sensitivityFunction,
                                      @JsonProperty("variable") PhaseTapChangerAngle sensitivityVariable) {
        super(sensitivityFunction, sensitivityVariable);
    }
}
