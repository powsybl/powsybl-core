/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import java.util.List;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface PilotPoint {

    record TargetVoltageEvent(String controlZoneName, double value) {
    }

    record ActiveBusOrBusbarSectionEvent(String controlZoneName, String id) {
    }

    /**
     * Get pilot point bus IDs of the bus/breaker view.
     */
    List<String> getBusIds();

    /**
     * Get pilot point busbar section IDs.
     */
    List<String> getBusbarSectionIds();

    /**
     * Get the ID of the bus or busbar section of the pilot point which is currently active, if defined.
     * This value is variant dependent.
     */
    Optional<String> getActiveBusOrBusbarSectionId();

    /**
     * Set the ID of the bus or busbar section of the pilot point which is currently active. It has to be one of the
     * pilot point bus IDs or busbar section IDs, or {@code null} to unset it. This value is variant dependent.
     */
    void setActiveBusOrBusbarSectionId(String activeBusOrBusbarSectionId);

    double getTargetV();

    void setTargetV(double targetV);
}
