/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class StaticVarCompensatorXmlTest extends AbstractXmlConverterTest {

    @Test
    void roundTripTest() throws IOException {
        // backward compatibility
        roundTripAllPreviousVersionedXmlTest("staticVarCompensatorRoundTripRef.xml");

        Network network = SvcTestCaseFactory.create();
        addProperties(network);
        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("staticVarCompensatorRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));
    }

    @Test
    void remoteRegulationRoundTripTest() throws IOException {
        Network network = SvcTestCaseFactory.createWithRemoteRegulatingTerminal();
        addProperties(network);
        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("regulatingStaticVarCompensatorRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));
    }

    @Test
    void readFaultyVersionRegulatingSvcFile() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkXml.read(getVersionedNetworkAsStream("faultyRegulatingStaticVarCompensatorRoundTripRef.xml", IidmXmlVersion.V_1_0)));
        assertTrue(e.getMessage().contains("staticVarCompensator.regulatingTerminal is not supported for IIDM-XML version 1.0. " +
                "IIDM-XML version should be >= 1.1"));
    }

    @Test
    void writeFaultyVersionRegulatingSvcFile() throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkXml.write(SvcTestCaseFactory.createWithRemoteRegulatingTerminal(), new ExportOptions().setVersion("1.0"), os));
            assertTrue(e.getMessage().contains("staticVarCompensator.regulatingTerminal is not defined as default and not supported for IIDM-XML version 1.0. " +
                    "IIDM-XML version should be >= 1.1"));
        }
    }

    private static void addProperties(Network network) {
        network.getStaticVarCompensator("SVC2")
                .setProperty("test", "test");
    }
}
