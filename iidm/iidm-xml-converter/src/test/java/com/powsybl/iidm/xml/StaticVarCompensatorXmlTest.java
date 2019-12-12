/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
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
        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionDir(CURRENT_IIDM_XML_VERSION) + "staticVarCompensatorRoundTripRef.xml");

        // Add a SVC with a remote regulating terminal
        network.getVoltageLevel("VL2")
                .newStaticVarCompensator()
                .setId("SVC3")
                .setConnectableBus("B2")
                .setBus("B2")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetPoint(390.0)
                .setReactivePowerSetPoint(1.0)
                .setRegulatingTerminal(network.getLoad("L2").getTerminal())
                .add();
        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionDir(CURRENT_IIDM_XML_VERSION) + "regulatingStaticVarCompensatorRoundTripRef.xml");
    }
}
