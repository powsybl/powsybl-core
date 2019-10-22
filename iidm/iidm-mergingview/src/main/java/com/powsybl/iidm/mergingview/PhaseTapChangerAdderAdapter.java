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

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public PhaseTapChangerAdderAdapter setLowTapPosition(final int lowTapPosition) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerAdderAdapter setTapPosition(final int tapPosition) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerAdderAdapter setRegulating(final boolean regulating) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerAdderAdapter setRegulationMode(final RegulationMode regulationMode) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerAdderAdapter setRegulationValue(final double regulationValue) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerAdderAdapter setRegulationTerminal(final Terminal regulationTerminal) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerStepAdderAdapter beginStep() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerAdapter add() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
