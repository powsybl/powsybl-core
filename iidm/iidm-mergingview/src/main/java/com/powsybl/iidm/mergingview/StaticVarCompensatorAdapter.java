/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.List;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.StaticVarCompensator;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class StaticVarCompensatorAdapter extends AbstractIdentifiableAdapter<StaticVarCompensator> implements StaticVarCompensator {

    protected StaticVarCompensatorAdapter(final StaticVarCompensator delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
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
    public double getBmin() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdapter setBmin(final double bMin) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getBmax() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdapter setBmax(final double bMax) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getVoltageSetPoint() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdapter setVoltageSetPoint(final double voltageSetPoint) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getReactivePowerSetPoint() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdapter setReactivePowerSetPoint(final double reactivePowerSetPoint) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public RegulationMode getRegulationMode() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdapter setRegulationMode(final RegulationMode regulationMode) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
