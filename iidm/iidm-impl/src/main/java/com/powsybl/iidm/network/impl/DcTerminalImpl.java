/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.DcConnectable;
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.DcTerminal;
import com.powsybl.iidm.network.ValidationUtil;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.Objects;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcTerminalImpl implements DcTerminal, MultiVariantObject {

    private Ref<? extends VariantManagerHolder> network;
    private DcConnectable dcConnectable;
    private final DcNode dcNode;
    protected final TDoubleArrayList p;
    protected final TDoubleArrayList i;
    private final TBooleanArrayList connected;
    private boolean removed = false;

    DcTerminalImpl(Ref<? extends VariantManagerHolder> network, DcNode dcNode, boolean connected) {
        this.network = Objects.requireNonNull(network);
        this.dcNode = Objects.requireNonNull(dcNode);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.connected = new TBooleanArrayList(variantArraySize);
        p = new TDoubleArrayList(variantArraySize);
        i = new TDoubleArrayList(variantArraySize);
        for (int iVar = 0; iVar < variantArraySize; iVar++) {
            this.connected.add(connected);
            this.p.add(Double.NaN);
            this.i.add(Double.NaN);
        }
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
    public DcNode getDcNode() {
        ValidationUtil.checkAccessOfRemovedEquipment(dcConnectable.getId(), this.removed);
        return this.dcNode;
    }

    @Override
    public double getP() {
        ValidationUtil.checkAccessOfRemovedEquipment(dcConnectable.getId(), this.removed);
        return this.p.get(getVariantManagerHolder().getVariantIndex());
    }

    @Override
    public DcTerminal setP(double p) {
        ValidationUtil.checkModifyOfRemovedEquipment(dcConnectable.getId(), this.removed);
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        this.p.set(variantIndex, p);
        // TODO notification
        return this;
    }

    @Override
    public double getI() {
        ValidationUtil.checkAccessOfRemovedEquipment(dcConnectable.getId(), this.removed);
        return this.i.get(getVariantManagerHolder().getVariantIndex());
    }

    @Override
    public DcTerminal setI(double i) {
        ValidationUtil.checkModifyOfRemovedEquipment(dcConnectable.getId(), this.removed);
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        this.i.set(variantIndex, i);
        // TODO notification
        return this;
    }

    @Override
    public boolean isConnected() {
        ValidationUtil.checkAccessOfRemovedEquipment(dcConnectable.getId(), this.removed);
        return this.connected.get(getVariantManagerHolder().getVariantIndex());
    }

    @Override
    public DcTerminal setConnected(boolean connected) {
        ValidationUtil.checkModifyOfRemovedEquipment(dcConnectable.getId(), this.removed);
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        this.connected.set(variantIndex, connected);
        // TODO notification
        return this;
    }

    @Override
    public void remove() {
        this.removed = true;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        connected.ensureCapacity(connected.size() + number);
        p.ensureCapacity(p.size() + number);
        i.ensureCapacity(i.size() + number);
        for (int iVar = 0; iVar < number; iVar++) {
            connected.add(connected.get(sourceIndex));
            p.add(p.get(sourceIndex));
            i.add(i.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        for (int iVar = 0; iVar < number; iVar++) {
            connected.removeAt(connected.size() - 1);
            p.removeAt(i.size() - 1);
            i.removeAt(i.size() - 1);
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            connected.set(index, connected.get(sourceIndex));
            p.set(index, p.get(sourceIndex));
            i.set(index, i.get(sourceIndex));
        }
    }

    <I extends DcConnectable<I>> void setDcConnectable(DcConnectable<I> dcConnectable) {
        this.dcConnectable = dcConnectable;
    }
}
