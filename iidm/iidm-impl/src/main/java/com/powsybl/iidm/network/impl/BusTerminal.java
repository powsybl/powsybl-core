/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A terminal connected to a bus/breaker topology.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BusTerminal extends AbstractTerminal {

    private static final String UNMODIFIABLE_REMOVED_EQUIPMENT = "Can not modify removed equipment";

    private final NodeBreakerView nodeBreakerView = new NodeBreakerView() {
        @Override
        public int getNode() {
            throw BusBreakerVoltageLevel.createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public void moveConnectable(int node, String voltageLevelId) {
            if (network == null) {
                throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT);
            }
            getConnectable().move(BusTerminal.this, getConnectionInfo(), node, voltageLevelId);
        }
    };

    private final BusBreakerViewExt busBreakerView = new BusBreakerViewExt() {

        @Override
        public BusExt getBus() {
            return isConnected() ? getConnectableBus() : null;
        }

        @Override
        public ConfiguredBus getConnectableBus() {
            return Optional.ofNullable(voltageLevel)
                    .map(vl -> ((BusBreakerVoltageLevel) vl).getBus(getConnectableBusId(), true))
                    .orElse(null);
        }

        @Override
        public void setConnectableBus(String busId) {
            if (network == null) {
                throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT);
            }
            Objects.requireNonNull(busId);
            BusBreakerVoltageLevel vl = (BusBreakerVoltageLevel) voltageLevel;

            // Assert that the new bus exists
            vl.getBus(busId, true);

            vl.detach(BusTerminal.this, false);
            int variantIndex = network.get().getVariantIndex();
            String oldValue = BusTerminal.this.connectableBusId.set(variantIndex, busId);
            vl.attach(BusTerminal.this, false);
            String variantId = network.get().getVariantManager().getVariantId(variantIndex);
            getConnectable().notifyUpdate("connectableBusId", variantId, oldValue, busId);
        }

        @Override
        public void moveConnectable(String busId, boolean connected) {
            if (network == null) {
                throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT);
            }
            getConnectable().move(BusTerminal.this, getConnectionInfo(), busId, connected);
        }

    };

    @Override
    public String getConnectionInfo() {
        return "bus " + getBusBreakerView().getConnectableBus().getId() + ", "
                + (getBusBreakerView().getBus() != null ? "connected" : "disconnected");
    }

    private final BusViewExt busView = new BusViewExt() {

        @Override
        public BusExt getBus() {
            return isConnected() ? this.getConnectableBus() : null;
        }

        @Override
        public MergedBus getConnectableBus() {
            if (voltageLevel == null) {
                return null;
            }
            ConfiguredBus bus = ((BusBreakerVoltageLevel) voltageLevel).getBus(getConnectableBusId(), true);
            return ((BusBreakerVoltageLevel) voltageLevel).calculatedBusTopology.getMergedBus(bus);
        }

    };

    // attributes depending on the variant

    private final TBooleanArrayList connected;

    private final ArrayList<String> connectableBusId;

    BusTerminal(Ref<? extends VariantManagerHolder> network, String connectableBusId, boolean connected) {
        super(network);
        Objects.requireNonNull(connectableBusId);
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.connected = new TBooleanArrayList(variantArraySize);
        this.connectableBusId = new ArrayList<>(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.connected.add(connected);
            this.connectableBusId.add(connectableBusId);
        }
    }

    void setConnectableBusId(String connectableBusId) {
        if (network == null) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT);
        }
        int variantIndex = network.get().getVariantIndex();
        String oldValue = this.connectableBusId.set(variantIndex, connectableBusId);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        getConnectable().notifyUpdate("connectableBusId", variantId, oldValue, connectableBusId);
    }

    String getConnectableBusId() {
        return Optional.ofNullable(network).map(n -> this.connectableBusId.get(n.get().getVariantIndex())).orElse(null);
    }

    void setConnected(boolean connected) {
        if (network == null) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT);
        }
        int variantIndex = network.get().getVariantIndex();
        boolean oldValue = this.connected.set(variantIndex, connected);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        getConnectable().notifyUpdate("connected", variantId, oldValue, connected);
    }

    @Override
    public boolean isConnected() {
        return Optional.ofNullable(network).map(n -> this.connected.get(n.get().getVariantIndex())).orElse(false);
    }

    @Override
    public boolean traverse(TopologyTraverser traverser, Set<Terminal> visitedTerminals) {
        if (voltageLevel == null) {
            throw new PowsyblException("Associated equipment is removed");
        }
        return ((BusBreakerVoltageLevel) voltageLevel).traverse(this, traverser, visitedTerminals);
    }

    @Override
    public void traverse(TopologyTraverser traverser) {
        if (voltageLevel == null) {
            throw new PowsyblException("Associated equipment is removed");
        }
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
        connected.ensureCapacity(connected.size() + number);
        connectableBusId.ensureCapacity(connectableBusId.size() + number);
        for (int i = 0; i < number; i++) {
            connected.add(connected.get(sourceIndex));
            connectableBusId.add(connectableBusId.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        for (int i = 0; i < number; i++) {
            connected.removeAt(connected.size() - 1);
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
            connected.set(index, connected.get(sourceIndex));
            connectableBusId.set(index, connectableBusId.get(sourceIndex));
        }
    }
}
