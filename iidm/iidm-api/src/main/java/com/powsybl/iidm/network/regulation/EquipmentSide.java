/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.regulation;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public enum EquipmentSide {
    ONE("1"),
    TWO("2"),
    THREE("3");

    private final String index;

    EquipmentSide(String index) {
        this.index = index;
    }

    public String index() {
        return index;
    }
}
