/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.modification.tripping.BranchTripping;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
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
}
