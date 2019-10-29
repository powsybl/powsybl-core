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
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.MinMaxReactiveLimitsAdder;
import com.powsybl.iidm.network.ReactiveCapabilityCurveAdder;
import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class GeneratorAdapter extends AbstractIdentifiableAdapter<Generator> implements Generator {

    protected GeneratorAdapter(final Generator delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public List<? extends TerminalAdapter> getTerminals() {
        return getDelegate().getTerminals().stream()
            .map(getIndex()::getTerminal)
            .collect(Collectors.toList());
    }


    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public TerminalAdapter getTerminal() {
        return getIndex().getTerminal(getDelegate().getTerminal());
    }

    @Override
    public ConnectableType getType() {
        return getDelegate().getType();
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
    public EnergySource getEnergySource() {
        return getDelegate().getEnergySource();
    }

    @Override
    public GeneratorAdapter setEnergySource(final EnergySource energySource) {
        getDelegate().setEnergySource(energySource);
        return this;
    }

    @Override
    public double getMaxP() {
        return getDelegate().getMaxP();
    }

    @Override
    public GeneratorAdapter setMaxP(final double maxP) {
        getDelegate().setMaxP(maxP);
        return this;
    }

    @Override
    public double getMinP() {
        return getDelegate().getMinP();
    }

    @Override
    public GeneratorAdapter setMinP(final double minP) {
        getDelegate().setMinP(minP);
        return this;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return getDelegate().isVoltageRegulatorOn();
    }

    @Override
    public GeneratorAdapter setVoltageRegulatorOn(final boolean voltageRegulatorOn) {
        getDelegate().setVoltageRegulatorOn(voltageRegulatorOn);
        return this;
    }

    @Override
    public double getTargetV() {
        return getDelegate().getTargetV();
    }

    @Override
    public GeneratorAdapter setTargetV(final double targetV) {
        getDelegate().setTargetV(targetV);
        return this;
    }

    @Override
    public double getTargetP() {
        return getDelegate().getTargetP();
    }

    @Override
    public GeneratorAdapter setTargetP(final double targetP) {
        getDelegate().setTargetP(targetP);
        return this;
    }

    @Override
    public double getTargetQ() {
        return getDelegate().getTargetQ();
    }

    @Override
    public GeneratorAdapter setTargetQ(final double targetQ) {
        getDelegate().setTargetQ(targetQ);
        return this;
    }

    @Override
    public double getRatedS() {
        return getDelegate().getRatedS();
    }

    @Override
    public GeneratorAdapter setRatedS(final double ratedS) {
        getDelegate().setRatedS(ratedS);
        return this;
    }

    @Override
    public TerminalAdapter getRegulatingTerminal() {
        return getIndex().getTerminal(getDelegate().getRegulatingTerminal());
    }

    @Override
    public GeneratorAdapter setRegulatingTerminal(final Terminal regulatingTerminal) {
        getDelegate().setRegulatingTerminal(regulatingTerminal);
        return this;
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
