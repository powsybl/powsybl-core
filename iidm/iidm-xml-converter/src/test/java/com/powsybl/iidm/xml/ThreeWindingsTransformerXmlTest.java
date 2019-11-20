/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import java.io.IOException;

import org.junit.Test;

import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;

import static com.powsybl.iidm.xml.IidmXmlTestConstants.*;

/**
 * @author Luma Zamarreno <zamarrenolm at aia.es>
 */
public class ThreeWindingsTransformerXmlTest extends AbstractXmlConverterTest {

    @Test
    public void roundTripTest() throws IOException {
        roundTripVersionnedXmlTest("threeWindingsTransformerRoundTripRef.xml", IIDM_VERSION_1_0_DIR_NAME);

        roundTripXmlTest(ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits(),
                         NetworkXml::writeAndValidate,
                         NetworkXml::read,
                IIDM_CURRENT_VERSION_DIR_NAME + "threeWindingsTransformerRoundTripRef.xml");
    }
}
