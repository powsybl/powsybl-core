/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.data.util;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class TerminalData {

    private final String connectableId;
    private final String side;

    public TerminalData(String connectableId, String side) {
        this.connectableId = connectableId;
        this.side = side;
    }

    public String getConnectableId() {
        return connectableId;
    }

    public String getSide() {
        return side;
    }
}
