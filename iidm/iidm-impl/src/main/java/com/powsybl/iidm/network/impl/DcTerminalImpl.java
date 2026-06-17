/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.ref.Ref;
import com.powsybl.commons.util.fastutil.ExtendedBooleanArrayList;
import com.powsybl.commons.util.fastutil.ExtendedDoubleArrayList;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraversalType;

import java.util.Objects;
import java.util.Set;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcTerminalImpl implements DcTerminal, MultiVariantObject {

    private final Ref<? extends VariantManagerHolder> network;
    private DcConnectable<?> dcConnectable;
    private final TwoSides side;
    private final TerminalNumber terminalNumber;
    private final DcNode dcNode;
    protected final ExtendedDoubleArrayList p;
    protected final ExtendedDoubleArrayList i;
    private final ExtendedBooleanArrayList connected;
    private boolean removed = false;

    DcTerminalImpl(Ref<? extends VariantManagerHolder> network, TwoSides side, TerminalNumber terminalNumber, DcNode dcNode, boolean connected) {
        if (side != null && terminalNumber != null) {
            throw new IllegalStateException("cannot have both side and number");
        }
        this.network = Objects.requireNonNull(network);
        this.side = side;
        this.terminalNumber = terminalNumber;
        this.dcNode = Objects.requireNonNull(dcNode);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.connected = new ExtendedBooleanArrayList(variantArraySize, connected);
        p = new ExtendedDoubleArrayList(variantArraySize, Double.NaN);
        i = new ExtendedDoubleArrayList(variantArraySize, Double.NaN);
    }

    protected VariantManagerHolder getVariantManagerHolder() {
        return network.get();
    }

    @Override
    public DcConnectable getDcConnectable() {
        ValidationUtil.checkAccessOfRemovedEquipment(dcConnectable.getId(), this.removed);
        return this.dcConnectable;
    }

    @Override
    public TwoSides getSide() {
        ValidationUtil.checkAccessOfRemovedEquipment(dcConnectable.getId(), this.removed);
        return this.side;
    }

    @Override
    public TerminalNumber getTerminalNumber() {
        ValidationUtil.checkAccessOfRemovedEquipment(dcConnectable.getId(), this.removed);
        return this.terminalNumber;
    }

    @Override
    public DcNode getDcNode() {
        ValidationUtil.checkAccessOfRemovedEquipment(dcConnectable.getId(), this.removed);
        return this.dcNode;
    }

    @Override
    public double getP() {
        ValidationUtil.checkAccessOfRemovedEquipment(dcConnectable.getId(), this.removed);
        return this.p.getDouble(getVariantManagerHolder().getVariantIndex());
    }

    @Override
    public DcTerminal setP(double p) {
        ValidationUtil.checkModifyOfRemovedEquipment(dcConnectable.getId(), this.removed);
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        double oldValue = this.p.set(variantIndex, p);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        getNetwork().getListeners().notifyUpdate(dcConnectable, () -> "p_dc" + getAttributeSideOrNumberSuffix(), variantId, oldValue, p);
        return this;
    }

    @Override
    public double getI() {
        ValidationUtil.checkAccessOfRemovedEquipment(dcConnectable.getId(), this.removed);
        return this.i.getDouble(getVariantManagerHolder().getVariantIndex());
    }

    @Override
    public DcTerminal setI(double i) {
        ValidationUtil.checkModifyOfRemovedEquipment(dcConnectable.getId(), this.removed);
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        double oldValue = this.i.set(variantIndex, i);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        getNetwork().getListeners().notifyUpdate(dcConnectable, () -> "i_dc" + getAttributeSideOrNumberSuffix(), variantId, oldValue, i);
        return this;
    }

    @Override
    public boolean isConnected() {
        ValidationUtil.checkAccessOfRemovedEquipment(dcConnectable.getId(), this.removed);
        return this.connected.getBoolean(getVariantManagerHolder().getVariantIndex());
    }

    @Override
    public DcTerminal setConnected(boolean connected) {
        ValidationUtil.checkModifyOfRemovedEquipment(dcConnectable.getId(), this.removed);
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        boolean oldValue = this.connected.set(variantIndex, connected);
        if (oldValue != connected) {
            ((AbstractNetwork) dcConnectable.getParentNetwork()).getDcTopologyModel().invalidateCache();
            String variantId = network.get().getVariantManager().getVariantId(variantIndex);
            getNetwork().getListeners().notifyUpdate(dcConnectable, () -> "connected_dc" + getAttributeSideOrNumberSuffix(), variantId, oldValue, connected);
        }
        return this;
    }

    @Override
    public DcBus getDcBus() {
        return isConnected() ? dcNode.getDcBus() : null;
    }

    public void remove() {
        this.removed = true;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        connected.growAndFill(number, connected.getBoolean(sourceIndex));
        p.growAndFill(number, p.getDouble(sourceIndex));
        i.growAndFill(number, i.getDouble(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        connected.removeElements(number);
        p.removeElements(number);
        i.removeElements(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            connected.set(index, connected.getBoolean(sourceIndex));
            p.set(index, p.getDouble(sourceIndex));
            i.set(index, i.getDouble(sourceIndex));
        }
    }

    <I extends DcConnectable<I>> void setDcConnectable(DcConnectable<I> dcConnectable) {
        this.dcConnectable = dcConnectable;
    }

    NetworkImpl getNetwork() {
        if (dcConnectable instanceof AbstractDcConnectable<?> abstractDcConnectable) {
            return abstractDcConnectable.getNetwork();
        } else if (dcConnectable instanceof AbstractAcDcConverter<?> abstractDcConverter) {
            return abstractDcConverter.getNetwork();
        }
        throw new IllegalStateException("Unexpected dcConnectable type: " + dcConnectable.getClass().getName());
    }

    Network getParentNetwork() {
        if (dcConnectable instanceof AbstractDcConnectable<?> abstractDcConnectable) {
            return abstractDcConnectable.getParentNetwork();
        } else if (dcConnectable instanceof AbstractAcDcConverter<?> abstractDcConverter) {
            return abstractDcConverter.getParentNetwork();
        }
        throw new IllegalStateException("Unexpected dcConnectable type: " + dcConnectable.getClass().getName());
    }

    String getAttributeSideOrNumberSuffix() {
        return "" + (side != null ? side.getNum() : "") + (terminalNumber != null ? terminalNumber.getNum() : "");
    }

    public boolean traverse(TopologyTraverser traverser, Set<DcTerminal> visitedDcTerminals, TraversalType traversalType) {
        if (removed) {
            throw new PowsyblException(String.format("Associated equipment %s is removed", dcConnectable.getId()));
        }
        return getTopologyModel().traverse(this, traverser, visitedDcTerminals, traversalType);
    }

    @Override
    public void traverse(DcTerminal.TopologyTraverser traverser) {
        traverse(traverser, TraversalType.DEPTH_FIRST);
    }

    @Override
    public void traverse(DcTerminal.TopologyTraverser traverser, TraversalType traversalType) {
        if (removed) {
            throw new PowsyblException(String.format("Associated equipment %s is removed", dcConnectable.getId()));
        }
        getTopologyModel().traverse(this, traverser, traversalType);
    }

    private DcTopologyModel getTopologyModel() {
        return ((AbstractNetwork) this.getParentNetwork()).getDcTopologyModel();
    }

    @Override
    public boolean disconnect() {
        return getTopologyModel().disconnect(this);
    }
}
