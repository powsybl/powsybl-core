/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.components;

import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.GraphUtil;
import com.powsybl.math.graph.GraphUtil.ConnectedComponentsComputationResult;
import gnu.trove.list.array.TIntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
abstract class AbstractComponentsManager<C extends Component> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractComponentsManager.class);

    private final String label;

    private List<C> components;

    protected AbstractComponentsManager(String label) {
        this.label = Objects.requireNonNull(label);
    }

    public void invalidate() {
        components = null;
    }

    public void update() {
        if (components != null) {
            return;
        }

        long startTime = System.currentTimeMillis();

        // reset
        for (Bus bus : getNetwork().getBusBreakerView().getBuses()) {
            setComponentNumber(bus, -1);
        }

        int num = 0;
        Map<String, Integer> id2num = new HashMap<>();
        List<Bus> num2bus = new ArrayList<>();
        for (Bus bus : getNetwork().getBusView().getBuses()) {
            num2bus.add(bus);
            id2num.put(bus.getId(), num);
            num++;
        }
        TIntArrayList[] adjacencyList = new TIntArrayList[num];
        for (int i = 0; i < adjacencyList.length; i++) {
            adjacencyList[i] = new TIntArrayList(3);
        }
        fillAdjacencyList(id2num, adjacencyList);

        ConnectedComponentsComputationResult result = GraphUtil.computeConnectedComponents(adjacencyList);

        components = new ArrayList<>(result.getComponentSize().length);
        for (int i = 0; i < result.getComponentSize().length; i++) {
            components.add(createComponent(i, result.getComponentSize()[i]));
        }

        for (int i = 0; i < result.getComponentNumber().length; i++) {
            Bus bus = num2bus.get(i);
            setComponentNumber(bus, result.getComponentNumber()[i]);
        }

        LOGGER.debug("{} components computed in {} ms", getComponentLabel(), System.currentTimeMillis() - startTime);
    }

    public List<C> getConnectedComponents() {
        update();
        return components;
    }

    public C getComponent(int num) {
        // update() must not be put here, but explicitly called each time before because update may
        // trigger a new component computation and so on a change in the value of the num component already passed
        // (and outdated consequently) in parameter of this method
        return num != -1 ? components.get(num) : null;
    }

    void addToAdjacencyList(Bus bus1, Bus bus2, Map<String, Integer> id2num, TIntArrayList[] adjacencyList) {
        if (bus1 != null && bus2 != null) {
            int busNum1 = id2num.get(bus1.getId());
            int busNum2 = id2num.get(bus2.getId());
            adjacencyList[busNum1].add(busNum2);
            adjacencyList[busNum2].add(busNum1);
        }
    }

    protected void fillAdjacencyList(Map<String, Integer> id2num, TIntArrayList[] adjacencyList) {
        for (Line line : getNetwork().getLines()) {
            Bus bus1 = line.getTerminal1().getBusView().getBus();
            Bus bus2 = line.getTerminal2().getBusView().getBus();
            addToAdjacencyList(bus1, bus2, id2num, adjacencyList);
        }
        for (TieLine tl : getNetwork().getTieLines()) {
            Bus bus1 = tl.getDanglingLine1().getTerminal().getBusView().getBus();
            Bus bus2 = tl.getDanglingLine2().getTerminal().getBusView().getBus();
            addToAdjacencyList(bus1, bus2, id2num, adjacencyList);
        }
        for (TwoWindingsTransformer transfo : getNetwork().getTwoWindingsTransformers()) {
            Bus bus1 = transfo.getTerminal1().getBusView().getBus();
            Bus bus2 = transfo.getTerminal2().getBusView().getBus();
            addToAdjacencyList(bus1, bus2, id2num, adjacencyList);
        }
        for (ThreeWindingsTransformer transfo : getNetwork().getThreeWindingsTransformers()) {
            Bus bus1 = transfo.getLeg1().getTerminal().getBusView().getBus();
            Bus bus2 = transfo.getLeg2().getTerminal().getBusView().getBus();
            Bus bus3 = transfo.getLeg3().getTerminal().getBusView().getBus();
            addToAdjacencyList(bus1, bus2, id2num, adjacencyList);
            addToAdjacencyList(bus1, bus3, id2num, adjacencyList);
            addToAdjacencyList(bus2, bus3, id2num, adjacencyList);
        }
    }

    private String getComponentLabel() {
        return label;
    }

    protected abstract Network getNetwork();

    protected abstract C createComponent(int num, int size);

    protected abstract void setComponentNumber(Bus bus, int num);

}
