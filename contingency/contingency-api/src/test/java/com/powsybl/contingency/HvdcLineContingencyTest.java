/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.tasks.HvdcLineTripping;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HvdcLineContingencyTest {

    @Test
    public void test() {
        HvdcLineContingency contingency = new HvdcLineContingency("id");
        assertEquals("id", contingency.getId());
        assertNull(contingency.getVoltageLevelId());
        assertEquals(ContingencyElementType.HVDC_LINE, contingency.getType());

        assertNotNull(contingency.toTask());
        assertTrue(contingency.toTask() instanceof HvdcLineTripping);

        contingency = new HvdcLineContingency("id", "voltageLevelId");
        assertEquals("voltageLevelId", contingency.getVoltageLevelId());

        new EqualsTester()
                .addEqualityGroup(new HvdcLineContingency("c1", "vl1"), new HvdcLineContingency("c1", "vl1"))
                .addEqualityGroup(new HvdcLineContingency("c2"), new HvdcLineContingency("c2"))
                .testEquals();

    }

    @Test
    public void test2() {
        Network network = HvdcTestNetwork.createLcc();
        ContingencyList contingencyList = ContingencyList.of(Contingency.hvdcLine("L"), Contingency.hvdcLine("L", "UNKNOWN"));
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());

        HvdcLineContingency hvdcCtg = (HvdcLineContingency) contingencies.get(0).getElements().get(0);
        assertEquals("L", hvdcCtg.getId());
        assertNull(hvdcCtg.getVoltageLevelId());
    }
}
