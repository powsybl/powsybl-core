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
import com.powsybl.sensitivity.factors.functions.BusVoltage;
import com.powsybl.sensitivity.factors.variables.TargetVoltage;

/**
 * Sensitivity factor for an impact of a targetV increase from a regulating equipment on a bus ref
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 * @see com.powsybl.sensitivity.factors.functions.BusVoltage
 * @see com.powsybl.sensitivity.factors.variables.TargetVoltage
 */
public class BusVoltagePerTargetV extends SensitivityFactor<BusVoltage, TargetVoltage> {

    /**
     * Sensitivity factor standard implementation constructor.
     *
     * @param sensitivityFunction sensitivity function
     * @param sensitivityVariable sensitivity variable
     */
    @JsonCreator
    public BusVoltagePerTargetV(@JsonProperty("function") BusVoltage sensitivityFunction,
                                @JsonProperty("variable") TargetVoltage sensitivityVariable) {
        super(sensitivityFunction, sensitivityVariable);
    }
}
