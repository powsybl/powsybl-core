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
class RevertConnectVoltageLevelOnLineTest extends AbstractConverterTest {

    @Test
    void revertConnectVoltageLevelOnLineNbTest() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("CJ"))
                .build();

        modification.apply(network);

        VoltageLevel vl = network.newVoltageLevel().setId("VL3").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs3").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker3").setName("breaker3").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        vl = network.newVoltageLevel().setId("VL4").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs4").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker4").setName("breaker4").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        network.newLine().setId("LINE34").setR(0.1).setX(0.1).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0).setNode1(1).setVoltageLevel1("VL3").setNode2(1).setVoltageLevel2("VL4").add();

        final NetworkModification modificationWithError1 = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id("line1NotFound")
                .withLine2Id("CJ_2")
                .withLineId("CJ")
                .build();
        assertThrows(PowsyblException.class, () -> modificationWithError1.apply(network, true, Reporter.NO_OP), "Line line1NotFound is not found");

        final NetworkModification modificationWithError2 = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id("CJ_1")
                .withLine2Id("line2NotFound")
                .withLineId("CJ")
                .build();
        assertThrows(PowsyblException.class, () -> modificationWithError2.apply(network, true, Reporter.NO_OP), "Line line2NotFound is not found");

        final NetworkModification modificationWithError3 = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id("CJ_1")
                .withLine2Id("LINE34")
                .withLineId("CJ")
                .build();
        assertThrows(PowsyblException.class, () -> modificationWithError3.apply(network, true, Reporter.NO_OP), "Lines CJ_1 and LINE34 should have one and only one voltage level in common at their extremities");

        // create limits on tee point side
        Line line1 = network.getLine("CJ_1");
        Line line2 = network.getLine("CJ_2");
        line1.newActivePowerLimits2().setPermanentLimit(100.).beginTemporaryLimit().setName("limit1").setValue(500).setAcceptableDuration(1200).endTemporaryLimit().add();
        line1.newApparentPowerLimits2().setPermanentLimit(200.).add();
        line1.newCurrentLimits2().setPermanentLimit(100.).beginTemporaryLimit().setName("limit3").setValue(900).setAcceptableDuration(60).endTemporaryLimit().add();

        line2.newActivePowerLimits1().setPermanentLimit(600.).beginTemporaryLimit().setName("limit4").setValue(1000).setAcceptableDuration(300).endTemporaryLimit().add();
        line2.newApparentPowerLimits1().setPermanentLimit(800.).add();
        line2.newCurrentLimits1().setPermanentLimit(900.).beginTemporaryLimit().setName("limit6").setValue(400).setAcceptableDuration(1200).endTemporaryLimit().add();

        modification = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id("CJ_1")
                .withLine2Id("CJ_2")
                .withLineId("CJ")
                .build();
        modification.apply(network, true, Reporter.NO_OP);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/fictitious-revert-connect-voltage-level-on-line-vl.xml");
    }

    @Test
    void revertConnectVoltageLevelOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();

        modification.apply(network);

        modification = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id("NHV1_NHV2_1_1")
                .withLine2Id("NHV1_NHV2_1_2")
                .withLineId("NHV1_NHV2_1")
                .build();
        modification.apply(network, true, Reporter.NO_OP);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-revert-connect-voltage-level-on-line-nb-vl.xml");
    }

    @Test
    void revertConnectVoltageLevelOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("bus")
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();

        modification.apply(network);

        modification = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id("NHV1_NHV2_1_1")
                .withLine2Id("NHV1_NHV2_1_2")
                .withLineId("NHV1_NHV2_1")
                .build();
        modification.apply(network, true, Reporter.NO_OP);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-revert-connect-voltage-level-on-line-bb-vl.xml");
    }

    @Test
    void testConstructor() {
        Network network = createNbBbNetwork();
        Line line1 = network.getLine("NHV1_NHV2_1");
        Line line2 = network.getLine("NHV1_NHV2_2");
        RevertConnectVoltageLevelOnLine modification = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id(line1.getId())
                .withLine2Id(line2.getId())
                .withLineId("NEW LINE ID")
                .build();
        assertEquals("NHV1_NHV2_1", modification.getLine1Id());
        assertEquals("NHV1_NHV2_2", modification.getLine2Id());
        assertEquals("NEW LINE ID", modification.getLineId());
        assertNull(modification.getLineName());

        modification = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id(line1.getId())
                .withLine2Id(line2.getId())
                .withLineId("NEW LINE ID")
                .withLineName("NEW LINE NAME")
                .build();
        assertEquals("NEW LINE NAME", modification.getLineName());
    }
}
