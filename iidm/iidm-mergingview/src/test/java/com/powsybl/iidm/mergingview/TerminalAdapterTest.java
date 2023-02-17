/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class TerminalAdapterTest {

    private MergingView mergingView;

    @BeforeEach
    void setup() {
        mergingView = MergingView.create("TerminalAdapterTest", "iidm");
        mergingView.merge(EurostagTutorialExample1Factory.create());
    }

    @Test
    void testSetterGetter() {
        final Terminal t1 = mergingView.getLoad("LOAD").getTerminal();
        assertTrue(t1 instanceof TerminalAdapter);
        assertTrue(t1.getVoltageLevel() instanceof BusBreakerVoltageLevelAdapter);

        assertNotNull(t1.getConnectable());
        final double p = 4.0;
        assertNotNull(t1.setP(p));
        assertEquals(p, t1.getP(), 0.0);
        final double q = 5.0;
        assertNotNull(t1.setQ(q));
        assertEquals(q, t1.getQ(), 0.0);
        assertEquals(Double.NaN, t1.getI(), 0.0);
        assertTrue(t1.disconnect());
        assertTrue(t1.connect());
        assertTrue(t1.isConnected());

        // BusBreakerView
        final Terminal.BusBreakerView busBreakerView = t1.getBusBreakerView();
        assertTrue(busBreakerView instanceof TerminalAdapter.BusBreakerViewAdapter);
        busBreakerView.setConnectableBus("NLOAD");
        assertNotNull(busBreakerView.getBus());
        assertNotNull(busBreakerView.getConnectableBus());

        // BusView
        final Terminal.BusView busView = t1.getBusView();
        assertTrue(busView instanceof TerminalAdapter.BusViewAdapter);
        assertNotNull(busView.getBus());
        assertNotNull(busView.getConnectableBus());

        // NodeBreakerView
        final Terminal.NodeBreakerView nodeBreakerView = t1.getNodeBreakerView();
        assertNotNull(nodeBreakerView);
        assertTrue(nodeBreakerView instanceof TerminalAdapter.NodeBreakerViewAdapter);

        PowsyblException e = assertThrows(PowsyblException.class, nodeBreakerView::getNode);
        assertTrue(e.getMessage().contains("Not supported in a bus breaker topology"));

        assertThrows(NullPointerException.class, () -> t1.traverse(null));
    }
}
