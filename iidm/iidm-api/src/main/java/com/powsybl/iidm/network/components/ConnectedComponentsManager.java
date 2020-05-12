/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.network.components;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import gnu.trove.list.array.TIntArrayList;

import java.util.Map;
import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class ConnectedComponentsManager extends AbstractComponentsManager<ConnectedComponent> {

    public ConnectedComponentsManager(Network network) {
        super(network);
    }

    @Override
    protected void fillAdjacencyList(Map<String, Integer> id2num, TIntArrayList[] adjacencyList) {
        super.fillAdjacencyList(id2num, adjacencyList);
        for (HvdcLine line : network.getHvdcLines()) {
            Bus bus1 = line.getConverterStation1().getTerminal().getBusView().getBus();
            Bus bus2 = line.getConverterStation2().getTerminal().getBusView().getBus();
            addToAdjacencyList(bus1, bus2, id2num, adjacencyList);
        }
    }

    @Override
    protected String getComponentLabel() {
        return "Connected";
    }

    @Override
    protected void setComponentNumber(Bus bus, int num) {
        Objects.requireNonNull(bus);
        bus.setConnectedComponentNumber(num);
    }

    protected ConnectedComponent createComponent(int num, int size) {
        return new ConnectedComponent(network, num, size);
    }
}
