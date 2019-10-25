/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class RatioTapChangerAdapter extends AbstractAdapter<RatioTapChanger> implements RatioTapChanger {

    protected RatioTapChangerAdapter(final RatioTapChanger delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public TerminalAdapter getRegulationTerminal() {
        return getIndex().getTerminal(getDelegate().getRegulationTerminal());
    }

    @Override
    public RatioTapChangerAdapter setRegulationTerminal(final Terminal regulationTerminal) {
        Terminal param = regulationTerminal;
        if (param instanceof AbstractAdapter<?>) {
            param = ((AbstractAdapter<Terminal>) param).getDelegate();
        }
        getDelegate().setRegulationTerminal(param);
        return this;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public int getLowTapPosition() {
        return getDelegate().getLowTapPosition();
    }

    @Override
    public RatioTapChangerAdapter setLowTapPosition(final int lowTapPosition) {
        getDelegate().setLowTapPosition(lowTapPosition);
        return this;
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
    public RatioTapChangerAdapter setTapPosition(final int tapPosition) {
        getDelegate().setTapPosition(tapPosition);
        return this;
    }

    @Override
    public int getStepCount() {
        return getDelegate().getStepCount();
    }

    @Override
    public RatioTapChangerStep getStep(final int tapPosition) {
        return getDelegate().getStep(tapPosition);
    }

    @Override
    public RatioTapChangerStep getCurrentStep() {
        return getDelegate().getCurrentStep();
    }

    @Override
    public boolean isRegulating() {
        return getDelegate().isRegulating();
    }

    @Override
    public RatioTapChangerAdapter setRegulating(final boolean regulating) {
        getDelegate().setRegulating(regulating);
        return this;
    }

    @Override
    public double getTargetV() {
        return getDelegate().getTargetV();
    }

    @Override
    public RatioTapChangerAdapter setTargetV(final double targetV) {
        getDelegate().setTargetV(targetV);
        return this;
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return getDelegate().hasLoadTapChangingCapabilities();
    }

    @Override
    public RatioTapChangerAdapter setLoadTapChangingCapabilities(final boolean status) {
        getDelegate().setLoadTapChangingCapabilities(status);
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return getDelegate().getTargetDeadband();
    }

    @Override
    public RatioTapChangerAdapter setTargetDeadband(final double targetDeadband) {
        getDelegate().setTargetDeadband(targetDeadband);
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
