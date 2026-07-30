/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.test.ReactiveLimitsTestNetworkFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ReactiveLimitsSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        // backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("reactiveLimitsRoundTripRef.xml");

        allFormatsRoundTripTest(ReactiveLimitsTestNetworkFactory.create(), "reactiveLimitsRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testImportRevertedMinQMaxQ(boolean checkRevertedMinQMaxQ) {
        String xml = """
<?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
<iidm:network xmlns:iidm="http://www.powsybl.org/schema/iidm/1_17" id="ReactiveLimits" sourceFormat="test" caseDate="2025-07-29T10:00:00.000+02:00" forecastDistance="0" minimumValidationLevel="STEADY_STATE_HYPOTHESIS">
    <iidm:substation id="S" country="FR" tso="RTE">
        <iidm:voltageLevel id="VL" nominalV="380.0" topologyKind="BUS_BREAKER">
            <iidm:busBreakerTopology>
                <iidm:bus id="B"/>
            </iidm:busBreakerTopology>
            <iidm:generator id="G1" energySource="OTHER" maxP="10.0" minP="0.0" targetV="380.0" voltageRegulatorOn="true" targetP="10.0" bus="B" connectableBus="B">
                <iidm:reactiveCapabilityCurve>
                    <iidm:point p="5.0" minQ="1.0" maxQ="10.0"/>
                    <iidm:point p="10.0" minQ="10.0" maxQ="2.0"/>
                </iidm:reactiveCapabilityCurve>
            </iidm:generator>
            <iidm:generator id="G2" energySource="OTHER" maxP="10.0" minP="0.0" targetV="380.0" voltageRegulatorOn="true" targetP="10.0" bus="B" connectableBus="B">
                <iidm:minMaxReactiveLimits minQ="1.0" maxQ="10.0"/>
            </iidm:generator>
        </iidm:voltageLevel>
    </iidm:substation>
</iidm:network>

            """;

        ImportOptions options = new ImportOptions()
                .setCheckRevertedMinQMaxQ(checkRevertedMinQMaxQ);

        if (checkRevertedMinQMaxQ) {
            Network network = NetworkSerDe.read(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                    options,
                    null);

            ReactiveCapabilityCurve curve = network.getGenerator("G1")
                    .getReactiveLimits(ReactiveCapabilityCurve.class);

            assertEquals(2.0, curve.getMinQ(100.0));
            assertEquals(10.0, curve.getMaxQ(100.0));
        } else {
            ValidationException e = assertThrows(
                    ValidationException.class,
                    () -> NetworkSerDe.read(
                            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                            options,
                            null));

            assertTrue(e.getMessage().contains(
                    "maximum reactive power is expected to be greater than or equal to minimum reactive power"));
        }
    }
}
