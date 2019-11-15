/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerStep;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class PhaseTapChangerAdapter extends AbstractTapChangerAdapter<PhaseTapChanger, PhaseTapChangerStep> implements PhaseTapChanger {

    protected PhaseTapChangerAdapter(final PhaseTapChanger delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public RegulationMode getRegulationMode() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerAdapter setRegulationMode(final RegulationMode regulationMode) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getRegulationValue() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerAdapter setRegulationValue(final double regulationValue) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
