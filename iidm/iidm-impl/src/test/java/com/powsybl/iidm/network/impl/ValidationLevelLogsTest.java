/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl;

import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

class ValidationLevelLogsTest {

    @Test
    void equipmentAndSteadyStateTest() throws IOException {
        // create a network associated with a reportNode
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(
                        PowsyblTestReportResourceBundle.TEST_BASE_NAME,
                        PowsyblCoreReportResourceBundle.BASE_NAME,
                        "i18n.iidm-test-reports")
                .withMessageTemplate("testValidationLevelLogs")
                .withUntypedValue("name", "test")
                .build();

        Network network = NetworkFactory.findDefault().createNetwork("oneLoad", "test");
        ReportNodeContext reportNodeContext = network.getReportNodeContext();
        reportNodeContext.pushReportNode(reportNode);

        // Define the network model
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        createNetworkWithEquipmentValidationLevel(network);

        // We check that the SILENT option for action on error works
        // While creating only network equipment if we have established validation level to EQUIPMENT,
        // We should not see anything reported
        assertTrue(checkReportNode("""
                Test validationLevel Logs
                """, network.getReportNodeContext().getReportNode()));

        network.runValidationChecks(false, network.getReportNodeContext().getReportNode());

        assertTrue(checkReportNode("""
                + Test validationLevel Logs
                   + Running validation checks on IIDM network oneLoad
                      p0 is invalid
                      p0 is invalid
                """, network.getReportNodeContext().getReportNode()));

        ValidationException e = assertThrows(ValidationException.class, () -> network.setMinimumAcceptableValidationLevel(ValidationLevel.STEADY_STATE_HYPOTHESIS));
        assertTrue(e.getMessage().contains("Network 'oneLoad': Network should be corrected in order to correspond to validation level STEADY_STATE_HYPOTHESIS"));

        // Define the steady-state attributes
        network.getLoads().iterator().next().setP0(10.0).setQ0(-5.0);
        network.runValidationChecks(false, network.getReportNodeContext().getReportNode());

        assertTrue(checkReportNode("""
                + Test validationLevel Logs
                   + Running validation checks on IIDM network oneLoad
                      p0 is invalid
                      p0 is invalid
                   Running validation checks on IIDM network oneLoad
                """, network.getReportNodeContext().getReportNode()));

        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
    }

    private static boolean checkReportNode(String expected, ReportNode reportNode) throws IOException {
        StringWriter sw = new StringWriter();
        reportNode.print(sw);
        assertEquals(expected, TestUtil.normalizeLineSeparator(sw.toString()));
        return true;
    }

    public static void createNetworkWithEquipmentValidationLevel(Network network) {
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .setTso("RTE")
                .setGeographicalTags("A")
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(24.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus loadBus = vl1.getBusBreakerView().newBus()
                .setId("LoadBus")
                .add();
        vl1.newLoad()
                .setId("LOAD")
                .setBus(loadBus.getId())
                .setConnectableBus(loadBus.getId())
                .add();
    }
}
