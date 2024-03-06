/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.ReactiveLimitsHolder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.iidm.network.extensions.VoltageRegulationAdder;
import com.powsybl.iidm.network.impl.extensions.VoltageRegulationImpl;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public class VoltageRegulationAdderImpl<T extends Injection<T> & ReactiveLimitsHolder> extends AbstractExtensionAdder<T, VoltageRegulation<T>> implements VoltageRegulationAdder<T> {

    private Terminal regulatingTerminal;
    private Boolean voltageRegulatorOn = null;
    private double targetV = Double.NaN;

    public VoltageRegulationAdderImpl(T extendable) {
        super(extendable);
    }

    @Override
    protected VoltageRegulation<T> createExtension(T extendable) {
        if (regulatingTerminal == null) {
            regulatingTerminal = extendable.getTerminal();
        }
        return new VoltageRegulationImpl<>(extendable, regulatingTerminal, voltageRegulatorOn, targetV);
    }

    @Override
    public VoltageRegulationAdder<T> withVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public VoltageRegulationAdder<T> withTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public VoltageRegulationAdder<T> withRegulatingTerminal(Terminal terminal) {
        this.regulatingTerminal = terminal;
        return this;
    }
}
