/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface PilotPoint {

    record TargetVoltageEvent(String controlZoneName, double value) {
    }

    /**
     * A bus of the bus/breaker view referenced by a pilot point, identified by its voltage level ID and its bus ID.
     */
    record BusRef(String voltageLevelId, String busId) {

        public BusRef {
            Objects.requireNonNull(voltageLevelId, "Pilot point bus voltage level ID is null");
            Objects.requireNonNull(busId, "Pilot point bus ID is null");
        }
    }

    /**
     * Get pilot point buses of the bus/breaker view.
     */
    List<BusRef> getBuses();

    /**
     * Get pilot point busbar section IDs.
     */
    List<String> getBusbarSectionIds();

    double getTargetV();

    void setTargetV(double targetV);
}
