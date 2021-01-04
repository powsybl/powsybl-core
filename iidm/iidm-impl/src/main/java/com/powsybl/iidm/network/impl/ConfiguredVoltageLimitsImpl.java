/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.VoltageLimits;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class ConfiguredVoltageLimitsImpl extends AbstractOperationalLimits implements VoltageLimits {

    private double highVoltage;
    private double lowVoltage;

    ConfiguredVoltageLimitsImpl(OperationalLimitsOwner owner, double lowVoltage, double highVoltage) {
        super(owner);
        this.highVoltage = highVoltage;
        this.lowVoltage = lowVoltage;
    }

    @Override
    public double getHighVoltage() {
        return highVoltage;
    }

    @Override
    public VoltageLimits setHighVoltage(double highVoltage) {
        ValidationUtil.checkDefinedVoltageLimits(owner, lowVoltage, highVoltage);
        double oldValue = this.highVoltage;
        this.highVoltage = highVoltage;
        owner.notifyUpdate(LimitType.VOLTAGE, "highVoltage", oldValue, this.highVoltage);
        return this;
    }

    @Override
    public double getLowVoltage() {
        return lowVoltage;
    }

    @Override
    public VoltageLimits setLowVoltage(double lowVoltage) {
        ValidationUtil.checkDefinedVoltageLimits(owner, lowVoltage, highVoltage);
        double oldValue = this.lowVoltage;
        this.lowVoltage = lowVoltage;
        owner.notifyUpdate(LimitType.VOLTAGE, "lowVoltage", oldValue, this.lowVoltage);
        return this;
    }
}
