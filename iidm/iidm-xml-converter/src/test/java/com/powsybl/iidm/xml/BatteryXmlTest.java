/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public class BatteryXmlTest extends AbstractConverterTest {

    @Test
    public void batteryRoundTripTest() throws IOException {
        roundTripXmlTest(BatteryNetworkFactory.create(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/batteryRoundTripRef.xml");
    }

    @Test
    public void test() throws IOException {
        Properties properties = new Properties();
        FileDataSource dataSource = new FileDataSource(Paths.get("/home/benhamedcha"), "test");
        new XMLExporter(platformConfig).export(BatteryNetworkFactory.create(), properties, dataSource);
    }
}
