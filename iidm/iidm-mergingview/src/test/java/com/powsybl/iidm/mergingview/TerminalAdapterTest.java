/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TerminalAdapterTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("TerminalAdapterTest", "iidm");
        mergingView.merge(EurostagTutorialExample1Factory.create());
    }

    @Test
    public void testSetterGetter() {
        final Terminal t1 = mergingView.getLoad("LOAD").getTerminal();
        assertTrue(t1 instanceof TerminalAdapter);
        assertTrue(t1.getVoltageLevel() instanceof VoltageLevelAdapter);

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

        final List<String> traversed = new ArrayList<>();
        t1.traverse(new VoltageLevel.TopologyTraverser() {
            @Override
            public boolean traverse(Terminal terminal, boolean connected) {
                traversed.add(terminal.getConnectable().getId());
                return true;
            }

            @Override
            public boolean traverse(Switch aSwitch) {
                return true;
            }
        });
        assertEquals(Arrays.asList("LOAD", "NHV2_NLOAD", "NHV2_NLOAD", "NHV1_NHV2_1", "NHV1_NHV2_2", "NHV2_NLOAD", "LOAD", "NHV1_NHV2_1", "NHV1_NHV2_2", "NGEN_NHV1", "NHV1_NHV2_1", "NHV1_NHV2_2", "NHV2_NLOAD", "NHV1_NHV2_2", "NHV1_NHV2_1", "NGEN_NHV1", "NHV1_NHV2_2", "NHV1_NHV2_1", "NHV2_NLOAD", "NGEN_NHV1", "GEN", "NGEN_NHV1", "NHV1_NHV2_1", "NHV1_NHV2_2"), traversed);

        // BusBreakerView
        final Terminal.BusBreakerView busBreakerView = t1.getBusBreakerView();
        assertTrue(busBreakerView instanceof TerminalBusBreakerViewAdapter);
        busBreakerView.setConnectableBus("NLOAD");
        assertNotNull(busBreakerView.getBus());
        assertNotNull(busBreakerView.getConnectableBus());

        // BusView
        final Terminal.BusView busView = t1.getBusView();
        assertTrue(busView instanceof TerminalBusViewAdapter);
        assertNotNull(busView.getBus());
        assertNotNull(busView.getConnectableBus());

        // NodeBreakerView
        final Terminal.NodeBreakerView nodeBreakerView = t1.getNodeBreakerView();
        assertNotNull(nodeBreakerView);
        assertTrue(nodeBreakerView instanceof TerminalNodeBreakerViewAdapter);

        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Not supported in a bus breaker topology");
        nodeBreakerView.getNode();
    }
}
