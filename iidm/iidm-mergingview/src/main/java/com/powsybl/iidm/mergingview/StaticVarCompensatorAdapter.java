/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.RegulationMode;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class StaticVarCompensatorAdapter extends AbstractInjectionAdapter<StaticVarCompensator> implements StaticVarCompensator {

    StaticVarCompensatorAdapter(final StaticVarCompensator delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public double getBmin() {
        return getDelegate().getBmin();
    }

    @Override
    public StaticVarCompensator setBmin(final double bMin) {
        getDelegate().setBmin(bMin);
        return this;
    }

    @Override
    public double getBmax() {
        return getDelegate().getBmax();
    }

    @Override
    public StaticVarCompensator setBmax(final double bMax) {
        getDelegate().setBmax(bMax);
        return this;
    }

    @Override
    public double getVoltageSetpoint() {
        return getDelegate().getVoltageSetpoint();
    }

    @Override
    public StaticVarCompensator setVoltageSetpoint(final double voltageSetpoint) {
        getDelegate().setVoltageSetpoint(voltageSetpoint);
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return getDelegate().getReactivePowerSetpoint();
    }

    @Override
    public StaticVarCompensator setReactivePowerSetpoint(final double reactivePowerSetpoint) {
        getDelegate().setReactivePowerSetpoint(reactivePowerSetpoint);
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return getDelegate().getRegulationMode();
    }

    @Override
    public StaticVarCompensator setRegulationMode(final RegulationMode regulationMode) {
        getDelegate().setRegulationMode(regulationMode);
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        return getIndex().getTerminal(getDelegate().getRegulatingTerminal());
    }

    @Override
    public StaticVarCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        Terminal terminal = regulatingTerminal;
        if (terminal instanceof TerminalAdapter) {
            terminal = ((TerminalAdapter) terminal).getDelegate();
        }
        getDelegate().setRegulatingTerminal(terminal);
        return this;
    }
}
