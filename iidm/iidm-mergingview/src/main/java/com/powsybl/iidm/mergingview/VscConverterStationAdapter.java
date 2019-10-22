/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.List;

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

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public HvdcType getHvdcType() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public float getLossFactor() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VscConverterStationAdapter setLossFactor(final float lossFactor) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TerminalAdapter getTerminal() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ConnectableType getType() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public List<? extends TerminalAdapter> getTerminals() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public <L extends ReactiveLimits> L getReactiveLimits(final Class<L> type) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public HvdcConverterStation setVoltageRegulatorOn(final boolean voltageRegulatorOn) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getVoltageSetpoint() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public HvdcConverterStation setVoltageSetpoint(final double voltageSetpoint) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getReactivePowerSetpoint() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public HvdcConverterStation setReactivePowerSetpoint(final double reactivePowerSetpoint) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
