/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;

import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author José Antonio Marqués <marquesja at aia.es
 */
public class ThreeWindingsTransformerPhaseAngleClockXmlSerializerTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.setCaseDate(DateTime.parse("2018-03-05T13:30:30.486+01:00"));
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer("3WT");

        ThreeWindingsTransformerPhaseAngleClock pac = new ThreeWindingsTransformerPhaseAngleClock(transformer, 3, 1);
        transformer.addExtension(ThreeWindingsTransformerPhaseAngleClock.class, pac);

        Network network2 = roundTripXmlTest(network,
            NetworkXml::writeAndValidate,
            NetworkXml::read,
            "/threeWindingsTransformerPhaseAngleClock.xml");

        ThreeWindingsTransformerPhaseAngleClock pacXml = network2.getThreeWindingsTransformer("3WT").getExtension(ThreeWindingsTransformerPhaseAngleClock.class);
        assertNotNull(pacXml);
        assertEquals(3, pacXml.getPhaseAngleClockLeg2());
        assertEquals(1, pacXml.getPhaseAngleClockLeg3());
    }
}
