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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class PilotPointImpl implements PilotPoint {

    private final List<String> busbarSectionsOrBusesIds;

    private double targetV;

    private ControlZoneImpl controlZone;

    PilotPointImpl(List<String> busbarSectionsOrBusesIds, double targetV) {
        this.busbarSectionsOrBusesIds = Objects.requireNonNull(busbarSectionsOrBusesIds);
        this.targetV = targetV;
    }

    public void setControlZone(ControlZoneImpl controlZone) {
        this.controlZone = Objects.requireNonNull(controlZone);
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
        return targetV;
    }

    @Override
    public void setTargetV(double targetV) {
        if (Double.isNaN(targetV)) {
            throw new PowsyblException("Invalid pilot point target voltage for zone '" + controlZone.getName() + "'");
        }
        if (targetV != this.targetV) {
            double oldTargetV = this.targetV;
            this.targetV = targetV;
            SecondaryVoltageControlImpl secondaryVoltageControl = controlZone.getSecondaryVoltageControl();
            NetworkImpl network = (NetworkImpl) secondaryVoltageControl.getExtendable();
            network.getListeners().notifyExtensionUpdate(secondaryVoltageControl, "pilotPointTargetV",
                    new TargetVoltageEvent(controlZone.getName(), oldTargetV), new TargetVoltageEvent(controlZone.getName(), targetV));
        }
    }
}
