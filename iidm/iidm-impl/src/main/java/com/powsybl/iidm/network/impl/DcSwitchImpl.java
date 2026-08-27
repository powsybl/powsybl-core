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

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcSwitchImpl extends AbstractDcConnectable<DcSwitch> implements DcSwitch {

    public static final String OPEN_ATTRIBUTE = "open";
    public static final String R_ATTRIBUTE = "r";

    private final DcSwitchKind kind;
    private final TBooleanArrayList open;
    private double r;

    DcSwitchImpl(Ref<NetworkImpl> ref,
                 Ref<SubnetworkImpl> subnetworkRef,
                 String id,
                 String name,
                 boolean fictitious,
                 DcSwitchKind kind,
                 boolean open,
                 double r) {
        super(ref, subnetworkRef, id, name, fictitious);
        this.kind = kind;

        int variantArraySize = getNetwork().getVariantManager().getVariantArraySize();
        this.open = new TBooleanArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.open.add(open);
        }
        this.r = r;
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
    public DcTerminal getDcTerminal1() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal1");
        return this.dcTerminals.get(0);
    }

    @Override
    public DcTerminal getDcTerminal2() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal2");
        return this.dcTerminals.get(1);
    }

    @Override
    public DcTerminal getDcTerminal(TwoSides side) {
        Objects.requireNonNull(side);
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal");
        if (side == TwoSides.ONE) {
            return this.dcTerminals.get(0);
        } else if (side == TwoSides.TWO) {
            return this.dcTerminals.get(1);
        }
        throw new IllegalStateException("Unexpected side: " + side);
    }

    @Override
    public TwoSides getSide(DcTerminal dcTerminal) {
        Objects.requireNonNull(dcTerminal);
        if (getDcTerminal1() == dcTerminal) {
            return TwoSides.ONE;
        } else if (getDcTerminal2() == dcTerminal) {
            return TwoSides.TWO;
        } else {
            throw new IllegalStateException("The DC terminal is not connected to this DC switch");
        }
    }

    @Override
    public boolean isOpen() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, OPEN_ATTRIBUTE);
        return this.open.get(getNetwork().getVariantIndex());
    }

    @Override
    public DcSwitch setOpen(boolean open) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, OPEN_ATTRIBUTE);
        int variantIndex = getNetwork().getVariantIndex();
        boolean oldValue = this.open.get(variantIndex);
        if (oldValue != open) {
            this.open.set(variantIndex, open);
            ((AbstractNetwork) getParentNetwork()).getDcTopologyModel().invalidateCache();
            String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
            getNetwork().getListeners().notifyUpdate(this, OPEN_ATTRIBUTE, variantId, oldValue, open);
        }
        return this;
    }

    @Override
    public void remove() {
        // drop the topology graph edge before the connectable (and its terminals) is removed,
        // because removeDcSwitch needs the switch to be accessible (not yet flagged as removed)
        ((AbstractNetwork) getParentNetwork()).getDcTopologyModel().removeDcSwitch(getId());
        super.remove();
    }

    @Override
    public double getR() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, R_ATTRIBUTE);
        return r;
    }

    @Override
    public DcSwitch setR(double r) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, R_ATTRIBUTE);
        ValidationUtil.checkDoubleParamPositive(this, r, R_ATTRIBUTE);

        double oldValue = this.r;
        this.r = r;

        if ((r == 0.0) != (oldValue == 0.0)) {
            // if we change the value of r from 0 to non-zero
            // or vice versa, topology must be recomputed.
            ((AbstractNetwork) getParentNetwork()).getDcTopologyModel().invalidateAllVariantsCache();
        }
        getNetwork().getListeners().notifyUpdate(this, R_ATTRIBUTE, oldValue, r);

        return this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        open.ensureCapacity(open.size() + number);
        for (int i = 0; i < number; i++) {
            open.add(open.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        for (int i = 0; i < number; i++) {
            open.removeAt(open.size() - 1);
        }
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            open.set(index, open.get(sourceIndex));
        }
    }
}
