/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import java.io.IOException;

import org.junit.Test;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;

/**
 * @author Luma Zamarreno <zamarrenolm at aia.es>
 */
public class ThreeWindingsTransformerXmlTest extends AbstractConverterTest {

    @Test
    public void currentLimitsRoundTripTest() throws IOException {
        roundTripXmlTest(ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits(),
                         NetworkXml::writeAndValidate,
                         NetworkXml::read,
                         "/threeWindingsTransformerRoundTripRef.xml");
    }

    @Test
    public void ratioTapChangerRoundTripTest() throws IOException {
        roundTripXmlTest(ThreeWindingsTransformerNetworkFactory.createWithRatioTapChangerInLeg1(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/threeWindingsTransformerRtcRoundTripRef.xml");
    }

    @Test
    public void phaseTapChangerRoundTripTest() throws IOException {
        roundTripXmlTest(ThreeWindingsTransformerNetworkFactory.createWithPhaseTapChangers(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/threeWindingsTransformerPtcRoundTripRef.xml");
    }
}
