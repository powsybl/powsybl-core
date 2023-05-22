/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class ReplaceTieLinesByLinesTest extends AbstractConverterTest {

    @Test
    void roundTrip() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        new ReplaceTieLinesByLines().apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-replace-tl.xml");
    }

    @Test
    void postProcessor() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        NetworkXml.write(network, tmpDir.resolve("tl-test.xml"));
        Network read = Network.read(tmpDir.resolve("tl-test.xml"), LocalComputationManager.getDefault(),
                new ImportConfig("replaceTieLinesByLines"), new Properties());
        roundTripXmlTest(read, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-replace-tl.xml");
    }
}
