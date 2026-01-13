/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.tck.extensions;

import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.extensions.StandbyAutomatonAdder;
import com.powsybl.iidm.network.tck.extensions.AbstractStandbyAutomatonTest;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
class StandbyAutomatonTest extends AbstractStandbyAutomatonTest {

    @Test
    void checkVoltageConfigTest() {
        Network network = SvcTestCaseFactory.create();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertNotNull(svc);

        // Builder
        StandbyAutomatonAdder standbyAutomatonAdder = svc.newExtension(StandbyAutomatonAdder.class)
            .withB0(0.0001f)
            .withStandbyStatus(true);

        // LowVoltageThreshold > HighVoltageThreshold
        standbyAutomatonAdder.withLowVoltageSetpoint(390f)
            .withHighVoltageSetpoint(345f)
            .withLowVoltageThreshold(385f)
            .withHighVoltageThreshold(350f);
        ValidationException e0 = assertThrows(ValidationException.class, standbyAutomatonAdder::add);
        assertEquals("Static var compensator 'SVC2': Inconsistent low (385.0) and high (350.0) voltage thresholds",
            e0.getMessage());

        // lowVoltageSetpoint invalid
        standbyAutomatonAdder.withLowVoltageSetpoint(Double.NaN)
            .withHighVoltageSetpoint(400f)
            .withLowVoltageThreshold(385f)
            .withHighVoltageThreshold(405f);
        e0 = assertThrows(ValidationException.class, standbyAutomatonAdder::add);
        assertEquals("Static var compensator 'SVC2': low voltage setpoint (NaN) is invalid",
            e0.getMessage());

        // highVoltageSetpoint invalid
        standbyAutomatonAdder.withLowVoltageSetpoint(390f)
            .withHighVoltageSetpoint(Double.NaN)
            .withLowVoltageThreshold(385f)
            .withHighVoltageThreshold(405f);
        e0 = assertThrows(ValidationException.class, standbyAutomatonAdder::add);
        assertEquals("Static var compensator 'SVC2': high voltage setpoint (NaN) is invalid",
            e0.getMessage());

        // lowVoltageSetpoint invalid
        standbyAutomatonAdder.withLowVoltageSetpoint(390f)
            .withHighVoltageSetpoint(400f)
            .withLowVoltageThreshold(Double.NaN)
            .withHighVoltageThreshold(405f);
        e0 = assertThrows(ValidationException.class, standbyAutomatonAdder::add);
        assertEquals("Static var compensator 'SVC2': low voltage threshold (NaN) is invalid",
            e0.getMessage());

        // highVoltageSetpoint invalid
        standbyAutomatonAdder.withLowVoltageSetpoint(390f)
            .withHighVoltageSetpoint(400f)
            .withLowVoltageThreshold(385f)
            .withHighVoltageThreshold(Double.NaN);
        e0 = assertThrows(ValidationException.class, standbyAutomatonAdder::add);
        assertEquals("Static var compensator 'SVC2': high voltage threshold (NaN) is invalid",
            e0.getMessage());
    }

    @Test
    void reportNodeTests() {

        ReportNode reportRoot = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("test")
                .build();

        // Prepare the test
        Network network = SvcTestCaseFactory.create();
        network.getReportNodeContext().pushReportNode(reportRoot);
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertNotNull(svc);

        // Builder
        StandbyAutomatonAdder standbyAutomatonAdder = svc.newExtension(StandbyAutomatonAdder.class)
            .withB0(0.0001f)
            .withStandbyStatus(true);

        // lowVoltageSetpoint < lowVoltageThreshold
        standbyAutomatonAdder.withLowVoltageSetpoint(380f)
            .withHighVoltageSetpoint(400f)
            .withLowVoltageThreshold(385f)
            .withHighVoltageThreshold(405f)
            .add();

        StandbyAutomaton standbyAutomaton = svc.getExtension(StandbyAutomaton.class);
        assertNotNull(standbyAutomaton);
        assertEquals(380f, standbyAutomaton.getLowVoltageSetpoint(), 0.0);
        assertEquals(400f, standbyAutomaton.getHighVoltageSetpoint(), 0.0);
        assertEquals(385f, standbyAutomaton.getLowVoltageThreshold(), 0.0);
        assertEquals(405f, standbyAutomaton.getHighVoltageThreshold(), 0.0);

        // highVoltageSetpoint > highVoltageThreshold
        standbyAutomatonAdder.withLowVoltageSetpoint(390f)
            .withHighVoltageSetpoint(410f)
            .withLowVoltageThreshold(385f)
            .withHighVoltageThreshold(405f)
            .add();

        ReportNode reportNode = svc.getNetwork().getReportNodeContext().getReportNode();
        assertEquals(2, reportNode.getChildren().size());
        assertEquals("Static VAR compensator 'SVC2': invalid low voltage setpoint (380.0) < threshold (385.0)",
            reportNode.getChildren().get(0).getMessage());

        assertEquals("Static VAR compensator 'SVC2': invalid high voltage setpoint (410.0) > threshold (405.0)",
            reportNode.getChildren().get(1).getMessage());

    }
}
