/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.iidm.network.BoundaryLine;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class XnodeTest {

    @Test
    void test() {
        BoundaryLine dl = Mockito.mock(BoundaryLine.class);
        Xnode xnode = new XnodeImpl(dl, "XXXXXX11");

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
