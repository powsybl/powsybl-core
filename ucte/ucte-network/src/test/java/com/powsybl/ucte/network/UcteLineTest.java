/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.junit.Assert;
import org.junit.Test;

import static com.powsybl.ucte.network.UcteElementStatus.REAL_ELEMENT_IN_OPERATION;
import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteLineTest extends AbstractUcteElementTest {

    private UcteElementId createElementId() {
        UcteNodeCode node1 = new UcteNodeCode(UcteCountryCode.FR, "AAAAA", UcteVoltageLevelCode.VL_380, 'A');
        UcteNodeCode node2 = new UcteNodeCode(UcteCountryCode.BE, "BBBBB", UcteVoltageLevelCode.VL_380, 'B');
        return new UcteElementId(node1, node2, '1');
    }

    @Test
    public void test() {
        UcteElementId id = createElementId();
        UcteLine line = new UcteLine(id, REAL_ELEMENT_IN_OPERATION, 1.0f, 2.0f, 3.0f, 1000, "Line");

        // Test the constructor
        assertEquals(id, line.getId());
        assertEquals(id.toString(), line.toString());
        assertEquals(REAL_ELEMENT_IN_OPERATION, line.getStatus());
        assertEquals(1.0f, line.getResistance(), 0.0f);
        assertEquals(2.0f, line.getReactance(), 0.0f);
        assertEquals(3.0f, line.getSusceptance(), 0.0f);
        assertEquals(Integer.valueOf(1000), line.getCurrentLimit());

        // Test getters and setters
        testElement(line);
    }

    @Test
    public void testFix() {
        UcteElementId id = createElementId();
        UcteLine invalidLine1 = new UcteLine(id, REAL_ELEMENT_IN_OPERATION, 0.0f, 0.0f, 0.0f, -1, null);
        invalidLine1.fix();
        Assert.assertEquals(0.05f, invalidLine1.getReactance(), 0.0f);

        UcteLine invalidLine2 = new UcteLine(id, REAL_ELEMENT_IN_OPERATION, 0.0f, -0.01f, 0.0f, null, null);
        invalidLine2.fix();
        Assert.assertEquals(-0.05f, invalidLine2.getReactance(), 0.0f);
    }
}
