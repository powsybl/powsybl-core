/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.iidm.network.DanglingLine;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XnodeTest {

    @Test
    public void test() {
        DanglingLine dl = Mockito.mock(DanglingLine.class);
        Xnode xnode = new Xnode(dl, "XXXXXX11");

        assertEquals("xnode", xnode.getName());
        assertSame(dl, xnode.getExtendable());

        assertEquals("XXXXXX11", xnode.getCode());

        try {
            xnode.setCode(null);
            fail();
        } catch (NullPointerException ignored) {
        }

        xnode.setCode("XXXXXX21");
        assertEquals("XXXXXX21", xnode.getCode());
    }
}
