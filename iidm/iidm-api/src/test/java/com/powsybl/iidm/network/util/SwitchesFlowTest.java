/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import org.junit.Test;
import org.mockito.Mockito;

import com.powsybl.iidm.network.VoltageLevel;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class SwitchesFlowTest {

    @Test
    public void test() {
        VoltageLevel voltageLevel = new VoltageLevelTestData().getVoltageLevel();

        SwitchesFlow switchesFlow = new SwitchesFlow(voltageLevel);

    }

    private static final class VoltageLevelTestData {
        private VoltageLevel vl;

        private VoltageLevelTestData() {
            vl = Mockito.mock(VoltageLevel.class);
            // when(bbvBus.getVoltageLevel()).thenReturn(vl);
            // when(network.getBusView()).thenReturn(bv);
            // when(bv.getBus(eq("busId"))).thenReturn(bvBus);

        }

        VoltageLevel getVoltageLevel() {
            return vl;
        }
    }
}
