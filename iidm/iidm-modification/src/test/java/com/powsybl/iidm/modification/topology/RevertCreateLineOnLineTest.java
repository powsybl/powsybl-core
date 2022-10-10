/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
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
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createBbNetwork;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbBbNetwork;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbNetworkWithBusbarSection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class RevertCreateLineOnLineTest extends AbstractXmlConverterTest {

    @Test
    public void revertCreateLineOnLineNbTest() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        VoltageLevel vl = network.newVoltageLevel().setId("VL3").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs3").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker3").setName("breaker3").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        vl = network.newVoltageLevel().setId("VL4").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs4").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker4").setName("breaker4").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        network.newLine().setId("LINE34").setR(0.1).setX(0.1).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0).setNode1(1).setVoltageLevel1("VL3").setNode2(1).setVoltageLevel2("VL4").add();

        final NetworkModification modificationWithError1 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("line1NotFound")
                .withLineToBeMerged2Id("CJ_1")
                .withLineToBeDeletedId("CJ_2")
                .withMergedLineId("CJ")
                .build();
        assertThrows("Line line1NotFound is not found", PowsyblException.class, () -> modificationWithError1.apply(network, true, Reporter.NO_OP));
        final NetworkModification modificationWithError11 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("line1NotFound")
                .withLineToBeMerged2Id("CJ_1")
                .withLineToBeDeletedId("CJ_2")
                .withMergedLineId("CJ")
                .build();
        modificationWithError11.apply(network, false, Reporter.NO_OP);
        assertNull(network.getLine("CJ"));

        final NetworkModification modificationWithError2 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("line2NotFound")
                .withLineToBeDeletedId("CJ_3")
                .withMergedLineId("CJ")
                .build();
        assertThrows("Line line2NotFound is not found", PowsyblException.class, () -> modificationWithError2.apply(network, true, Reporter.NO_OP));
        final NetworkModification modificationWithError21 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("line2NotFound")
                .withLineToBeDeletedId("CJ_3")
                .withMergedLineId("CJ")
                .build();
        modificationWithError21.apply(network, false, Reporter.NO_OP);
        assertNull(network.getLine("CJ"));

        final NetworkModification modificationWithError3 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("line3NotFound")
                .withMergedLineId("CJ")
                .build();
        assertThrows("Line line3NotFound is not found", PowsyblException.class, () -> modificationWithError3.apply(network, true, Reporter.NO_OP));
        final NetworkModification modificationWithError31 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("line3NotFound")
                .withMergedLineId("CJ")
                .build();
        modificationWithError31.apply(network, false, Reporter.NO_OP);
        assertNull(network.getLine("CJ"));

        final NetworkModification modificationWithError4 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("LINE34")
                .withMergedLineId("CJ")
                .build();
        assertThrows("Unable to find the attachment point and the attached voltage level from lines CJ_1, CJ_2 and LINE34", PowsyblException.class, () -> modificationWithError4.apply(network, true, Reporter.NO_OP));
        final NetworkModification modificationWithError41 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("LINE34")
                .withMergedLineId("CJ")
                .build();
        modificationWithError41.apply(network, false, Reporter.NO_OP);
        assertNull(network.getLine("CJ"));

        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("testLine")
                .withMergedLineId("CJ_NEW")
                .build();
        modification.apply(network, true, Reporter.NO_OP);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-revert-create-line-on-line-l.xml");
    }

    @Test
    public void revertCreateLineOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1_2")
                .withLineToBeMerged2Id("NHV1_NHV2_1_1")
                .withLineToBeDeletedId("testLine")
                .withMergedLineId("NHV1_NHV2_1")
                .build();
        modification.apply(network, true, Reporter.NO_OP);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-revert-create-line-on-line-nb-l.xml");
    }

    @Test
    public void revertCreateLineOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId("bus").withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1_1")
                .withLineToBeMerged2Id("NHV1_NHV2_1_2")
                .withLineToBeDeletedId("testLine")
                .withMergedLineId("NHV1_NHV2_1")
                .build();
        modification.apply(network, true, Reporter.NO_OP);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-revert-create-line-on-line-bb-l.xml");
    }

    @Test
    public void testConstructor() {
        RevertCreateLineOnLine modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1")
                .withLineToBeMerged2Id("NHV1_NHV2_2")
                .withLineToBeDeletedId("NHV1_NHV2_3")
                .withMergedLineId("NEW LINE ID")
                .build();
        assertEquals("NHV1_NHV2_1", modification.getLineToBeMerged1Id());
        assertEquals("NHV1_NHV2_2", modification.getLineToBeMerged2Id());
        assertEquals("NHV1_NHV2_3", modification.getLineToBeDeletedId());
        assertEquals("NEW LINE ID", modification.getMergedLineId());
        assertNull(modification.getMergedLineName());

        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1")
                .withLineToBeMerged2Id("NHV1_NHV2_2")
                .withLineToBeDeletedId("NHV1_NHV2_3")
                .withMergedLineId("NEW LINE ID")
                .withMergedLineName("NEW LINE NAME")
                .build();
        assertEquals("NEW LINE NAME", modification.getMergedLineName());
    }

    @Test
    public void testSetters() {
        RevertCreateLineOnLine modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1")
                .withLineToBeMerged2Id("NHV1_NHV2_2")
                .withLineToBeDeletedId("NHV1_NHV2_3")
                .withMergedLineId("NEW LINE ID")
                .build();
        modification.setLineToBeMerged1Id("NHV1_NHV2_1 _A")
                .setLineToBeMerged2Id("NHV1_NHV2_2 _B")
                .setLineToBeDeletedId("NHV1_NHV2_3 _C")
                .setMergedLineId("NEW LINE ID_C")
                .setMergedLineName("NEW LINE NAME");
        assertEquals("NHV1_NHV2_1 _A", modification.getLineToBeMerged1Id());
        assertEquals("NHV1_NHV2_2 _B", modification.getLineToBeMerged2Id());
        assertEquals("NHV1_NHV2_3 _C", modification.getLineToBeDeletedId());
        assertEquals("NEW LINE ID_C", modification.getMergedLineId());
        assertEquals("NEW LINE NAME", modification.getMergedLineName());
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
