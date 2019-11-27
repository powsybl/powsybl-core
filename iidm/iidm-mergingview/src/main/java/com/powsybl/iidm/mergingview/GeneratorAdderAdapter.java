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
public class GeneratorAdderAdapter extends AbstractIdentifiableAdderAdapter<GeneratorAdder> implements GeneratorAdder {

    GeneratorAdderAdapter(final GeneratorAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public GeneratorAdapter add() {
        checkAndSetUniqueId();
        return new GeneratorAdapter(getDelegate().add(), getIndex());
    }

    @Override
    public GeneratorAdderAdapter setRegulatingTerminal(final Terminal regulatingTerminal) {
        Terminal param = regulatingTerminal;
        if (param instanceof TerminalAdapter) {
            param = ((TerminalAdapter) param).getDelegate();
        }
        getDelegate().setRegulatingTerminal(param);
        return this;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public GeneratorAdderAdapter setNode(final int node) {
        getDelegate().setNode(node);
        return this;
    }

    @Override
    public GeneratorAdderAdapter setBus(final String bus) {
        getDelegate().setBus(bus);
        return this;
    }

    @Override
    public GeneratorAdderAdapter setConnectableBus(final String connectableBus) {
        getDelegate().setConnectableBus(connectableBus);
        return this;
    }

    @Override
    public GeneratorAdderAdapter setEnergySource(final EnergySource energySource) {
        getDelegate().setEnergySource(energySource);
        return this;
    }

    @Override
    public GeneratorAdderAdapter setMinP(final double minP) {
        getDelegate().setMinP(minP);
        return this;
    }

    @Override
    public GeneratorAdderAdapter setMaxP(final double maxP) {
        getDelegate().setMaxP(maxP);
        return this;
    }

    @Override
    public GeneratorAdderAdapter setVoltageRegulatorOn(final boolean voltageRegulatorOn) {
        getDelegate().setVoltageRegulatorOn(voltageRegulatorOn);
        return this;
    }

    @Override
    public GeneratorAdderAdapter setTargetP(final double targetP) {
        getDelegate().setTargetP(targetP);
        return this;
    }

    @Override
    public GeneratorAdderAdapter setTargetQ(final double targetQ) {
        getDelegate().setTargetQ(targetQ);
        return this;
    }

    @Override
    public GeneratorAdderAdapter setTargetV(final double targetV) {
        getDelegate().setTargetV(targetV);
        return this;
    }

    @Override
    public GeneratorAdderAdapter setRatedS(final double ratedS) {
        getDelegate().setRatedS(ratedS);
        return this;
    }
}
