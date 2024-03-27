/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.tck.extensions;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomatonAdder;
import com.powsybl.iidm.network.impl.extensions.StandbyAutomatonImpl;
import com.powsybl.iidm.network.tck.extensions.AbstractStandbyAutomatonTest;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        IllegalArgumentException e0 = assertThrows(IllegalArgumentException.class, standbyAutomatonAdder::add);
        assertEquals("Inconsistent low (385.0) and high (350.0) voltage thresholds for StaticVarCompensator SVC2",
            e0.getMessage());

        // lowVoltageSetpoint invalid
        standbyAutomatonAdder.withLowVoltageSetpoint(Double.NaN)
            .withHighVoltageSetpoint(400f)
            .withLowVoltageThreshold(385f)
            .withHighVoltageThreshold(405f);
        e0 = assertThrows(IllegalArgumentException.class, standbyAutomatonAdder::add);
        assertEquals("lowVoltageSetpoint (NaN) is invalid for StaticVarCompensator SVC2",
            e0.getMessage());

        // highVoltageSetpoint invalid
        standbyAutomatonAdder.withLowVoltageSetpoint(390f)
            .withHighVoltageSetpoint(Double.NaN)
            .withLowVoltageThreshold(385f)
            .withHighVoltageThreshold(405f);
        e0 = assertThrows(IllegalArgumentException.class, standbyAutomatonAdder::add);
        assertEquals("highVoltageSetpoint (NaN) is invalid for StaticVarCompensator SVC2",
            e0.getMessage());

        // lowVoltageSetpoint invalid
        standbyAutomatonAdder.withLowVoltageSetpoint(390f)
            .withHighVoltageSetpoint(400f)
            .withLowVoltageThreshold(Double.NaN)
            .withHighVoltageThreshold(405f);
        e0 = assertThrows(IllegalArgumentException.class, standbyAutomatonAdder::add);
        assertEquals("lowVoltageThreshold (NaN) is invalid for StaticVarCompensator SVC2",
            e0.getMessage());

        // highVoltageSetpoint invalid
        standbyAutomatonAdder.withLowVoltageSetpoint(390f)
            .withHighVoltageSetpoint(400f)
            .withLowVoltageThreshold(385f)
            .withHighVoltageThreshold(Double.NaN);
        e0 = assertThrows(IllegalArgumentException.class, standbyAutomatonAdder::add);
        assertEquals("highVoltageThreshold (NaN) is invalid for StaticVarCompensator SVC2",
            e0.getMessage());
    }

    @Test
    void logsTests() {
        ListAppender<ILoggingEvent> logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(StandbyAutomatonImpl.class)).addAppender(logWatcher);

        // Prepare the test
        Network network = SvcTestCaseFactory.create();
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

        // highVoltageSetpoint > highVoltageThreshold
        standbyAutomatonAdder.withLowVoltageSetpoint(390f)
            .withHighVoltageSetpoint(410f)
            .withLowVoltageThreshold(385f)
            .withHighVoltageThreshold(405f)
            .add();

        // Checks
        List<ILoggingEvent> logsList = logWatcher.list;
        assertEquals(2, logsList.size());
        assertEquals("Invalid low voltage setpoint 380.0 < threshold 385.0 for StaticVarCompensator SVC2",
            logsList.get(0).getFormattedMessage());
        assertEquals("Invalid high voltage setpoint 410.0 > threshold 405.0 for StaticVarCompensator SVC2",
            logsList.get(1).getFormattedMessage());
    }
}
