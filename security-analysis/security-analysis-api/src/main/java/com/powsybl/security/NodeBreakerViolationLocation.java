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

/**
 * @author Étienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class NodeBreakerViolationLocation implements ViolationLocation {
    private final String voltageLevelId;
    private final List<String> busBarIds;

    public NodeBreakerViolationLocation(String voltageLevelId, List<String> busBarIds) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId, "'voltageLevelId' should not be null");
        this.busBarIds = Objects.requireNonNull(busBarIds, "'busBarIds' should not be null");
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public List<String> getBusBarIds() {
        return busBarIds;
    }

    @Override
    public Type getType() {
        return Type.NODE_BREAKER;
    }

    @Override
    public String toString() {
        return "NodeBreakerVoltageLocation{" +
            "voltageLevelId='" + voltageLevelId + '\'' +
            ", busBarIds=" + busBarIds +
            '}';
    }
}
