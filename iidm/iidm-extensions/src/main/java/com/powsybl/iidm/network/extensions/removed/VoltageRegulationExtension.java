/**
 * Copyright (c) 2024-2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions.removed;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public class VoltageRegulationExtension implements Extension<Battery> {

    private boolean voltageRegulatorOn;

    private double targetV;

    private Terminal regulatingTerminal;

    private Battery extendable;

    public static String NAME = "voltageRegulation";

    public VoltageRegulationExtension(Battery battery, Terminal regulatingTerminal, Boolean voltageRegulatorOn, double targetV) {
        this.extendable = battery;
        checkRegulatingTerminal(regulatingTerminal, getNetworkFromExtendable());
        if (voltageRegulatorOn == null) {
            throw new PowsyblException("Voltage regulator status is not defined");
        }
        setRegulatingTerminal(regulatingTerminal);
        this.voltageRegulatorOn = voltageRegulatorOn;
        this.targetV = targetV;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Battery getExtendable() {
        return this.extendable;
    }

    @Override
    public void setExtendable(Battery extendable) {
        this.extendable = extendable;
    }

    private static void checkRegulatingTerminal(Terminal regulatingTerminal, Network network) {
        if (regulatingTerminal != null && regulatingTerminal.getVoltageLevel().getNetwork() != network) {
            throw new PowsyblException("regulating terminal is not part of the same network");
        }
    }

    private Network getNetworkFromExtendable() {
        return getExtendable().getTerminal().getVoltageLevel().getNetwork();
    }

    public boolean isVoltageRegulatorOn() {
        return this.voltageRegulatorOn;
    }

    public void setVoltageRegulatorOn(boolean voltageRegulationOn) {
        this.voltageRegulatorOn = voltageRegulationOn;
    }

    public double getTargetV() {
        return this.targetV;
    }

    public void setTargetV(double targetV) {
        this.targetV = targetV;
    }

    public Terminal getRegulatingTerminal() {
        return this.regulatingTerminal;
    }

    public void setRegulatingTerminal(Terminal terminal) {
        this.regulatingTerminal = terminal;
    }
}
