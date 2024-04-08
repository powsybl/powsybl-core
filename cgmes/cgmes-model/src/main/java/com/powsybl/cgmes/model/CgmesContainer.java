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

    CgmesContainer(String voltageLevel, String substation, String line, String name) {
        this.voltageLevel = voltageLevel;
        this.substation = substation;
        this.line = line;
        this.name = name;
    }

    public String substation() {
        return substation;
    }

    public String voltageLevel() {
        return voltageLevel;
    }

    public String name() {
        return name;
    }

    public String id() {
        if (line != null) {
            return line;
        } else if (voltageLevel != null) {
            return voltageLevel;
        } else if (substation != null) {
            return substation;
        } else {
            return null;
        }
    }

    public boolean isVoltageLevel() {
        return voltageLevel != null;
    }

    public boolean isSubstation() {
        return substation != null;
    }

    private final String voltageLevel;
    private final String substation;
    private final String line;
    private final String name;
}
