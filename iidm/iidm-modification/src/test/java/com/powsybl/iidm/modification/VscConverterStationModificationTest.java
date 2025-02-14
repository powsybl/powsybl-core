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
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
class VscConverterStationModificationTest {

    private Network network;
    private VscConverterStation vsc;

    @BeforeEach
    public void setUp() {
        network = FourSubstationsNodeBreakerFactory.create();
        assertTrue(network.getVscConverterStationCount() > 0);
        vsc = network.getVscConverterStations().iterator().next();
    }

    @Test
    void testConstructorCoherence() {
        // Ok
        assertDoesNotThrow(
            () -> new VscConverterStationModification(vsc.getId(), null, 10.));
        assertDoesNotThrow(
            () -> new VscConverterStationModification(vsc.getId(), 10., null));
        assertDoesNotThrow(
            () -> new VscConverterStationModification(vsc.getId(), 10., 10.));
        // Warn log but ok
        assertDoesNotThrow(
            () -> new VscConverterStationModification(vsc.getId(), null, null));
    }

    @Test
    void testApplyChecks() {
        VscConverterStationModification modif1 = new VscConverterStationModification("UNKNOWN_ID", 1.,
            2.);
        assertThrows(PowsyblException.class, () -> modif1.apply(network, true, ReportNode.NO_OP),
            "An invalid ID should fail to apply.");
        assertDoesNotThrow(() -> modif1.apply(network, false, ReportNode.NO_OP));

        VscConverterStationModification modif2 = new VscConverterStationModification(vsc.getId(), 1.,
            2.);
        modif2.apply(network, true, ReportNode.NO_OP);
        assertEquals(1, vsc.getVoltageSetpoint(), "Failed to modify network during apply.");
        assertEquals(2, vsc.getReactivePowerSetpoint(), "Failed to modify network during apply.");
    }

    @Test
    void testGetters() {
        VscConverterStationModification modif = new VscConverterStationModification("UNKNOWN_ID",
            1., null);
        assertEquals(OptionalDouble.empty(), modif.getOptionalReactivePowerSetpoint());
        assertNull(modif.getReactivePowerSetpoint());
        assertEquals(OptionalDouble.of(1.), modif.getOptionalVoltageSetpoint());
        assertEquals(1., modif.getVoltageSetpoint());
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new VscConverterStationModification("ID", 10., null);
        assertEquals("VscConverterStationModification", networkModification.getName());
    }

    @Test
    void testHasImpact() {
        NetworkModification modification1 = new VscConverterStationModification("UNKNOWN_ID", 1., 2.);
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new VscConverterStationModification("VSC1", 400., 500.);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));

        NetworkModification modification3 = new VscConverterStationModification("VSC1", 1., 500.);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification3.hasImpactOnNetwork(network));

        NetworkModification modification4 = new VscConverterStationModification("VSC1", 400., 1.);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification4.hasImpactOnNetwork(network));

        NetworkModification modification5 = new VscConverterStationModification("VSC1", null, null);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification5.hasImpactOnNetwork(network));
    }
}
