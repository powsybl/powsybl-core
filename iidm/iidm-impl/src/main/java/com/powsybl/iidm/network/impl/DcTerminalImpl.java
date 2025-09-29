/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.commons.util.fastutil.ExtendedBooleanArrayList;
import com.powsybl.commons.util.fastutil.ExtendedDoubleArrayList;
import com.powsybl.iidm.network.DcConnectable;
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.DcTerminal;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.ValidationUtil;

import java.util.Objects;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcTerminalImpl implements DcTerminal, MultiVariantObject {

    private final Ref<? extends VariantManagerHolder> network;
    private DcConnectable<?> dcConnectable;
    private final TwoSides side;
    private final DcNode dcNode;
    protected final ExtendedDoubleArrayList p;
    protected final ExtendedDoubleArrayList i;
    private final ExtendedBooleanArrayList connected;
    private boolean removed = false;

    DcTerminalImpl(Ref<? extends VariantManagerHolder> network, TwoSides side, DcNode dcNode, boolean connected) {
        this.network = Objects.requireNonNull(network);
        this.side = side;
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
        getNetwork().getListeners().notifyUpdate(dcConnectable, () -> "p" + getAttributeSideSuffix(), variantId, oldValue, p);
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
        getNetwork().getListeners().notifyUpdate(dcConnectable, () -> "i" + getAttributeSideSuffix(), variantId, oldValue, i);
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
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        getNetwork().getListeners().notifyUpdate(dcConnectable, () -> "connected" + getAttributeSideSuffix(), variantId, oldValue, connected);
        return this;
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

    String getAttributeSideSuffix() {
        return "" + (side != null ? side.getNum() : "");
    }
}
