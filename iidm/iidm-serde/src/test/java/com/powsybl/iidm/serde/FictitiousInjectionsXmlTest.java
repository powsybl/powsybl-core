/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class FictitiousInjectionsXmlTest extends AbstractIidmSerDeTest {

    @Test
    void testBb() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00+01:00"));
        network.getBusBreakerView().getBus("NGEN").setFictitiousP0(1.0).setFictitiousQ0(2.0);
        network.getBusBreakerView().getBus("NLOAD").setFictitiousP0(3.0);
        network.getBusBreakerView().getBus("NHV1").setFictitiousQ0(4.0);
        roundTripXmlTest(network,
                NetworkSerDe::writeAndValidate,
                NetworkSerDe::validateAndRead,
                getVersionedNetworkPath("eurostag-fict-inj.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility
        roundTripVersionedXmlFromMinToCurrentVersionTest("eurostag-fict-inj.xml", IidmVersion.V_1_8);
    }

    @Test
    void testNb() throws IOException {
        Network network = FictitiousSwitchFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.getVoltageLevel("C").getNodeBreakerView()
                .setFictitiousP0(0, 1.0)
                .setFictitiousQ0(1, 2.0)
                .setFictitiousP0(2, 3.0)
                .setFictitiousQ0(2, 4.0);
        roundTripXmlTest(network,
                NetworkSerDe::writeAndValidate,
                NetworkSerDe::validateAndRead,
                getVersionedNetworkPath("fictitiousSwitchRef-fict-inj.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility
        roundTripVersionedXmlFromMinToCurrentVersionTest("fictitiousSwitchRef-fict-inj.xml", IidmVersion.V_1_8);
    }
}
