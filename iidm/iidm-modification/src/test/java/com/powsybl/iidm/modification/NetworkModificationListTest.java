/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.modification.tripping.BranchTripping;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class NetworkModificationListTest {

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(network.getLine("NHV1_NHV2_1").getTerminal1().isConnected());
        assertTrue(network.getLine("NHV1_NHV2_1").getTerminal2().isConnected());

        BranchTripping tripping1 = new BranchTripping("NHV1_NHV2_1", "VLHV1");
        BranchTripping tripping2 = new BranchTripping("NHV1_NHV2_1", "VLHV2");
        NetworkModificationList modificationList = new NetworkModificationList(tripping1, tripping2);
        modificationList.apply(network);

        assertFalse(network.getLine("NHV1_NHV2_1").getTerminal1().isConnected());
        assertFalse(network.getLine("NHV1_NHV2_1").getTerminal2().isConnected());
    }

    @Test
    void testHasImpact() {
        Network network = EurostagTutorialExample1Factory.create();

        BranchTripping tripping1 = new BranchTripping("NHV1_NHV2_1", "VLHV1");
        BranchTripping tripping2 = new BranchTripping("NHV1_NHV2_1", "VLHV2");
        LoadModification modification1 = new LoadModification("LOAD_NOT_EXISTING", true, -20.0, null);
        NetworkModificationList modificationList = new NetworkModificationList(tripping1, tripping2, modification1);
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modificationList.hasImpactOnNetwork(network));

        LoadModification modification2 = new LoadModification("LOAD", true, null, 2.0);
        LoadModification modification3 = new LoadModification("LOAD", true, 5.0, null);
        NetworkModificationList modificationList2 = new NetworkModificationList(modification2, modification3);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modificationList2.hasImpactOnNetwork(network));

        LoadModification modification4 = new LoadModification("LOAD", true, null, null);
        LoadModification modification5 = new LoadModification("LOAD", false, 600.0, 200.0);
        NetworkModificationList modificationList3 = new NetworkModificationList(modification5, modification4);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modificationList3.hasImpactOnNetwork(network));
    }
}
