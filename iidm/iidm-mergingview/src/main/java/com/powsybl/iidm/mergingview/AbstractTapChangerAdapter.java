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

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
abstract class AbstractTapChangerAdapter<P extends TapChanger<P, S>, S extends TapChangerStep<S>> extends AbstractAdapter<P> implements TapChanger<P, S> {

    protected AbstractTapChangerAdapter(P delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public int getLowTapPosition() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public P setLowTapPosition(int lowTapPosition) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getHighTapPosition() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getTapPosition() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public P setTapPosition(int tapPosition) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getStepCount() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public S getStep(int tapPosition) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public S getCurrentStep() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean isRegulating() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public P setRegulating(boolean regulating) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Terminal getRegulationTerminal() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public P setRegulationTerminal(Terminal regulationTerminal) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getTargetDeadband() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public P setTargetDeadband(double targetDeadband) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
