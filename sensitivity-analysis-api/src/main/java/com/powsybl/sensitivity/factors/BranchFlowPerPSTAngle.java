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

/**
 * Sensitivity factor for an impact of phase shift transformer angle on a line flow
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class BranchFlowPerPSTAngle extends SensitivityFactor {

    /**
     * Sensitivity factor standard implementation constructor.
     */
    public BranchFlowPerPSTAngle(String functionId, String variableId,
                                   ContingencyContext contingencyContext) {
        super(SensitivityFunctionType.BRANCH_ACTIVE_POWER, functionId,
                SensitivityVariableType.TRANSFORMER_PHASE, variableId,
                false, contingencyContext);
    }
}
