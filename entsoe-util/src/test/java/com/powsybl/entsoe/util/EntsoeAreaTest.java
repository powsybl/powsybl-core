/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.iidm.network.Substation;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EntsoeAreaTest {

    @Test
    public void test() {
        Substation substation = Mockito.mock(Substation.class);
        EntsoeArea area = new EntsoeArea(substation, EntsoeGeographicalCode.D1);

        assertEquals("entsoeArea", area.getName());
        assertSame(substation, area.getExtendable());

        assertEquals(EntsoeGeographicalCode.D1, area.getCode());

        try {
            area.setCode(null);
            fail();
        } catch (NullPointerException ignored) {
        }

        area.setCode(EntsoeGeographicalCode.FR);
        assertEquals(EntsoeGeographicalCode.FR, area.getCode());
    }
}
