/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.sensitivity.SensitivityFactor;
import com.powsybl.sensitivity.SensitivityFunctionType;
import com.powsybl.sensitivity.SensitivityVariableType;

public class BranchFlowPerInjectionIncrease extends SensitivityFactor {

    /**
     * Sensitivity factor standard implementation constructor.
     */
    public BranchFlowPerInjectionIncrease(String functionId, String variableId,
                                          ContingencyContext contingencyContext) {
        super(SensitivityFunctionType.BRANCH_ACTIVE_POWER, functionId,
                SensitivityVariableType.INJECTION_ACTIVE_POWER, variableId,
                false, contingencyContext);
    }
}
