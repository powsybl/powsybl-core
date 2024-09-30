/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Étienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class NodeBreakerVoltageLocation implements ViolationLocation {
    private final String voltageLevelId;
    private final List<String> busBarIds;
    private final String busId;

    public NodeBreakerVoltageLocation(String voltageLevelId, List<String> busBarIds, String busId) {
        Objects.requireNonNull(voltageLevelId, "voltageLevelId");
        this.busId = busId;
        this.voltageLevelId = voltageLevelId;
        this.busBarIds = busBarIds;
    }

    @Override
    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    @Override
    public List<String> getBusBarIds() {
        return busBarIds;
    }

    @Override
    public Optional<String> getBusId() {
        return Optional.ofNullable(busId);
    }

    @Override
    public String getId() {
        return busBarIds.isEmpty() ? voltageLevelId : busBarIds.get(0);
    }

    @Override
    public String toString() {
        return "NodeBreakerVoltageLocation{" +
            "voltageLevelId='" + voltageLevelId + '\'' +
            ", busBarIds=" + busBarIds +
            '}';
    }
}
