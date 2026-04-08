/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ControlZoneAdderImpl implements ControlZoneAdder {

    private final SecondaryVoltageControlAdderImpl parent;

    private String name;

    private PilotPointImpl pilotPoint;

    private final List<ControlUnit> controlUnits = new ArrayList<>();

    ControlZoneAdderImpl(SecondaryVoltageControlAdderImpl parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    public SecondaryVoltageControlAdderImpl getParent() {
        return parent;
    }

    @Override
    public ControlZoneAdderImpl withName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    void setPilotPoint(PilotPointImpl pilotPoint) {
        this.pilotPoint = Objects.requireNonNull(pilotPoint);
    }

    @Override
    public PilotPointAdderImpl newPilotPoint() {
        return new PilotPointAdderImpl(this);
    }

    void addControlUnit(ControlUnitImpl controlUnit) {
        controlUnits.add(Objects.requireNonNull(controlUnit));
    }

    @Override
    public ControlUnitAdderImpl newControlUnit() {
        return new ControlUnitAdderImpl(this);
    }

    @Override
    public SecondaryVoltageControlAdderImpl add() {
        if (name == null) {
            throw new PowsyblException("Zone name is not set");
        }
        if (pilotPoint == null) {
            throw new PowsyblException("Pilot point is not set for zone '" + name + "'");
        }
        if (controlUnits.isEmpty()) {
            throw new PowsyblException("Empty control unit list for zone '" + name + "'");
        }
        ControlZoneImpl controlZone = new ControlZoneImpl(name, pilotPoint, controlUnits);
        pilotPoint.setControlZone(controlZone);
        for (var controlUnit : controlUnits) {
            ((ControlUnitImpl) controlUnit).setControlZone(controlZone);
        }
        parent.addControlZone(controlZone);
        return parent;
    }
}
