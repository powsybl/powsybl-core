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

    RatioTapChangerAdapter(final RatioTapChanger delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public double getTargetV() {
        return getDelegate().getTargetV();
    }

    @Override
    public RatioTapChanger setTargetV(final double targetV) {
        getDelegate().setTargetV(targetV);
        return this;
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return getDelegate().hasLoadTapChangingCapabilities();
    }

    @Override
    public RatioTapChanger setLoadTapChangingCapabilities(final boolean status) {
        getDelegate().setLoadTapChangingCapabilities(status);
        return this;
    }
}
