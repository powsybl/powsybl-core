/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.iidm.network.Line;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MergedXnodeTest {

    @Test
    public void test() {
        Line line = Mockito.mock(Line.class);
        MergedXnode xnode = new MergedXnode(line, 0.5f, 0.5f, 1.0, 2.0, 3.0, 4.0, "XXXXXX11");

        assertEquals("mergedXnode", xnode.getName());
        assertSame(line, xnode.getExtendable());

        assertEquals(0.5f, xnode.getRdp(), 0f);
        assertEquals(0.5f, xnode.getXdp(), 0f);
        assertEquals(1.0, xnode.getXnodeP1(), 0.0);
        assertEquals(2.0, xnode.getXnodeQ1(), 0.0);
        assertEquals(3.0, xnode.getXnodeP2(), 0.0);
        assertEquals(4.0, xnode.getXnodeQ2(), 0.0);
        assertEquals("XXXXXX11", xnode.getCode());

        try {
            xnode.setCode(null);
            fail();
        } catch (NullPointerException ignored) {
        }
        try {
            xnode.setRdp(2f);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            xnode.setXdp(-3f);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        xnode.setRdp(0.6f);
        xnode.setXdp(0.6f);
        xnode.setXnodeP1(10.0);
        xnode.setXnodeQ1(11.0);
        xnode.setXnodeP2(12.0);
        xnode.setXnodeQ2(13.0);
        xnode.setCode("XXXXXX21");

        assertEquals(0.6f, xnode.getRdp(), 0f);
        assertEquals(0.6f, xnode.getXdp(), 0f);
        assertEquals(10.0, xnode.getXnodeP1(), 0.0);
        assertEquals(11.0, xnode.getXnodeQ1(), 0.0);
        assertEquals(12.0, xnode.getXnodeP2(), 0.0);
        assertEquals(13.0, xnode.getXnodeQ2(), 0.0);
        assertEquals("XXXXXX21", xnode.getCode());
    }
}
