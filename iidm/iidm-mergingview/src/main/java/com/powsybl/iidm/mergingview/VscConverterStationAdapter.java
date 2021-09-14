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
public class VscConverterStationAdapter extends AbstractHvdcConverterStationAdapter<VscConverterStation> implements VscConverterStation {

    VscConverterStationAdapter(final VscConverterStation delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public HvdcConverterStation setVoltageRegulatorOn(final boolean voltageRegulatorOn) {
        return getIndex().getHvdcConverterStation(getDelegate().setVoltageRegulatorOn(voltageRegulatorOn));
    }

    @Override
    public HvdcConverterStation setVoltageSetpoint(final double voltageSetpoint) {
        return getIndex().getHvdcConverterStation(getDelegate().setVoltageSetpoint(voltageSetpoint));
    }

    @Override
    public HvdcConverterStation setReactivePowerSetpoint(final double reactivePowerSetpoint) {
        return getIndex().getHvdcConverterStation(getDelegate().setReactivePowerSetpoint(reactivePowerSetpoint));
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ReactiveLimits getReactiveLimits() {
        return getDelegate().getReactiveLimits();
    }

    @Override
    public <L extends ReactiveLimits> L getReactiveLimits(final Class<L> type) {
        return getDelegate().getReactiveLimits(type);
    }

    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        return getDelegate().newReactiveCapabilityCurve();
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return getDelegate().newMinMaxReactiveLimits();
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return getDelegate().isVoltageRegulatorOn();
    }

    @Override
    public double getVoltageSetpoint() {
        return getDelegate().getVoltageSetpoint();
    }

    @Override
    public double getReactivePowerSetpoint() {
        return getDelegate().getReactivePowerSetpoint();
    }

    @Override
    public VscConverterStation setRegulatingTerminal(final Terminal regulatingTerminal) {
        Terminal terminal = regulatingTerminal;
        if (terminal instanceof TerminalAdapter) {
            terminal = ((TerminalAdapter) terminal).getDelegate();
        }
        getDelegate().setRegulatingTerminal(terminal);
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        return getIndex().getTerminal(getDelegate().getRegulatingTerminal());
    }
}
