/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TopologyLevel;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

import static com.powsybl.iidm.xml.AbstractXmlConverterTest.getVersionDir;
import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertEquals;

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
        testConversion(NetworkXml.read(getClass().getResourceAsStream(getVersionDir(IidmXmlVersion.V_1_0) + "fictitiousSwitchRef.xml")));

        testConversion(FictitiousSwitchFactory.create());
    }

    private void testConversion(Network network) throws IOException {
        writeXmlTest(network, TopologyLevelTest::writeNodeBreaker, getVersionDir(CURRENT_IIDM_XML_VERSION) + "fictitiousSwitchRef.xml");

        network.getSwitchStream().forEach(sw -> sw.setRetained(false));
        network.getSwitch("BJ").setRetained(true);

        writeXmlTest(network, TopologyLevelTest::writeBusBreaker, getVersionDir(CURRENT_IIDM_XML_VERSION) + "fictitiousSwitchRef-bbk.xml");
        writeXmlTest(network, TopologyLevelTest::writeBusBranch, getVersionDir(CURRENT_IIDM_XML_VERSION) + "fictitiousSwitchRef-bbr.xml");
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
