/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Set;

/**
 * A terminal connected to a node breaker topology.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class NodeTerminal extends AbstractTerminal {

    private final int node;

    // attributes depending on the state

    protected final TDoubleArrayList v;

    protected final TDoubleArrayList angle;

    protected final TIntArrayList connectedComponentNumber;

    protected final TIntArrayList synchronousComponentNumber;

    private final NodeBreakerView nodeBreakerView = new NodeBreakerView() {

        @Override
        public int getNode() {
            return node;
        }
    };

    private final BusBreakerViewExt busBreakerView = new BusBreakerViewExt() {

        @Override
        public BusExt getBus() {
            return ((NodeBreakerVoltageLevel) voltageLevel).getCalculatedBusBreakerTopology().getBus(node);
        }

        @Override
        public BusExt getConnectableBus() {
            return ((NodeBreakerVoltageLevel) voltageLevel).getCalculatedBusBreakerTopology().getConnectableBus(node);
        }

        @Override
        public void setConnectableBus(String busId) {
            throw NodeBreakerVoltageLevel.createNotSupportedNodeBreakerTopologyException();
        }

    };

    private final BusViewExt busView = new BusViewExt() {

        @Override
        public BusExt getBus() {
            return ((NodeBreakerVoltageLevel) voltageLevel).getCalculatedBusTopology().getBus(node);
        }

        @Override
        public BusExt getConnectableBus() {
            return ((NodeBreakerVoltageLevel) voltageLevel).getCalculatedBusTopology().getConnectableBus(node);
        }

    };

    NodeTerminal(Ref<? extends MultiStateObject> network, int node) {
        super(network);
        this.node = node;
        int stateArraySize = network.get().getStateManager().getStateArraySize();
        v = new TDoubleArrayList(stateArraySize);
        angle = new TDoubleArrayList(stateArraySize);
        connectedComponentNumber = new TIntArrayList(stateArraySize);
        synchronousComponentNumber = new TIntArrayList(stateArraySize);
        for (int i = 0; i < stateArraySize; i++) {
            v.add(Double.NaN);
            angle.add(Double.NaN);
            connectedComponentNumber.add(0);
            synchronousComponentNumber.add(0);
        }
    }

    public int getNode() {
        return node;
    }

    @Override
    protected double getV() {
        return v.get(network.get().getStateIndex());
    }

    void setV(double v) {
        if (v < 0) {
            throw new ValidationException(connectable, "voltage cannot be < 0");
        }
        this.v.set(network.get().getStateIndex(), v);
    }

    double getAngle() {
        return angle.get(network.get().getStateIndex());
    }

    void setAngle(double angle) {
        this.angle.set(network.get().getStateIndex(), angle);
    }

    int getConnectedComponentNumber() {
        return connectedComponentNumber.get(network.get().getStateIndex());
    }

    void setConnectedComponentNumber(int connectedComponentNumber) {
        this.connectedComponentNumber.set(network.get().getStateIndex(), connectedComponentNumber);
    }

    int getSynchronousComponentNumber() {
        return synchronousComponentNumber.get(network.get().getStateIndex());
    }

    void setSynchronousComponentNumber(int componentNumber) {
        this.synchronousComponentNumber.set(network.get().getStateIndex(), componentNumber);
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        return nodeBreakerView;
    }

    @Override
    public BusBreakerViewExt getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public BusViewExt getBusView() {
        return busView;
    }

    @Override
    public boolean isConnected() {
        return ((NodeBreakerVoltageLevel) voltageLevel).isConnected(this);
    }

    @Override
    public void traverse(VoltageLevel.TopologyTraverser traverser, Set<Terminal> traversedTerminals) {
        ((NodeBreakerVoltageLevel) voltageLevel).traverse(this, traverser, traversedTerminals);
    }

    @Override
    public void traverse(VoltageLevel.TopologyTraverser traverser) {
        ((NodeBreakerVoltageLevel) voltageLevel).traverse(this, traverser);
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);
        v.ensureCapacity(v.size() + number);
        angle.ensureCapacity(angle.size() + number);
        connectedComponentNumber.ensureCapacity(connectedComponentNumber.size() + number);
        synchronousComponentNumber.ensureCapacity(synchronousComponentNumber.size() + number);
        for (int i = 0; i < number; i++) {
            v.add(v.get(sourceIndex));
            angle.add(angle.get(sourceIndex));
            connectedComponentNumber.add(connectedComponentNumber.get(sourceIndex));
            synchronousComponentNumber.add(synchronousComponentNumber.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        v.remove(v.size() - number, number);
        angle.remove(angle.size() - number, number);
        connectedComponentNumber.remove(connectedComponentNumber.size() - number, number);
        synchronousComponentNumber.remove(synchronousComponentNumber.size() - number, number);
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        super.allocateStateArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            v.set(index, v.get(sourceIndex));
            angle.set(index, angle.get(sourceIndex));
            connectedComponentNumber.set(index, connectedComponentNumber.get(sourceIndex));
            synchronousComponentNumber.set(index, synchronousComponentNumber.get(sourceIndex));
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + node + "]";
    }

}
