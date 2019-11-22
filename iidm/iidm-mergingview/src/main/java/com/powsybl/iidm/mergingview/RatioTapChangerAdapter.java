/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class RatioTapChangerAdapter extends AbstractTapChangerAdapter<RatioTapChanger, RatioTapChangerStep> implements RatioTapChanger {

    protected RatioTapChangerAdapter(final RatioTapChanger delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public double getTargetV() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public RatioTapChangerAdapter setTargetV(final double targetV) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public RatioTapChangerAdapter setLoadTapChangingCapabilities(final boolean status) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
