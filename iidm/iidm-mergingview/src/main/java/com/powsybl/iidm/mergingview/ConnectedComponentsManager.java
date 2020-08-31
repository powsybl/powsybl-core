/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.components.AbstractConnectedComponentsManager;
import com.powsybl.iidm.network.components.ConnectedComponent;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
class ConnectedComponentsManager extends AbstractConnectedComponentsManager<ConnectedComponent> {

    private final MergingView mergingView;

    ConnectedComponentsManager(MergingView mergingView) {
        this.mergingView = Objects.requireNonNull(mergingView);
    }

    @Override
    protected Network getNetwork() {
        return mergingView;
    }

    @Override
    protected ConnectedComponent createComponent(int num, int size) {
        return new ConnectedComponent(mergingView, num, size);
    }

    @Override
    protected void setComponentNumber(Bus bus, int num) {
        Objects.requireNonNull(bus);

        BusAdapter adapter = (BusAdapter) bus;
        adapter.setConnectedComponentNumber(num);
    }
}
