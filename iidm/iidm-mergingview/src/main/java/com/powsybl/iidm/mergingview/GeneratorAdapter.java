/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

import java.util.Optional;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class GeneratorAdapter extends AbstractInjectionAdapter<Generator> implements Generator {

    GeneratorAdapter(final Generator delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Generator setRegulatingTerminal(final Terminal regulatingTerminal) {
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
    public EnergySource getEnergySource() {
        return getDelegate().getEnergySource();
    }

    @Override
    public Generator setEnergySource(final EnergySource energySource) {
        getDelegate().setEnergySource(energySource);
        return this;
    }

    @Override
    public double getMaxP() {
        return getDelegate().getMaxP();
    }

    @Override
    public Generator setMaxP(final double maxP) {
        getDelegate().setMaxP(maxP);
        return this;
    }

    @Override
    public double getMinP() {
        return getDelegate().getMinP();
    }

    @Override
    public Generator setMinP(final double minP) {
        getDelegate().setMinP(minP);
        return this;
    }

    @Override
    public Optional<Boolean> isVoltageRegulatorOn() {
        return getDelegate().isVoltageRegulatorOn();
    }

    @Override
    public Generator setVoltageRegulatorOn(final boolean voltageRegulatorOn) {
        getDelegate().setVoltageRegulatorOn(voltageRegulatorOn);
        return this;
    }

    @Override
    public Generator unsetVoltageRegulatorOn() {
        getDelegate().unsetVoltageRegulatorOn();
        return this;
    }

    @Override
    public double getTargetV() {
        return getDelegate().getTargetV();
    }

    @Override
    public Generator setTargetV(final double targetV) {
        getDelegate().setTargetV(targetV);
        return this;
    }

    @Override
    public double getTargetP() {
        return getDelegate().getTargetP();
    }

    @Override
    public Generator setTargetP(final double targetP) {
        getDelegate().setTargetP(targetP);
        return this;
    }

    @Override
    public double getTargetQ() {
        return getDelegate().getTargetQ();
    }

    @Override
    public Generator setTargetQ(final double targetQ) {
        getDelegate().setTargetQ(targetQ);
        return this;
    }

    @Override
    public double getRatedS() {
        return getDelegate().getRatedS();
    }

    @Override
    public Generator setRatedS(final double ratedS) {
        getDelegate().setRatedS(ratedS);
        return this;
    }
}
