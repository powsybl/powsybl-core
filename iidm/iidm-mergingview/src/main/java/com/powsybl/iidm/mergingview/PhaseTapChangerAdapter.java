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

    PhaseTapChangerAdapter(final PhaseTapChanger delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public RegulationMode getRegulationMode() {
        return getDelegate().getRegulationMode();
    }

    @Override
    public PhaseTapChanger setRegulationMode(final RegulationMode regulationMode) {
        getDelegate().setRegulationMode(regulationMode);
        return this;
    }

    @Override
    public double getRegulationValue() {
        return getDelegate().getRegulationValue();
    }

    @Override
    public PhaseTapChanger setRegulationValue(final double regulationValue) {
        getDelegate().setRegulationValue(regulationValue);
        return this;
    }

    @Override
    public PhaseTapChanger unsetRegulating() {
        getDelegate().unsetRegulating();
        return this;
    }
}
