/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.VoltageLevel;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class BusBreakerAnonymiserBugTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = NetworkSerDeTest.createEurostagTutorialExample1();
        // add a switch
        VoltageLevel vlgen = network.getVoltageLevel("VLGEN");
        vlgen.getBusBreakerView().newBus()
                .setId("NEW_BUS")
                .add();
        vlgen.getBusBreakerView().newSwitch()
                .setId("NEW_SWITCH")
                .setBus1("NGEN")
                .setBus2("NEW_BUS")
                .add();

        PlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "true");
        MemDataSource dataSource = new MemDataSource();

        // check we have no more exception
        var exporter = new XMLExporter(platformConfig);
        Assertions.assertThatCode(() -> exporter.export(network, properties, dataSource))
                .doesNotThrowAnyException();
    }
}
