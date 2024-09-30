/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Étienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class BusBreakerViolationLocation implements ViolationLocation {
    private final String voltageLevelId;
    private final String busId;

    public BusBreakerViolationLocation(String voltageLevelId, String busId) {
        Objects.requireNonNull(voltageLevelId, "voltageLevelId");
        this.voltageLevelId = voltageLevelId;
        this.busId = busId;
    }

    @Override
    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    @Override
    public Optional<String> getBusId() {
        return Optional.ofNullable(busId);
    }

    @Override
    public String getId() {
        return busId;
    }

    @Override
    public Type getType() {
        return Type.BUS_BREAKER;
    }

    @Override
    public String toString() {
        return "BusBreakerViolationLocation{" +
            "voltageLevelId='" + voltageLevelId + '\'' +
            ", busId='" + busId + '\'' +
            '}';
    }
}
