/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.iidm.modification.tripping.BusTripping;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
public class BusContingencyTest {

    @Test
    public void test() {
        Contingency contingency = Contingency.bus("idBus");
        assertEquals("idBus", contingency.getId());
        assertEquals(1, contingency.getElements().size());
        assertEquals(ContingencyElementType.BUS, contingency.getElements().get(0).getType());

        BusContingency busContingency = new BusContingency("idBus");
        assertEquals("idBus", busContingency.getId());
        assertEquals(ContingencyElementType.BUS, busContingency.getType());

        assertNotNull(busContingency.toModification());
        assertTrue(busContingency.toModification() instanceof BusTripping);

        new EqualsTester()
                .addEqualityGroup(new BusContingency("bus1"), new BusContingency("bus1"))
                .addEqualityGroup(new BusContingency("bus2"), new BusContingency("bus2"))
                .testEquals();
    }

    @Test
    public void test2() {
        Network network = EurostagTutorialExample1Factory.create();
        ContingencyList contingencyList = ContingencyList.of(Contingency.bus("NGEN"));
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());

        BusContingency busCtg = (BusContingency) contingencies.get(0).getElements().get(0);
        assertEquals("NGEN", busCtg.getId());
    }
}
