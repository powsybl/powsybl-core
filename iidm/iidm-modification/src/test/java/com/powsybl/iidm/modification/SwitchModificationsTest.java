/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class SwitchModificationsTest {

    private final Network network = NetworkTest1Factory.create();
    private final String existingSwitchId = "generator1Breaker1";
    private final String notExistingSwitchId = "dummy";

    @Test
    void test() {

        Switch sw = network.getSwitch(existingSwitchId);
        Generator generator = network.getGenerator("generator1");

        assertFalse(sw.isOpen());
        assertTrue(generator.getTerminal().isConnected());

        new OpenSwitch(existingSwitchId).apply(network);
        assertTrue(sw.isOpen());
        assertFalse(generator.getTerminal().isConnected());

        new CloseSwitch(existingSwitchId).apply(network);
        assertFalse(sw.isOpen());
        assertTrue(generator.getTerminal().isConnected());
    }

    @Test
    void testInvalidOpenSwitch() {
        OpenSwitch openSwitch = new OpenSwitch(notExistingSwitchId);
        PowsyblException exception = assertThrows(PowsyblException.class, () -> openSwitch.apply(network, true, ReportNode.NO_OP));
        assertEquals("Switch 'dummy' not found", exception.getMessage());

        // With no exception thrown
        ReportNode reportNode = ReportNode.newRootReportNode()
            .withMessageTemplate("test", "test")
            .build();
        openSwitch.apply(network, false, reportNode);
        assertEquals("Switch 'dummy' not found",
            reportNode.getChildren().get(0).getMessage());
    }

    @Test
    void testInvalidCloseSwitch() {
        CloseSwitch closeSwitch = new CloseSwitch(notExistingSwitchId);
        PowsyblException exception = assertThrows(PowsyblException.class, () -> closeSwitch.apply(network, true, ReportNode.NO_OP));
        assertEquals("Switch 'dummy' not found", exception.getMessage());

        // With no exception thrown
        ReportNode reportNode = ReportNode.newRootReportNode()
            .withMessageTemplate("test", "test")
            .build();
        closeSwitch.apply(network, false, reportNode);
        assertEquals("Switch 'dummy' not found",
            reportNode.getChildren().get(0).getMessage());
    }

    @Test
    void testDryRun() {
        // CloseSwitch - Passing dryRun
        CloseSwitch closeSwitchPassing = new CloseSwitch(existingSwitchId);
        assertTrue(closeSwitchPassing.dryRun(network));

        // Useful methods for dry run
        assertFalse(closeSwitchPassing.hasImpactOnNetwork());
        assertTrue(closeSwitchPassing.isLocalDryRunPossible());

        // CloseSwitch - Failing dryRun
        ReportNode reportNodeCloseSwitch = ReportNode.newRootReportNode()
            .withMessageTemplate("", "")
            .build();
        CloseSwitch closeSwitchFailing = new CloseSwitch(notExistingSwitchId);
        assertFalse(closeSwitchFailing.dryRun(network, reportNodeCloseSwitch));
        assertEquals("Dry-run failed for CloseSwitch. The issue is: Switch 'dummy' not found",
            reportNodeCloseSwitch.getChildren().get(0).getChildren().get(0).getMessage());

        // OpenSwitch - Passing dryRun
        OpenSwitch openSwitchPassing = new OpenSwitch(existingSwitchId);
        assertTrue(openSwitchPassing.dryRun(network));

        // OpenSwitch - Failing dryRun
        ReportNode reportNodeOpenSwitch = ReportNode.newRootReportNode()
            .withMessageTemplate("", "")
            .build();
        OpenSwitch openSwitchFailing = new OpenSwitch(notExistingSwitchId);
        assertFalse(openSwitchFailing.dryRun(network, reportNodeOpenSwitch));
        assertEquals("Dry-run failed for OpenSwitch. The issue is: Switch 'dummy' not found",
            reportNodeOpenSwitch.getChildren().get(0).getChildren().get(0).getMessage());
    }
}
