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
public class RevertCreateLineOnLineTest extends AbstractXmlConverterTest {

    @Test
    public void revertCreateLineOnLineNbTest() throws IOException {
        Network network = createNbNetwork();
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLine("VLTEST", BBS, line, adder);
        modification.apply(network);

        VoltageLevel vl = network.newVoltageLevel().setId("VL3").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs3").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker3").setName("breaker3").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        vl = network.newVoltageLevel().setId("VL4").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs4").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker4").setName("breaker4").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        network.newLine().setId("LINE34").setR(0.1).setX(0.1).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0).setNode1(1).setVoltageLevel1("VL3").setNode2(1).setVoltageLevel2("VL4").add();

        final NetworkModification modificationWithError1 = new RevertCreateLineOnLine("line1NotFound", "CJ_1", "CJ_2", "CJ", null);
        assertThrows("Line line1NotFound is not found", PowsyblException.class, () -> modificationWithError1.apply(network));
        final NetworkModification modificationWithError11 = new RevertCreateLineOnLine("line1NotFound", "CJ_1", "CJ_2", "CJ", null);
        modificationWithError11.apply(network, false, Reporter.NO_OP);
        assertNull(network.getLine("CJ"));

        final NetworkModification modificationWithError2 = new RevertCreateLineOnLine("CJ_1", "line2NotFound", "CJ_3", "CJ", null);
        assertThrows("Line line2NotFound is not found", PowsyblException.class, () -> modificationWithError2.apply(network));
        final NetworkModification modificationWithError21 = new RevertCreateLineOnLine("CJ_1", "line2NotFound", "CJ_3", "CJ", null);
        modificationWithError21.apply(network, false, Reporter.NO_OP);
        assertNull(network.getLine("CJ"));

        final NetworkModification modificationWithError3 = new RevertCreateLineOnLine("CJ_1", "CJ_2", "line3NotFound", "CJ", null);
        assertThrows("Line line3NotFound is not found", PowsyblException.class, () -> modificationWithError3.apply(network));
        final NetworkModification modificationWithError31 = new RevertCreateLineOnLine("CJ_1", "CJ_2", "line3NotFound", "CJ", null);
        modificationWithError31.apply(network, false, Reporter.NO_OP);
        assertNull(network.getLine("CJ"));

        final NetworkModification modificationWithError4 = new RevertCreateLineOnLine("CJ_1", "CJ_2", "LINE34", "CJ", null);
        assertThrows("Unable to find the attachment point and the attached voltage level from lines CJ_1, CJ_2 and LINE34", PowsyblException.class, () -> modificationWithError4.apply(network));
        final NetworkModification modificationWithError41 = new RevertCreateLineOnLine("CJ_1", "CJ_2", "LINE34", "CJ", null);
        modificationWithError41.apply(network, false, Reporter.NO_OP);
        assertNull(network.getLine("CJ"));

        modification = new RevertCreateLineOnLine("CJ_1", "CJ_2", "testLine", "CJ_NEW", null);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-revert-create-line-on-line-l.xml");
    }

    @Test
    public void revertCreateLineOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLine(VOLTAGE_LEVEL_ID, BBS, line, adder);
        modification.apply(network);

        modification = new RevertCreateLineOnLine("NHV1_NHV2_1_2", "NHV1_NHV2_1_1", "testLine", "NHV1_NHV2_1", null);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-revert-create-line-on-line-nb-l.xml");
    }

    @Test
    public void revertCreateLineOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLine(VOLTAGE_LEVEL_ID, "bus", line, adder);
        modification.apply(network);

        modification = new RevertCreateLineOnLine("NHV1_NHV2_1_1", "NHV1_NHV2_1_2", "testLine", "NHV1_NHV2_1", null);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-revert-create-line-on-line-bb-l.xml");
    }

    @Test
    public void testConstructor() {
        RevertCreateLineOnLine modification = new RevertCreateLineOnLine("NHV1_NHV2_1", "NHV1_NHV2_2", "NHV1_NHV2_3", "NEW LINE ID", null);
        assertEquals("NHV1_NHV2_1", modification.getLineAZId());
        assertEquals("NHV1_NHV2_2", modification.getLineBZId());
        assertEquals("NHV1_NHV2_3", modification.getLineCZId());
        assertEquals("NEW LINE ID", modification.getLineId());
        assertNull(modification.getLineName());

        modification = new RevertCreateLineOnLine("NHV1_NHV2_1", "NHV1_NHV2_2", "NHV1_NHV2_3", "NEW LINE ID", "NEW LINE NAME");
        assertEquals("NEW LINE NAME", modification.getLineName());
    }

    @Test
    public void testSetters() {
        RevertCreateLineOnLine modification = new RevertCreateLineOnLine("NHV1_NHV2_1", "NHV1_NHV2_2", "NHV1_NHV2_3", "NEW LINE ID", null);
        modification.setLineAZId("NHV1_NHV2_1 _A")
                .setLineBZId("NHV1_NHV2_2 _B")
                .setLineCZId("NHV1_NHV2_3 _C")
                .setLineId("NEW LINE ID_C")
                .setLineName("NEW LINE NAME");
        assertEquals("NHV1_NHV2_1 _A", modification.getLineAZId());
        assertEquals("NHV1_NHV2_2 _B", modification.getLineBZId());
        assertEquals("NHV1_NHV2_3 _C", modification.getLineCZId());
        assertEquals("NEW LINE ID_C", modification.getLineId());
        assertEquals("NEW LINE NAME", modification.getLineName());
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
