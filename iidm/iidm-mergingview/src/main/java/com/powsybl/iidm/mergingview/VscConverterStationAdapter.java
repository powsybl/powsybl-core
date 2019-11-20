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

    protected VscConverterStationAdapter(final VscConverterStation delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
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
    public VscConverterStationAdapter setVoltageRegulatorOn(final boolean voltageRegulatorOn) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getVoltageSetpoint() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VscConverterStationAdapter setVoltageSetpoint(final double voltageSetpoint) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getReactivePowerSetpoint() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VscConverterStationAdapter setReactivePowerSetpoint(final double reactivePowerSetpoint) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
