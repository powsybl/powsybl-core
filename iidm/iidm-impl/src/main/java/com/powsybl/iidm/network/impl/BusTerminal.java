/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.TopologyPoint;
import com.powsybl.commons.ref.Ref;
import com.powsybl.math.graph.TraversalType;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

/**
 * A terminal connected to a bus/breaker topology.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class BusTerminal extends AbstractTerminal {

    private BusBreakerTopologyModel getTopologyModel() {
        return (BusBreakerTopologyModel) voltageLevel.getTopologyModel();
    }

    private final NodeBreakerView nodeBreakerView = new NodeBreakerView() {
        @Override
        public int getNode() {
            throw BusBreakerTopologyModel.createNotSupportedBusBreakerTopologyException();
        }

        @Override
        public void moveConnectable(int node, String voltageLevelId) {
            if (removed) {
                throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
            }
            getConnectable().move(BusTerminal.this, node, voltageLevelId);
        }
    };

    private final BusBreakerViewExt busBreakerView = new BusBreakerViewExt() {

        @Override
        public BusExt getBus() {
            if (removed) {
                throw new PowsyblException(CANNOT_ACCESS_BUS_REMOVED_EQUIPMENT + connectable.id);
            }
            return isConnected() ? getConnectableBus() : null;
        }

        @Override
        public ConfiguredBus getConnectableBus() {
            if (removed) {
                throw new PowsyblException(CANNOT_ACCESS_BUS_REMOVED_EQUIPMENT + connectable.id);
            }
            return getTopologyModel().getBus(getConnectableBusId(), true);
        }

        @Override
        public void setConnectableBus(String busId) {
            if (removed) {
                throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
            }
            Objects.requireNonNull(busId);
            BusBreakerTopologyModel topologyModel = getTopologyModel();

            // Assert that the new bus exists
            topologyModel.getBus(busId, true);

            topologyModel.detach(BusTerminal.this);
            int variantIndex = getVariantManagerHolder().getVariantIndex();
            String oldValue = BusTerminal.this.connectableBusId.set(variantIndex, busId);
            topologyModel.attach(BusTerminal.this, false);
            String variantId = getVariantManagerHolder().getVariantManager().getVariantId(variantIndex);
            getConnectable().notifyUpdate("connectableBusId", variantId, oldValue, busId);
        }

        @Override
        public void moveConnectable(String busId, boolean connected) {
            if (removed) {
                throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
            }
            getConnectable().move(BusTerminal.this, busId, connected);
        }

    };

    @Override
    public TopologyPoint getTopologyPoint() {
        return new BusTopologyPointImpl(getVoltageLevel().getId(), getConnectableBusId(), isConnected());
    }

    private final BusViewExt busView = new BusViewExt() {

        @Override
        public BusExt getBus() {
            if (removed) {
                throw new PowsyblException(CANNOT_ACCESS_BUS_REMOVED_EQUIPMENT + connectable.id);
            }
            return isConnected() ? this.getConnectableBus() : null;
        }

        @Override
        public MergedBus getConnectableBus() {
            if (removed) {
                throw new PowsyblException(CANNOT_ACCESS_BUS_REMOVED_EQUIPMENT + connectable.id);
            }
            ConfiguredBus bus = getTopologyModel().getBus(getConnectableBusId(), true);
            return getTopologyModel().calculatedBusTopology.getMergedBus(bus);
        }

    };

    // attributes depending on the variant

    private final TBooleanArrayList connected;

    private final ArrayList<String> connectableBusId;

    BusTerminal(Ref<? extends VariantManagerHolder> network, ThreeSides side, String connectableBusId, boolean connected) {
        super(network, side);
        Objects.requireNonNull(connectableBusId);
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.connected = new TBooleanArrayList(variantArraySize);
        this.connectableBusId = new ArrayList<>(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.connected.add(connected);
            this.connectableBusId.add(connectableBusId);
        }
    }

    void unsetConnectableBusId() {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        this.connectableBusId.set(variantIndex, null);
    }

    String getConnectableBusId() {
        if (removed) {
            throw new PowsyblException(CANNOT_ACCESS_BUS_REMOVED_EQUIPMENT + connectable.id);
        }
        return this.connectableBusId.get(getVariantManagerHolder().getVariantIndex());
    }

    void setConnected(boolean connected) {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        boolean oldValue = this.connected.set(variantIndex, connected);
        String variantId = getVariantManagerHolder().getVariantManager().getVariantId(variantIndex);
        getConnectable().notifyUpdate("connected" + getAttributeSideSuffix(), variantId, oldValue, connected);
    }

    @Override
    public boolean isConnected() {
        if (removed) {
            throw new PowsyblException("Cannot access connectivity status of removed equipment " + connectable.id);
        }
        return this.connected.get(getVariantManagerHolder().getVariantIndex());
    }

    @Override
    public boolean traverse(TopologyTraverser traverser, Set<Terminal> visitedTerminals, TraversalType traversalType) {
        if (removed) {
            throw new PowsyblException(String.format("Associated equipment %s is removed", connectable.id));
        }
        return getTopologyModel().traverse(this, traverser, visitedTerminals, traversalType);
    }

    @Override
    public void traverse(TopologyTraverser traverser) {
        traverse(traverser, TraversalType.DEPTH_FIRST);
    }

    @Override
    public void traverse(TopologyTraverser traverser, TraversalType traversalType) {
        if (removed) {
            throw new PowsyblException(String.format("Associated equipment %s is removed", connectable.id));
        }
        getTopologyModel().traverse(this, traverser, traversalType);
    }

    @Override
    protected double getV() {
        if (removed) {
            throw new PowsyblException("Cannot access v of removed equipment " + connectable.id);
        }
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
