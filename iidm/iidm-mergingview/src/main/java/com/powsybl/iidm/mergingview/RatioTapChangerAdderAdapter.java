/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class RatioTapChangerAdderAdapter extends AbstractAdapter<RatioTapChangerAdder> implements RatioTapChangerAdder {

    RatioTapChangerAdderAdapter(final RatioTapChangerAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public RatioTapChanger add() {
        return getIndex().getRatioTapChanger(getDelegate().add());
    }

    @Override
    public RatioTapChangerAdder setRegulationTerminal(final Terminal regulationTerminal) {
        // RatioTapChangerAdderImpl.setRegulationTerminal is casting parameter to TerminalExt
        // So, we need to check if parameter is adapted or not
        Terminal terminal = regulationTerminal;
        if (regulationTerminal instanceof TerminalAdapter) {
            terminal = ((TerminalAdapter) terminal).getDelegate();
        }
        getDelegate().setRegulationTerminal(terminal);
        return this;
    }

    @Override
    public RatioTapChangerAdder.StepAdder beginStep() {
        return new RatioTapChangerStepAdderAdapter(this, getDelegate().beginStep(), getIndex());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public RatioTapChangerAdder setLowTapPosition(final int lowTapPosition) {
        getDelegate().setLowTapPosition(lowTapPosition);
        return this;
    }

    @Override
    public RatioTapChangerAdder setTapPosition(final int tapPosition) {
        getDelegate().setTapPosition(tapPosition);
        return this;
    }

    @Override
    public RatioTapChangerAdder setLoadTapChangingCapabilities(final boolean loadTapChangingCapabilities) {
        getDelegate().setLoadTapChangingCapabilities(loadTapChangingCapabilities);
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulating(final boolean regulating) {
        getDelegate().setRegulating(regulating);
        return this;
    }

    @Override
    public RatioTapChangerAdder setTargetV(final double targetV) {
        getDelegate().setTargetV(targetV);
        return this;
    }

    @Override
    public RatioTapChangerAdder setTargetDeadband(final double targetDeadband) {
        getDelegate().setTargetDeadband(targetDeadband);
        return this;
    }
}
