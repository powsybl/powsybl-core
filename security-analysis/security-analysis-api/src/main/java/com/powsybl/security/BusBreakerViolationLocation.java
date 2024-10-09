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
public class BusBreakerViolationLocation implements ViolationLocation {
    private final List<String> busIds;

    public BusBreakerViolationLocation(List<String> busIds) {
        this.busIds = Objects.requireNonNull(busIds, "busIds should not be null.");
    }

    public List<String> getBusIds() {
        return busIds;
    }

    @Override
    public Type getType() {
        return Type.BUS_BREAKER;
    }

    @Override
    public String toString() {
        return "BusBreakerViolationLocation{" +
            "busIds='" + busIds + '\'' +
            '}';
    }
}
