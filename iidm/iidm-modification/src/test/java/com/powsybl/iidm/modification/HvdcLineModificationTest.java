/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class HvdcLineModificationTest {

    private Network network;
    private HvdcLine l;
    private static final double EPSILON = Math.pow(10, -15);

    @BeforeEach
    public void setUp() {
        network = HvdcTestNetwork.createLcc();
        l = network.getHvdcLine("L");
    }

    @Test
    void testModificationFailure() {
        HvdcLineModification hvdcLineModification = new HvdcLineModification("dummy", true, null, null, null, null, null);
        PowsyblException exception = assertThrows(PowsyblException.class, () -> hvdcLineModification.apply(network, true, ReportNode.NO_OP));
        assertEquals("Hvdc line 'dummy' not found", exception.getMessage());
    }

    @Test
    void testModification() {
        HvdcLineModification hvdcLineModification;

        // ActivePowerSetpoint - relative value null
        assertEquals(280.0, l.getActivePowerSetpoint(), EPSILON);
        hvdcLineModification = new HvdcLineModification(l.getId(), null, 300.0, null, null, null, null);
        hvdcLineModification.apply(network, true, ReportNode.NO_OP);
        assertEquals(300.0, l.getActivePowerSetpoint(), EPSILON);

        // ActivePowerSetpoint - relative value false
        hvdcLineModification = new HvdcLineModification(l.getId(), null, 200.0, null, null, null, false);
        hvdcLineModification.apply(network, true, ReportNode.NO_OP);
        assertEquals(200.0, l.getActivePowerSetpoint(), EPSILON);

        // ActivePowerSetpoint - relative value true
        hvdcLineModification = new HvdcLineModification(l.getId(), null, 80.0, null, null, null, true);
        hvdcLineModification.apply(network, true, ReportNode.NO_OP);
        assertEquals(280.0, l.getActivePowerSetpoint(), EPSILON);

        // Relative value null - Nothing happens
        hvdcLineModification = new HvdcLineModification(l.getId(), null, null, null, null, null, null);
        hvdcLineModification.apply(network, true, ReportNode.NO_OP);
        assertEquals(280.0, l.getActivePowerSetpoint(), EPSILON);

        // Relative value false - Nothing happens
        hvdcLineModification = new HvdcLineModification(l.getId(), null, null, null, null, null, false);
        hvdcLineModification.apply(network, true, ReportNode.NO_OP);
        assertEquals(280.0, l.getActivePowerSetpoint(), EPSILON);

        // Relative value true - Nothing happens
        hvdcLineModification = new HvdcLineModification(l.getId(), null, null, null, null, null, true);
        hvdcLineModification.apply(network, true, ReportNode.NO_OP);
        assertEquals(280.0, l.getActivePowerSetpoint(), EPSILON);

        // Converter mode
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, l.getConvertersMode());
        hvdcLineModification = new HvdcLineModification(l.getId(), null, null, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, null, null, null);
        hvdcLineModification.apply(network, true, ReportNode.NO_OP);
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, l.getConvertersMode());

        // HvdcAngleDroopActivePowerControl inactive - Nothing happens
        hvdcLineModification = new HvdcLineModification(l.getId(), true, null, null, 50.0, 12.0, null);
        hvdcLineModification.apply(network, true, ReportNode.NO_OP);

        // HvdcAngleDroopActivePowerControl active
        HvdcAngleDroopActivePowerControl hvdcAngleDroopActivePowerControl = l.newExtension(HvdcAngleDroopActivePowerControlAdder.class).add();
        assertFalse(hvdcAngleDroopActivePowerControl.isEnabled());
        assertEquals(0.0, hvdcAngleDroopActivePowerControl.getP0(), EPSILON);
        assertEquals(0.0, hvdcAngleDroopActivePowerControl.getDroop(), EPSILON);
        hvdcLineModification = new HvdcLineModification(l.getId(), true, null, null, 50.0, 12.0, null);
        hvdcLineModification.apply(network, true, ReportNode.NO_OP);
        assertTrue(hvdcAngleDroopActivePowerControl.isEnabled());
        assertEquals(12.0, hvdcAngleDroopActivePowerControl.getP0(), EPSILON);
        assertEquals(50.0, hvdcAngleDroopActivePowerControl.getDroop(), EPSILON);
    }

    @Test
    void testDryRunModification() {
        HvdcLineModification hvdcLineModification;

        // Failing dryRun
        ReportNode reportNode = ReportNode.newRootReportNode()
            .withMessageTemplate("", "")
            .build();
        hvdcLineModification = new HvdcLineModification("LINE_NOT_EXISTING", null, 300.0, null, null, null, null);
        assertFalse(hvdcLineModification.dryRun(network, reportNode));
        assertEquals("Dry-run failed for HvdcLineModification. The issue is: Hvdc line 'LINE_NOT_EXISTING' not found",
            reportNode.getChildren().get(0).getChildren().get(0).getMessage());

        // Failing dryRun
        hvdcLineModification = new HvdcLineModification(l.getId(), true, null, null, 50.0, 12.0, true);
        assertFalse(hvdcLineModification.dryRun(network, reportNode));
        assertEquals("Dry-run failed for HvdcLineModification. The issue is: Relative value is set to true but it will not be applied since active power setpoint is undefined (null)",
            reportNode.getChildren().get(1).getChildren().get(0).getMessage());
        assertEquals("Dry-run failed for HvdcLineModification. The issue is: AV emulation enable is defined but it will not be applied since the hvdc line L does not have a HvdcAngleDroopActivePowerControl extension",
            reportNode.getChildren().get(1).getChildren().get(1).getMessage());
        assertEquals("Dry-run failed for HvdcLineModification. The issue is: P0 is defined but it will not be applied since the hvdc line L does not have a HvdcAngleDroopActivePowerControl extension",
            reportNode.getChildren().get(1).getChildren().get(2).getMessage());
        assertEquals("Dry-run failed for HvdcLineModification. The issue is: Droop is defined but it will not be applied since the hvdc line L does not have a HvdcAngleDroopActivePowerControl extension",
            reportNode.getChildren().get(1).getChildren().get(3).getMessage());

        // Passing dryRun
        l.newExtension(HvdcAngleDroopActivePowerControlAdder.class).add();
        hvdcLineModification = new HvdcLineModification(l.getId(), true, null, null, 50.0, 12.0, null);
        assertTrue(hvdcLineModification.dryRun(network));
    }
}
