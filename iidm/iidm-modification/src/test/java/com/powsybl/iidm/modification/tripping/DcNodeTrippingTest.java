/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
class DcNodeTrippingTest extends AbstractTrippingTest {

    @Test
    void dcNodeTrippingTest() {
        Network network = DcDetailedNetworkFactory.createVscAsymmetricalMonopole();
        network.getDcNode("dcNodeGbPos").getDcTerminals().forEach(t -> assertTrue(t.isConnected()));

        DcNodeTripping tripping = new DcNodeTripping("dcNodeGbPos");
        tripping.apply(network);

        network.getDcNode("dcNodeGbPos").getDcTerminals().forEach(t -> assertFalse(t.isConnected()));
    }

    @Test
    void unknownConverterTrippingTest() {
        Network network = DcDetailedNetworkFactory.createVscAsymmetricalMonopole();

        DcNodeTripping tripping = new DcNodeTripping("dcNode");
        assertThrows(PowsyblException.class, () -> tripping.apply(network, true, ReportNode.NO_OP));
        assertDoesNotThrow(() -> tripping.apply(network));
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new DcNodeTripping("ID");
        assertEquals("DcNodeTripping", networkModification.getName());
    }

    @Test
    void testHasImpact() {
        Network network = DcDetailedNetworkFactory.createVscAsymmetricalMonopole();

        NetworkModification modification1 = new DcNodeTripping("WRONG_ID");
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new DcNodeTripping("dcNodeGbPos");
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));
        modification2.apply(network);

        NetworkModification modification3 = new DcNodeTripping("dcNodeGbPos");
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification3.hasImpactOnNetwork(network));
    }
}
