/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.iidm.modification.tripping.VoltageSourceConverterTripping;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
class VoltageSourceConverterContingencyTest {

    @Test
    void test() {
        Contingency contingency = Contingency.voltageSourceConverter("id");
        assertEquals("id", contingency.getId());
        assertEquals(1, contingency.getElements().size());
        assertEquals(ContingencyElementType.VOLTAGE_SOURCE_CONVERTER, contingency.getElements().getFirst().getType());

        VoltageSourceConverterContingency convContingency = new VoltageSourceConverterContingency("id");
        assertEquals("id", convContingency.getId());
        assertEquals(ContingencyElementType.VOLTAGE_SOURCE_CONVERTER, convContingency.getType());

        assertNotNull(convContingency.toModification());
        assertInstanceOf(VoltageSourceConverterTripping.class, convContingency.toModification());

        new EqualsTester()
                .addEqualityGroup(new VoltageSourceConverterContingency("conv1"), new VoltageSourceConverterContingency("conv1"))
                .addEqualityGroup(new VoltageSourceConverterContingency("conv2"), new VoltageSourceConverterContingency("conv2"))
                .testEquals();
    }

    @Test
    void test2() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        ContingencyList contingencyList = ContingencyList.of(Contingency.voltageSourceConverter("VscFr"), Contingency.voltageSourceConverter("unknown"));
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());

        VoltageSourceConverterContingency convCtg = (VoltageSourceConverterContingency) contingencies.getFirst().getElements().getFirst();
        assertEquals("VscFr", convCtg.getId());
    }
}
