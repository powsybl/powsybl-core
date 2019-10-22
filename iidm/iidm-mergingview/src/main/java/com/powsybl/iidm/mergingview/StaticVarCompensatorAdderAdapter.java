/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class StaticVarCompensatorAdderAdapter extends AbstractAdapter<StaticVarCompensatorAdder> implements StaticVarCompensatorAdder {

    protected StaticVarCompensatorAdderAdapter(final StaticVarCompensatorAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public StaticVarCompensatorAdderAdapter setNode(final int node) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdderAdapter setBus(final String bus) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdderAdapter setConnectableBus(final String connectableBus) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdderAdapter setId(final String id) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdderAdapter setName(final String name) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdderAdapter setBmin(final double bMin) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdderAdapter setBmax(final double bMax) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdderAdapter setVoltageSetPoint(final double voltageSetPoint) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdderAdapter setReactivePowerSetPoint(final double reactivePowerSetPoint) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdderAdapter setRegulationMode(final RegulationMode regulationMode) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public StaticVarCompensatorAdapter add() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
