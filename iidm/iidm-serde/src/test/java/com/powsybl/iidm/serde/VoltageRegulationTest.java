/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.regulation.RegulationMode;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class VoltageRegulationTest extends AbstractIidmSerDeTest {

    @Test
    void remoteVoltageRegulationWithoutLocalTarget() throws IOException {
        allFormatsRoundTripFromVersionedXmlTest("remoteVoltageRegulationWithoutLocalTarget.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION);

        // Tests for backward compatibility
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("remoteVoltageRegulationWithoutLocalTarget.xml", IidmVersion.V_1_18);
    }

    @Test
    void remoteVoltageRegulationOff() throws IOException {
        allFormatsRoundTripFromVersionedXmlTest("remoteVoltageRegulationOff.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION);

        // Tests for backward compatibility
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("remoteVoltageRegulationOff.xml", IidmVersion.V_1_17);
    }

    @Test
    void reactivePowerPerActivePowerExport() throws IOException {
        allFormatsRoundTripFromVersionedXmlTest("reactivePowerPerActivePowerExport.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION);

        // Tests for backward compatibility
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("reactivePowerPerActivePowerExport.xml", IidmVersion.V_1_17);

        // Mode not supported before 1.19: voltage regulation object is not exported
        Network network = NetworkSerDe.read(getVersionedNetworkAsStream("reactivePowerPerActivePowerExport.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION));
        assertEquals(RegulationMode.REACTIVE_POWER_PER_ACTIVE_POWER, network.getBattery("battery").getVoltageRegulation().getMode());
        for (IidmVersion version : List.of(IidmVersion.V_1_17, IidmVersion.V_1_18)) {
            String xml = writeXml(network, version);
            assertFalse(xml.contains(RegulationMode.REACTIVE_POWER_PER_ACTIVE_POWER.name()));
            assertTrue(xml.contains("battery id=\"battery\""));
        }

        // Mode supported from 1.19
        String xml19 = writeXml(network, IidmVersion.V_1_19);
        assertTrue(xml19.contains(RegulationMode.REACTIVE_POWER_PER_ACTIVE_POWER.name()));
    }

    private static String writeXml(Network network, IidmVersion version) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        NetworkSerDe.write(network, new ExportOptions().setVersion(version.toString(".")), os);
        return os.toString(StandardCharsets.UTF_8);
    }
}
