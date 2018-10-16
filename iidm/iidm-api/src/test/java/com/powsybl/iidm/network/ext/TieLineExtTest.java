package com.powsybl.iidm.network.ext;
/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
import com.powsybl.iidm.network.Line;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class TieLineExtTest {

    @Test
    public void test() {
        Line line = Mockito.mock(Line.class);
        TieLineExt.HalfLineImpl hl1 = new TieLineExt.HalfLineImpl();
        hl1.setId("hl1")
                .setB1(1.0)
                .setB2(2.0)
                .setG1(3.0)
                .setG2(4.0)
                .setR(5.0)
                .setX(6.0)
                .setXnodeP(7.0)
                .setXnodeQ(8.0);
        TieLineExt.HalfLineImpl hl2 = new TieLineExt.HalfLineImpl();
        hl2.setId("hl2")
                .setB1(11.0)
                .setB2(12.0)
                .setG1(13.0)
                .setG2(14.0)
                .setR(15.0)
                .setX(16.0)
                .setXnodeP(17.0)
                .setXnodeQ(18.0);

        TieLineExt tieLine = new TieLineExt(line, "ucte", hl1, hl2);
        assertEquals(20.0, tieLine.getR(), 0.0);
        assertEquals(22.0, tieLine.getX(), 0.0);
        assertEquals(16.0, tieLine.getG1(), 0.0);
        assertEquals(12.0, tieLine.getB1(), 0.0);
        assertEquals(18.0, tieLine.getG2(), 0.0);
        assertEquals(14.0, tieLine.getB2(), 0.0);
        assertEquals("ucte", tieLine.getUcteXnodeCode());
        Assert.assertSame(line, tieLine.getExtendable());
    }
}
