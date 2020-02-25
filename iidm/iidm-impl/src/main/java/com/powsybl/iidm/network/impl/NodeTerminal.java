/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.ValidationException;
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

    // attributes depending on the variant

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

    NodeTerminal(Ref<? extends VariantManagerHolder> network, int node) {
        super(network);
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
        return v.get(network.get().getVariantIndex());
    }

    void setV(double v) {
        if (v < 0) {
            throw new ValidationException(connectable, "voltage cannot be < 0");
        }
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.v.set(variantIndex, v);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("v", variantId, oldValue, v);
    }

    double getAngle() {
        return angle.get(network.get().getVariantIndex());
    }

    void setAngle(double angle) {
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.angle.set(variantIndex, angle);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("angle", variantId, oldValue, angle);
    }

    int getConnectedComponentNumber() {
        return connectedComponentNumber.get(network.get().getVariantIndex());
    }

    void setConnectedComponentNumber(int connectedComponentNumber) {
        int variantIndex = network.get().getVariantIndex();
        int oldValue = this.connectedComponentNumber.set(variantIndex, connectedComponentNumber);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("connectedComponentNumber", variantId, oldValue, connectedComponentNumber);
    }

    int getSynchronousComponentNumber() {
        return synchronousComponentNumber.get(network.get().getVariantIndex());
    }

    void setSynchronousComponentNumber(int componentNumber) {
        int variantIndex = network.get().getVariantIndex();
        int oldValue = this.synchronousComponentNumber.set(variantIndex, componentNumber);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
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
