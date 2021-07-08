/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.MultipleExtensionsTestNetworkFactory;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertEquals;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class XMLExporterTest extends AbstractXmlConverterTest {

    public void exporterTest(Network network, IidmXmlVersion version) throws IOException {
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "false");

        MemDataSource dataSource = new MemDataSource();
        new XMLExporter().export(network, properties, dataSource);
        // check the exported file and compare it to iidm reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData(null, "xiidm"))) {
            compareXml(getVersionedNetworkAsStream("multiple-extensions.xml", version), is);
        }
    }

    @Test
    public void exportTest() throws IOException {
        exporterTest(MultipleExtensionsTestNetworkFactory.create(), CURRENT_IIDM_XML_VERSION);
    }

    @Test
    public void paramsTest() {
        var xmlExporter = new XMLExporter();
        assertEquals(10, xmlExporter.getParameters().size());
        assertEquals("IIDM XML v1.5 exporter", xmlExporter.getComment());
    }
}
