/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.test.PowsyblCoreTestReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.topology.DefaultNamingStrategy;
import com.powsybl.iidm.modification.topology.RemoveFeederBay;
import com.powsybl.iidm.modification.topology.RemoveFeederBayBuilder;
import com.powsybl.iidm.modification.tripping.BranchTripping;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

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
        modificationList.apply(network);

        assertFalse(network.getLine("NHV1_NHV2_1").getTerminal1().isConnected());
        assertFalse(network.getLine("NHV1_NHV2_1").getTerminal2().isConnected());
    }

    @Test
    void applicationSuccessTest() throws IOException {
        String lineId = "NHV1_NHV2_1";
        assertTrue(network.getLine(lineId).getTerminal1().isConnected());
        assertTrue(network.getLine(lineId).getTerminal2().isConnected());

        // Operation list: Open, close and remove the line
        BranchTripping tripping = new BranchTripping(lineId, "VLHV1");
        RemoveFeederBay removal = new RemoveFeederBayBuilder().withConnectableId(lineId).build();
        NetworkModificationList task = new NetworkModificationList(tripping, tripping, removal);
        boolean dryRunIsOk = assertDoesNotThrow(() -> task.apply(network, new DefaultNamingStrategy(), true));
        assertTrue(dryRunIsOk);
        assertNotNull(network.getLine("NHV1_NHV2_1"));
        assertTrue(network.getLine("NHV1_NHV2_1").getTerminal1().isConnected());
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("test")
                .build();
        assertTrue(task.apply(network, LocalComputationManager.getDefault(), true));
        assertTrue(task.apply(network, LocalComputationManager.getDefault(), reportNode, true));
        assertTrue(task.apply(network, false, reportNode, true));
        assertTrue(task.apply(network, false, LocalComputationManager.getDefault(), reportNode, true));
        assertTrue(task.apply(network, new DefaultNamingStrategy(), LocalComputationManager.getDefault(), true));
        assertTrue(task.apply(network, new DefaultNamingStrategy(), LocalComputationManager.getDefault(), reportNode, true));
        assertTrue(task.apply(network, new DefaultNamingStrategy(), reportNode, true));
        assertTrue(task.apply(network, new DefaultNamingStrategy(), false, reportNode, true));
        StringWriter sw1 = new StringWriter();
        reportNode.getChildren().get(reportNode.getChildren().size() - 1).print(sw1);
        assertEquals("""
            + Dry-run: Checking if network modification NetworkModificationList can be applied on network 'sim1'
               Connectable NHV1_NHV2_1 removed
               Dry-run: Network modifications can successfully be applied on network 'sim1'
            """, TestUtil.normalizeLineSeparator(sw1.toString()));
    }

    @Test
    void applicationFailureTest() throws IOException {
        String lineId = "NHV1_NHV2_1";
        assertTrue(network.getLine(lineId).getTerminal1().isConnected());
        assertTrue(network.getLine(lineId).getTerminal2().isConnected());

        // Operation list: remove and open the line. The second operation could not be performed because of the effect of the first
        RemoveFeederBay removal = new RemoveFeederBayBuilder().withConnectableId(lineId).build();
        BranchTripping tripping = new BranchTripping(lineId, "VLHV1");
        NetworkModificationList task = new NetworkModificationList(removal, tripping);

        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("test")
                .build();
        boolean dryRunIsOk = assertDoesNotThrow(() -> task.apply(network, reportNode, true));
        // The full dry-run returns that a problem was encountered and that the full NetworkModificationList could not be performed.
        // No operation was applied on the network.
        assertFalse(dryRunIsOk);
        assertNotNull(network.getLine("NHV1_NHV2_1"));
        assertTrue(network.getLine("NHV1_NHV2_1").getTerminal1().isConnected());
        StringWriter sw1 = new StringWriter();
        reportNode.print(sw1);
        assertEquals("""
            + test reportNode
               + Dry-run: Checking if network modification NetworkModificationList can be applied on network 'sim1'
                  Connectable NHV1_NHV2_1 removed
                  Dry-run failed for NetworkModificationList. The issue is: Branch 'NHV1_NHV2_1' not found
            """, TestUtil.normalizeLineSeparator(sw1.toString()));

        // If we ignore the dry-run result and try to apply the NetworkModificationList, an exception is thrown and
        // the network is in an "unstable" state.
        assertDoesNotThrow(() -> task.apply(network, new DefaultNamingStrategy(), false, LocalComputationManager.getDefault(), ReportNode.NO_OP, false));
        assertThrows(PowsyblException.class, () -> task.apply(network, true, ReportNode.NO_OP), "Branch '" + lineId + "' not found");
        assertNull(network.getLine("NHV1_NHV2_1"));
    }

    @Test
    void testHasImpact() {
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
