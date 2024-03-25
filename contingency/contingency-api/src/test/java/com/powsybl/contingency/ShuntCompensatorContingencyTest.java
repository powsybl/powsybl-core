/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.modification.tripping.ShuntCompensatorTripping;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
class ShuntCompensatorContingencyTest {

    @Test
    void test() {
        Contingency contingency = Contingency.shuntCompensator("id");
        assertEquals("id", contingency.getId());
        assertEquals(1, contingency.getElements().size());

        ShuntCompensatorContingency scContingency = new ShuntCompensatorContingency("id");
        assertEquals("id", scContingency.getId());
        assertEquals(ContingencyElementType.SHUNT_COMPENSATOR, scContingency.getType());

        assertNotNull(scContingency.toModification());
        assertTrue(scContingency.toModification() instanceof ShuntCompensatorTripping);

        new EqualsTester()
                .addEqualityGroup(new ShuntCompensatorContingency("sc1"), new ShuntCompensatorContingency("sc1"))
                .addEqualityGroup(new ShuntCompensatorContingency("sc2"), new ShuntCompensatorContingency("sc2"))
                .testEquals();
    }

    @Test
    void test2() {
        Network network = HvdcTestNetwork.createLcc();
        ContingencyList contingencyList = ContingencyList.of(Contingency.shuntCompensator("C1_Filter2"), Contingency.shuntCompensator("unknown"));
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());

        ShuntCompensatorContingency scCtg = (ShuntCompensatorContingency) contingencies.get(0).getElements().get(0);
        assertEquals("C1_Filter2", scCtg.getId());
    }
}
