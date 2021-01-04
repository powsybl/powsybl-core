/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class VoltageLimitsAdderImpl implements VoltageLimitsAdder {

    private final OperationalLimitsOwner owner;
    private double highVoltage = Double.NaN;
    private double lowVoltage = Double.NaN;

    VoltageLimitsAdderImpl(OperationalLimitsOwner owner) {
        this.owner = owner;
    }

    @Override
    public VoltageLimitsAdder setHighVoltage(double highVoltage) {
        this.highVoltage = highVoltage;
        return this;
    }

    @Override
    public VoltageLimitsAdder setLowVoltage(double lowVoltage) {
        this.lowVoltage = lowVoltage;
        return this;
    }

    @Override
    public VoltageLimits add() {
        ValidationUtil.checkDefinedVoltageLimits(owner, lowVoltage, highVoltage);
        VoltageLimits voltageLimits = new ConfiguredVoltageLimitsImpl(owner, lowVoltage, highVoltage);
        owner.setOperationalLimits(LimitType.VOLTAGE, voltageLimits);
        return voltageLimits;
    }
}
