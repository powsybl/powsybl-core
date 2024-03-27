/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.modification.tripping.BranchTripping;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class BranchContingencyTest {

    @Test
    void test() {
        BranchContingency contingency = new BranchContingency("id");
        assertEquals("id", contingency.getId());
        assertNull(contingency.getVoltageLevelId());
        assertEquals(ContingencyElementType.BRANCH, contingency.getType());

        assertNotNull(contingency.toModification());
        assertTrue(contingency.toModification() instanceof BranchTripping);

        contingency = new BranchContingency("id", "voltageLevelId");
        assertEquals("voltageLevelId", contingency.getVoltageLevelId());

        new EqualsTester()
                .addEqualityGroup(new BranchContingency("c1", "vl1"), new BranchContingency("c1", "vl1"))
                .addEqualityGroup(new BranchContingency("c2"), new BranchContingency("c2"))
                .testEquals();

        Contingency contingency2 = Contingency.line("LINE");
        assertEquals("LINE", contingency2.getId());
        assertEquals(1, contingency2.getElements().size());
        assertEquals("LINE", contingency2.getElements().get(0).getId());
        assertEquals(ContingencyElementType.LINE, contingency2.getElements().get(0).getType());

        new EqualsTester()
                .addEqualityGroup(new LineContingency("c1", "vl1"), new LineContingency("c1", "vl1"))
                .addEqualityGroup(new LineContingency("c2"), new LineContingency("c2"))
                .testEquals();

        Contingency contingency3 = Contingency.twoWindingsTransformer("TWT");
        assertEquals("TWT", contingency3.getId());
        assertEquals(1, contingency3.getElements().size());
        assertEquals("TWT", contingency3.getElements().get(0).getId());
        assertEquals(ContingencyElementType.TWO_WINDINGS_TRANSFORMER, contingency3.getElements().get(0).getType());

        new EqualsTester()
                .addEqualityGroup(new TwoWindingsTransformerContingency("c1", "vl1"), new TwoWindingsTransformerContingency("c1", "vl1"))
                .addEqualityGroup(new TwoWindingsTransformerContingency("c2"), new TwoWindingsTransformerContingency("c2"))
                .testEquals();
    }

    @Test
    void test2() {
        Network network = EurostagTutorialExample1Factory.create();

        ContingencyList contingencyList = ContingencyList.of(
                Contingency.line("NHV1_NHV2_1"),
                Contingency.line("UNKNOWN"),
                Contingency.line("NHV1_NHV2_2", "UNKNOWN"),
                Contingency.twoWindingsTransformer("NGEN_NHV1"),
                Contingency.twoWindingsTransformer("UNKNOWN"),
                Contingency.twoWindingsTransformer("NHV2_NLOAD", "UNKNOWN"),
                Contingency.branch("NHV1_NHV2_1"),
                Contingency.branch("UNKNOWN"),
                Contingency.branch("NHV1_NHV2_2", "UNKNOWN"),
                Contingency.branch("NGEN_NHV1"),
                Contingency.branch("NHV2_NLOAD", "UNKNOWN")
        );

        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(4, contingencies.size());

        LineContingency lineCtg = (LineContingency) contingencies.get(0).getElements().get(0);
        assertEquals("NHV1_NHV2_1", lineCtg.getId());
        assertNull(lineCtg.getVoltageLevelId());

        TwoWindingsTransformerContingency twtCtg = (TwoWindingsTransformerContingency) contingencies.get(1).getElements().get(0);
        assertEquals("NGEN_NHV1", twtCtg.getId());
        assertNull(twtCtg.getVoltageLevelId());

        BranchContingency branchCtg = (BranchContingency) contingencies.get(2).getElements().get(0);
        assertEquals("NHV1_NHV2_1", branchCtg.getId());
        assertNull(branchCtg.getVoltageLevelId());

        branchCtg = (BranchContingency) contingencies.get(3).getElements().get(0);
        assertEquals("NGEN_NHV1", branchCtg.getId());
        assertNull(branchCtg.getVoltageLevelId());
    }
}
