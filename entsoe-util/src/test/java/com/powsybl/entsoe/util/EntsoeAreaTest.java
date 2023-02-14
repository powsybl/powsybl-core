/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class EntsoeAreaTest {

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();
        Substation substation = network.getSubstation("P1");
        substation.newExtension(EntsoeAreaAdder.class).withCode(EntsoeGeographicalCode.D1).add();
        EntsoeArea area = substation.getExtension(EntsoeArea.class);

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
