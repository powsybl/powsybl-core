/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import gnu.trove.list.array.TByteArrayList;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SwitchImpl extends AbstractIdentifiable<Switch> implements Switch, MultiVariantObject {

    private final VoltageLevelExt voltageLevel;

    private final SwitchKind kind;

    private boolean fictitious;

    private final TByteArrayList open;

    private final TByteArrayList retained;

    SwitchImpl(VoltageLevelExt voltageLevel,
               String id, String name, SwitchKind kind, final boolean open, boolean retained, boolean fictitious) {
        super(id, name);
        this.voltageLevel = voltageLevel;
        this.kind = kind;
        this.fictitious = fictitious;
        int variantArraySize = voltageLevel.getNetwork().getVariantManager().getVariantArraySize();
        this.open = new TByteArrayList(variantArraySize);
        this.retained = new TByteArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.open.add((byte) (open ? 1 : 0));
            this.retained.add((byte) (retained ? 1 : 0));
        }
    }

    @Override
    public VoltageLevelExt getVoltageLevel() {
        return voltageLevel;
    }

    @Override
    public SwitchKind getKind() {
        return kind;
    }

    @Override
    public boolean isOpen() {
        return open.get(voltageLevel.getNetwork().getVariantIndex()) == 1;
    }

    @Override
    public void setOpen(boolean open) {
        NetworkImpl network = voltageLevel.getNetwork();
        int index = network.getVariantIndex();
        boolean oldValue = this.open.get(index) == 1;
        if (oldValue != open) {
            this.open.set(index, (byte) (open ? 1 : 0));
            voltageLevel.invalidateCache();
            network.getListeners().notifyUpdate(this, "open", oldValue, open);
        }
    }

    @Override
    public boolean isRetained() {
        return retained.get(voltageLevel.getNetwork().getVariantIndex()) == 1;
    }

    @Override
    public void setRetained(boolean retained) {
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new ValidationException(this, "retain status is not modifiable in a non node/breaker voltage level");
        }
        NetworkImpl network = voltageLevel.getNetwork();
        int index = network.getVariantIndex();
        boolean oldValue = this.retained.get(index) == 1;
        if (oldValue != retained) {
            this.retained.set(index, (byte) (retained ? 1 : 0));
            voltageLevel.invalidateCache();
            network.getListeners().notifyUpdate(this, "retained", oldValue, retained);
        }
    }

    @Override
    public boolean isFictitious() {
        return fictitious;
    }

    @Override
    public void setFictitious(boolean fictitious) {
        boolean oldValue = this.fictitious;
        if (oldValue != fictitious) {
            this.fictitious = fictitious;
            voltageLevel.invalidateCache();
            NetworkImpl network = voltageLevel.getNetwork();
            network.getListeners().notifyUpdate(this, "fictitious", oldValue, fictitious);
        }
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        open.ensureCapacity(open.size() + number);
        open.fill(initVariantArraySize, initVariantArraySize + number, open.get(sourceIndex));
        retained.ensureCapacity(retained.size() + number);
        retained.fill(initVariantArraySize, initVariantArraySize + number, retained.get(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        open.remove(open.size() - number, number);
        retained.remove(retained.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, final int sourceIndex) {
        for (int index : indexes) {
            open.set(index, open.get(sourceIndex));
            retained.set(index, retained.get(sourceIndex));
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Switch";
    }

}
