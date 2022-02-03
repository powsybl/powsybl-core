/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.network.modification.tripping.LoadTripping;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Hadrien Godard <hadrien.godard at artelys.com>
 */
public class LoadContingencyTest {

    @Test
    public void test() {
        Contingency contingency = Contingency.load("id");
        assertEquals("id", contingency.getId());
        assertEquals(1, contingency.getElements().size());
        assertEquals(ContingencyElementType.LOAD, contingency.getElements().get(0).getType());

        LoadContingency loadContingency = new LoadContingency("id");
        assertEquals("id", loadContingency.getId());
        assertEquals(ContingencyElementType.LOAD, loadContingency.getType());

        assertNotNull(loadContingency.toModification());
        assertTrue(loadContingency.toModification() instanceof LoadTripping);

        new EqualsTester()
                .addEqualityGroup(new LoadContingency("g1"), new LoadContingency("g1"))
                .addEqualityGroup(new LoadContingency("g2"), new LoadContingency("g2"))
                .testEquals();
    }

    @Test
    public void test2() {
        Network network = EurostagTutorialExample1Factory.create();
        ContingencyList contingencyList = ContingencyList.of(Contingency.load("LOAD"), Contingency.load("unknown"));
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());

        LoadContingency loadCtg = (LoadContingency) contingencies.get(0).getElements().get(0);
        assertEquals("LOAD", loadCtg.getId());
    }
}
