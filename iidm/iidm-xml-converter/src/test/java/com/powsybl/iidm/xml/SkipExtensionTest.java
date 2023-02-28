/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static com.powsybl.commons.test.ComparisonUtils.compareXml;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
class SkipExtensionTest extends AbstractXmlConverterTest {

    @Test
    void testSkipExtension() throws IOException {
        Network network = NetworkXml.read(getVersionedNetworkAsStream("multiple-extensions.xml", IidmXmlVersion.V_1_0));

        Properties properties = new Properties();
        properties.put(XMLExporter.EXTENSIONS_LIST, "");
        properties.put(XMLExporter.VERSION, "1.0");

        // Write the file
        Path networkFile = tmpDir.resolve("noExtension.xiidm");
        network.write("XIIDM", properties, networkFile);

        // Compare
        compareXml(getVersionedNetworkAsStream("noExtension.xml", IidmXmlVersion.V_1_0), Files.newInputStream(networkFile));
    }
}
