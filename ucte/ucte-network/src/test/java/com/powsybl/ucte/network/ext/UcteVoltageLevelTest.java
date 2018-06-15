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
public class UcteVoltageLevelTest {

    @Test
    public void test() {
        UcteNodeCode node = new UcteNodeCode(UcteCountryCode.FR, "AAAAA", UcteVoltageLevelCode.VL_380, '1');
        UcteSubstation substation = new UcteSubstation("SUBST", Collections.emptyList());
        UcteVoltageLevel vl = new UcteVoltageLevel("VL", substation, Collections.singletonList(node));

        assertEquals("VL", vl.getName());
        assertEquals(substation, vl.getSubstation());
        assertEquals(1, vl.getNodes().size());
        assertEquals(node, vl.getNodes().iterator().next());
    }
}
