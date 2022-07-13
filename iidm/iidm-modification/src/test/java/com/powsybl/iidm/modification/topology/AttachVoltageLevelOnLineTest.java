/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class AttachVoltageLevelOnLineTest extends AbstractXmlConverterTest {

    @Test
    public void attachVoltageLevelOnLineNbTest() throws IOException {
        Network network = createNbNetwork();
        NetworkModification modification = new AttachVoltageLevelOnLine("VLTEST", BBS,
                network.getLine("CJ"));
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-line-split-vl.xml");
    }

    @Test
    public void attachVoltageLevelOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        NetworkModification modification = new AttachVoltageLevelOnLine(VOLTAGE_LEVEL_ID, BBS,
                network.getLine("NHV1_NHV2_1"));
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-line-split-nb-vl.xml");
    }

    @Test
    public void attachVoltageLevelOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        NetworkModification modification = new AttachVoltageLevelOnLine(VOLTAGE_LEVEL_ID, "bus",
                network.getLine("NHV1_NHV2_1"));
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-line-split-bb-vl.xml");
    }

    @Test
    public void testConstructor() {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        AttachVoltageLevelOnLine modification = new AttachVoltageLevelOnLine(VOLTAGE_LEVEL_ID, BBS, line);
        assertEquals(VOLTAGE_LEVEL_ID, modification.getVoltageLevelId());
        assertEquals(BBS, modification.getBbsOrBusId());
        assertEquals(50, modification.getPercent(), 0.0);
        assertSame(line, modification.getLine());
        assertEquals(line.getId() + "_1", modification.getLine1Id());
        assertNull(modification.getLine1Name());
        assertEquals(line.getId() + "_2", modification.getLine2Id());
        assertNull(modification.getLine2Name());
    }

    @Test
    public void testSetters() {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        AttachVoltageLevelOnLine modification = new AttachVoltageLevelOnLine(VOLTAGE_LEVEL_ID, BBS, line);
        modification.setPercent(40.0)
                .setLine1Id(line.getId() + "_A")
                .setLine1Name("A")
                .setLine2Id(line.getId() + "_B")
                .setLine2Name("B");
        assertEquals(40, modification.getPercent(), 0.0);
        assertEquals(line.getId() + "_A", modification.getLine1Id());
        assertEquals("A", modification.getLine1Name());
        assertEquals(line.getId() + "_B", modification.getLine2Id());
        assertEquals("B", modification.getLine2Name());
    }

}
