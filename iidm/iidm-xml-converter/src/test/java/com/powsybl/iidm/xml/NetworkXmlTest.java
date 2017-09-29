/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.joda.time.DateTime;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkXmlTest extends AbstractConverterTest {

    static Network createEurostagTutorialExample1() {
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

    @Test
    public void testGzipGunzip() throws IOException {
        Network network = createEurostagTutorialExample1();
        Path file1 = tmpDir.resolve("n.xml");
        NetworkXml.write(network, file1);
        Network network2 = NetworkXml.copy(network);
        Path file2 = tmpDir.resolve("n2.xml");
        NetworkXml.write(network2, file2);
        assertArrayEquals(Files.readAllBytes(file1), Files.readAllBytes(file2));
    }
}
