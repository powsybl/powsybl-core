/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network.ext;

import com.powsybl.ucte.network.UcteCountryCode;
import com.powsybl.ucte.network.UcteNodeCode;
import com.powsybl.ucte.network.UcteVoltageLevelCode;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteSubstationTest {

    @Test
    public void test() {
        UcteNodeCode node = new UcteNodeCode(UcteCountryCode.FR, "AAAAA", UcteVoltageLevelCode.VL_380, '1');
        UcteSubstation s1 = new UcteSubstation("S1", Collections.emptyList());
        UcteVoltageLevel vl = new UcteVoltageLevel("VL", s1, Collections.singletonList(node));
        UcteSubstation s2 = new UcteSubstation("S2", Collections.singletonList(vl));

        assertEquals("S2", s2.getName());
        assertEquals(1, s2.getVoltageLevels().size());
        assertEquals(vl, s2.getVoltageLevels().iterator().next());
        assertEquals(node, s2.getNodes().iterator().next());
    }
}
