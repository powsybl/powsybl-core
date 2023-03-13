/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.LccConverterStationAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LccConverterStationAdderAdapter extends AbstractHvdcConverterStationAdderAdapter<LccConverterStationAdder, LccConverterStation> implements LccConverterStationAdder {

    LccConverterStationAdderAdapter(LccConverterStationAdder delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public LccConverterStation add() {
        return getIndex().getLccConverterStation(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public LccConverterStationAdder setPowerFactor(float powerFactor) {
        getDelegate().setPowerFactor(powerFactor);
        return this;
    }
}
