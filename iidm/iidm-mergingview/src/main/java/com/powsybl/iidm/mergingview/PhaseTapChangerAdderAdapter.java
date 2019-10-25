/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.PhaseTapChanger.RegulationMode;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class PhaseTapChangerAdderAdapter extends AbstractAdapter<PhaseTapChangerAdder> implements PhaseTapChangerAdder {

    protected PhaseTapChangerAdderAdapter(final PhaseTapChangerAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public PhaseTapChangerAdapter add() {
        return getIndex().getPhaseTapChanger(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public PhaseTapChangerAdderAdapter setLowTapPosition(final int lowTapPosition) {
        getDelegate().setLowTapPosition(lowTapPosition);
        return this;
    }

    @Override
    public PhaseTapChangerAdderAdapter setTapPosition(final int tapPosition) {
        getDelegate().setTapPosition(tapPosition);
        return this;
    }

    @Override
    public PhaseTapChangerAdderAdapter setRegulating(final boolean regulating) {
        getDelegate().setRegulating(regulating);
        return this;
    }

    @Override
    public PhaseTapChangerAdderAdapter setRegulationMode(final RegulationMode regulationMode) {
        getDelegate().setRegulationMode(regulationMode);
        return this;
    }

    @Override
    public PhaseTapChangerAdderAdapter setRegulationValue(final double regulationValue) {
        getDelegate().setRegulationValue(regulationValue);
        return this;
    }

    @Override
    public PhaseTapChangerAdderAdapter setRegulationTerminal(final Terminal regulationTerminal) {
        // PhaseTapChangerAdderImpl.setRegulationTerminal is casting parameter to
        // TerminalExt
        // So, we need to check if parameter is adapted or not
        final boolean isAdapted = regulationTerminal instanceof AbstractAdapter<?>;
        getDelegate().setRegulationTerminal(isAdapted ? ((AbstractAdapter<Terminal>) regulationTerminal).getDelegate() : regulationTerminal);
        return this;
    }

    @Override
    public PhaseTapChangerAdderAdapter setTargetDeadband(final double targetDeadband) {
        getDelegate().setTargetDeadband(targetDeadband);
        return this;
    }

    @Override
    public PhaseTapChangerStepAdderAdapter beginStep() {
        return new PhaseTapChangerStepAdderAdapter(this, getDelegate().beginStep(), getIndex());
    }
}
