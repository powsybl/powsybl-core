/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerWithExtensionsFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class CreateVoltageLevelTopologyTest extends AbstractModificationTest {

    @Test
    void test() throws IOException {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        modification.apply(network);
        writeXmlTest(network, "/create-vl-topo-test.xiidm");
    }

    @Test
    void testComplete() throws IOException {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withBusbarSectionPrefixId("BBS_TEST")
                .withSwitchPrefixId("SW_TEST")
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        modification.apply(network);
        writeXmlTest(network, "/create-vl-topo-test-complete.xiidm");
    }

    @Test
    void testWithNullSwitchKind() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTest")
                .build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, null, SwitchKind.DISCONNECTOR)
                .build();
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("All switch kinds must be defined", e.getMessage());
        assertEquals("core.iidm.modification.undefinedSwitchKind", reportNode.getChildren().get(0).getMessageKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithUnsupportedSwitchKind() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTest")
                .build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.LOAD_BREAK_SWITCH, SwitchKind.DISCONNECTOR)
                .build();
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Switch kinds must be DISCONNECTOR or BREAKER", e.getMessage());
        assertEquals("core.iidm.modification.wrongSwitchKind", reportNode.getChildren().get(0).getMessageKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithNegativeCount() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTest")
                .build();
        NetworkModification modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(-1)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("busbar count must be >= 1", e.getMessage());
        assertEquals("core.iidm.modification.countLowerThanMin", reportNode.getChildren().get(0).getMessageKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithOneSection() {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(2)
                .withSectionCount(1)
                .build();
        modification.apply(network);

        BusbarSection bbs1 = network.getBusbarSection("VLTEST_1_1");
        BusbarSection bbs2 = network.getBusbarSection("VLTEST_2_1");
        assertNotNull(bbs1);
        assertNotNull(bbs2);

        BusbarSectionPosition bbsp1 = bbs1.getExtension(BusbarSectionPosition.class);
        BusbarSectionPosition bbsp2 = bbs2.getExtension(BusbarSectionPosition.class);
        assertNotNull(bbsp1);
        assertNotNull(bbsp2);

        assertEquals(1, bbsp1.getBusbarIndex());
        assertEquals(1, bbsp1.getSectionIndex());

        assertEquals(2, bbsp2.getBusbarIndex());
        assertEquals(1, bbsp2.getSectionIndex());
    }

    @Test
    void testWithUnexpectedSwitchKindsSize() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTest")
                .build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER)
                .build();
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Unexpected switch kinds count (1). Should be 3", e.getMessage());
        assertEquals("core.iidm.modification.unexpectedSwitchKindsCount", reportNode.getChildren().get(0).getMessageKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithNotExistingVoltageLevel() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTest")
                .build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId("NOT_EXISTING")
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Voltage level NOT_EXISTING is not found", e.getMessage());
        assertEquals("core.iidm.modification.voltageLevelNotFound", reportNode.getChildren().get(0).getMessageKey());
    }

    @Test
    void testWithBusBreakerVoltageLevel() throws IOException {
        Network network = EurostagTutorialExample1Factory.create().setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.newVoltageLevel()
                .setId("VLTEST")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId("VLTEST")
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .build();
        modification.apply(network);
        writeXmlTest(network, "/eurostag-new-voltage-level.xml");
    }

    @Test
    void testWithBusBreakerVoltageLevelAndConnectFeeders() {
        Network network = EurostagTutorialExample1Factory.create().setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.newVoltageLevel()
                .setId("VLTEST")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId("VLTEST")
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withConnectExistingConnectables(true)
                .build();
        assertThrows(PowsyblException.class, () -> modification.apply(network, true, ReportNode.NO_OP));

    }

    @Test
    void testErrorIfNotSwitchKindsDefinedAndNodeBreaker() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTest")
                .build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .build();
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Unexpected switch kinds count (0). Should be 3", e.getMessage());
        assertEquals("core.iidm.modification.unexpectedSwitchKindsCount", reportNode.getChildren().get(0).getMessageKey());
    }

    @Test
    void testWithReportNode() throws IOException {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestCreateVoltageLevelTopology")
                .build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        modification.apply(network, LocalComputationManager.getDefault(), reportNode);
        testReportNode(reportNode, "/reportNode/create-voltage-level-topology-report.txt");
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId(VLTEST)
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
            .build();
        assertEquals("CreateVoltageLevelTopology", networkModification.getName());
    }

    @Test
    void testHasImpact() {
        Network network = createNbNetwork();
        NetworkModification modification1 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId(VLTEST)
            .withAlignedBusesOrBusbarCount(-1)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId(VLTEST)
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
            .build();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));

        NetworkModification modification3 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("WRONG_VL")
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification3.hasImpactOnNetwork(network));

        NetworkModification modification4 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("VLTEST")
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification4.hasImpactOnNetwork(network));

        NetworkModification modification5 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("VLTEST")
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, null)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification5.hasImpactOnNetwork(network));

        NetworkModification modification6 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("VLTEST")
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.LOAD_BREAK_SWITCH)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification6.hasImpactOnNetwork(network));

        network.newVoltageLevel()
            .setId("VLTEST_BUS")
            .setNominalV(400.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        CreateVoltageLevelTopology modification7 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("VLTEST_BUS")
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .build();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification7.hasImpactOnNetwork(network));
    }

    @Test
    void testWithAlreadyExistingTopology() {
        Network network = createNbNetwork();
        // Add busbar section position extension to the busbar section of voltage level C
        network.getBusbarSection("D").newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();

        // Add a busbar section with one section "below" the busbar section D with default naming strategy
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("C")
            .withAlignedBusesOrBusbarCount(1)
            .withSectionCount(1)
            .withLowSectionIndex(1)
            .withSwitchKinds()
            .withLowBusOrBusbarIndex(2)
            .build();
        modification.apply(network);

        BusbarSection bbs = network.getBusbarSection("C_2_1");
        assertNotNull(bbs);
        BusbarSectionPosition bbsPosition = bbs.getExtension(BusbarSectionPosition.class);
        assertEquals(2, bbsPosition.getBusbarIndex());
        assertEquals(1, bbsPosition.getSectionIndex());
        assertEquals(2, network.getVoltageLevel("C").getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithAlreadyExistingTopologyAndWrongPosition() {
        Network network = createNbNetwork();
        // Add busbar section position extension to the busbar section of voltage level C
        network.getBusbarSection("D").newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();

        // Add a busbar section with one section with the same indexes as the busbar section D with default naming strategy
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("C")
            .withAlignedBusesOrBusbarCount(1)
            .withSectionCount(1)
            .withLowSectionIndex(1)
            .withLowBusOrBusbarIndex(1)
            .build();
        assertThrows(PowsyblException.class, () -> modification.apply(network, true, ReportNode.NO_OP));

        // Check that nothing is added if throwException is false
        modification.apply(network, false, ReportNode.NO_OP);

        BusbarSection bbs = network.getBusbarSection("C_2_1");
        assertNull(bbs);
        assertEquals(1, network.getVoltageLevel("C").getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testConnectConnectablesWithNoBusbarSectionPositionExtension() {
        Network network = FourSubstationsNodeBreakerFactory.create(); // It has no position extensions
        NetworkModification networkModification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId("S1VL2")
                .withAlignedBusesOrBusbarCount(1)
                .withSectionCount(1)
                .withLowBusOrBusbarIndex(3)
                .withConnectExistingConnectables(true)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> networkModification.apply(network, true, ReportNode.NO_OP));
        assertEquals("Some busbar sections have no position in voltage level S1VL2", e.getMessage());
    }

    @Test
    void testConnectFeeders() {
        Network network = FourSubstationsNodeBreakerWithExtensionsFactory.create();
        VoltageLevel s1Vl2 = network.getVoltageLevel("S1VL2");
        int switchCountBeforeModification = s1Vl2.getNodeBreakerView().getSwitchCount();

        new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(s1Vl2.getId())
                .withAlignedBusesOrBusbarCount(2)
                .withSectionCount(1)
                .withLowBusOrBusbarIndex(3)
                .withConnectExistingConnectables(true)
                .build().apply(network);

        int switchCountAfterModification = s1Vl2.getNodeBreakerView().getSwitchCount();
        long connectableCount = s1Vl2.getConnectableStream().filter(c -> !(c instanceof BusbarSection)).count();

        // New switches count should be: switchCountBeforeModification + 2 * connectableCount (all feeders have been connected to both new busbar sections) + 4 (to connect the coupling device)
        assertNotEquals(switchCountBeforeModification, switchCountAfterModification);
        assertEquals(switchCountBeforeModification + 2 * connectableCount + 4, switchCountAfterModification);
    }

    @Test
    void testConnectConnectablesWithFork() {
        Network network = createNetworkWithForkFeeder();
        network.getBusbarSection("BBS").newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(1)
                .add();
        assertEquals(3, network.getVoltageLevel("VL").getNodeBreakerView().getSwitchCount());
        new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId("VL")
                .withAlignedBusesOrBusbarCount(1)
                .withSectionCount(1)
                .withLowBusOrBusbarIndex(2)
                .withConnectExistingConnectables(true)
                .build().apply(network);
        BusbarSection newBbs = network.getBusbarSection("VL_2_1");
        assertEquals(4, newBbs.getTerminal().getVoltageLevel().getNodeBreakerView().getSwitchCount()); // One switch has been created for the fork
        assertNotNull(network.getSwitch("LD_DISCONNECTOR_4_3"));
    }

    @Test
    void testConnectConnectablesWithMultipleSections() throws IOException {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));

        new CreateCouplingDeviceBuilder().withBusOrBusbarSectionId1("bbs1").withBusOrBusbarSectionId2("bbs3").build().apply(network);

        new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId("vl1")
                .withAlignedBusesOrBusbarCount(2)
                .withSectionCount(2)
                .withSwitchKinds(SwitchKind.BREAKER)
                .withLowBusOrBusbarIndex(3)
                .withConnectExistingConnectables(true)
                .build().apply(network);
        writeXmlTest(network, "/testNetworkNodeBreakerWithNewBusbarSectionsConnectedConnectables.xiidm");

        // Check that calling again ConnectFeedersToBusbarSections does nothing
        new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(c -> !(c instanceof BusbarSection)).toList())
                .withBusbarSectionsToConnect(network.getBusbarSectionStream().filter(b -> b.getId().contains("vl1")).toList())
                .withConnectCouplingDevices(true)
                .build().apply(network);
        writeXmlTest(network, "/testNetworkNodeBreakerWithNewBusbarSectionsConnectedConnectables.xiidm");
    }

}
