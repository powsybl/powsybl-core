/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
class ShuntCompensatorModificationTest {

    private static Network network;
    private static ShuntCompensator shunt;

    @BeforeAll
    static void setUpInit() {
        network = FourSubstationsNodeBreakerFactory.create();
        assertTrue(network.getShuntCompensatorCount() > 0);
        shunt = network.getShuntCompensators().iterator().next();
    }

    @BeforeEach
    public void setUp() {
        shunt.setSectionCount(0);
    }

    @Test
    void testConstructorCoherence() {
        assertThrows(NullPointerException.class, () -> new ShuntCompensatorModification(null, null, 1),
            "Null id value should not be accepted.");
    }

    @Test
    void testApplyChecks() {
        ShuntCompensatorModification modif = new ShuntCompensatorModification(shunt.getId(), null, 1);
        assertDoesNotThrow(() -> modif.apply(network, true, ReportNode.NO_OP));
        assertEquals(1, shunt.getSectionCount(), "A valid apply should modify the value");
        ShuntCompensatorModification modif1 = new ShuntCompensatorModification("UNKNOWN_ID", null, 0);
        assertThrows(PowsyblException.class, () -> modif1.apply(network, true, ReportNode.NO_OP),
            "An invalid ID should fail to apply.");
        assertDoesNotThrow(() -> modif1.apply(network, false, ReportNode.NO_OP),
            "An invalid ID should not throw if throwException is false.");
        ShuntCompensatorModification modif2 = new ShuntCompensatorModification(shunt.getId(),
            null, shunt.getMaximumSectionCount() + 1);
        assertThrows(PowsyblException.class, () -> modif2.apply(network, true, ReportNode.NO_OP),
            "Trying to set the number of section outside of range should not be accepted.");
        assertEquals(1, shunt.getSectionCount(), "Failed applies should not modify the value");
    }

    @Test
    void testReconnection() {
        shunt.getTerminal().disconnect();
        new ShuntCompensatorModification(shunt.getId(), true, null).apply(network);
        Assertions.assertTrue(shunt.getTerminal().isConnected());
    }

    @Test
    void testDisconnection() {
        new ShuntCompensatorModification(shunt.getId(), false, null).apply(network);
        Assertions.assertFalse(shunt.getTerminal().isConnected());
    }

    @Test
    void testConnectionAndPositionChange() {
        shunt.getTerminal().disconnect();
        new ShuntCompensatorModification(shunt.getId(), true, 1).apply(network);
        Assertions.assertTrue(shunt.getTerminal().isConnected());
        Assertions.assertEquals(1, shunt.getSectionCount());
    }

    @Test
    void testConnectionOnRegulatingTerminal() {
        // heterogeneous controls not taken into account yet.
        network.getGenerator("GH1").getTerminal().disconnect();
        network.getGenerator("GH2").getTerminal().disconnect();
        Generator g3 = network.getGenerator("GH3");
        g3.setTargetV(33.);
        g3.setVoltageRegulatorOn(true);
        shunt.getTerminal().disconnect();
        shunt.setRegulatingTerminal(g3.getRegulatingTerminal());
        shunt.setTargetV(2);
        shunt.setTargetDeadband(1);
        shunt.setVoltageRegulatorOn(true);
        new ShuntCompensatorModification(shunt.getId(), true, null).apply(network);
        Assertions.assertTrue(shunt.getTerminal().isConnected());
        Assertions.assertEquals(2.0, shunt.getTargetV(), 0.1); // and not 33.
    }

    @Test
    void testConnectShuntCorrectSetPointWithNoRegulatingElmt() {
        network.getGenerator("GH1").getTerminal().disconnect();
        network.getGenerator("GH2").getTerminal().disconnect();
        Generator g3 = network.getGenerator("GH3");
        g3.setVoltageRegulatorOn(false);
        shunt.setTargetV(2);
        shunt.setTargetDeadband(1);
        shunt.setVoltageRegulatorOn(true);
        shunt.setRegulatingTerminal(g3.getTerminal());
        new ShuntCompensatorModification(shunt.getId(), true, null).apply(network);
        Assertions.assertTrue(shunt.getTerminal().isConnected());
        Assertions.assertEquals(2.0, shunt.getTargetV(), 0.1);
    }
}
