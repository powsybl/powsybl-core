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
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcSwitchImpl extends AbstractIdentifiable<DcSwitch> implements DcSwitch, MultiVariantObject {

    public static final String OPEN_ATTRIBUTE = "open";

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;
    private final DcSwitchKind kind;
    private final DcNode dcNode1;
    private final DcNode dcNode2;
    private final TBooleanArrayList open;
    private boolean removed = false;

    DcSwitchImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef, String id, String name, boolean fictitious,
                 DcSwitchKind kind, DcNode dcNode1, DcNode dcNode2, boolean open) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
        this.subnetworkRef = subnetworkRef;
        this.kind = kind;
        this.dcNode1 = dcNode1;
        this.dcNode2 = dcNode2;

        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.open = new TBooleanArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.open.add(open);
        }
    }

    @Override
    public NetworkImpl getNetwork() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "network");
        return networkRef.get();
    }

    @Override
    public Network getParentNetwork() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "network");
        return Optional.ofNullable((Network) subnetworkRef.get()).orElse(getNetwork());
    }

    protected VariantManagerHolder getVariantManagerHolder() {
        return getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "DC Switch";
    }

    @Override
    public DcSwitchKind getKind() {
        return this.kind;
    }

    @Override
    public DcNode getDcNode1() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "dcNode1");
        return this.dcNode1;
    }

    @Override
    public DcNode getDcNode2() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "dcNode2");
        return this.dcNode2;
    }

    @Override
    public boolean isOpen() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, OPEN_ATTRIBUTE);
        return this.open.get(getVariantManagerHolder().getVariantIndex());
    }

    @Override
    public DcSwitch setOpen(boolean open) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, OPEN_ATTRIBUTE);
        int variantIndex = getVariantManagerHolder().getVariantIndex();
        boolean oldValue = this.open.get(variantIndex);
        if (oldValue != open) {
            this.open.set(variantIndex, open);
            String variantId = getVariantManagerHolder().getVariantManager().getVariantId(variantIndex);
            getNetwork().getListeners().notifyUpdate(this, OPEN_ATTRIBUTE, variantId, oldValue, open);
        }
        return this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        open.ensureCapacity(open.size() + number);
        for (int i = 0; i < number; i++) {
            open.add(open.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        for (int i = 0; i < number; i++) {
            open.removeAt(open.size() - 1);
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            open.set(index, open.get(sourceIndex));
        }
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();

        network.getListeners().notifyBeforeRemoval(this);

        network.getIndex().remove(this);

        network.getListeners().notifyAfterRemoval(id);
        this.removed = true;
    }
}
