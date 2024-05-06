/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.iidm.network.extensions.VoltageRegulationAdder;
import com.powsybl.iidm.network.impl.extensions.VoltageRegulationImpl;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public class VoltageRegulationAdderImpl extends AbstractExtensionAdder<Battery, VoltageRegulation> implements VoltageRegulationAdder {

    private Terminal regulatingTerminal;
    private Boolean voltageRegulatorOn = null;
    private double targetV = Double.NaN;

    public VoltageRegulationAdderImpl(Battery battery) {
        super(battery);
    }

    @Override
    protected VoltageRegulation createExtension(Battery battery) {
        if (regulatingTerminal == null) {
            regulatingTerminal = battery.getTerminal();
        }
        return new VoltageRegulationImpl(battery, regulatingTerminal, voltageRegulatorOn, targetV);
    }

    @Override
    public VoltageRegulationAdder withVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public VoltageRegulationAdder withTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public VoltageRegulationAdder withRegulatingTerminal(Terminal terminal) {
        this.regulatingTerminal = terminal;
        return this;
    }
}
