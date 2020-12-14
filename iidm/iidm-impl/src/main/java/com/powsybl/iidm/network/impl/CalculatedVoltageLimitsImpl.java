/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.VoltageLimits;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CalculatedVoltageLimitsImpl implements VoltageLimits {

    private final CalculatedBus bus;

    CalculatedVoltageLimitsImpl(CalculatedBus bus) {
        this.bus = Objects.requireNonNull(bus);
    }

    @Override
    public void remove() {
        throw new ValidationException(bus, "Voltage limits cannot be removed from a calculated object: directly remove from the configured object.");
    }

    @Override
    public double getHighVoltage() {
        return bus.getHighVoltageLimit();
    }

    @Override
    public VoltageLimits setHighVoltage(double highVoltage) {
        throw new ValidationException(bus, "Voltage limits cannot be set on a calculated object: directly set on the configured object.");
    }

    @Override
    public double getLowVoltage() {
        return bus.getLowVoltageLimit();
    }

    @Override
    public VoltageLimits setLowVoltage(double lowVoltage) {
        throw new ValidationException(bus, "Voltage limits cannot be set on a calculated object: directly set on the configured object.");
    }
}
