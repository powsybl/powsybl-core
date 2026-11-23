/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraversalType;

import java.util.Set;

/**
 * A terminal connected to a node breaker topology.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NodeTerminal extends AbstractTerminal {

    private final int node;

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
            getConnectable().move(NodeTerminal.this, node, voltageLevelId);
        }
    };

    private NodeBreakerTopologyModel getTopologyModel() {
        return (NodeBreakerTopologyModel) voltageLevel.getTopologyModel();
    }

    private final BusBreakerViewExt busBreakerView = new BusBreakerViewExt() {

        @Override
        public BusExt getBus() {
            if (removed) {
                throw new PowsyblException(CANNOT_ACCESS_BUS_REMOVED_EQUIPMENT + connectable.id);
            }
            return getTopologyModel().getCalculatedBusBreakerTopology().getBus(node);
        }

        @Override
        public BusExt getConnectableBus() {
            if (removed) {
                throw new PowsyblException(CANNOT_ACCESS_BUS_REMOVED_EQUIPMENT + connectable.id);
            }
            return getTopologyModel().getCalculatedBusBreakerTopology().getConnectableBus(node);
        }

        @Override
        public void setConnectableBus(String busId) {
            throw NodeBreakerTopologyModel.createNotSupportedNodeBreakerTopologyException();
        }

        @Override
        public void moveConnectable(String busId, boolean connected) {
            if (removed) {
                throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
            }
            getConnectable().move(NodeTerminal.this, busId, connected);
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
            return getTopologyModel().getCalculatedBusTopology().getBus(node);
        }

        @Override
        public BusExt getConnectableBus() {
            if (removed) {
                throw new PowsyblException(CANNOT_ACCESS_BUS_REMOVED_EQUIPMENT + connectable.id);
            }
            return getTopologyModel().getCalculatedBusTopology().getConnectableBus(node);
        }

    };

    NodeTerminal(Ref<? extends VariantManagerHolder> network, ThreeSides side, TerminalNumber terminalNumber, int node) {
        super(network, side, terminalNumber);
        this.node = node;
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
        return getTopologyModel().getV(node);
    }

    void setV(double v) {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        if (v < 0) {
            throw new ValidationException(connectable, "voltage cannot be < 0");
        }
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        double oldValue = getTopologyModel().setV(node, v);
        String variantId = getVariantManagerHolder().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("v", variantId, oldValue, v);
    }

    double getAngle() {
        if (removed) {
            throw new PowsyblException("Cannot access angle of removed equipment " + connectable.id);
        }
        return getTopologyModel().getAngle(node);
    }

    void setAngle(double angle) {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        double oldValue = getTopologyModel().setAngle(node, angle);
        String variantId = getVariantManagerHolder().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("angle", variantId, oldValue, angle);
    }

    int getConnectedComponentNumber() {
        if (removed) {
            throw new PowsyblException("Cannot access connected component of removed equipment " + connectable.id);
        }
        return getTopologyModel().getConnectedComponentNumber(node);
    }

    void setConnectedComponentNumber(int connectedComponentNumber) {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        int oldValue = getTopologyModel().setConnectedComponentNumber(node, connectedComponentNumber);
        String variantId = getVariantManagerHolder().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("connectedComponentNumber", variantId, oldValue, connectedComponentNumber);
    }

    int getSynchronousComponentNumber() {
        if (removed) {
            throw new PowsyblException("Cannot access synchronous component of removed equipment " + connectable.id);
        }
        return getTopologyModel().getSynchronousComponentNumber(node);
    }

    void setSynchronousComponentNumber(int componentNumber) {
        if (removed) {
            throw new PowsyblException(UNMODIFIABLE_REMOVED_EQUIPMENT + connectable.id);
        }
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        int oldValue = getTopologyModel().setSynchronousComponentNumber(node, componentNumber);
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
        return getTopologyModel().isConnected(this);
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
    public String toString() {
        return getClass().getSimpleName() + "[" + node + "]";
    }
}
