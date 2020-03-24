/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyVisitor;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusbarSectionAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("BusbarSectionAdapterTest", "iidm");
    }

    @Test
    public void testSetterGetter() {
        final Network networkRef = NetworkTest1Factory.create();
        mergingView.merge(networkRef);

        final BusbarSection expectedSJB = networkRef.getBusbarSection("voltageLevel1BusbarSection1");
        final BusbarSection actualSJB   = mergingView.getBusbarSection("voltageLevel1BusbarSection1");
        assertNotNull(actualSJB);
        assertTrue(actualSJB instanceof BusbarSectionAdapter);
        assertSame(mergingView, actualSJB.getNetwork());

        assertEquals(expectedSJB.getType(), actualSJB.getType());
        assertTrue(actualSJB.getTerminal() instanceof TerminalAdapter);
        actualSJB.getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });

        assertEquals(expectedSJB.getV(), actualSJB.getV(), 0.0d);
        assertEquals(expectedSJB.getAngle(), actualSJB.getAngle(), 0.0d);

        // Topology
        TopologyVisitor visitor = mock(TopologyVisitor.class);
        mergingView.getVoltageLevel("voltageLevel1").visitEquipments(visitor);
        verify(visitor, times(2)).visitBusbarSection(any(BusbarSection.class));

        // Not implemented yet !
        TestUtil.notImplemented(actualSJB::remove);
    }
}
