/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class ReplaceTeePointByVoltageLevelOnLineTest extends AbstractConverterTest {

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

    @Test
    void replaceTeePointByVoltageLevelOnLineNbTest() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        // create additional line to test bad configuration
        VoltageLevel vl = network.newVoltageLevel().setId("VL3").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs3").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker3").setName("breaker3").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        vl = network.newVoltageLevel().setId("VL4").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs4").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker4").setName("breaker4").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        network.newLine().setId("LINE34").setR(0.1).setX(0.1).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0).setNode1(1).setVoltageLevel1("VL3").setNode2(1).setVoltageLevel2("VL4").add();

        NetworkModification modificationWithError1 = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("line1NotFound")
                .withTeePointLine2("CJ_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId(BBS)
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError1.apply(network, true, Reporter.NO_OP)).getMessage().contains("Line line1NotFound is not found"));

        NetworkModification modificationWithError2 = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("CJ_1")
                .withTeePointLine2("line2NotFound")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId(BBS)
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError2.apply(network, true, Reporter.NO_OP)).getMessage().contains("Line line2NotFound is not found"));

        NetworkModification modificationWithError3 = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("CJ_1")
                .withTeePointLine2("CJ_2")
                .withTeePointLineToRemove("line3NotFound")
                .withBbsOrBusId(BBS)
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError3.apply(network, true, Reporter.NO_OP)).getMessage().contains("Line line3NotFound is not found"));

        NetworkModification modificationWithError5 = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("CJ_1")
                .withTeePointLine2("CJ_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId("notFoundBusbarSection")
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError5.apply(network, true, Reporter.NO_OP)).getMessage().contains("Busbar section notFoundBusbarSection is not found in voltage level VLTEST"));

        NetworkModification modificationWithError6 = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("CJ_1")
                .withTeePointLine2("CJ_2")
                .withTeePointLineToRemove("LINE34")
                .withBbsOrBusId(BBS)
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError6.apply(network, true, Reporter.NO_OP)).getMessage().contains("Unable to find the tee point and the tapped voltage level from lines CJ_1, CJ_2 and LINE34"));

        modification = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("CJ_1")
                .withTeePointLine2("CJ_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId(BBS)
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        modification.apply(network, true, Reporter.NO_OP);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-replace-tee-point-by-voltage-level-on-line-nb.xml");
    }

    @Test
    void replaceTeePointByVoltageLevelOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        modification = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("NHV1_NHV2_1_1")
                .withTeePointLine2("NHV1_NHV2_1_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId(BBS)
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        modification.apply(network, true, Reporter.NO_OP);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-replace-tee-point-by-voltage-level-on-line-nbbb.xml");
    }

    @Test
    void replaceTeePointByVoltageLevelOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId("bus").withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        NetworkModification modificationWithError = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("NHV1_NHV2_1_1")
                .withTeePointLine2("NHV1_NHV2_1_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId("busNotFound")
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError.apply(network, true, Reporter.NO_OP)).getMessage().contains("Bus busNotFound is not found in voltage level " + VOLTAGE_LEVEL_ID));

        modification = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("NHV1_NHV2_1_1")
                .withTeePointLine2("NHV1_NHV2_1_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId("bus")
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        modification.apply(network, true, Reporter.NO_OP);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-replace-tee-point-by-voltage-level-on-line-bb.xml");
    }

    @Test
    void testConstructor() {
        ReplaceTeePointByVoltageLevelOnLine modification = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("NHV1_NHV2_1")
                .withTeePointLine2("NHV1_NHV2_2")
                .withTeePointLineToRemove("NHV1_NHV2_3")
                .withBbsOrBusId("BBS")
                .withNewLine1Id("NEW LINE1 ID")
                .withNewLine2Id("NEW LINE2 ID").build();
        assertEquals("NHV1_NHV2_1", modification.getTeePointLine1Id());
        assertEquals("NHV1_NHV2_2", modification.getTeePointLine2Id());
        assertEquals("NHV1_NHV2_3", modification.getTeePointLineToRemoveId());
        assertEquals("BBS", modification.getBbsOrBusId());
        assertEquals("NEW LINE1 ID", modification.getNewLine1Id());
        assertNull(modification.getNewLine1Name());
        assertEquals("NEW LINE2 ID", modification.getNewLine2Id());
        assertNull(modification.getNewLine2Name());

        modification = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("NHV1_NHV2_1")
                .withTeePointLine2("NHV1_NHV2_2")
                .withTeePointLineToRemove("NHV1_NHV2_3")
                .withBbsOrBusId("BBS")
                .withNewLine1Id("NEW LINE1 ID")
                .withNewLine1Name("NEW LINE1 NAME")
                .withNewLine2Id("NEW LINE2 ID")
                .withNewLine2Name("NEW LINE2 NAME").build();
        assertEquals("NEW LINE1 NAME", modification.getNewLine1Name());
        assertEquals("NEW LINE2 NAME", modification.getNewLine2Name());
    }
}
