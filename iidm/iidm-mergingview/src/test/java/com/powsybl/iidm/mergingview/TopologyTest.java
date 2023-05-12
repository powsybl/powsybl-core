/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
class TopologyTest {

    @Test
    void test() {
        MergingView cgm = MergingViewFactory.createCGM(null);
        Network n1 = cgm.getNetwork("n1");
        Network n2 = cgm.getNetwork("n2");
        assertNotNull(n1);
        assertNotNull(n2);

        TieLine mergedLine = cgm.getTieLine("DL1 + DL2");
        assertNotNull(mergedLine);
        assertNotNull(mergedLine.getBoundaryLine1().getTerminal().getBusView().getBus());
        // FIXME(mathbagu) assertNotNull(mergedLine.getTerminal2().getBusView().getBus());

        final String idVL1 = "VL1";
        VoltageLevel.BusBreakerView vl1 = n1.getVoltageLevel(idVL1).getBusBreakerView();
        VoltageLevelAdapter.BusBreakerViewExt vl1Ext = ((VoltageLevelAdapter) cgm.getVoltageLevel(idVL1)).getBusBreakerView();

        assertEquals(1, vl1.getBusStream().count());
        assertEquals(1, vl1Ext.getBusStream().count());
        assertEquals(1, n1.getVoltageLevel(idVL1).getBusView().getBusStream().count());
        assertEquals(1, cgm.getVoltageLevel(idVL1).getBusView().getBusStream().count());

        n1.getSwitch("SW1_1").setRetained(true);
        n1.getSwitch("SW1_2").setRetained(true);
        assertEquals(3, vl1.getBusStream().count());
        assertEquals(3, vl1Ext.getBusStream().count());

        n1.getLoad("LOAD1").getTerminal().disconnect();
        assertEquals(3, vl1.getBusStream().count());
        assertEquals(3, vl1Ext.getBusStream().count());
        // FIXME(mathbagu): should invalidate the cache?
        n1.getLoad("LOAD1").remove();
        assertEquals(3, vl1.getBusStream().count());
        assertEquals(3, vl1Ext.getBusStream().count());

        final String idVL2 = "VL2";
        VoltageLevel.BusBreakerView vl2 = n2.getVoltageLevel(idVL2).getBusBreakerView();
        VoltageLevelAdapter.BusBreakerViewExt vl2Ext = ((VoltageLevelAdapter) cgm.getVoltageLevel(idVL2)).getBusBreakerView();

        n2.getLoad("LOAD2").getTerminal().disconnect();
        assertEquals(3, vl2.getBusStream().count());
        assertEquals(3, vl2Ext.getBusStream().count());
        n2.getLoad("LOAD2").getTerminal().connect();
        assertEquals(3, vl2.getBusStream().count());
        assertEquals(3, vl2Ext.getBusStream().count());
        n2.getLoad("LOAD2").remove();
        assertEquals(3, vl2.getBusStream().count());
        assertEquals(3, vl2Ext.getBusStream().count());
    }

}
