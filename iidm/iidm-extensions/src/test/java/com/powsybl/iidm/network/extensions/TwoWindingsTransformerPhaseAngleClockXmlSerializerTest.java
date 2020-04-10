/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

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

        TwoWindingsTransformerPhaseAngleClock pac = new TwoWindingsTransformerPhaseAngleClockImpl(transformer, 3);
        transformer.addExtension(TwoWindingsTransformerPhaseAngleClock.class, pac);

        Network network2 = roundTripXmlTest(network,
            NetworkXml::writeAndValidate,
            NetworkXml::read,
            "/twoWindingsTransformerPhaseAngleClock.xml");

        TwoWindingsTransformerPhaseAngleClock pacXml = network2.getTwoWindingsTransformer("NHV2_NLOAD")
            .getExtension(TwoWindingsTransformerPhaseAngleClock.class);
        assertNotNull(pacXml);
        assertEquals(3, pacXml.getPhaseAngleClock());
    }
}
