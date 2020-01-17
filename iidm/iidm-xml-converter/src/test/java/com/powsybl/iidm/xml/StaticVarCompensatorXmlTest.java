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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StaticVarCompensatorXmlTest extends AbstractXmlConverterTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void roundTripTest() throws IOException {
        // backward compatibility
        roundTripVersionnedXmlTest("staticVarCompensatorRoundTripRef.xml", IidmXmlVersion.V_1_0);

        Network network = SvcTestCaseFactory.create();
        addProperties(network);
        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionDir(CURRENT_IIDM_XML_VERSION) + "staticVarCompensatorRoundTripRef.xml");
    }

    @Test
    public void remoteRegulationRoundTripTest() throws IOException {
        Network network = SvcTestCaseFactory.createWithRemoteRegulatingTerminal();
        addProperties(network);
        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionDir(CURRENT_IIDM_XML_VERSION) + "regulatingStaticVarCompensatorRoundTripRef.xml");
    }

    @Test
    public void faultyVersionRegulatingSvcFile() {
        exception.expect(PowsyblException.class);
        exception.expectMessage("staticVarCompensator.regulatingTerminal is not supported for IIDM-XML version 1.0. " +
                "IIDM-XML version should be >= 1.1");
        NetworkXml.read(getClass().getResourceAsStream(getVersionDir(IidmXmlVersion.V_1_0) +
                "faultyRegulatingStaticVarCompensatorRoundTripRef.xml"));
    }

    private static void addProperties(Network network) {
        network.getStaticVarCompensator("SVC2")
                .setProperty("test", "test");
    }
}
