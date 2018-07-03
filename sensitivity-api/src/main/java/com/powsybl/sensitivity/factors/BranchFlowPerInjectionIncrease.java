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
import com.powsybl.sensitivity.factors.variables.InjectionIncrease;

public class BranchFlowPerInjectionIncrease extends SensitivityFactor<BranchFlow, InjectionIncrease> {

    /**
     * Sensitivity factor standard implementation constructor.
     *
     * @param sensitivityFunction sensitivity function
     * @param sensitivityVariable sensitivity variable
     */
    @JsonCreator
    public BranchFlowPerInjectionIncrease(@JsonProperty("function") BranchFlow sensitivityFunction,
                                          @JsonProperty("variable") InjectionIncrease sensitivityVariable) {
        super(sensitivityFunction, sensitivityVariable);
    }
}
