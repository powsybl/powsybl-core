/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.TopologyPoint;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.math.graph.TraversalType;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Set;

/**
 * A terminal connected to a node breaker topology.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NodeTerminal extends AbstractTerminal {

    private final int node;

    // attributes depending on the variant

    protected final TDoubleArrayList v;

    protected final TDoubleArrayList angle;

    protected final TIntArrayList connectedComponentNumber;

    protected final TIntArrayList synchronousComponentNumber;

    private final NodeBreakerView nodeBreakerView = new NodeBreakerView() {

        @Override
        public int getNode() {
            if (removed) {
                throw new PowsyblException("Cannot access node of removed equipment " + connectable.id);
            }
            return node;
        }

        @Override
        public void moveConnectable(int node, String voltageLevelId) {
            if (removed) {
                throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
            }
            getConnectable().move(NodeTerminal.this, getTopologyPoint(), node, voltageLevelId);
        }
    };

    private final BusBreakerViewExt busBreakerView = new BusBreakerViewExt() {

        @Override
        public BusExt getBus() {
            if (removed) {
                throw new PowsyblException(CANNOT_ACCESS_BUS_REMOVED_EQUIPMENT + connectable.id);
            }
            return ((NodeBreakerVoltageLevel) voltageLevel).getCalculatedBusBreakerTopology().getBus(node);
        }

        @Override
        public BusExt getConnectableBus() {
            if (removed) {
                throw new PowsyblException(CANNOT_ACCESS_BUS_REMOVED_EQUIPMENT + connectable.id);
            }
            return ((NodeBreakerVoltageLevel) voltageLevel).getCalculatedBusBreakerTopology().getConnectableBus(node);
        }

        @Override
        public void setConnectableBus(String busId) {
            throw NodeBreakerVoltageLevel.createNotSupportedNodeBreakerTopologyException();
        }

        @Override
        public void moveConnectable(String busId, boolean connected) {
            if (removed) {
                throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
            }
            getConnectable().move(NodeTerminal.this, getTopologyPoint(), busId, connected);
        }

    };

    @Override
    public TopologyPoint getTopologyPoint() {
        return new NodeTopologyPointImpl(getVoltageLevel().getId(), getNode());
    }

    private final BusViewExt busView = new BusViewExt() {

        @Override
        public BusExt getBus() {
            if (removed) {
                throw new PowsyblException(CANNOT_ACCESS_BUS_REMOVED_EQUIPMENT + connectable.id);
            }
            return ((NodeBreakerVoltageLevel) voltageLevel).getCalculatedBusTopology().getBus(node);
        }

        @Override
        public BusExt getConnectableBus() {
            if (removed) {
                throw new PowsyblException(CANNOT_ACCESS_BUS_REMOVED_EQUIPMENT + connectable.id);
            }
            return ((NodeBreakerVoltageLevel) voltageLevel).getCalculatedBusTopology().getConnectableBus(node);
        }

    };

    NodeTerminal(Ref<? extends VariantManagerHolder> network, ThreeSides side, int node) {
        super(network, side);
        this.node = node;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        v = new TDoubleArrayList(variantArraySize);
        angle = new TDoubleArrayList(variantArraySize);
        connectedComponentNumber = new TIntArrayList(variantArraySize);
        synchronousComponentNumber = new TIntArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            v.add(Double.NaN);
            angle.add(Double.NaN);
            connectedComponentNumber.add(0);
            synchronousComponentNumber.add(0);
        }
    }

    protected void notifyUpdate(String attribute, String variantId, Object oldValue, Object newValue) {
        getConnectable().notifyUpdate(attribute, variantId, oldValue, newValue);
    }

    public int getNode() {
        return node;
    }

    @Override
    protected double getV() {
        if (removed) {
            throw new PowsyblException("Cannot access v of removed equipment " + connectable.id);
        }
        return v.get(getVariantManagerHolder().getVariantIndex());
    }

    void setV(double v) {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        if (v < 0) {
            throw new ValidationException(connectable, "voltage cannot be < 0");
        }
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        double oldValue = this.v.set(variantIndex, v);
        String variantId = getVariantManagerHolder().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("v", variantId, oldValue, v);
    }

    double getAngle() {
        if (removed) {
            throw new PowsyblException("Cannot access angle of removed equipment " + connectable.id);
        }
        return angle.get(getVariantManagerHolder().getVariantIndex());
    }

    void setAngle(double angle) {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        double oldValue = this.angle.set(variantIndex, angle);
        String variantId = getVariantManagerHolder().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("angle", variantId, oldValue, angle);
    }

    int getConnectedComponentNumber() {
        if (removed) {
            throw new PowsyblException("Cannot access connected component of removed equipment " + connectable.id);
        }
        return connectedComponentNumber.get(getVariantManagerHolder().getVariantIndex());
    }

    void setConnectedComponentNumber(int connectedComponentNumber) {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        int oldValue = this.connectedComponentNumber.set(variantIndex, connectedComponentNumber);
        String variantId = getVariantManagerHolder().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("connectedComponentNumber", variantId, oldValue, connectedComponentNumber);
    }

    int getSynchronousComponentNumber() {
        if (removed) {
            throw new PowsyblException("Cannot access synchronous component of removed equipment " + connectable.id);
        }
        return synchronousComponentNumber.get(getVariantManagerHolder().getVariantIndex());
    }

    void setSynchronousComponentNumber(int componentNumber) {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        int oldValue = this.synchronousComponentNumber.set(variantIndex, componentNumber);
        String variantId = getVariantManagerHolder().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("synchronousComponentNumber", variantId, oldValue, componentNumber);
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
        if (removed) {
            throw new PowsyblException("Cannot access connectivity status of removed equipment " + connectable.id);
        }
        return ((NodeBreakerVoltageLevel) voltageLevel).isConnected(this);
    }

    @Override
    public boolean traverse(TopologyTraverser traverser, Set<Terminal> visitedTerminals, TraversalType traversalType) {
        if (removed) {
            throw new PowsyblException(String.format("Associated equipment %s is removed", connectable.id));
        }
        return ((NodeBreakerVoltageLevel) voltageLevel).traverse(this, traverser, visitedTerminals, traversalType);
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
        ((NodeBreakerVoltageLevel) voltageLevel).traverse(this, traverser, traversalType);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
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
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        v.remove(v.size() - number, number);
        angle.remove(angle.size() - number, number);
        connectedComponentNumber.remove(connectedComponentNumber.size() - number, number);
        synchronousComponentNumber.remove(synchronousComponentNumber.size() - number, number);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
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
