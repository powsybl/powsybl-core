/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.commons.ConverterBaseTest;
import eu.itesla_project.iidm.network.test.FicticiousSwitchFactory;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class FicticiousSwitchTest extends ConverterBaseTest {

    @Test
    public void roundTripTest() throws IOException {
        NetworkXml.write(FicticiousSwitchFactory.create(), Paths.get("/tmp/ficticious.xiidm"));

        roundTripXmlTest(FicticiousSwitchFactory.create(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/ficticiousSwitchRef.xml");
    }

}
