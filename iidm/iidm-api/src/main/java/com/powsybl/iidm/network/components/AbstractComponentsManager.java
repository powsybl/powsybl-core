/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
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

    private final boolean ac;

    private final boolean dc;

    protected AbstractComponentsManager(String label, boolean ac, boolean dc) {
        this.label = Objects.requireNonNull(label);
        this.ac = ac;
        this.dc = dc;
    }

    public void invalidate() {
        components = null;
    }

    public void update() {
        if (components != null) {
            return;
        }

        long startTime = System.currentTimeMillis();

        reset();

        int num = 0;
        int numAc = 0;
        Map<String, Integer> acBusId2num = new HashMap<>();
        Map<String, Integer> dcBusId2num = new HashMap<>();
        List<Bus> num2AcBus = new ArrayList<>();
        List<DcBus> num2DcBus = new ArrayList<>();
        if (ac) {
            for (Bus bus : getNetwork().getBusView().getBuses()) {
                num2AcBus.add(bus);
                acBusId2num.put(bus.getId(), num);
                num++;
                numAc++;
            }
        }
        if (dc) {
            for (DcBus dcBus : getNetwork().getDcBuses()) {
                num2DcBus.add(dcBus);
                dcBusId2num.put(dcBus.getId(), num);
                num++;
            }
        }
        TIntArrayList[] adjacencyList = new TIntArrayList[num];
        for (int i = 0; i < adjacencyList.length; i++) {
            adjacencyList[i] = new TIntArrayList(3);
        }
        fillAdjacencyList(acBusId2num, dcBusId2num, adjacencyList);

        ConnectedComponentsComputationResult result = GraphUtil.computeConnectedComponents(adjacencyList);

        components = new ArrayList<>(result.getComponentSize().length);
        for (int i = 0; i < result.getComponentSize().length; i++) {
            components.add(createComponent(i, result.getComponentSize()[i]));
        }

        for (int i = 0; i < result.getComponentNumber().length; i++) {
            if (i < numAc) {
                Bus bus = num2AcBus.get(i);
                setComponentNumber(bus, result.getComponentNumber()[i]);
            } else {
                DcBus dcBus = num2DcBus.get(i - numAc);
                setComponentNumber(dcBus, result.getComponentNumber()[i]);
            }
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

    private void addToAdjacencyList(Bus bus1, Bus bus2, Map<String, Integer> acBusId2num, TIntArrayList[] adjacencyList) {
        if (bus1 != null && bus2 != null) {
            int busNum1 = acBusId2num.get(bus1.getId());
            int busNum2 = acBusId2num.get(bus2.getId());
            adjacencyList[busNum1].add(busNum2);
            adjacencyList[busNum2].add(busNum1);
        }
    }

    private void addToAdjacencyList(DcBus dcBus1, DcBus dcBus2, Map<String, Integer> dcBusId2num, TIntArrayList[] adjacencyList) {
        if (dcBus1 != null && dcBus2 != null) {
            int busNum1 = dcBusId2num.get(dcBus1.getId());
            int busNum2 = dcBusId2num.get(dcBus2.getId());
            adjacencyList[busNum1].add(busNum2);
            adjacencyList[busNum2].add(busNum1);
        }
    }

    private void addToAdjacencyList(Bus bus, DcBus dcBus, Map<String, Integer> acBusId2num, Map<String, Integer> dcBusId2num, TIntArrayList[] adjacencyList) {
        if (bus != null && dcBus != null) {
            int acBusNum = acBusId2num.get(bus.getId());
            int dcBusNum = dcBusId2num.get(dcBus.getId());
            adjacencyList[acBusNum].add(dcBusNum);
            adjacencyList[dcBusNum].add(acBusNum);
        }
    }

    private void fillAdjacencyList(Map<String, Integer> acBusId2num, Map<String, Integer> dcBusId2num, TIntArrayList[] adjacencyList) {
        if (ac) {
            for (Line line : getNetwork().getLines()) {
                Bus bus1 = line.getTerminal1().getBusView().getBus();
                Bus bus2 = line.getTerminal2().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, acBusId2num, adjacencyList);
            }
            for (TieLine tl : getNetwork().getTieLines()) {
                Bus bus1 = tl.getDanglingLine1().getTerminal().getBusView().getBus();
                Bus bus2 = tl.getDanglingLine2().getTerminal().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, acBusId2num, adjacencyList);
            }
            for (TwoWindingsTransformer transfo : getNetwork().getTwoWindingsTransformers()) {
                Bus bus1 = transfo.getTerminal1().getBusView().getBus();
                Bus bus2 = transfo.getTerminal2().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, acBusId2num, adjacencyList);
            }
            for (ThreeWindingsTransformer transfo : getNetwork().getThreeWindingsTransformers()) {
                Bus bus1 = transfo.getLeg1().getTerminal().getBusView().getBus();
                Bus bus2 = transfo.getLeg2().getTerminal().getBusView().getBus();
                Bus bus3 = transfo.getLeg3().getTerminal().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, acBusId2num, adjacencyList);
                addToAdjacencyList(bus1, bus3, acBusId2num, adjacencyList);
                addToAdjacencyList(bus2, bus3, acBusId2num, adjacencyList);
            }
        }
        if (dc) {
            for (DcLine dcLine : getNetwork().getDcLines()) {
                DcBus dcBus1 = dcLine.getDcTerminal1().getBus();
                DcBus dcBus2 = dcLine.getDcTerminal2().getBus();
                addToAdjacencyList(dcBus1, dcBus2, dcBusId2num, adjacencyList);
            }
            for (AcDcConverter<?> acDcConverter : getNetwork().getDcConnectables(AcDcConverter.class)) {
                DcBus dcBus1 = acDcConverter.getDcTerminal1().getBus();
                DcBus dcBus2 = acDcConverter.getDcTerminal2().getBus();
                addToAdjacencyList(dcBus1, dcBus2, dcBusId2num, adjacencyList);
            }
        }
        if (ac && dc) {
            for (HvdcLine line : getNetwork().getHvdcLines()) {
                Bus bus1 = line.getConverterStation1().getTerminal().getBusView().getBus();
                Bus bus2 = line.getConverterStation2().getTerminal().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, acBusId2num, adjacencyList);
            }
            for (AcDcConverter<?> acDcConverter : getNetwork().getDcConnectables(AcDcConverter.class)) {
                Bus bus1 = acDcConverter.getTerminal1().getBusView().getBus();
                DcBus dcBus1 = acDcConverter.getDcTerminal1().getBus();
                DcBus dcBus2 = acDcConverter.getDcTerminal2().getBus();
                addToAdjacencyList(bus1, dcBus1, acBusId2num, dcBusId2num, adjacencyList);
                addToAdjacencyList(bus1, dcBus2, acBusId2num, dcBusId2num, adjacencyList);
                acDcConverter.getTerminal2().ifPresent(t2 -> {
                    Bus bus2 = t2.getBusView().getBus();
                    addToAdjacencyList(bus2, dcBus1, acBusId2num, dcBusId2num, adjacencyList);
                    addToAdjacencyList(bus2, dcBus2, acBusId2num, dcBusId2num, adjacencyList);
                });
            }
        }
    }

    private String getComponentLabel() {
        return label;
    }

    protected abstract Network getNetwork();

    protected abstract C createComponent(int num, int size);

    protected abstract void setComponentNumber(Bus bus, int num);

    protected abstract void setComponentNumber(DcBus dcBus, int num);

    private void reset() {
        if (ac) {
            for (Bus bus : getNetwork().getBusBreakerView().getBuses()) {
                setComponentNumber(bus, -1);
            }
        }
        if (dc) {
            for (DcBus bus : getNetwork().getDcBuses()) {
                setComponentNumber(bus, -1);
            }
        }
    }

}
