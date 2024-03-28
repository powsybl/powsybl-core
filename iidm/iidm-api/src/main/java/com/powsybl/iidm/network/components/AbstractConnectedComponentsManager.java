/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.components;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.HvdcLine;
import gnu.trove.list.array.TIntArrayList;

import java.util.Map;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public abstract class AbstractConnectedComponentsManager<C extends Component> extends AbstractComponentsManager<C> {

    protected AbstractConnectedComponentsManager() {
        super("Connected");
    }

    @Override
    protected void fillAdjacencyList(Map<String, Integer> id2num, TIntArrayList[] adjacencyList) {
        super.fillAdjacencyList(id2num, adjacencyList);
        for (HvdcLine line : getNetwork().getHvdcLines()) {
            Bus bus1 = line.getConverterStation1().getTerminal().getBusView().getBus();
            Bus bus2 = line.getConverterStation2().getTerminal().getBusView().getBus();
            addToAdjacencyList(bus1, bus2, id2num, adjacencyList);
        }
    }

}
