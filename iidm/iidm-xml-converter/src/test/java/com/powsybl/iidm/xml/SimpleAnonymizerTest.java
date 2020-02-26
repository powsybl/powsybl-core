/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SimpleAnonymizerTest extends AbstractXmlConverterTest {

    private void anonymisationTest(Network network) throws IOException {
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);

        // export with anonymisation on
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "true");
        MemDataSource dataSource = new MemDataSource();
        new XMLExporter(platformConfig).export(network, properties, dataSource);

        // check we have 2 files, the anonymized IIDM XML and a CSV mapping file and compare to anonymized reference files
        try (InputStream is = new ByteArrayInputStream(dataSource.getData(null, "xiidm"))) {
            compareXml(getVersionedNetworkAsStream("eurostag-tutorial-example1-anonymized.xml", CURRENT_IIDM_XML_VERSION), is);
        }
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("_mapping", "csv"))) {
            compareTxt(getClass().getResourceAsStream("/eurostag-tutorial-example1-mapping.csv"), is);
        }

        // re-import the IIDM XML using the CSV mapping file
        Network network2 = new XMLImporter(platformConfig).importData(dataSource, null);
        MemDataSource dataSource2 = new MemDataSource();
        new XMLExporter(platformConfig).export(network2, null, dataSource2);

        // check that re-imported IIDM XML has been deanonymized and is equals to reference file
        roundTripXmlTest(network2,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("eurostag-tutorial-example1.xml", CURRENT_IIDM_XML_VERSION));
    }

    @Test
    public void test() throws IOException {
        anonymisationTest(NetworkXml.read(getVersionedNetworkAsStream("eurostag-tutorial-example1.xml", IidmXmlVersion.V_1_0)));

        anonymisationTest(NetworkXmlTest.createEurostagTutorialExample1());
    }
}
