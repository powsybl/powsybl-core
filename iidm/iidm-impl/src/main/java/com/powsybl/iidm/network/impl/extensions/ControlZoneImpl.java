/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.extensions.ControlUnit;
import com.powsybl.iidm.network.extensions.ControlZone;
import com.powsybl.iidm.network.extensions.PilotPoint;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ControlZoneImpl implements ControlZone {

    private SecondaryVoltageControlImpl secondaryVoltageControl;

    private final String name;

    private final PilotPoint pilotPoint;

    private final List<ControlUnit> controlUnits;

    ControlZoneImpl(String name, PilotPoint pilotPoint, List<ControlUnit> controlUnits) {
        this.name = Objects.requireNonNull(name);
        this.pilotPoint = Objects.requireNonNull(pilotPoint);
        this.controlUnits = Objects.requireNonNull(controlUnits);
    }

    void setSecondaryVoltageControl(SecondaryVoltageControlImpl secondaryVoltageControl) {
        this.secondaryVoltageControl = Objects.requireNonNull(secondaryVoltageControl);
    }

    public SecondaryVoltageControlImpl getSecondaryVoltageControl() {
        return secondaryVoltageControl;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PilotPoint getPilotPoint() {
        return pilotPoint;
    }

    @Override
    public List<ControlUnit> getControlUnits() {
        return Collections.unmodifiableList(controlUnits);
    }

    @Override
    public Optional<ControlUnit> getControlUnit(String id) {
        Objects.requireNonNull(id);
        return controlUnits.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    void extendVariantArraySize(int number, int sourceIndex) {
        ((PilotPointImpl) pilotPoint).extendVariantArraySize(number, sourceIndex);
        for (ControlUnit controlUnit : controlUnits) {
            ((ControlUnitImpl) controlUnit).extendVariantArraySize(number, sourceIndex);
        }
    }

    void reduceVariantArraySize(int number) {
        ((PilotPointImpl) pilotPoint).reduceVariantArraySize(number);
        for (ControlUnit controlUnit : controlUnits) {
            ((ControlUnitImpl) controlUnit).reduceVariantArraySize(number);
        }
    }

    void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        ((PilotPointImpl) pilotPoint).allocateVariantArrayElement(indexes, sourceIndex);
        for (ControlUnit controlUnit : controlUnits) {
            ((ControlUnitImpl) controlUnit).allocateVariantArrayElement(indexes, sourceIndex);
        }
    }
}
