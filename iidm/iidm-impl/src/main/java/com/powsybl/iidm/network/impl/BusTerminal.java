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

    // attributes depending on the variant

    private final BitSet connected;

    private final ArrayList<String> connectableBusId;

    BusTerminal(Ref<? extends VariantManagerHolder> network, String connectableBusId, boolean connected) {
        super(network);
        Objects.requireNonNull(connectableBusId);
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.connectableBusId = new ArrayList<>(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.connectableBusId.add(connectableBusId);
        }
        this.connected = new BitSet(variantArraySize);
        this.connected.set(0, variantArraySize, connected);
    }

    void setConnectableBusId(String connectableBusId) {
        this.connectableBusId.set(network.get().getVariantIndex(), connectableBusId);
    }

    String getConnectableBusId() {
        return this.connectableBusId.get(network.get().getVariantIndex());
    }

    void setConnected(boolean connected) {
        this.connected.set(network.get().getVariantIndex(), connected);
    }

    @Override
    public boolean isConnected() {
        return this.connected.get(network.get().getVariantIndex());
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
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        connectableBusId.ensureCapacity(connectableBusId.size() + number);
        for (int i = 0; i < number; i++) {
            connectableBusId.add(connectableBusId.get(sourceIndex));
            connected.set(initVariantArraySize + i, connected.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        for (int i = 0; i < number; i++) {
            connectableBusId.remove(connectableBusId.size() - 1);
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        connectableBusId.set(index, null);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            connectableBusId.set(index, connectableBusId.get(sourceIndex));
            connected.set(index, connected.get(sourceIndex));
        }
    }

}
