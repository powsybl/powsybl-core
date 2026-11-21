/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.PilotPoint;
import com.powsybl.iidm.network.impl.NetworkImpl;
import com.powsybl.iidm.network.impl.VariantManagerHolder;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class PilotPointImpl implements PilotPoint {

    private final List<String> busIds;

    private final List<String> busbarSectionIds;

    private final List<String> activeBusOrBusbarSectionId;

    private final TDoubleArrayList targetV;

    private ControlZoneImpl controlZone;

    PilotPointImpl(List<String> busIds, List<String> busbarSectionIds, String activeBusOrBusbarSectionId, double targetV,
                   VariantManagerHolder variantManagerHolder) {
        this.busIds = new ArrayList<>(Objects.requireNonNull(busIds));
        this.busbarSectionIds = new ArrayList<>(Objects.requireNonNull(busbarSectionIds));
        int variantArraySize = variantManagerHolder.getVariantManager().getVariantArraySize();
        this.activeBusOrBusbarSectionId = new ArrayList<>(variantArraySize);
        this.targetV = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.activeBusOrBusbarSectionId.add(activeBusOrBusbarSectionId);
            this.targetV.add(targetV);
        }
    }

    static void checkActiveBusOrBusbarSectionId(String activeBusOrBusbarSectionId, List<String> busIds, List<String> busbarSectionIds) {
        if (activeBusOrBusbarSectionId != null
                && !busIds.contains(activeBusOrBusbarSectionId)
                && !busbarSectionIds.contains(activeBusOrBusbarSectionId)) {
            throw new PowsyblException("Active bus or busbar section '" + activeBusOrBusbarSectionId
                    + "' is not one of the pilot point buses or busbar sections");
        }
    }

    public void setControlZone(ControlZoneImpl controlZone) {
        this.controlZone = Objects.requireNonNull(controlZone);
    }

    protected int getVariantIndex() {
        return controlZone.getSecondaryVoltageControl().getVariantManagerHolder().getVariantIndex();
    }

    @Override
    public List<String> getBusIds() {
        return Collections.unmodifiableList(busIds);
    }

    @Override
    public List<String> getBusbarSectionIds() {
        return Collections.unmodifiableList(busbarSectionIds);
    }

    @Override
    public Optional<String> getActiveBusOrBusbarSectionId() {
        return Optional.ofNullable(activeBusOrBusbarSectionId.get(getVariantIndex()));
    }

    @Override
    public void setActiveBusOrBusbarSectionId(String activeBusOrBusbarSectionId) {
        checkActiveBusOrBusbarSectionId(activeBusOrBusbarSectionId, busIds, busbarSectionIds);
        int variantIndex = getVariantIndex();
        String oldActiveBusOrBusbarSectionId = this.activeBusOrBusbarSectionId.get(variantIndex);
        if (!Objects.equals(activeBusOrBusbarSectionId, oldActiveBusOrBusbarSectionId)) {
            this.activeBusOrBusbarSectionId.set(variantIndex, activeBusOrBusbarSectionId);
            SecondaryVoltageControlImpl secondaryVoltageControl = controlZone.getSecondaryVoltageControl();
            NetworkImpl network = (NetworkImpl) secondaryVoltageControl.getExtendable();
            String variantId = network.getVariantManager().getVariantId(variantIndex);
            network.getListeners().notifyExtensionUpdate(secondaryVoltageControl, "pilotPointActiveBusOrBusbarSectionId", variantId,
                    new ActiveBusOrBusbarSectionEvent(controlZone.getName(), oldActiveBusOrBusbarSectionId),
                    new ActiveBusOrBusbarSectionEvent(controlZone.getName(), activeBusOrBusbarSectionId));
        }
    }

    protected void updateIds(UnaryOperator<String> updater) {
        busIds.replaceAll(updater);
        busbarSectionIds.replaceAll(updater);
        activeBusOrBusbarSectionId.replaceAll(id -> id != null ? updater.apply(id) : null);
    }

    protected void removeIdIf(Predicate<String> predicate) {
        busIds.removeIf(predicate);
        busbarSectionIds.removeIf(predicate);
        activeBusOrBusbarSectionId.replaceAll(id -> id != null && predicate.test(id) ? null : id);
    }

    @Override
    public double getTargetV() {
        return targetV.get(getVariantIndex());
    }

    @Override
    public void setTargetV(double targetV) {
        if (Double.isNaN(targetV)) {
            throw new PowsyblException("Invalid pilot point target voltage for zone '" + controlZone.getName() + "'");
        }
        int variantIndex = getVariantIndex();
        double oldTargetV = this.targetV.get(variantIndex);
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
        targetV.ensureCapacity(targetV.size() + number);
        for (int i = 0; i < number; ++i) {
            activeBusOrBusbarSectionId.add(activeBusOrBusbarSectionId.get(sourceIndex));
            targetV.add(targetV.get(sourceIndex));
        }
    }

    void reduceVariantArraySize(int number) {
        activeBusOrBusbarSectionId.subList(activeBusOrBusbarSectionId.size() - number, activeBusOrBusbarSectionId.size()).clear();
        targetV.remove(targetV.size() - number, number);
    }

    void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            activeBusOrBusbarSectionId.set(index, activeBusOrBusbarSectionId.get(sourceIndex));
            targetV.set(index, targetV.get(sourceIndex));
        }
    }
}
