/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;
import static org.junit.Assert.*;

/**
 * Create a new Line from a given Line Adder and attach it on an existing Line by cutting the latter.
 *
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateLineOnLineTest extends AbstractXmlConverterTest {

    @Test
    public void createLineOnLineNbTest() throws IOException {
        Network network = createNbNetwork();
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLine("VLTEST", BBS, line, adder);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-line-split-l.xml");
    }

    @Test
    public void createLineOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLine(VOLTAGE_LEVEL_ID, BBS, line, adder);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-line-split-nb-l.xml");
    }

    @Test
    public void createLineOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLine(VOLTAGE_LEVEL_ID, "bus", line, adder);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-line-split-bb-l.xml");
    }

    @Test
    public void testConstructor() {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        CreateLineOnLine modification = new CreateLineOnLine(VOLTAGE_LEVEL_ID, BBS, line, adder);
        assertEquals(VOLTAGE_LEVEL_ID, modification.getVoltageLevelId());
        assertEquals(BBS, modification.getBbsOrBusId());
        assertEquals(50, modification.getPercent(), 0.0);
        assertSame(line, modification.getLine());
        assertSame(adder, modification.getLineAdder());
        assertEquals(line.getId() + "_VL", modification.getFictitiousVlId());
        assertNull(modification.getFictitiousVlName());
        assertFalse(modification.isCreateFictSubstation());
        assertEquals(line.getId() + "_S", modification.getFictitiousSubstationId());
        assertNull(modification.getFictitiousSubstationName());
        assertEquals(line.getId() + "_1", modification.getLine1Id());
        assertNull(modification.getLine1Name());
        assertEquals(line.getId() + "_2", modification.getLine2Id());
        assertNull(modification.getLine2Name());
    }

    @Test
    public void testSetters() {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        CreateLineOnLine modification = new CreateLineOnLine(VOLTAGE_LEVEL_ID, BBS, line, adder);
        modification.setPercent(40.0)
                .setFictitiousVlId("FICT_VL")
                .setFictitiousVlName("FICT")
                .setCreateFictSubstation(true)
                .setFictitiousSubstationId("FICT_S")
                .setFictitiousSubstationName("FICT2")
                .setLine1Id(line.getId() + "_A")
                .setLine1Name("A")
                .setLine2Id(line.getId() + "_B")
                .setLine2Name("B");
        assertEquals(40, modification.getPercent(), 0.0);
        assertEquals("FICT_VL", modification.getFictitiousVlId());
        assertEquals("FICT", modification.getFictitiousVlName());
        assertTrue(modification.isCreateFictSubstation());
        assertEquals("FICT_S", modification.getFictitiousSubstationId());
        assertEquals("FICT2", modification.getFictitiousSubstationName());
        assertEquals(line.getId() + "_A", modification.getLine1Id());
        assertEquals("A", modification.getLine1Name());
        assertEquals(line.getId() + "_B", modification.getLine2Id());
        assertEquals("B", modification.getLine2Name());
    }

    @Test
    public void testCompleteBuilder() throws IOException {
        Network network = createNbNetwork();
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder()
                .withVoltageLevelId("VLTEST")
                .withBusbarSectionOrBusId(BBS)
                .withLine(line)
                .withLineAdder(adder)
                .withPercent(40)
                .withFictitiousVoltageLevelId("FICTVL")
                .withFictitiousVoltageLevelName("FICTITIOUSVL")
                .withCreateFictitiousSubstation(true)
                .withFictitiousSubstationId("FICTSUB")
                .withFictitiousSubstationName("FICTITIOUSSUB")
                .withLine1Id("FICT1L")
                .withLine1Name("FICT1LName")
                .withLine2Id("FICT2L")
                .withLine2Name("FICT2LName")
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-line-split-l-complete.xml");
    }

    @Test
    public void testIncompleteBuilder() throws IOException {
        Network network = createNbNetwork();
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder()
                .withVoltageLevelId("VLTEST")
                .withBusbarSectionOrBusId(BBS)
                .withLine(line)
                .withLineAdder(adder)
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-line-split-l.xml");

    }

    private static LineAdder createLineAdder(Line line, Network network) {
        return network.newLine()
                .setId("testLine")
                .setR(line.getR())
                .setX(line.getX())
                .setB1(line.getB1())
                .setG1(line.getG1())
                .setB2(line.getB2())
                .setG2(line.getG2());
    }
}
