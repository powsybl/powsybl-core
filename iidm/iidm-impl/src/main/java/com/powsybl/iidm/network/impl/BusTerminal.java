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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Objects;
import java.util.Set;

/**
 * A terminal connected to a bus/breaker topology.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BusTerminal extends AbstractTerminal {

    private final NodeBreakerView nodeBreakerView = () -> {
        throw BusBreakerVoltageLevel.createNotSupportedBusBreakerTopologyException();
    };

    private final BusBreakerViewExt busBreakerView = new BusBreakerViewExt() {

        @Override
        public BusExt getBus() {
            return isConnected() ? getConnectableBus() : null;
        }

        @Override
        public ConfiguredBus getConnectableBus() {
            return ((BusBreakerVoltageLevel) voltageLevel).getBus(getConnectableBusId(), true);
        }

        @Override
        public void setConnectableBus(String busId) {
            Objects.requireNonNull(busId);
            VoltageLevelExt vl = voltageLevel;
            voltageLevel.detach(BusTerminal.this);
            setConnectableBusId(busId);
            vl.attach(BusTerminal.this, false);
        }
    };

    private final BusViewExt busView = new BusViewExt() {

        @Override
        public BusExt getBus() {
            return isConnected() ? this.getConnectableBus() : null;
        }

        @Override
        public MergedBus getConnectableBus() {
            ConfiguredBus bus = ((BusBreakerVoltageLevel) voltageLevel).getBus(getConnectableBusId(), true);
            return ((BusBreakerVoltageLevel) voltageLevel).calculatedBusTopology.getMergedBus(bus);
        }

    };

    // attributes depending on the state

    private final BitSet connected;

    private final ArrayList<String> connectableBusId;

    BusTerminal(Ref<? extends MultiStateObject> network, String connectableBusId, boolean connected) {
        super(network);
        Objects.requireNonNull(connectableBusId);
        int stateArraySize = network.get().getStateManager().getStateArraySize();
        this.connectableBusId = new ArrayList<>(stateArraySize);
        for (int i = 0; i < stateArraySize; i++) {
            this.connectableBusId.add(connectableBusId);
        }
        this.connected = new BitSet(stateArraySize);
        this.connected.set(0, stateArraySize, connected);
    }

    void setConnectableBusId(String connectableBusId) {
        this.connectableBusId.set(network.get().getStateIndex(), connectableBusId);
    }

    String getConnectableBusId() {
        return this.connectableBusId.get(network.get().getStateIndex());
    }

    void setConnected(boolean connected) {
        this.connected.set(network.get().getStateIndex(), connected);
    }

    @Override
    public boolean isConnected() {
        return this.connected.get(network.get().getStateIndex());
    }

    @Override
    public void traverse(VoltageLevel.TopologyTraverser traverser, Set<Terminal> traversedTerminals) {
        ((BusBreakerVoltageLevel) voltageLevel).traverse(this, traverser, traversedTerminals);
    }

    @Override
    public void traverse(VoltageLevel.TopologyTraverser traverser) {
        ((BusBreakerVoltageLevel) voltageLevel).traverse(this, traverser);
    }

    @Override
    protected double getV() {
        return busBreakerView.getBus() != null ? busBreakerView.getBus().getV() : Double.NaN;
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
    public String toString() {
        return getClass().getSimpleName() + "[" + getConnectableBusId() + "]";
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);
        connectableBusId.ensureCapacity(connectableBusId.size() + number);
        for (int i = 0; i < number; i++) {
            connectableBusId.add(connectableBusId.get(sourceIndex));
            connected.set(initStateArraySize + i, connected.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        for (int i = 0; i < number; i++) {
            connectableBusId.remove(connectableBusId.size() - 1);
        }
    }

    @Override
    public void deleteStateArrayElement(int index) {
        super.deleteStateArrayElement(index);
        connectableBusId.set(index, null);
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        super.allocateStateArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            connectableBusId.set(index, connectableBusId.get(sourceIndex));
            connected.set(index, connected.get(sourceIndex));
        }
    }

}
