/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
class StaticVarCompensatorModificationTest {

    private Network network;
    private StaticVarCompensator svc;

    @BeforeEach
    public void setUp() {
        network = FourSubstationsNodeBreakerFactory.create();
        assertTrue(network.getStaticVarCompensatorCount() > 0);
        svc = network.getStaticVarCompensators().iterator().next();
        svc.setReactivePowerSetpoint(0);
        svc.setVoltageSetpoint(0);
    }

    @Test
    void testConstructorCoherence() {
        // Ok
        String id = svc.getId();
        assertDoesNotThrow(
            () -> new StaticVarCompensatorModification(id, null, 10.));
        assertDoesNotThrow(
            () -> new StaticVarCompensatorModification(id, 10., null));
        assertDoesNotThrow(
            () -> new StaticVarCompensatorModification(id, 10., 10.));
        // Warn log but ok
        assertDoesNotThrow(
            () -> new StaticVarCompensatorModification(id, null, null));
    }

    @Test
    void testApplyChecks() {
        StaticVarCompensatorModification modif1 = new StaticVarCompensatorModification("UNKNOWN_ID",
            1., 2.);
        assertThrows(PowsyblException.class, () -> modif1.apply(network, true, ReportNode.NO_OP),
            "An invalid ID should fail to apply.");
        assertDoesNotThrow(() -> modif1.apply(network, false, ReportNode.NO_OP),
            "An invalid ID should not throw if throwException is false.");

        StaticVarCompensatorModification modif2 = new StaticVarCompensatorModification(svc.getId(),
            1., 2.);
        modif2.apply(network, true, ReportNode.NO_OP);
        assertEquals(1, svc.getVoltageSetpoint(), "Failed to modify network during apply.");
        assertEquals(2, svc.getReactivePowerSetpoint(), "Failed to modify network during apply.");
    }

    @Test
    void testGetters() {
        StaticVarCompensatorModification modif = new StaticVarCompensatorModification("UNKNOWN_ID",
            1., null);
        assertEquals(OptionalDouble.empty(), modif.getOptionalReactivePowerSetpoint());
        assertNull(modif.getReactivePowerSetpoint());
        assertEquals(OptionalDouble.of(1.), modif.getOptionalVoltageSetpoint());
        assertEquals(1., modif.getVoltageSetpoint());
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new StaticVarCompensatorModification("ID", 1.0, 1.);
        assertEquals("StaticVarCompensatorModification", networkModification.getName());
    }
}
