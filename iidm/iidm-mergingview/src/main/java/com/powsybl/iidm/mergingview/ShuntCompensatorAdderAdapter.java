/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShuntCompensatorAdderAdapter extends AbstractInjectionAdderAdapter<ShuntCompensatorAdder> implements ShuntCompensatorAdder {

    ShuntCompensatorAdderAdapter(final ShuntCompensatorAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public ShuntCompensator add() {
        checkAndSetUniqueId();
        return getIndex().getShuntCompensator(getDelegate().add());
    }

    @Override
    public ShuntCompensatorAdder setRegulatingTerminal(Terminal regulatingTerminal) {
        Terminal terminal = regulatingTerminal;
        if (terminal instanceof TerminalAdapter) {
            terminal = ((TerminalAdapter) terminal).getDelegate();
        }
        getDelegate().setRegulatingTerminal(terminal);
        return this;
    }

    @Override
    public ShuntCompensatorLinearModelAdder newLinearModel() {
        return new ShuntCompensatorLinearModelAdderAdapter(getDelegate().newLinearModel(), this);
    }

    @Override
    public ShuntCompensatorNonLinearModelAdder newNonLinearModel() {
        return new ShuntCompensatorNonLinearModelAdderAdapter(getDelegate().newNonLinearModel(), this);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ShuntCompensatorAdder setCurrentSectionCount(final int currentSectionCount) {
        getDelegate().setCurrentSectionCount(currentSectionCount);
        return this;
    }

    @Override
    public ShuntCompensatorAdder setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        getDelegate().setVoltageRegulatorOn(voltageRegulatorOn);
        return this;
    }

    @Override
    public ShuntCompensatorAdder setTargetV(double targetV) {
        getDelegate().setTargetV(targetV);
        return this;
    }

    @Override
    public ShuntCompensatorAdder setTargetDeadband(double targetDeadband) {
        getDelegate().setTargetDeadband(targetDeadband);
        return this;
    }
}
