/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class FictitiousInjectionsXmlTest extends AbstractXmlConverterTest {

    @Test
    public void testBb() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2013-01-15T18:45:00+01:00"));
        network.getBusBreakerView().getBus("NGEN").setFictitiousP0(1.0).setFictitiousQ0(2.0);
        network.getBusBreakerView().getBus("NLOAD").setFictitiousP0(3.0);
        network.getBusBreakerView().getBus("NHV1").setFictitiousQ0(4.0);
        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::validateAndRead,
                getVersionedNetworkPath("eurostag-fict-inj.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility
        roundTripVersionedXmlFromMinToCurrentVersionTest("eurostag-fict-inj.xml", IidmXmlVersion.V_1_8);
    }

    @Test
    public void testNb() throws IOException {
        Network network = FictitiousSwitchFactory.create();
        network.setCaseDate(DateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.getVoltageLevel("C").getNodeBreakerView()
                .setFictitiousP0(0, 1.0)
                .setFictitiousQ0(1, 2.0)
                .setFictitiousP0(2, 3.0)
                .setFictitiousQ0(2, 4.0);
        roundTripTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::validateAndRead,
                getVersionedNetworkPath("fictitiousSwitchRef-fict-inj.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility
        roundTripVersionedXmlFromMinToCurrentVersionTest("fictitiousSwitchRef-fict-inj.xml", IidmXmlVersion.V_1_8);
    }
}
