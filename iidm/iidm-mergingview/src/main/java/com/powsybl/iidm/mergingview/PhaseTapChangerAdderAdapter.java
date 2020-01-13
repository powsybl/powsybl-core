/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChanger.RegulationMode;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class PhaseTapChangerAdderAdapter extends AbstractAdapter<PhaseTapChangerAdder> implements PhaseTapChangerAdder {

    private class StepAdderAdapter extends AbstractAdapter<PhaseTapChangerAdder.StepAdder> implements PhaseTapChangerAdder.StepAdder {

        StepAdderAdapter(final PhaseTapChangerAdder.StepAdder delegate, final MergingViewIndex index) {
            super(delegate, index);
        }

        @Override
        public PhaseTapChangerAdder endStep() {
            getDelegate().endStep();
            return PhaseTapChangerAdderAdapter.this;
        }

        // -------------------------------
        // Simple delegated methods ------
        // -------------------------------
        @Override
        public PhaseTapChangerAdder.StepAdder setAlpha(final double alpha) {
            getDelegate().setAlpha(alpha);
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setRho(final double rho) {
            getDelegate().setRho(rho);
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setR(final double r) {
            getDelegate().setR(r);
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setX(final double x) {
            getDelegate().setX(x);
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setG(final double g) {
            getDelegate().setG(g);
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setB(final double b) {
            getDelegate().setB(b);
            return this;
        }
    }

    PhaseTapChangerAdderAdapter(final PhaseTapChangerAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public PhaseTapChanger add() {
        return getIndex().getPhaseTapChanger(getDelegate().add());
    }

    @Override
    public PhaseTapChangerAdder setRegulationTerminal(final Terminal regulationTerminal) {
        // PhaseTapChangerAdderImpl.setRegulationTerminal is casting parameter to
        // TerminalExt
        // So, we need to check if parameter is adapted or not
        Terminal terminal = regulationTerminal;
        if (regulationTerminal instanceof TerminalAdapter) {
            terminal = ((TerminalAdapter) terminal).getDelegate();
        }
        getDelegate().setRegulationTerminal(terminal);
        return this;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public PhaseTapChangerAdder setLowTapPosition(final int lowTapPosition) {
        getDelegate().setLowTapPosition(lowTapPosition);
        return this;
    }

    @Override
    public PhaseTapChangerAdder setTapPosition(final int tapPosition) {
        getDelegate().setTapPosition(tapPosition);
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulating(final boolean regulating) {
        getDelegate().setRegulating(regulating);
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulationMode(final RegulationMode regulationMode) {
        getDelegate().setRegulationMode(regulationMode);
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulationValue(final double regulationValue) {
        getDelegate().setRegulationValue(regulationValue);
        return this;
    }

    @Override
    public PhaseTapChangerAdder setTargetDeadband(final double targetDeadband) {
        getDelegate().setTargetDeadband(targetDeadband);
        return this;
    }

    @Override
    public PhaseTapChangerAdder.StepAdder beginStep() {
        return new PhaseTapChangerAdderAdapter.StepAdderAdapter(getDelegate().beginStep(), getIndex());
    }
}
