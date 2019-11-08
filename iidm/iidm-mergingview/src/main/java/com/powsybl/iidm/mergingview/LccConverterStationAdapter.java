/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.LccConverterStation;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LccConverterStationAdapter extends AbstractHvdcConverterStationAdapter<LccConverterStation> implements LccConverterStation {

    LccConverterStationAdapter(final LccConverterStation delegate, final MergingViewIndex index) {
        super(delegate, index);
    }


    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public float getPowerFactor() {
        return getDelegate().getPowerFactor();
    }

    @Override
    public LccConverterStationAdapter setPowerFactor(final float powerFactor) {
        getDelegate().setPowerFactor(powerFactor);
        return this;
    }
}
