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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class ConnectVoltageLevelOnLineTest extends AbstractXmlConverterTest {

    @Test
    public void attachVoltageLevelOnLineNbTest() throws IOException {
        Network network = createNbNetwork();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("CJ"))
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-line-split-vl.xml");
    }

    @Test
    public void connectVoltageLevelOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-line-split-nb-vl.xml");
    }

    @Test
    public void connectVoltageLevelOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("bus")
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-line-split-bb-vl.xml");
    }

    @Test
    public void testConstructor() {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        ConnectVoltageLevelOnLine modification = new ConnectVoltageLevelOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).build();
        assertEquals(BBS, modification.getBbsOrBusId());
        assertEquals(50, modification.getPositionPercent(), 0.0);
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
        ConnectVoltageLevelOnLine modification = new ConnectVoltageLevelOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).build();
        modification.setPositionPercent(40.0)
                .setLine1Id(line.getId() + "_A")
                .setLine1Name("A")
                .setLine2Id(line.getId() + "_B")
                .setLine2Name("B");
        assertEquals(40, modification.getPositionPercent(), 0.0);
        assertEquals(line.getId() + "_A", modification.getLine1Id());
        assertEquals("A", modification.getLine1Name());
        assertEquals(line.getId() + "_B", modification.getLine2Id());
        assertEquals("B", modification.getLine2Name());
    }

    @Test
    public void testCompleteBuilder() throws IOException {
        Network network = createNbNetwork();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withPositionPercent(40)
                .withBusbarSectionOrBusId(BBS)
                .withLine1Id("FICT1L")
                .withLine1Name("FICT1LName")
                .withLine2Id("FICT2L")
                .withLine2Name("FICT2LName")
                .withLine(network.getLine("CJ"))
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-line-split-vl-complete.xml");
    }

    @Test
    public void testIncompleteBuilder() throws IOException {
        Network network = createNbNetwork();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("CJ"))
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-line-split-vl.xml");
    }

    @Test
    public void testExceptions() {
        Network network1 = createNbNetwork();

        NetworkModification modification2 = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("NOT_EXISTING")
                .withLine(network1.getLine("CJ"))
                .build();
        PowsyblException exception2 = assertThrows(PowsyblException.class, () -> modification2.apply(network1, true, Reporter.NO_OP));
        assertEquals("Identifiable NOT_EXISTING not found", exception2.getMessage());

        Network network2 = createBbNetwork();
        NetworkModification modification3 = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("NOT_EXISTING")
                .withLine(network2.getLine("NHV1_NHV2_1"))
                .build();
        PowsyblException exception3 = assertThrows(PowsyblException.class, () -> modification3.apply(network2, true, Reporter.NO_OP));
        assertEquals("Identifiable NOT_EXISTING not found", exception3.getMessage());
    }

    @Test
    public void testIgnore() throws IOException {
        Network network = EurostagTutorialExample1Factory.create().setCaseDate(DateTime.parse("2013-01-15T18:45:00.000+01:00"));
        NetworkModification modification1 = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("NOT_EXISTING")
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();
        modification1.apply(network);
        NetworkModification modification2 = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("LOAD")
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();
        modification2.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-tutorial-example1.xml");
    }
}
