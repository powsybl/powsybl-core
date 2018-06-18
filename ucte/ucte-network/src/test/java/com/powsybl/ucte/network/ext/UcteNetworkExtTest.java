/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network.ext;

import com.powsybl.ucte.network.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteNetworkExtTest extends UcteNetworkImplTest {

    @Test
    public void test() {
        UcteNetworkExt network = (UcteNetworkExt) UcteNetworkFactory.createNetwork(() -> new UcteNetworkExt(new UcteNetworkImpl(), 0.01f));
        testNetwork(network);
    }

    private void testNetwork(UcteNetworkExt network) {
        super.testNetwork(network);

        UcteNodeCode code1 = new UcteNodeCode(UcteCountryCode.XX, "AAAAA", UcteVoltageLevelCode.VL_380, '1');
        UcteNodeCode code2 = new UcteNodeCode(UcteCountryCode.XX, "BBBBB", UcteVoltageLevelCode.VL_220, '1');

        assertEquals(2, network.getSubstations().size());
        assertEquals(network.getVoltageLevel(code1).getSubstation(), network.getVoltageLevel(code2).getSubstation());
    }
}
