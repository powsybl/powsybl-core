/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.BBS;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.VOLTAGE_LEVEL_ID;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createBbNetwork;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbBbNetwork;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbNetwork;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class LineOnLineIntoVoltageLevelOnLineTest extends AbstractXmlConverterTest {

    @Test
    public void lineOnLineIntoVoltageLevelOnLineNbTest() throws IOException {
        Network network = createNbNetwork();
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new AttachNewLineOnLine("VLTEST", BBS, line, adder);
        modification.apply(network);

        VoltageLevel vl = network.newVoltageLevel().setId("VL3").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs3").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker3").setName("breaker3").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        vl = network.newVoltageLevel().setId("VL4").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs4").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker4").setName("breaker4").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        network.newLine().setId("LINE34").setR(0.1).setX(0.1).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0).setNode1(1).setVoltageLevel1("VL3").setNode2(1).setVoltageLevel2("VL4").add();

        NetworkModification modificationWithError1 = new LineOnLineIntoVoltageLevelOnLine("line1NotFound", "CJ_2", "testLine", "NEW LINE1", null, "NEW LINE2", null);
        assertThrows("Line line1NotFound is not found", PowsyblException.class, () -> modificationWithError1.apply(network));
        NetworkModification modificationWithError2 = new LineOnLineIntoVoltageLevelOnLine("CJ_1", "line2NotFound", "testLine", "NEW LINE1", null, "NEW LINE2", null);
        assertThrows("Line line2NotFound is not found", PowsyblException.class, () -> modificationWithError2.apply(network));
        NetworkModification modificationWithError3 = new LineOnLineIntoVoltageLevelOnLine("CJ_1", "CJ_2", "line3NotFound", "NEW LINE1", null, "NEW LINE2", null);
        assertThrows("Line line3NotFound is not found", PowsyblException.class, () -> modificationWithError3.apply(network));
        NetworkModification modificationWithError4 = new LineOnLineIntoVoltageLevelOnLine("CJ_1", "CJ_2", "LINE34", "NEW LINE1", null, "NEW LINE2", null);
        assertThrows("Unable to find the attachment point and the attached voltage level from lines CJ_1, CJ_2 and LINE34", PowsyblException.class, () -> modificationWithError4.apply(network));

        modification = new LineOnLineIntoVoltageLevelOnLine("CJ_1", "CJ_2", "testLine", "NEW LINE1", null, "NEW LINE2", null);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-line-on-line-into-voltage-level-on-line-nb.xml");
    }

    @Test
    public void lineOnLineIntoVoltageLevelOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new AttachNewLineOnLine(VOLTAGE_LEVEL_ID, BBS, line, adder);
        modification.apply(network);

        modification = new LineOnLineIntoVoltageLevelOnLine("NHV1_NHV2_1_1", "NHV1_NHV2_1_2", "testLine", "NEW LINE1", null, "NEW LINE2", null);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-line-on-line-into-voltage-level-on-line-nbbb.xml");
    }

    @Test
    public void lineOnLineIntoVoltageLevelOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new AttachNewLineOnLine(VOLTAGE_LEVEL_ID, "bus", line, adder);
        modification.apply(network);

        modification = new LineOnLineIntoVoltageLevelOnLine("NHV1_NHV2_1_1", "NHV1_NHV2_1_2", "testLine", "NEW LINE1", null, "NEW LINE2", null);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-line-on-line-into-voltage-level-on-line-bb.xml");
    }

    @Test
    public void testConstructor() {
        LineOnLineIntoVoltageLevelOnLine modification = new LineOnLineIntoVoltageLevelOnLine("NHV1_NHV2_1", "NHV1_NHV2_2", "NHV1_NHV2_3", "NEW LINE1 ID", null, "NEW LINE2 ID", null);
        assertEquals("NHV1_NHV2_1", modification.getLine1ZId());
        assertEquals("NHV1_NHV2_2", modification.getLineZ2Id());
        assertEquals("NHV1_NHV2_3", modification.getLineZPId());
        assertEquals("NEW LINE1 ID", modification.getLine1CId());
        assertNull(modification.getLine1CName());
        assertEquals("NEW LINE2 ID", modification.getLineC2Id());
        assertNull(modification.getLineC2Name());

        modification = new LineOnLineIntoVoltageLevelOnLine("NHV1_NHV2_1", "NHV1_NHV2_2", "NHV1_NHV2_3", "NEW LINE1 ID", "NEW LINE1 NAME", "NEW LINE2 ID", "NEW LINE2 NAME");
        assertEquals("NEW LINE1 NAME", modification.getLine1CName());
        assertEquals("NEW LINE2 NAME", modification.getLineC2Name());
    }

    @Test
    public void testSetters() {
        LineOnLineIntoVoltageLevelOnLine modification = new LineOnLineIntoVoltageLevelOnLine("NHV1_NHV2_1", "NHV1_NHV2_2", "NHV1_NHV2_3", "NEW LINE1 ID", null, "NEW LINE2 ID", null);
        modification.setLine1ZId("NHV1_NHV2_1 _A")
                .setLineZ2Id("NHV1_NHV2_2 _B")
                .setLineZPId("NHV1_NHV2_3 _C")
                .setLine1CId("NEW LINE1 ID_C")
                .setLine1CName("NEW LINE1 NAME")
                .setLineC2Id("NEW LINE2 ID_C")
                .setLineC2Name("NEW LINE2 NAME");
        assertEquals("NHV1_NHV2_1 _A", modification.getLine1ZId());
        assertEquals("NHV1_NHV2_2 _B", modification.getLineZ2Id());
        assertEquals("NHV1_NHV2_3 _C", modification.getLineZPId());
        assertEquals("NEW LINE1 ID_C", modification.getLine1CId());
        assertEquals("NEW LINE1 NAME", modification.getLine1CName());
        assertEquals("NEW LINE2 ID_C", modification.getLineC2Id());
        assertEquals("NEW LINE2 NAME", modification.getLineC2Name());
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
