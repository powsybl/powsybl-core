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
import com.powsybl.sensitivity.factors.functions.BranchFlow;
import com.powsybl.sensitivity.factors.variables.LinearGlsk;

/**
 * Sensitivity factor for an impact of linear GLSK variation on a branch flow
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 * @see BranchFlow
 * @see LinearGlsk
 */
public class BranchFlowPerLinearGlsk extends SensitivityFactor<BranchFlow, LinearGlsk> {

    /**
     * Constructor
     *
     * @param sensitivityFunction network branch which flow is used as sensitivity function
     * @param sensitivityVariable linear GLSK which active injection is used as sensitivity variable
     * @throws NullPointerException if sensitivityFunction or sensitivityVariable is null
     */
    @JsonCreator
    public BranchFlowPerLinearGlsk(@JsonProperty("function") BranchFlow sensitivityFunction,
                                   @JsonProperty("variable") LinearGlsk sensitivityVariable) {
        super(sensitivityFunction, sensitivityVariable);
    }
}
