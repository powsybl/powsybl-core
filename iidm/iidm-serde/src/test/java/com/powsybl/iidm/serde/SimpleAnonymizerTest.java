/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.powsybl.commons.test.ComparisonUtils.compareTxt;
import static com.powsybl.commons.test.ComparisonUtils.compareXml;
import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SimpleAnonymizerTest extends AbstractIidmSerDeTest {

    private void anonymisationTest(Network network) throws IOException {
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);

        // export with anonymisation on
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "true");
        MemDataSource dataSource = new MemDataSource();
        new XMLExporter(platformConfig).export(network, properties, dataSource);

        // check we have 2 files, the anonymized IIDM XML and a CSV mapping file and compare to anonymized reference files
        try (InputStream is = new ByteArrayInputStream(dataSource.getData(null, "xiidm"))) {
            compareXml(getVersionedNetworkAsStream("eurostag-tutorial-example1-anonymized.xml", CURRENT_IIDM_VERSION), is);
        }
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("_mapping", "csv"))) {
            compareTxt(getClass().getResourceAsStream("/eurostag-tutorial-example1-mapping.csv"), is);
        }

        // re-import the IIDM XML using the CSV mapping file
        Network network2 = new XMLImporter(platformConfig).importData(dataSource, NetworkFactory.findDefault(), null);
        MemDataSource dataSource2 = new MemDataSource();
        new XMLExporter(platformConfig).export(network2, null, dataSource2);

        // check that re-imported IIDM XML has been deanonymized and is equals to reference file
        allFormatsRoundTripTest(network2, "eurostag-tutorial-example1.xml", CURRENT_IIDM_VERSION);
    }

    @Test
    void test() throws IOException {
        anonymisationTest(NetworkSerDe.read(getVersionedNetworkAsStream("eurostag-tutorial-example1.xml", IidmVersion.V_1_0)));

        anonymisationTest(NetworkSerDeTest.createEurostagTutorialExample1());
    }
}
