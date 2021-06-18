/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
 * Sensitivity factor for an impact of a targetV increase from a regulating equipment on a bus ref
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class BusVoltagePerTargetV extends SensitivityFactor {

    /**
     * Sensitivity factor standard implementation constructor.
     */
    public BusVoltagePerTargetV(String functionId, String variableId,
                                   ContingencyContext contingencyContext) {
        super(SensitivityFunctionType.BUS_VOLTAGE, functionId,
                SensitivityVariableType.BUS_TARGET_VOLTAGE, variableId,
                false, contingencyContext);
    }
}
