/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class StaticVarCompensatorXmlTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        // backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("staticVarCompensatorRoundTripRef.xml");

        Network network = SvcTestCaseFactory.create();
        addProperties(network);
        allFormatsRoundTripTest(network, "staticVarCompensatorRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    @Test
    void noRegulatioModeTest() throws IOException {
        // backward compatibility: regulation mode is exported as OFF if it was not set in input file
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("staticVarCompensatorNoRegulationMode.xml", "staticVarCompensatorRegulationModeOFF.xml", IidmVersion.V_1_7);

        // regulation mode is exported as VOLTAGE and regulating is set to false if regulation mode was not set in input file
        allFormatsRoundTripFromVersionedXmlTest("staticVarCompensatorNoRegulationMode.xml", "notRegulatingStaticVarCompensatorRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    @Test
    void remoteRegulationRoundTripTest() throws IOException {
        Network network = SvcTestCaseFactory.createWithRemoteRegulatingTerminal();
        addProperties(network);
        allFormatsRoundTripTest(network, "regulatingStaticVarCompensatorRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    @Test
    void readFaultyVersionRegulatingSvcFile() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerDe.read(getVersionedNetworkAsStream("faultyRegulatingStaticVarCompensatorRoundTripRef.xml", IidmVersion.V_1_0)));
        assertTrue(e.getMessage().contains("staticVarCompensator.regulatingTerminal is not supported for IIDM version 1.0. " +
                "IIDM version should be >= 1.1"));
    }

    @Test
    void writeFaultyVersionRegulatingSvcFile() throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerDe.write(SvcTestCaseFactory.createWithRemoteRegulatingTerminal(), new ExportOptions().setVersion("1.0"), os));
            assertTrue(e.getMessage().contains("staticVarCompensator.regulatingTerminal is not defined as default and not supported for IIDM version 1.0. " +
                    "IIDM version should be >= 1.1"));
        }
    }

    private static void addProperties(Network network) {
        network.getStaticVarCompensator("SVC2")
                .setProperty("test", "test");
    }
}
