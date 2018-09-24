/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import java.io.IOException;
import java.io.InputStream;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import org.junit.Test;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;

import static org.junit.Assert.assertEquals;

/**
 * @author Luma Zamarreno <zamarrenolm at aia.es>
 */
public class ThreeWindingsTransformerXmlTest extends AbstractConverterTest {

    @Test
    public void roundTripTest() throws IOException {
        roundTripXmlTest(ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits(),
                         NetworkXml::writeAndValidate,
                         NetworkXml::read,
                         "/threeWindingsTransformerRoundTripRef.xml");
    }

    @Test
    public void testReadV10() {
        InputStream in = getClass().getResourceAsStream("/refsV1_0/threeWindingsTransformerRoundTripRef.xml");
        Network read = NetworkXml.read(in);
        ThreeWindingsTransformer.Leg2or3 leg2 = read.getThreeWindingsTransformer("3WT").getLeg2();
        ThreeWindingsTransformer.Leg2or3 leg3 = read.getThreeWindingsTransformer("3WT").getLeg3();
        assertEquals(0.9801, leg2.getRatioTapChanger().getStep(0).getR(), 0.0);
        assertEquals(0.1089, leg2.getRatioTapChanger().getStep(1).getX(), 0.0);
        assertEquals(0.8264462809917356, leg3.getRatioTapChanger().getStep(1).getG(), 0.0);
        assertEquals(0.09090909090909093, leg3.getRatioTapChanger().getStep(2).getB(), 0.0);
    }
}
