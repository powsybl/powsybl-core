/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

import com.powsybl.commons.report.ReportNode;
import org.junit.jupiter.api.Test;

import static com.powsybl.ucte.network.UcteElementStatus.REAL_ELEMENT_IN_OPERATION;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class UcteLineTest extends AbstractUcteElementTest {

    private UcteElementId createElementId() {
        UcteNodeCode node1 = new UcteNodeCode(UcteCountryCode.FR, "AAAAA", UcteVoltageLevelCode.VL_380, 'A');
        UcteNodeCode node2 = new UcteNodeCode(UcteCountryCode.BE, "BBBBB", UcteVoltageLevelCode.VL_380, 'B');
        return new UcteElementId(node1, node2, '1');
    }

    @Test
    void test() {
        UcteElementId id = createElementId();
        UcteLine line = new UcteLine(id, REAL_ELEMENT_IN_OPERATION, 1.0, 2.0, 3.0, 1000, "Line");

        // Test the constructor
        assertEquals(id, line.getId());
        assertEquals(id.toString(), line.toString());
        assertEquals(REAL_ELEMENT_IN_OPERATION, line.getStatus());
        assertEquals(1.0, line.getResistance(), 0.0);
        assertEquals(2.0, line.getReactance(), 0.0);
        assertEquals(3.0, line.getSusceptance(), 0.0);
        assertEquals(Integer.valueOf(1000), line.getCurrentLimit());

        // Test getters and setters
        testElement(line);
    }

    @Test
    void testFix() {
        UcteElementId id = createElementId();
        UcteLine invalidLine1 = new UcteLine(id, REAL_ELEMENT_IN_OPERATION, 0.0, 0.0, 0.0, -1, null);
        invalidLine1.fix(ReportNode.NO_OP);
        assertEquals(0.05, invalidLine1.getReactance(), 0.0);

        UcteLine invalidLine2 = new UcteLine(id, REAL_ELEMENT_IN_OPERATION, 0.0, -0.01, 0.0, null, null);
        invalidLine2.fix(ReportNode.NO_OP);
        assertEquals(-0.05, invalidLine2.getReactance(), 0.0);
    }
}
