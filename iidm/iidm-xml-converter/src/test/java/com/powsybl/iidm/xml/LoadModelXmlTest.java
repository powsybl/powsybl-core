/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LoadModelXmlTest extends AbstractXmlConverterTest {

    @Test
    void zipModelTest() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2020-07-16T10:08:48.321+02:00"));
        network.getVoltageLevel("VLLOAD").newLoad()
                .setId("LOAD2")
                .setBus("NLOAD")
                .setP0(10)
                .setQ0(5)
                .newZipModel()
                    .setC0p(0.3)
                    .setC1p(0.5)
                    .setC2p(0.2)
                    .setC0q(0.1)
                    .setC1q(0.2)
                    .setC2q(0.7)
                    .add()
                .add();

        roundTripTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("eurostag-tutorial-example1-zip-load-model.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility, load model is skipped
        roundTripAllPreviousVersionedXmlTest("eurostag-tutorial-example1.xml");
    }

    @Test
    void expoModelTest() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2020-07-16T10:08:48.321+02:00"));
        network.getVoltageLevel("VLLOAD").newLoad()
                .setId("LOAD2")
                .setBus("NLOAD")
                .setP0(10)
                .setQ0(5)
                .newExponentialModel()
                    .setNp(0.6)
                    .setNq(0.5)
                    .add()
                .add();

        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("eurostag-tutorial-example1-expo-load-model.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility, load model is skipped
        roundTripAllPreviousVersionedXmlTest("eurostag-tutorial-example1.xml");
    }
}
