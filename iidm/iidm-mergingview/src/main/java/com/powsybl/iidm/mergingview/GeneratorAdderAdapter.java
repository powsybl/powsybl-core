/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class GeneratorAdderAdapter extends AbstractInjectionAdderAdapter<GeneratorAdder> implements GeneratorAdder {

    GeneratorAdderAdapter(final GeneratorAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Generator add() {
        checkAndSetUniqueId();
        return getIndex().getGenerator(getDelegate().add());
    }

    @Override
    public GeneratorAdder setRegulatingTerminal(final Terminal regulatingTerminal) {
        Terminal terminal = regulatingTerminal;
        if (terminal instanceof TerminalAdapter) {
            terminal = ((TerminalAdapter) terminal).getDelegate();
        }
        getDelegate().setRegulatingTerminal(terminal);
        return this;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public GeneratorAdder setEnergySource(final EnergySource energySource) {
        getDelegate().setEnergySource(energySource);
        return this;
    }

    @Override
    public GeneratorAdder setMinP(final double minP) {
        getDelegate().setMinP(minP);
        return this;
    }

    @Override
    public GeneratorAdder setMaxP(final double maxP) {
        getDelegate().setMaxP(maxP);
        return this;
    }

    @Override
    public GeneratorAdder setVoltageRegulatorOn(final boolean voltageRegulatorOn) {
        getDelegate().setVoltageRegulatorOn(voltageRegulatorOn);
        return this;
    }

    @Override
    public GeneratorAdder setTargetP(final double targetP) {
        getDelegate().setTargetP(targetP);
        return this;
    }

    @Override
    public GeneratorAdder setTargetQ(final double targetQ) {
        getDelegate().setTargetQ(targetQ);
        return this;
    }

    @Override
    public GeneratorAdder setTargetV(final double targetV) {
        getDelegate().setTargetV(targetV);
        return this;
    }

    @Override
    public GeneratorAdder setRatedS(final double ratedS) {
        getDelegate().setRatedS(ratedS);
        return this;
    }
}
