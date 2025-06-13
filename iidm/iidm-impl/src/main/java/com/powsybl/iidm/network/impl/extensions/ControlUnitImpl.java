/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.extensions.ControlUnit;
import com.powsybl.iidm.network.impl.NetworkImpl;
import com.powsybl.iidm.network.impl.VariantManagerHolder;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ControlUnitImpl implements ControlUnit {

    private final String id;

    private final TBooleanArrayList participate;

    private ControlZoneImpl controlZone;

    public ControlUnitImpl(String id, boolean participate, VariantManagerHolder variantManagerHolder) {
        this.id = Objects.requireNonNull(id);
        int variantArraySize = variantManagerHolder.getVariantManager().getVariantArraySize();
        this.participate = new TBooleanArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.participate.add(participate);
        }
    }

    public void setControlZone(ControlZoneImpl controlZone) {
        this.controlZone = Objects.requireNonNull(controlZone);
    }

    protected int getVariantIndex() {
        return controlZone.getSecondaryVoltageControl().getVariantManagerHolder().getVariantIndex();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isParticipate() {
        return participate.get(getVariantIndex());
    }

    @Override
    public void setParticipate(boolean participate) {
        int variantIndex = getVariantIndex();
        boolean oldParticipate = this.participate.get(variantIndex);
        if (participate != oldParticipate) {
            this.participate.set(variantIndex, participate);
            SecondaryVoltageControlImpl secondaryVoltageControl = controlZone.getSecondaryVoltageControl();
            NetworkImpl network = (NetworkImpl) secondaryVoltageControl.getExtendable();
            String variantId = network.getVariantManager().getVariantId(variantIndex);
            network.getListeners().notifyExtensionUpdate(secondaryVoltageControl, "controlUnitParticipate", variantId,
                    new ParticipateEvent(controlZone.getName(), id, oldParticipate), new ParticipateEvent(controlZone.getName(), id, participate));
        }
    }

    void extendVariantArraySize(int number, int sourceIndex) {
        participate.ensureCapacity(participate.size() + number);
        for (int i = 0; i < number; ++i) {
            participate.add(participate.get(sourceIndex));
        }
    }

    void reduceVariantArraySize(int number) {
        participate.remove(participate.size() - number, number);
    }

    void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            participate.set(index, participate.get(sourceIndex));
        }
    }
}
