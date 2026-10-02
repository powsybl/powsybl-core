/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
class VoltagePerReactivePowerControlXmlSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = SvcTestCaseFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2019-05-27T12:17:02.504+02:00"));
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertNotNull(svc);

        svc.getVoltageRegulation()
            .setSlope(0.5)
            .setMode(RegulationMode.VOLTAGE_PER_REACTIVE_POWER);

        Network network2 = allFormatsRoundTripTest(network, "/voltagePerReactivePowerControl.xml", CURRENT_IIDM_VERSION);

        StaticVarCompensator svc2 = network2.getStaticVarCompensator("SVC2");
        assertNotNull(svc2);
        assertEquals(0.5, svc2.getVoltageRegulation().getSlope(), 0.0);

        // backward compatibility checks from version 1.5
        allFormatsRoundTripFromVersionedXmlFromMinToMaxVersionTest("voltagePerReactivePowerControl.xml", IidmVersion.V_1_5, CURRENT_IIDM_VERSION);
    }

    @ParameterizedTest
    @MethodSource("getVersions")
    void importWithReactivePowerRegulationAndZeroSlopeExtension(IidmVersion version) {
        Network network = NetworkSerDe.read(getVersionedNetworkAsStream("/voltagePerReactivePowerControlZeroSlope.xml", version));
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertNotNull(svc);
        assertNotNull(svc.getVoltageRegulation());
        assertEquals(RegulationMode.REACTIVE_POWER, svc.getVoltageRegulation().getMode());
        assertTrue(svc.getVoltageRegulation().isRegulating());
        assertTrue(Double.isNaN(svc.getVoltageRegulation().getSlope()));
        assertEquals(-170.0, svc.getLocalTargetQ(), 0.001);
        assertNull(svc.getVoltageRegulation().getTerminal());
    }

    @ParameterizedTest
    @MethodSource("getVersions")
    void importWithRegulatingTerminal(IidmVersion version) {
        Network network = NetworkSerDe.read(getVersionedNetworkAsStream("/voltagePerReactivePowerControlWithTerminal.xml", version));
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertNotNull(svc);
        assertNotNull(svc.getVoltageRegulation());
        assertEquals(RegulationMode.VOLTAGE_PER_REACTIVE_POWER, svc.getVoltageRegulation().getMode());
        assertTrue(svc.getVoltageRegulation().isRegulating());
        assertEquals(0.5, svc.getVoltageRegulation().getSlope(), 0.001);
        assertEquals(380., svc.getVoltageRegulation().getTargetValue(), 0.001);
        assertEquals(network.getGenerator("G1").getTerminal(), svc.getVoltageRegulation().getTerminal());
    }

    static Stream<Arguments> getVersions() {
        // 1.18: first version after the extension extinction
        return Stream.of(allBetweenVersions(IidmVersion.V_1_5, IidmVersion.V_1_18)).map(Arguments::of);
    }
}
