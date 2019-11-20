/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * @author Teofil Calin Banc<teofil-calin.banc at rte-france.com>
 */
public class TopologyLevelTest extends AbstractConverterTest {

    @Test
    public void testComparison() {
        assertEquals(TopologyLevel.NODE_BREAKER, TopologyLevel.min(TopologyKind.NODE_BREAKER, TopologyLevel.NODE_BREAKER));
        assertEquals(TopologyLevel.BUS_BREAKER, TopologyLevel.min(TopologyKind.NODE_BREAKER, TopologyLevel.BUS_BREAKER));
        assertEquals(TopologyLevel.BUS_BRANCH, TopologyLevel.min(TopologyKind.NODE_BREAKER, TopologyLevel.BUS_BRANCH));

        assertEquals(TopologyLevel.BUS_BREAKER, TopologyLevel.min(TopologyKind.BUS_BREAKER, TopologyLevel.NODE_BREAKER));
        assertEquals(TopologyLevel.BUS_BREAKER, TopologyLevel.min(TopologyKind.BUS_BREAKER, TopologyLevel.BUS_BREAKER));
        assertEquals(TopologyLevel.BUS_BRANCH, TopologyLevel.min(TopologyKind.BUS_BREAKER, TopologyLevel.BUS_BRANCH));
    }

    @Test
    public void testConversion() throws IOException {
        testConversion(NetworkXml.read(getClass().getResourceAsStream("/V1_0/fictitiousSwitchRef.xml")));

        testConversion(FictitiousSwitchFactory.create());
    }

    private void testConversion(Network network) throws IOException {
        writeXmlTest(network, TopologyLevelTest::writeNodeBreaker, "/V1_1/fictitiousSwitchRef.xml");

        network.getSwitchStream().forEach(sw -> sw.setRetained(false));
        network.getSwitch("BJ").setRetained(true);

        writeXmlTest(network, TopologyLevelTest::writeBusBreaker, "/V1_1/fictitiousSwitchRef-bbk.xml");
        writeXmlTest(network, TopologyLevelTest::writeBusBranch, "/V1_1/fictitiousSwitchRef-bbr.xml");
    }
    
    private static void writeNodeBreaker(Network network, Path path) {
        ExportOptions options = new ExportOptions()
            .setTopologyLevel(TopologyLevel.NODE_BREAKER);

        NetworkXml.write(network, options, path);
    }

    private static void writeBusBreaker(Network network, Path path) {
        ExportOptions options = new ExportOptions()
            .setTopologyLevel(TopologyLevel.BUS_BREAKER);

        NetworkXml.write(network, options, path);
    }

    private static void writeBusBranch(Network network, Path path) {
        ExportOptions options = new ExportOptions()
            .setTopologyLevel(TopologyLevel.BUS_BRANCH);

        NetworkXml.write(network, options, path);
    }
}
