/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class GeneratorAdderAdapter extends AbstractAdapter<GeneratorAdder> implements GeneratorAdder {

    protected GeneratorAdderAdapter(final GeneratorAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public GeneratorAdderAdapter setNode(final int node) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setBus(final String bus) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setConnectableBus(final String connectableBus) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setId(final String id) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setName(final String name) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setEnergySource(final EnergySource energySource) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setMaxP(final double maxP) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setMinP(final double minP) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setVoltageRegulatorOn(final boolean voltageRegulatorOn) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setRegulatingTerminal(final Terminal regulatingTerminal) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setTargetP(final double targetP) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setTargetQ(final double targetQ) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setTargetV(final double targetV) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdderAdapter setRatedS(final double ratedS) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdapter add() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
