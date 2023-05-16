/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
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
        assertThrows(PowsyblException.class, () -> modif1.apply(network, true, Reporter.NO_OP),
            "An invalid ID should fail to apply.");

        StaticVarCompensatorModification modif2 = new StaticVarCompensatorModification(svc.getId(),
            1., 2.);
        modif2.apply(network, true, Reporter.NO_OP);
        assertEquals(1, svc.getVoltageSetpoint(), "Failed to modify network during apply.");
        assertEquals(2, svc.getReactivePowerSetpoint(), "Failed to modify network during apply.");
    }
}
