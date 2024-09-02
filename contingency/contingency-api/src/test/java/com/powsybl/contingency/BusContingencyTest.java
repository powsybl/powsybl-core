/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.iidm.modification.tripping.BusTripping;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
class BusContingencyTest {

    @Test
    void test() {
        Contingency contingency = Contingency.bus("idBus");
        assertEquals("idBus", contingency.getId());
        assertEquals(1, contingency.getElements().size());
        assertEquals(ContingencyElementType.BUS, contingency.getElements().get(0).getType());

        BusContingency busContingency = new BusContingency("idBus");
        assertEquals("idBus", busContingency.getId());
        assertEquals(ContingencyElementType.BUS, busContingency.getType());

        assertNotNull(busContingency.toModification());
        assertInstanceOf(BusTripping.class, busContingency.toModification());

        new EqualsTester()
                .addEqualityGroup(new BusContingency("bus1"), new BusContingency("bus1"))
                .addEqualityGroup(new BusContingency("bus2"), new BusContingency("bus2"))
                .testEquals();
    }

    @Test
    void test2() {
        Network network = EurostagTutorialExample1Factory.create();
        ContingencyBuilder builder = new ContingencyBuilder("NHV1");
        builder.addIdentifiable(network.getBusBreakerView().getBus("NHV1"));
        ContingencyList contingencyList = ContingencyList.of(Contingency.bus("NGEN"), builder.build());
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());

        BusContingency busCtg = (BusContingency) contingencies.get(0).getElements().get(0);
        assertEquals("NGEN", busCtg.getId());
        BusContingency busCtg2 = (BusContingency) contingencies.get(1).getElements().get(0);
        assertEquals("NHV1", busCtg2.getId());
    }
}
