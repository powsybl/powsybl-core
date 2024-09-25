/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import java.util.Objects;

/**
 * @author Étienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class VoltageLevelViolationLocation implements ViolationLocation {

    private final String voltageLevelId;

    public VoltageLevelViolationLocation(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId, "voltageLevelId");
        this.voltageLevelId = voltageLevelId;
    }

    @Override
    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    @Override
    public String getId() {
        return voltageLevelId;
    }

    @Override
    public String toString() {
        return "VoltageLevelViolationLocation{" +
            "subjectId='" + voltageLevelId + '\'' +
            '}';
    }
}
