/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.modification.topology.RemoveFeederBay;
import com.powsybl.iidm.modification.topology.RemoveFeederBayBuilder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.modification.tripping.BranchTripping;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class NetworkModificationListTest {

    private Network network;

    @BeforeEach
    void init() {
        network = EurostagTutorialExample1Factory.create();
    }

    @Test
    void test() {
        assertTrue(network.getLine("NHV1_NHV2_1").getTerminal1().isConnected());
        assertTrue(network.getLine("NHV1_NHV2_1").getTerminal2().isConnected());

        BranchTripping tripping1 = new BranchTripping("NHV1_NHV2_1", "VLHV1");
        BranchTripping tripping2 = new BranchTripping("NHV1_NHV2_1", "VLHV2");
        NetworkModificationList modificationList = new NetworkModificationList(tripping1, tripping2);

        boolean dryRunIsOk = Assertions.assertDoesNotThrow(() -> modificationList.fullDryRun(network));
        assertTrue(dryRunIsOk);
        modificationList.apply(network);

        assertFalse(network.getLine("NHV1_NHV2_1").getTerminal1().isConnected());
        assertFalse(network.getLine("NHV1_NHV2_1").getTerminal2().isConnected());
    }

    @Test
    void applicationFailureTest() {
        String lineId = "NHV1_NHV2_1";
        assertTrue(network.getLine(lineId).getTerminal1().isConnected());
        assertTrue(network.getLine(lineId).getTerminal2().isConnected());

        // Operation list:
        //  1. Remove a line;
        //  2. Open it.
        // The second operation could not be performed because of the effect of the first
        RemoveFeederBay removal = new RemoveFeederBayBuilder().withConnectableId(lineId).build();
        BranchTripping tripping = new BranchTripping(lineId, "VLHV1");
        NetworkModificationList task = new NetworkModificationList(removal, tripping);

        boolean dryRunIsOk = Assertions.assertDoesNotThrow(() -> task.fullDryRun(network));
        // The full dry-run returns that a problem was encountered and that the full NetworkModificationList could not be performed.
        // No operation was applied on the network.
        assertFalse(dryRunIsOk);
        assertNotNull(network.getLine("NHV1_NHV2_1"));
        assertTrue(network.getLine("NHV1_NHV2_1").getTerminal1().isConnected());

        // If we ignore the dry-run result and try to apply the NetworkModificationList, an exception is thrown and
        // the network is in an "unstable" state.
        Assertions.assertThrows(PowsyblException.class, () -> task.apply(network), "Branch '" + lineId + "' not found");
        assertNull(network.getLine("NHV1_NHV2_1"));
    }
}
