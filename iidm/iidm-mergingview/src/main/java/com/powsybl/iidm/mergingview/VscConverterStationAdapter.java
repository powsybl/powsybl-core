/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.List;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.MinMaxReactiveLimitsAdder;
import com.powsybl.iidm.network.ReactiveCapabilityCurveAdder;
import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.iidm.network.VscConverterStation;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VscConverterStationAdapter extends AbstractIdentifiableAdapter<VscConverterStation> implements VscConverterStation {

    protected VscConverterStationAdapter(final VscConverterStation delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public TerminalAdapter getTerminal() {
        return getIndex().getTerminal(getDelegate().getTerminal());
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
    public HvdcType getHvdcType() {
        return getDelegate().getHvdcType();
    }

    @Override
    public float getLossFactor() {
        return getDelegate().getLossFactor();
    }

    @Override
    public VscConverterStationAdapter setLossFactor(final float lossFactor) {
        getDelegate().setLossFactor(lossFactor);
        return this;
    }

    @Override
    public ConnectableType getType() {
        return getDelegate().getType();
    }

    @Override
    public List<? extends TerminalAdapter> getTerminals() {
        return getDelegate().getTerminals().stream()
                .map(getIndex()::getTerminal)
                .collect(Collectors.toList());
    }

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

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
