/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public record DCEquipment(String id, String type, String node1, String node2) {

    public boolean isAdjacentTo(DCEquipment otherDcEquipment) {
        // Two DCEquipment are adjacent if they share a node.
        return node1.equals(otherDcEquipment.node1) || node1.equals(otherDcEquipment.node2)
                || node2 != null && (node2.equals(otherDcEquipment.node1) || node2.equals(otherDcEquipment.node2));
    }

    public boolean isConverter() {
        return VS_CONVERTER.equals(type) || CS_CONVERTER.equals(type);
    }

    public boolean isLine() {
        return DC_LINE_SEGMENT.equals(type);
    }

}
