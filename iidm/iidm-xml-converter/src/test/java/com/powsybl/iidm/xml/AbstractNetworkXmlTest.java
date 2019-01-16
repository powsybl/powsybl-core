/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.fail;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public abstract class AbstractNetworkXmlTest extends AbstractConverterTest {

    public void writeToXmlTest(Network network, String ref) {
        Path xmlFile = tmpDir.resolve("n.xml");
        NetworkXml.writeAndValidate(network, xmlFile);
        try {
//            String result = new String(Files.readAllBytes(xmlFile));
//            System.out.println(result);
            compareXml(getClass().getResourceAsStream(ref), Files.newInputStream(xmlFile));
        } catch (IOException e) {
            fail();
        }
    }
}
