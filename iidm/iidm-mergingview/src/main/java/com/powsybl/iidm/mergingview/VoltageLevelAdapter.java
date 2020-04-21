/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public interface VoltageLevelAdapter extends VoltageLevel {

    interface BusBreakerViewExt extends BusBreakerView {

        Bus getBus(Bus bus);

    }

    interface BusViewExt extends BusView {

        Bus getBus(Bus bus);

    }

    @Override BusBreakerViewExt getBusBreakerView();

    @Override BusViewExt getBusView();

    void invalidateCache();
}
