/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class DanglingLineXmlTest extends AbstractXmlConverterTest {

    @Test
    public void test() throws IOException {
        roundTripAllVersionedXmlTest("danglingLine.xml");
    }

    @Test
    public void testWithGeneration() throws IOException {
        Network network = DanglingLineNetworkFactory.createWithGeneration();
        network.setCaseDate(DateTime.parse("2020-07-16T10:08:48.321+02:00"));
        network.getDanglingLine("DL").setProperty("test", "test");
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::read, getVersionedNetworkPath("danglingLineWithGeneration.xml", IidmXmlConstants.CURRENT_IIDM_XML_VERSION));

        // backward compatibility checks from version 1.3
        roundTripVersionedXmlFromMinToCurrentVersionTest("danglingLineWithGeneration.xml", IidmXmlVersion.V_1_3);

        // check it fails for all versions < 1.3
        testForAllPreviousVersions(IidmXmlVersion.V_1_3, version -> {
            ExportOptions options = new ExportOptions().setVersion(version.toString("."));
            Path path = tmpDir.resolve("fail");
            try {
                NetworkXml.write(network, options, path);
                fail();
            } catch (PowsyblException e) {
                assertEquals("danglingLine.generation is not null and not supported for IIDM-XML version " + version.toString(".") + ". IIDM-XML version should be >= 1.3", e.getMessage());
            }
        });

        // check it doesn't fail for all versions < 1.3 if IidmVersionIncompatibilityBehavior is to log error
        testForAllPreviousVersions(IidmXmlVersion.V_1_3, version -> {
            try {
                writeXmlTest(network, (n, path) -> write(n, path, version), getVersionedNetworkPath("danglingLineWithGeneration.xml", version));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private static void write(Network network, Path path, IidmXmlVersion version) {
        ExportOptions options = new ExportOptions().setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR)
                .setVersion(version.toString("."));
        NetworkXml.write(network, options, path);
    }
}
