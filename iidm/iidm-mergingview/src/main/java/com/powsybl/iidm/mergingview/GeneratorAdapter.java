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
public class GeneratorAdapter extends AbstractInjectionAdapter<Generator> implements Generator {

    protected GeneratorAdapter(final Generator delegate, final MergingViewIndex index) {
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
    public EnergySource getEnergySource() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdapter setEnergySource(final EnergySource energySource) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getMaxP() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdapter setMaxP(final double maxP) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getMinP() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdapter setMinP(final double minP) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdapter setVoltageRegulatorOn(final boolean voltageRegulatorOn) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TerminalAdapter getRegulatingTerminal() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdapter setRegulatingTerminal(final Terminal regulatingTerminal) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getTargetV() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdapter setTargetV(final double targetV) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getTargetP() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdapter setTargetP(final double targetP) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getTargetQ() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdapter setTargetQ(final double targetQ) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getRatedS() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public GeneratorAdapter setRatedS(final double ratedS) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
