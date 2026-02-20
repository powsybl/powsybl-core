/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.fastutil.ExtendedDoubleArrayList;
import com.powsybl.iidm.network.extensions.PilotPoint;
import com.powsybl.iidm.network.impl.NetworkImpl;
import com.powsybl.iidm.network.impl.VariantManagerHolder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class PilotPointImpl implements PilotPoint {

    private final List<String> busbarSectionsOrBusesIds;

    private final ExtendedDoubleArrayList targetV;

    private ControlZoneImpl controlZone;

    PilotPointImpl(List<String> busbarSectionsOrBusesIds, double targetV, VariantManagerHolder variantManagerHolder) {
        this.busbarSectionsOrBusesIds = Objects.requireNonNull(busbarSectionsOrBusesIds);
        int variantArraySize = variantManagerHolder.getVariantManager().getVariantArraySize();
        this.targetV = new ExtendedDoubleArrayList(variantArraySize, targetV);
    }

    public void setControlZone(ControlZoneImpl controlZone) {
        this.controlZone = Objects.requireNonNull(controlZone);
    }

    protected int getVariantIndex() {
        return controlZone.getSecondaryVoltageControl().getVariantManagerHolder().getVariantIndex();
    }

    /**
     * Get pilot point busbar section ID or bus ID of the bus/breaker view.
     */
    @Override
    public List<String> getBusbarSectionsOrBusesIds() {
        return Collections.unmodifiableList(busbarSectionsOrBusesIds);
    }

    @Override
    public double getTargetV() {
        return targetV.getDouble(getVariantIndex());
    }

    @Override
    public void setTargetV(double targetV) {
        if (Double.isNaN(targetV)) {
            throw new PowsyblException("Invalid pilot point target voltage for zone '" + controlZone.getName() + "'");
        }
        int variantIndex = getVariantIndex();
        double oldTargetV = this.targetV.getDouble(variantIndex);
        if (targetV != oldTargetV) {
            this.targetV.set(variantIndex, targetV);
            SecondaryVoltageControlImpl secondaryVoltageControl = controlZone.getSecondaryVoltageControl();
            NetworkImpl network = (NetworkImpl) secondaryVoltageControl.getExtendable();
            String variantId = network.getVariantManager().getVariantId(variantIndex);
            network.getListeners().notifyExtensionUpdate(secondaryVoltageControl, "pilotPointTargetV", variantId,
                    new TargetVoltageEvent(controlZone.getName(), oldTargetV), new TargetVoltageEvent(controlZone.getName(), targetV));
        }
    }

    void extendVariantArraySize(int number, int sourceIndex) {
        targetV.growAndFill(number, targetV.getDouble(sourceIndex));
    }

    void reduceVariantArraySize(int number) {
        targetV.removeElements(number);
    }

    void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            targetV.set(index, targetV.getDouble(sourceIndex));
        }
    }
}
