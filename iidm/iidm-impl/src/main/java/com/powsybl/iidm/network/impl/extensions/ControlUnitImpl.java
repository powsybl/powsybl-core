/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.extensions.ControlUnit;
import com.powsybl.iidm.network.impl.NetworkImpl;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ControlUnitImpl implements ControlUnit {

    private final String id;

    private boolean participate;

    private ControlZoneImpl controlZone;

    public ControlUnitImpl(String id, boolean participate) {
        this.id = Objects.requireNonNull(id);
        this.participate = participate;
    }

    public void setControlZone(ControlZoneImpl controlZone) {
        this.controlZone = Objects.requireNonNull(controlZone);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isParticipate() {
        return participate;
    }

    @Override
    public void setParticipate(boolean participate) {
        if (participate != this.participate) {
            this.participate = participate;
            SecondaryVoltageControlImpl secondaryVoltageControl = controlZone.getSecondaryVoltageControl();
            NetworkImpl network = (NetworkImpl) secondaryVoltageControl.getExtendable();
            network.getListeners().notifyExtensionUpdate(secondaryVoltageControl, "controlUnitParticipate", null,
                    new ParticipateEvent(controlZone.getName(), id, !this.participate), new ParticipateEvent(controlZone.getName(), id, this.participate));
        }
    }
}
