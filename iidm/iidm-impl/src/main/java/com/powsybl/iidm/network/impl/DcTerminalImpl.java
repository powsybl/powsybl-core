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

import java.util.Objects;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcTerminalImpl implements DcTerminal, MultiVariantObject {

    private Ref<? extends VariantManagerHolder> network;
    private DcConnectable dcConnectable;
    private final DcNode dcNode;
    private final TBooleanArrayList connected;
    private boolean removed = false;

    DcTerminalImpl(Ref<? extends VariantManagerHolder> network, DcNode dcNode, boolean connected) {
        this.network = Objects.requireNonNull(network);
        this.dcNode = Objects.requireNonNull(dcNode);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.connected = new TBooleanArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.connected.add(connected);
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
    public boolean isConnected() {
        ValidationUtil.checkAccessOfRemovedEquipment(dcConnectable.getId(), this.removed);
        return this.connected.get(getVariantManagerHolder().getVariantIndex());
    }

    @Override
    public DcTerminal setConnected(boolean connected) {
        ValidationUtil.checkModifyOfRemovedEquipment(dcConnectable.getId(), this.removed);
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        this.connected.set(variantIndex, connected);
        return this;
    }

    @Override
    public void remove() {
        this.removed = true;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        connected.ensureCapacity(connected.size() + number);
        for (int i = 0; i < number; i++) {
            connected.add(connected.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        for (int i = 0; i < number; i++) {
            connected.removeAt(connected.size() - 1);
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
        }
    }

    <I extends DcConnectable<I>> void setDcConnectable(DcConnectable<I> dcConnectable) {
        this.dcConnectable = dcConnectable;
    }
}
