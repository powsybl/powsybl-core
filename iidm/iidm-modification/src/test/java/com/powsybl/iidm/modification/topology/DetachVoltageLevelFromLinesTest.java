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
public class DetachVoltageLevelFromLinesTest extends AbstractXmlConverterTest {

    @Test
    public void detachVoltageLevelFromLinesNbTest() throws IOException {
        Network network = createNbNetwork();
        NetworkModification modification = new AttachVoltageLevelOnLine("VLTEST", BBS, network.getLine("CJ"));
        modification.apply(network);

        final NetworkModification modificationWithError1 = new DetachVoltageLevelFromLines("line1NotFound", "CJ_2", "CJ", null);
        assertThrows("Line line1NotFound is not found", PowsyblException.class, () -> modificationWithError1.apply(network));

        final NetworkModification modificationWithError2 = new DetachVoltageLevelFromLines("CJ_1", "line2NotFound", "CJ", null);
        assertThrows("Line line2NotFound is not found", PowsyblException.class, () -> modificationWithError2.apply(network));

        modification = new DetachVoltageLevelFromLines("CJ_1", "CJ_2", "CJ", null);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-reverse-line-split-vl.xml");
    }

    @Test
    public void detachVoltageLevelFromLinesNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        NetworkModification modification = new AttachVoltageLevelOnLine(VOLTAGE_LEVEL_ID, BBS, network.getLine("NHV1_NHV2_1"));
        modification.apply(network);

        modification = new DetachVoltageLevelFromLines("NHV1_NHV2_1_1", "NHV1_NHV2_1_2", "NHV1_NHV2_1", null);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-reverse-line-split-nb-vl.xml");
    }

    @Test
    public void detachVoltageLevelFromLinesBbTest() throws IOException {
        Network network = createBbNetwork();
        NetworkModification modification = new AttachVoltageLevelOnLine(VOLTAGE_LEVEL_ID, "bus", network.getLine("NHV1_NHV2_1"));
        modification.apply(network);

        modification = new DetachVoltageLevelFromLines("NHV1_NHV2_1_1", "NHV1_NHV2_1_2", "NHV1_NHV2_1", null);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-reverse-line-split-bb-vl.xml");
    }

    @Test
    public void testConstructor() {
        Network network = createNbBbNetwork();
        Line line1 = network.getLine("NHV1_NHV2_1");
        Line line2 = network.getLine("NHV1_NHV2_2");
        DetachVoltageLevelFromLines modification = new DetachVoltageLevelFromLines(line1.getId(), line2.getId(), "NEW LINE ID", null);
        assertEquals("NHV1_NHV2_1", modification.getLine1Id());
        assertEquals("NHV1_NHV2_2", modification.getLine2Id());
        assertEquals("NEW LINE ID", modification.getLineId());
        assertNull(modification.getLineName());

        modification = new DetachVoltageLevelFromLines(line1.getId(), line2.getId(), "NEW LINE ID", "NEW LINE NAME");
        assertEquals("NEW LINE NAME", modification.getLineName());
    }

    @Test
    public void testSetters() {
        Network network = createNbBbNetwork();
        Line line1 = network.getLine("NHV1_NHV2_1");
        Line line2 = network.getLine("NHV1_NHV2_2");
        DetachVoltageLevelFromLines modification = new DetachVoltageLevelFromLines(line1.getId(), line2.getId(), "NEW LINE ID", null);
        modification.setLine1Id(line1.getId() + "_A")
                .setLine2Id(line2.getId() + "_B")
                .setLineId("NEW LINE ID_C")
                .setLineName("NEW LINE NAME");
        assertEquals(line1.getId() + "_A", modification.getLine1Id());
        assertEquals(line2.getId() + "_B", modification.getLine2Id());
        assertEquals("NEW LINE ID_C", modification.getLineId());
        assertEquals("NEW LINE NAME", modification.getLineName());
    }
}
