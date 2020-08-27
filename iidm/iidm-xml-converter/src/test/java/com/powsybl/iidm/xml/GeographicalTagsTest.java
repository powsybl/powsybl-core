/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class GeographicalTagsTest extends AbstractXmlConverterTest {

    private void geographicalTagsTest(Network network, IidmXmlVersion version) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(XMLExporter.VERSION, version.toString("."));
        MemDataSource dataSource = new MemDataSource();
        new XMLExporter().export(network, properties, dataSource);

        // compare with a file with geographical tags in properties
        try (InputStream is = new ByteArrayInputStream(dataSource.getData(null, "xiidm"))) {
            compareXml(getVersionedNetworkAsStream("eurostag-tutorial-example1.xml", version), is);
        }
    }

    @Test
    public void test() throws IOException {
        geographicalTagsTest(NetworkXml.read(getVersionedNetworkAsStream("eurostag-tutorial-example1-geographicalTags.xml", IidmXmlVersion.V_1_0)), IidmXmlVersion.V_1_0);
        geographicalTagsTest(NetworkXml.read(getVersionedNetworkAsStream("eurostag-tutorial-example1-geographicalTags.xml", IidmXmlVersion.V_1_1)), IidmXmlVersion.V_1_1);
        geographicalTagsTest(NetworkXml.read(getVersionedNetworkAsStream("eurostag-tutorial-example1-geographicalTags.xml", IidmXmlVersion.V_1_2)), IidmXmlVersion.V_1_2);
    }
}
