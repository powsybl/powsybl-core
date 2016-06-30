/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.commons.ConverterBaseTest;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkXmlTest extends ConverterBaseTest {

    private Network createEurostagTutorialExample1() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2013-01-15T18:45:00+01:00"));
        return network;
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripXmlTest(createEurostagTutorialExample1(),
                         NetworkXml::writeAndValidate,
                         NetworkXml::read,
                         "/eurostag-tutorial-example1.xml");
    }

    @Test
    public void testValidationIssueWithProperties() throws Exception {
        Network network = createEurostagTutorialExample1();
        network.getGenerator("GEN").getProperties().setProperty("test", "foo");
        Path xmlFile = tmpDir.resolve("n.xml");
        NetworkXml.writeAndValidate(network, xmlFile);
    }

}