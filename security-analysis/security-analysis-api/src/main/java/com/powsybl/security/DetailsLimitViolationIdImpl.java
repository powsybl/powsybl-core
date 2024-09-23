/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import java.util.Collections;
import java.util.List;

/**
 * @author Étienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class DetailsLimitViolationIdImpl implements LimitViolationId {
    private final String voltageLevelId;
    private final String busId;
    private final List<String> busBarIds;

    public DetailsLimitViolationIdImpl(String voltageLevelId, String busId) {
        this(voltageLevelId, busId, Collections.emptyList());
    }

    public DetailsLimitViolationIdImpl(String voltageLevelId, String busId, List<String> busBarIds) {
        this.voltageLevelId = voltageLevelId;
        this.busId = busId;
        this.busBarIds = busBarIds;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public String getBusId() {
        return busId;
    }

    public List<String> getBusBarIds() {
        return busBarIds;
    }

    @Override
    public String getId() {
        return busId;
    }

    @Override
    public String toString() {
        return "DetailsLimitViolationIdImpl{" +
            "voltageLevelId='" + voltageLevelId + '\'' +
            ", busId='" + busId + '\'' +
            ", busBarIds=" + busBarIds +
            '}';
    }
}
