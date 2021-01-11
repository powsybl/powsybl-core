/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClockAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.xml.AbstractXmlConverterTest.getVersionedNetworkPath;
import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author José Antonio Marqués <marquesja at aia.es
 */
public class TwoWindingsTransformerPhaseAngleClockXmlSerializerTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2019-05-27T12:17:02.504+02:00"));
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer("NHV2_NLOAD");

        transformer.newExtension(TwoWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClock(3).add();

        Network network2 = roundTripXmlTest(network,
            NetworkXml::writeAndValidate,
            NetworkXml::read,
            getVersionedNetworkPath("/twoWindingsTransformerPhaseAngleClock.xml", CURRENT_IIDM_XML_VERSION));

        TwoWindingsTransformerPhaseAngleClock pacXml = network2.getTwoWindingsTransformer("NHV2_NLOAD")
            .getExtension(TwoWindingsTransformerPhaseAngleClock.class);
        assertNotNull(pacXml);
        assertEquals(3, pacXml.getPhaseAngleClock());
    }
}
