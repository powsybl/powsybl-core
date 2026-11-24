/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.GraphUtil;
import com.powsybl.math.graph.GraphUtil.ConnectedComponentsComputationResult;
import gnu.trove.list.array.TIntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Valentin Carrez {@literal <valentin.carrez at rte-france.com>}
 */
class SimpleComponentsManager implements ComponentsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleComponentsManager.class);

    private final NetworkImpl network;
    private final String label;

    private List<Component> components;

    private final boolean ac;
    private final boolean dc;

    SimpleComponentsManager(NetworkImpl network, String label, boolean ac, boolean dc) {
        this.network = network;
        this.label = Objects.requireNonNull(label);
        this.ac = ac;
        this.dc = dc;
    }

    /*@Override
    public void onCreation(Identifiable<?> identifiable) {
        components = null;
    }

    @Override
    public void beforeRemoval(Identifiable<?> identifiable) {
        components = null;
    }

    @Override
    public void onUpdate(Identifiable<?> identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
        components = null;
    }

    @Override
    public void onPropertyAdded(Identifiable<?> identifiable, String key, Object newValue) {
        components = null;
    }

    @Override
    public void onPropertyReplaced(Identifiable<?> identifiable, String key, Object oldValue, Object newValue) {
        components = null;
    }

    @Override
    public void onPropertyRemoved(Identifiable<?> identifiable, String key, Object oldValue) {
        components = null;
    }*/

    public void update() {
        if (components != null) {
            return;
        }

        long startTime = System.currentTimeMillis();

        reset();

        int num = 0;
        Map<String, Integer> busId2num = new HashMap<>();
        List<Bus> num2AcBus = new ArrayList<>();
        List<DcBus> num2DcBus = new ArrayList<>();
        if (ac) {
            for (Bus bus : network.getBusView().getBuses()) {
                num2AcBus.add(bus);
                busId2num.put(bus.getId(), num);
                num++;
            }
        }
        final int nbAcBuses = num2AcBus.size();
        if (dc) {
            for (DcBus dcBus : network.getDcBuses()) {
                num2DcBus.add(dcBus);
                busId2num.put(dcBus.getId(), num);
                num++;
            }
        }
        TIntArrayList[] adjacencyList = new TIntArrayList[num];
        for (int i = 0; i < adjacencyList.length; i++) {
            adjacencyList[i] = new TIntArrayList(3);
        }
        fillAdjacencyList(busId2num, adjacencyList);

        ConnectedComponentsComputationResult result = GraphUtil.computeConnectedComponents(adjacencyList);

        components = new ArrayList<>(result.getComponentSize().length);
        for (int i = 0; i < result.getComponentSize().length; i++) {
            components.add(createComponent(i, result.getComponentSize()[i]));
        }

        for (int i = 0; i < result.getComponentNumber().length; i++) {
            if (i < nbAcBuses) {
                Bus bus = num2AcBus.get(i);
                setComponentNumber(bus, result.getComponentNumber()[i]);
            } else {
                DcBus dcBus = num2DcBus.get(i - nbAcBuses);
                setComponentNumber(dcBus, result.getComponentNumber()[i]);
            }
        }

        LOGGER.debug("{} components computed in {} ms", label, System.currentTimeMillis() - startTime);
    }

    private Component createComponent(int num, int size) {
        if (ac && dc) {
            return new ConnectedComponentImpl(num, size, network.getRef());
        } else if (ac) {
            return new SynchronousComponentImpl(num, size, network.getRef());
        } else {
            return new DcComponentImpl(num, size, network.getRef());
        }
    }

    private void addToAdjacencyList(Identifiable<?> bus1, Identifiable<?> bus2, Map<String, Integer> busId2num, TIntArrayList[] adjacencyList) {
        if (bus1 != null && bus2 != null) {
            int busNum1 = busId2num.get(bus1.getId());
            int busNum2 = busId2num.get(bus2.getId());
            adjacencyList[busNum1].add(busNum2);
            adjacencyList[busNum2].add(busNum1);
        }
    }

    private void fillAdjacencyList(Map<String, Integer> busId2num, TIntArrayList[] adjacencyList) {
        fillAcAdjacencyList(busId2num, adjacencyList);
        fillDcAdjacencyList(busId2num, adjacencyList);
        fillAcDcAdjacencyList(busId2num, adjacencyList);
    }

    private void fillAcAdjacencyList(Map<String, Integer> busId2num, TIntArrayList[] adjacencyList) {
        if (ac) {
            for (Line line : network.getLines()) {
                Bus bus1 = line.getTerminal1().getBusView().getBus();
                Bus bus2 = line.getTerminal2().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, busId2num, adjacencyList);
            }
            for (TieLine tl : network.getTieLines()) {
                Bus bus1 = tl.getBoundaryLine1().getTerminal().getBusView().getBus();
                Bus bus2 = tl.getBoundaryLine2().getTerminal().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, busId2num, adjacencyList);
            }
            for (TwoWindingsTransformer transfo : network.getTwoWindingsTransformers()) {
                Bus bus1 = transfo.getTerminal1().getBusView().getBus();
                Bus bus2 = transfo.getTerminal2().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, busId2num, adjacencyList);
            }
            for (ThreeWindingsTransformer transfo : network.getThreeWindingsTransformers()) {
                Bus bus1 = transfo.getLeg1().getTerminal().getBusView().getBus();
                Bus bus2 = transfo.getLeg2().getTerminal().getBusView().getBus();
                Bus bus3 = transfo.getLeg3().getTerminal().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, busId2num, adjacencyList);
                addToAdjacencyList(bus1, bus3, busId2num, adjacencyList);
                addToAdjacencyList(bus2, bus3, busId2num, adjacencyList);
            }
            // Note that AC/DC converters with two AC terminals are not included here (AC synchronous component):
            // The converter does not synchronize the 2 AC terminals together,
            // the converter does not impose phase or frequency alignment.
            // The adjacency of the two AC terminals is however added in the case of ac && dc (connected component).
        }
    }

    private void fillDcAdjacencyList(Map<String, Integer> busId2num, TIntArrayList[] adjacencyList) {
        if (dc) {
            for (DcLine dcLine : network.getDcLines()) {
                DcBus dcBus1 = dcLine.getDcTerminal1().getDcBus();
                DcBus dcBus2 = dcLine.getDcTerminal2().getDcBus();
                addToAdjacencyList(dcBus1, dcBus2, busId2num, adjacencyList);
            }
            for (AcDcConverter<?> acDcConverter : network.getDcConnectables(AcDcConverter.class)) {
                DcBus dcBus1 = acDcConverter.getDcTerminal1().getDcBus();
                DcBus dcBus2 = acDcConverter.getDcTerminal2().getDcBus();
                addToAdjacencyList(dcBus1, dcBus2, busId2num, adjacencyList);
            }
            for (DcSwitch dcSwitch : network.getDcSwitches()) {
                if (!dcSwitch.isOpen()) {
                    DcBus dcBus1 = dcSwitch.getDcNode1().getDcBus();
                    DcBus dcBus2 = dcSwitch.getDcNode2().getDcBus();
                    if (dcBus1 != dcBus2) {
                        addToAdjacencyList(dcBus1, dcBus2, busId2num, adjacencyList);
                    }
                }
            }
        }
    }

    private void fillAcDcAdjacencyList(Map<String, Integer> busId2num, TIntArrayList[] adjacencyList) {
        if (ac && dc) {
            for (HvdcLine line : network.getHvdcLines()) {
                Bus bus1 = line.getConverterStation1().getTerminal().getBusView().getBus();
                Bus bus2 = line.getConverterStation2().getTerminal().getBusView().getBus();
                addToAdjacencyList(bus1, bus2, busId2num, adjacencyList);
            }
            for (AcDcConverter<?> acDcConverter : network.getDcConnectables(AcDcConverter.class)) {
                Bus bus1 = acDcConverter.getTerminal1().getBusView().getBus();
                DcBus dcBus1 = acDcConverter.getDcTerminal1().getDcBus();
                DcBus dcBus2 = acDcConverter.getDcTerminal2().getDcBus();
                addToAdjacencyList(bus1, dcBus1, busId2num, adjacencyList);
                addToAdjacencyList(bus1, dcBus2, busId2num, adjacencyList);
                acDcConverter.getTerminal2().ifPresent(t2 -> {
                    Bus bus2 = t2.getBusView().getBus();
                    addToAdjacencyList(bus1, bus2, busId2num, adjacencyList);
                    addToAdjacencyList(bus2, dcBus1, busId2num, adjacencyList);
                    addToAdjacencyList(bus2, dcBus2, busId2num, adjacencyList);
                });
            }
        }
    }

    private void setComponentNumber(Bus bus, int num) {
        Objects.requireNonNull(bus);
        if (ac && dc) {
            ((BusExt) bus).setConnectedComponentNumber(num);
        } else {
            ((BusExt) bus).setSynchronousComponentNumber(num);
        }
    }

    private void setComponentNumber(DcBus dcBus, int num) {
        Objects.requireNonNull(dcBus);
        if (ac && dc) {
            ((DcBusImpl) dcBus).setConnectedComponentNumber(num);
        } else {
            ((DcBusImpl) dcBus).setDcComponentNumber(num);
        }
    }

    private void reset() {
        if (ac) {
            for (Bus bus : network.getBusBreakerView().getBuses()) {
                setComponentNumber(bus, -1);
            }
        }
        if (dc) {
            for (DcBus bus : network.getDcBuses()) {
                setComponentNumber(bus, -1);
            }
        }
    }

    @Override
    public void invalidate() {
        components = null;
    }

    @Override
    public void voltageLevelAdded(VoltageLevel voltageLevel) {

    }

    @Override
    public void invalidate(VoltageLevel voltageLevel) {
        components = null;
    }

    @Override
    public void voltageLevelRemoved(VoltageLevel voltageLevel) {

    }

    @Override
    public List<Component> getConnectedComponents() {
        update();
        return components;
    }

    @Override
    public Component getComponent(BusExt bus) {
        update();
        int index = ac && dc ? bus.getQuickConnectedComponentNumber() : bus.getQuickSynchronousComponentNumber();
        if (index >= 0 && index < components.size()) {
            return components.get(index);
        } else {
            return null;
        }
    }

    @Override
    public boolean isInMainComponent(BusExt bus) {
        Component component = getComponent(bus);
        return component != null && component.getNum() == ComponentConstants.MAIN_NUM;
    }

    @Override
    public Component getComponent(DcBusImpl bus) {
        update();
        int index = ac && dc ? bus.getQuickConnectedComponentNumber() : bus.getQuickDcConnectedComponentNumber();
        if (index >= 0 && index < components.size()) {
            return components.get(index);
        } else {
            return null;
        }
    }

    @Override
    public boolean isInMainComponent(DcBusImpl bus) {
        Component component = getComponent(bus);
        return component != null && component.getNum() == ComponentConstants.MAIN_NUM;
    }
}
