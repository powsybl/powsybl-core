/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.modification.tripping.HvdcLineTripping;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class HvdcLineContingencyTest {

    @Test
    void test() {
        HvdcLineContingency contingency = new HvdcLineContingency("id");
        assertEquals("id", contingency.getId());
        assertNull(contingency.getVoltageLevelId());
        assertEquals(ContingencyElementType.HVDC_LINE, contingency.getType());

        assertNotNull(contingency.toModification());
        assertInstanceOf(HvdcLineTripping.class, contingency.toModification());

        contingency = new HvdcLineContingency("id", "voltageLevelId");
        assertEquals("voltageLevelId", contingency.getVoltageLevelId());

        new EqualsTester()
                .addEqualityGroup(new HvdcLineContingency("c1", "vl1"), new HvdcLineContingency("c1", "vl1"))
                .addEqualityGroup(new HvdcLineContingency("c2"), new HvdcLineContingency("c2"))
                .testEquals();

    }

    @Test
    void test2() {
        Network network = HvdcTestNetwork.createLcc();
        ContingencyList contingencyList = ContingencyList.of(Contingency.hvdcLine("L"), Contingency.hvdcLine("L", "UNKNOWN"));
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());

        HvdcLineContingency hvdcCtg = (HvdcLineContingency) contingencies.get(0).getElements().get(0);
        assertEquals("L", hvdcCtg.getId());
        assertNull(hvdcCtg.getVoltageLevelId());
    }
}
