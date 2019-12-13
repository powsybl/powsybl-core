/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StaticVarCompensatorXmlTest extends AbstractXmlConverterTest {

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

    private static void addProperties(Network network) {
        network.getStaticVarCompensator("SVC2")
                .setProperty("test", "test");
    }
}
