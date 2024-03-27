/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.utils;

/**
 * @author bendaamerahm <ahmed.bendaamer at rte-france.com>
 */
public enum FileTypeEnum {

    SUBSTATIONS("postes-electriques"), AERIAL_LINES("lignes-aeriennes"), UNDERGROUND_LINES("lignes-souterraines");

    private String value;

    private FileTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
