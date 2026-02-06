/**
 * Copyright (c) 2021-2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions.removed;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.regulation.RegulationMode;

import java.util.Objects;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class RemoteReactivePowerControl implements Extension<Generator> {

    private double targetQ;
    private Terminal regulatingTerminal;
    private boolean enabled;
    public static String NAME = "generatorRemoteReactivePowerControl";
    private Generator extendable;

    public RemoteReactivePowerControl(Generator generator, double targetQ, Terminal regulatingTerminal, boolean enabled) {
        this.extendable = generator;
        this.targetQ = targetQ;
        this.regulatingTerminal = Objects.requireNonNull(regulatingTerminal);
        this.enabled = enabled;
        if (regulatingTerminal.getVoltageLevel().getParentNetwork() != getExtendable().getParentNetwork()) {
            throw new PowsyblException("Regulating terminal is not in the right Network ("
                + regulatingTerminal.getVoltageLevel().getParentNetwork().getId() + " instead of "
                + getExtendable().getParentNetwork().getId() + ")");
        }
        if (generator.getVoltageRegulation() == null) {
            generator.newVoltageRegulation()
                .withTargetValue(targetQ)
                .withMode(RegulationMode.REACTIVE_POWER)
                .withRegulating(enabled)
                .withTerminal(regulatingTerminal)
                .add();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Generator getExtendable() {
        return this.extendable;
    }

    @Override
    public void setExtendable(Generator extendable) {
        this.extendable = extendable;
    }

    /**
     * Get the reactive power target in MVar, at the remote regulating terminal. It is not the local target of the generator.
     */
    public double getTargetQ() {
        return this.targetQ;
    }

    /**
     * Get the regulating terminal where the reactive power should be controlled. The regulating terminal should be the
     * terminal of a branch or the terminal of a dangling line that will be merged later. It is not the regulating terminal
     * of the generator used for voltage control that is less restrictive.
     */
    public Terminal getRegulatingTerminal() {
        return this.regulatingTerminal;
    }

    /**
     * Says if the control is active or not.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setTargetQ(double targetQ) {
        this.targetQ = targetQ;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
    }
}
