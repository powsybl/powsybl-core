/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.fastutil.BooleanArrayListHack;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.ValidationException;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SwitchImpl extends AbstractIdentifiable<Switch> implements Switch, MultiVariantObject {

    private final VoltageLevelExt voltageLevel;

    private final SwitchKind kind;

    private final BooleanArrayListHack open;

    private final BooleanArrayListHack retained;

    SwitchImpl(VoltageLevelExt voltageLevel,
               String id, String name, boolean fictitious, SwitchKind kind, final boolean open, boolean retained) {
        super(id, name, fictitious);
        this.voltageLevel = voltageLevel;
        this.kind = kind;
        int variantArraySize = voltageLevel.getNetwork().getVariantManager().getVariantArraySize();
        this.open = new BooleanArrayListHack(variantArraySize, open);
        this.retained = new BooleanArrayListHack(variantArraySize, retained);
    }

    @Override
    public NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    public Network getParentNetwork() {
        return voltageLevel.getParentNetwork();
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
        return open.getBoolean(getNetwork().getVariantIndex());
    }

    @Override
    public void setOpen(boolean open) {
        NetworkImpl network = getNetwork();
        int index = network.getVariantIndex();
        boolean oldValue = this.open.getBoolean(index);
        if (oldValue != open) {
            this.open.set(index, open);
            voltageLevel.getTopologyModel().invalidateCache(isRetained());
            String variantId = network.getVariantManager().getVariantId(index);
            network.getListeners().notifyUpdate(this, "open", variantId, oldValue, open);
        }
    }

    @Override
    public boolean isRetained() {
        return retained.getBoolean(getNetwork().getVariantIndex());
    }

    @Override
    public void setRetained(boolean retained) {
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new ValidationException(this, "retain status is not modifiable in a non node/breaker voltage level");
        }
        NetworkImpl network = getNetwork();
        int index = network.getVariantIndex();
        boolean oldValue = this.retained.getBoolean(index);
        if (oldValue != retained) {
            this.retained.set(index, retained);
            voltageLevel.getTopologyModel().invalidateCache();
            String variantId = network.getVariantManager().getVariantId(index);
            network.getListeners().notifyUpdate(this, "retained", variantId, oldValue, retained);
        }
    }

    @Override
    public void setFictitious(boolean fictitious) {
        boolean oldValue = this.fictitious;
        if (oldValue != fictitious) {
            this.fictitious = fictitious;
            voltageLevel.getTopologyModel().invalidateCache();
            NetworkImpl network = getNetwork();
            network.getListeners().notifyUpdate(this, "fictitious", oldValue, fictitious);
        }
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        open.growAndFill(number, open.getBoolean(sourceIndex));
        retained.growAndFill(number, retained.getBoolean(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);

        open.removeElements(number);
        retained.removeElements(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, final int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);

        for (int index : indexes) {
            open.set(index, open.getBoolean(sourceIndex));
            retained.set(index, retained.getBoolean(sourceIndex));
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Switch";
    }
}
