/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerStep;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class PhaseTapChangerAdapter extends AbstractAdapter<PhaseTapChanger> implements PhaseTapChanger {

    protected PhaseTapChangerAdapter(final PhaseTapChanger delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public TerminalAdapter getRegulationTerminal() {
        return getIndex().getTerminal(getDelegate().getRegulationTerminal());
    }

    @Override
    public PhaseTapChangerAdapter setRegulationTerminal(final Terminal regulationTerminal) {
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
    public PhaseTapChangerAdapter setLowTapPosition(final int lowTapPosition) {
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
    public PhaseTapChangerAdapter setTapPosition(final int tapPosition) {
        getDelegate().setTapPosition(tapPosition);
        return this;
    }

    @Override
    public int getStepCount() {
        return getDelegate().getStepCount();
    }

    @Override
    public PhaseTapChangerStep getStep(final int tapPosition) {
        return getDelegate().getStep(tapPosition);
    }

    @Override
    public PhaseTapChangerStep getCurrentStep() {
        return getDelegate().getCurrentStep();
    }

    @Override
    public boolean isRegulating() {
        return getDelegate().isRegulating();
    }

    @Override
    public PhaseTapChangerAdapter setRegulating(final boolean regulating) {
        getDelegate().setRegulating(regulating);
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return getDelegate().getRegulationMode();
    }

    @Override
    public PhaseTapChangerAdapter setRegulationMode(final RegulationMode regulationMode) {
        getDelegate().setRegulationMode(regulationMode);
        return this;
    }

    @Override
    public double getRegulationValue() {
        return getDelegate().getRegulationValue();
    }

    @Override
    public PhaseTapChangerAdapter setRegulationValue(final double regulationValue) {
        getDelegate().setRegulationValue(regulationValue);
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return getDelegate().getTargetDeadband();
    }

    @Override
    public PhaseTapChangerAdapter setTargetDeadband(final double targetDeadband) {
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
