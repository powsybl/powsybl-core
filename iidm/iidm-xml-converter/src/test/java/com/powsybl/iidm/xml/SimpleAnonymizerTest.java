/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SimpleAnonymizerTest extends AbstractConverterTest {

    private void anonymisationTest(Network network, String xiidmRef, String anonymizedXiidmRef, String anonymizedCsvRef) throws IOException {
        // export with anonymisation on
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED_PROPERTIES, "true");
        MemDataSource dataSource = new MemDataSource();
        new XMLExporter().export(network, properties, dataSource);

        // check we have 2 files, the anonymized IIDM XML and a CSV mapping file and compare to anonymized reference files
        try (InputStream is = new ByteArrayInputStream(dataSource.getData(null, "xiidm"))) {
            compareXml(getClass().getResourceAsStream(anonymizedXiidmRef), is);
        }
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("_mapping", "csv"))) {
            compareTxt(getClass().getResourceAsStream(anonymizedCsvRef), is);
        }

        PlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);

        // re-import the IIDM XML using the CSV mapping file
        Network network2 = new XMLImporter(platformConfig).importData(dataSource, null);
        MemDataSource dataSource2 = new MemDataSource();
        new XMLExporter().export(network2, null, dataSource2);

        // check that re-imported IIDM XML has been deanonymized and is equals to reference file
        roundTripXmlTest(network2,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                xiidmRef);
    }

    @Test
    public void test() throws IOException {
        anonymisationTest(NetworkXmlTest.createEurostagTutorialExample1(),
                          "/eurostag-tutorial-example1.xml",
                          "/eurostag-tutorial-example1-anonymized.xml",
                          "/eurostag-tutorial-example1-mapping.csv");
    }
}
