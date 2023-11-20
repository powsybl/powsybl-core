/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimatedAdder;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.iidm.serializer.AbstractXmlConverterTest;
import com.powsybl.iidm.serializer.IidmXmlConstants;
import com.powsybl.iidm.serializer.NetworkXml;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class ThreeWindingsTransformerToBeEstimatedXmlTest extends AbstractXmlConverterTest {

    @Test
    void test() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.setCaseDate(DateTime.parse("2019-05-27T12:17:02.504+02:00"));

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        twt.newExtension(ThreeWindingsTransformerToBeEstimatedAdder.class)
                .withRatioTapChanger2Status(true)
                .withRatioTapChanger3Status(true)
                .add();

        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::validateAndRead,
                getVersionDir(IidmXmlConstants.CURRENT_IIDM_XML_VERSION) + "threeWindingsTransformerToBeEstimated.xiidm");
    }
}
