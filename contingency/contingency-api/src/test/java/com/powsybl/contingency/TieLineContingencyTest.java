/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.iidm.modification.tripping.TieLineTripping;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
class TieLineContingencyTest {

    @Test
    void test() {
        TieLineContingency contingency = new TieLineContingency("id");
        assertEquals("id", contingency.getId());
        assertNull(contingency.getVoltageLevelId());
        assertEquals(ContingencyElementType.TIE_LINE, contingency.getType());

        assertNotNull(contingency.toModification());
        assertInstanceOf(TieLineTripping.class, contingency.toModification());

        contingency = new TieLineContingency("id", "voltageLevelId");
        assertEquals("voltageLevelId", contingency.getVoltageLevelId());

        new EqualsTester()
                .addEqualityGroup(new TieLineContingency("c1", "vl1"), new TieLineContingency("c1", "vl1"))
                .addEqualityGroup(new TieLineContingency("c2"), new TieLineContingency("c2"))
                .testEquals();
    }

    @Test
    void test2() {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        ContingencyBuilder builder = new ContingencyBuilder("NHV1_NHV2_1");
        builder.addIdentifiable(network.getTieLine("NHV1_NHV2_1"));
        ContingencyList contingencyList = ContingencyList.of(builder.build(), Contingency.tieLine("NHV1_NHV2_1", "UNKNOWN"));
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());

        TieLineContingency tieLineCtg = (TieLineContingency) contingencies.get(0).getElements().get(0);
        assertEquals("NHV1_NHV2_1", tieLineCtg.getId());
        assertNull(tieLineCtg.getVoltageLevelId());
    }
}
