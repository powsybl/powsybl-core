/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimatedAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.IidmXmlConstants;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class TwoWindingsTransformerToBeEstimatedXmlTest extends AbstractXmlConverterTest {

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2019-05-27T12:17:02.504+02:00"));

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NHV2_NLOAD");
        twt.newExtension(TwoWindingsTransformerToBeEstimatedAdder.class)
                .withTapChanger(TwoWindingsTransformerToBeEstimated.TapChanger.RATIO_TAP_CHANGER)
                .add();

        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::validateAndRead,
                getVersionDir(IidmXmlConstants.CURRENT_IIDM_XML_VERSION) + "twoWindingsTransformerToBeEstimated.xiidm");
    }
}
