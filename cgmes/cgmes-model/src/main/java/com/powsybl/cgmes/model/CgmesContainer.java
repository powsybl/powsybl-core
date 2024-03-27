/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.model;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class CgmesContainer {

    CgmesContainer(String voltageLevel, String substation) {
        this.voltageLevel = voltageLevel;
        this.substation = substation;
    }

    public String substation() {
        return substation;
    }

    public String voltageLevel() {
        return voltageLevel;
    }

    private final String voltageLevel;
    private final String substation;
}
