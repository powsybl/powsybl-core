/*
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.google.common.util.concurrent.AtomicDouble;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.Load;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ConnectedComponents {

    private ConnectedComponents() {
    }

    public static int getCcNum(Bus b) {
        int ccNum = -1;
        if (b != null) {
            Component cc = b.getConnectedComponent();
            if (cc != null) {
                ccNum = cc.getNum();
            }
        }
        return ccNum;
    }

    public static double computeTotalActiveLoad(Component component) {
        Objects.requireNonNull(component);
        final AtomicDouble totalLoad = new AtomicDouble(0.);
        component.getBusStream().forEach(bus -> totalLoad.addAndGet(bus.getLoadStream().filter(load -> load.getTerminal().isConnected()).mapToDouble(Load::getP0).sum()));
        return totalLoad.get();
    }
}
