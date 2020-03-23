/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.util.ConnectedComponentImpl;
import gnu.trove.list.array.TIntArrayList;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;

/**
 * @author Thomas ADAM <tadam at silicom.fr>
 */
public class ConnectedComponentsManager extends AbstractComponentsManager<ConnectedComponentImpl> {

    public ConnectedComponentsManager(Network network) {
        super(network);
    }

    @Override
    public void fillAdjacencyList(Map<String, Integer> id2num, TIntArrayList[] adjacencyList) {
        super.fillAdjacencyList(id2num, adjacencyList);
        for (HvdcLine line : network.getHvdcLines()) {
            Bus bus1 = line.getConverterStation1().getTerminal().getBusView().getBus();
            Bus bus2 = line.getConverterStation2().getTerminal().getBusView().getBus();
            addToAdjacencyList(bus1, bus2, id2num, adjacencyList);
        }
    }

    @Override
    public String getComponentLabel() {
        return "Connected";
    }

    @Override
    public void setComponentNumber(Bus bus, int num) {
        Objects.requireNonNull(bus);
        System.out.println("ConnectedComponentsManager::setComponentNumber(" + bus.getId() + ", " + num + ")");
        System.out.print("   -> ");
        bus.setConnectedComponentNumber(num);
    }

    @Override
    public ConnectedComponentImpl createComponent(int num, int size) {
        return new ConnectedComponentImpl(num, size, new WeakReference<>(network));
    }
}
