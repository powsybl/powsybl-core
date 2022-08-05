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
public class DetachLineFromLinesTest extends AbstractXmlConverterTest {

    @Test
    public void detachNewLineFromLinesNbTest() throws IOException {
        Network network = createNbNetwork();
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new AttachNewLineOnLine("VLTEST", BBS, line, adder);
        modification.apply(network);

        final NetworkModification modificationWithError1 = new DetachLineFromLines("line1NotFound", "CJ_1", "CJ_2", "CJ", null);
        assertThrows("Line line1NotFound is not found", PowsyblException.class, () -> modificationWithError1.apply(network));

        final NetworkModification modificationWithError2 = new DetachLineFromLines("CJ_1", "line2NotFound", "CJ_3", "CJ", null);
        assertThrows("Line line2NotFound is not found", PowsyblException.class, () -> modificationWithError2.apply(network));

        final NetworkModification modificationWithError3 = new DetachLineFromLines("CJ_1", "CJ_2", "line3NotFound", "CJ", null);
        assertThrows("Line line3NotFound is not found", PowsyblException.class, () -> modificationWithError3.apply(network));

        modification = new DetachLineFromLines("CJ_1", "CJ_2", "testLine", "CJ_NEW", null);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-reverse-line-split-l.xml");
    }

    @Test
    public void detachNewLineFromLinesNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new AttachNewLineOnLine(VOLTAGE_LEVEL_ID, BBS, line, adder);
        modification.apply(network);

        modification = new DetachLineFromLines("NHV1_NHV2_1_1", "NHV1_NHV2_1_2", "testLine", "NHV1_NHV2_1", null);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-reverse-line-split-nb-l.xml");
    }

    @Test
    public void detachNewLineFromLinesBbTest() throws IOException {
        Network network = createBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new AttachNewLineOnLine(VOLTAGE_LEVEL_ID, "bus", line, adder);
        modification.apply(network);

        modification = new DetachLineFromLines("NHV1_NHV2_1_1", "NHV1_NHV2_1_2", "testLine", "NHV1_NHV2_1", null);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-reverse-line-split-bb-l.xml");
    }

    @Test
    public void testConstructor() {
        DetachLineFromLines modification = new DetachLineFromLines("NHV1_NHV2_1", "NHV1_NHV2_2", "NHV1_NHV2_3", "NEW LINE ID", null);
        assertEquals("NHV1_NHV2_1", modification.getLineAZId());
        assertEquals("NHV1_NHV2_2", modification.getLineBZId());
        assertEquals("NHV1_NHV2_3", modification.getLineCZId());
        assertEquals("NEW LINE ID", modification.getLineId());
        assertNull(modification.getLineName());

        modification = new DetachLineFromLines("NHV1_NHV2_1", "NHV1_NHV2_2", "NHV1_NHV2_3", "NEW LINE ID", "NEW LINE NAME");
        assertEquals("NEW LINE NAME", modification.getLineName());
    }

    @Test
    public void testSetters() {
        DetachLineFromLines modification = new DetachLineFromLines("NHV1_NHV2_1", "NHV1_NHV2_2", "NHV1_NHV2_3", "NEW LINE ID", null);
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
