/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.TapChanger;
import com.powsybl.iidm.network.TapChangerStep;
import com.powsybl.iidm.network.Terminal;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
abstract class AbstractTapChangerAdapter<P extends TapChanger<P, S>, S extends TapChangerStep<S>> extends AbstractAdapter<P> implements TapChanger<P, S> {

    AbstractTapChangerAdapter(P delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Terminal getRegulationTerminal() {
        return getIndex().getTerminal(getDelegate().getRegulationTerminal());
    }

    @Override
    public P setRegulationTerminal(final Terminal regulationTerminal) {
        Terminal terminal = regulationTerminal;
        if (regulationTerminal instanceof TerminalAdapter) {
            terminal = ((TerminalAdapter) terminal).getDelegate();
        }
        getDelegate().setRegulationTerminal(terminal);
        return (P) this;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public int getLowTapPosition() {
        return getDelegate().getLowTapPosition();
    }

    @Override
    public P setLowTapPosition(final int lowTapPosition) {
        getDelegate().setLowTapPosition(lowTapPosition);
        return (P) this;
    }

    @Override
    public int getHighTapPosition() {
        return getDelegate().getHighTapPosition();
    }

    @Override
    public int getTapPosition() {
        return getDelegate().getTapPosition();
    }

    @Override
    public OptionalInt findTapPosition() {
        return getDelegate().findTapPosition();
    }

    @Override
    public P setTapPosition(final int tapPosition) {
        getDelegate().setTapPosition(tapPosition);
        return (P) this;
    }

    @Override
    public P unsetTapPosition() {
        getDelegate().unsetTapPosition();
        return (P) this;
    }

    @Override
    public int getStepCount() {
        return getDelegate().getStepCount();
    }

    @Override
    public S getStep(final int tapPosition) {
        return getDelegate().getStep(tapPosition);
    }

    @Override
    public S getCurrentStep() {
        return getDelegate().getCurrentStep();
    }

    @Override
    public Optional<Boolean> isRegulating() {
        return getDelegate().isRegulating();
    }

    @Override
    public P setRegulating(final boolean regulating) {
        getDelegate().setRegulating(regulating);
        return (P) this;
    }

    @Override
    public double getTargetDeadband() {
        return getDelegate().getTargetDeadband();
    }

    @Override
    public P setTargetDeadband(final double targetDeadband) {
        getDelegate().setTargetDeadband(targetDeadband);
        return (P) this;
    }

    @Override
    public void remove() {
        getDelegate().remove();
    }
}
