/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model;

import java.util.Arrays;

/**
 * @author Jean-Pierre Arnould {@literal <jean-pierre.arnould at rte-france.com>}
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public enum NcKeyword {
    ASSESSED_ELEMENT("AE"),
    CONTINGENCY("CO"),
    EQUIPMENT_RELIABILITY("ER"),
    REMEDIAL_ACTION("RA"),
    STEADY_STATE_INSTRUCTION("SSI");

    private final String keyword;

    NcKeyword(String keyword) {
        this.keyword = keyword;
    }

    public static NcKeyword fromString(String keyword) {
        return Arrays.stream(values())
            .filter(value -> value.keyword.equals(keyword))
            .findFirst()
            .orElseThrow(() -> new NcException("Unknown NC profile keyword: " + keyword));
    }

    @Override
    public String toString() {
        return keyword;
    }
}
