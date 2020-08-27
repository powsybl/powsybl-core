/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class TopologyListener extends DefaultMergingViewListener {

    TopologyListener(final MergingViewIndex index) {
        super(index);
    }

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        if ((identifiable instanceof VoltageLevel) && attribute.equals("topology")) {
            VoltageLevelAdapter adapter = index.getVoltageLevel((VoltageLevel) identifiable);
            adapter.invalidateCache();
        }
    }
}
