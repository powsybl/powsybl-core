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
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.impl.extensions.BusbarSectionPositionImpl;
import com.powsybl.iidm.network.impl.extensions.ConnectablePositionImpl;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getUnusedOrderPositionsAfter;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getUnusedOrderPositionsBefore;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.TOP;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class CreateFeederBayTest extends AbstractModificationTest {

    @Test
    void baseNodeBreakerLoadTest() throws IOException {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        NetworkModification modification = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("bbs4")
                .withInjectionPositionOrder(115)
                .withInjectionFeederName("newLoadFeeder")
                .withInjectionDirection(BOTTOM)
                .build();
        modification.apply(network);
        writeXmlTest(network, "/network-node-breaker-with-new-load-bbs4.xml");

    }

    @Test
    void baseBusBreakerLoadTest() throws IOException {
        Network network = EurostagTutorialExample1Factory.create().setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
        LoadAdder loadAdder = network.getVoltageLevel("VLGEN").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        NetworkModification modification = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("NGEN")
                .withInjectionPositionOrder(115)
                .withInjectionFeederName("newLoadFeeder")
                .withInjectionDirection(BOTTOM)
                .build();
        modification.apply(network);
        writeXmlTest(network, "/eurostag-create-load-feeder-bay.xml");
    }

    @Test
    void getUnusedOrderPositionAfter() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        Optional<Range<Integer>> unusedOrderPositionsAfter = TopologyModificationUtils.getUnusedOrderPositionsAfter(network.getBusbarSection("bbs2"));
        assertTrue(unusedOrderPositionsAfter.isPresent());
        assertEquals(121, (int) unusedOrderPositionsAfter.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter.get().getMaximum());

        int loadPositionOrder = unusedOrderPositionsAfter.get().getMinimum();
        NetworkModification modification = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("bbs2")
                .withInjectionPositionOrder(loadPositionOrder)
                .withInjectionDirection(TOP)
                .build();
        modification.apply(network);

        ConnectablePosition<Load> newLoad = network.getLoad("newLoad").getExtension(ConnectablePosition.class);
        assertEquals(TOP, newLoad.getFeeder().getDirection());
        assertEquals(Optional.of(121), newLoad.getFeeder().getOrder());
    }

    @Test
    void getUnusedOrderPositionBefore() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        Optional<Range<Integer>> unusedOrderPositionsBefore = getUnusedOrderPositionsBefore(network.getBusbarSection("bbs2"));
        assertTrue(unusedOrderPositionsBefore.isPresent());
        assertEquals(71, (int) unusedOrderPositionsBefore.get().getMinimum());
        assertEquals(79, (int) unusedOrderPositionsBefore.get().getMaximum());
        int loadPositionOrder = unusedOrderPositionsBefore.get().getMaximum();

        NetworkModification modification = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("bbs2")
                .withInjectionPositionOrder(loadPositionOrder)
                .withInjectionDirection(BOTTOM)
                .build();
        modification.apply(network);

        ConnectablePosition<Load> newLoad = network.getLoad("newLoad").getExtension(ConnectablePosition.class);
        assertEquals(BOTTOM, newLoad.getFeeder().getDirection());
        assertEquals(Optional.of(79), newLoad.getFeeder().getOrder());
    }

    @Test
    void testException() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        ReportNode reportNode1 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestInvalidNetwork")
                .build();
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        //wrong network
        Network network1 = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        CreateFeederBay modification0 = new CreateFeederBayBuilder().
                withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("bbs1")
                .withInjectionPositionOrder(11)
                .withInjectionDirection(BOTTOM)
                .build();
        assertDoesNotThrow(() -> modification0.apply(network1, false, ReportNode.NO_OP));
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> modification0.apply(network1, true, reportNode1));
        assertEquals("Network given in parameters and in connectableAdder are different. Connectable newLoad of type LOAD was added then removed", e0.getMessage());
        assertEquals("core.iidm.modification.networkMismatch", reportNode1.getChildren().get(0).getMessageKey());

        // not found id
        ReportNode reportNode2 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestInvalidId")
                .build();
        CreateFeederBay modification1 = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("bbs")
                .withInjectionPositionOrder(115)
                .withInjectionDirection(BOTTOM)
                .build();
        assertDoesNotThrow(() -> modification1.apply(network, false, ReportNode.NO_OP));
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> modification1.apply(network, true, reportNode2));
        assertEquals("Bus or busbar section bbs not found", e1.getMessage());
        assertEquals("core.iidm.modification.notFoundBusOrBusbarSection", reportNode2.getChildren().get(0).getMessageKey());

        // wrong identifiable type
        ReportNode reportNode3 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestInvalidType")
                .build();
        CreateFeederBay modification2 = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("gen1")
                .withInjectionPositionOrder(115)
                .withInjectionDirection(BOTTOM)
                .build();
        assertDoesNotThrow(() -> modification2.apply(network, false, ReportNode.NO_OP));
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> modification2.apply(network, true, reportNode3));
        assertEquals("Unsupported type GENERATOR for identifiable gen1", e2.getMessage());
        assertEquals("core.iidm.modification.unsupportedIdentifiableType", reportNode3.getChildren().get(0).getMessageKey());
    }

    @Test
    void baseGeneratorTest() throws IOException {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        GeneratorAdder generatorAdder = network.getVoltageLevel("vl1").newGenerator()
                .setId("newGenerator")
                .newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withTargetValue(25.5).add()
                .setMaxP(9999)
                .setMinP(-9999)
                .setTargetV(25.5)
                .setTargetP(600)
                .setTargetQ(300)
                .setRatedS(10)
                .setEnergySource(EnergySource.NUCLEAR)
                .setEnsureIdUnicity(true);
        NetworkModification modification = new CreateFeederBayBuilder()
                .withInjectionAdder(generatorAdder)
                .withBusOrBusbarSectionId("bbs1")
                .withInjectionPositionOrder(71)
                .withInjectionDirection(BOTTOM)
                .build();
        modification.apply(network);
        writeXmlTest(network, "/network-node-breaker-with-new-generator-bbs1.xml");
    }

    @Test
    void baseEquipmentsTest() throws IOException {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        BatteryAdder batteryAdder = network.getVoltageLevel("vl1").newBattery()
                .setId("newBattery")
                .setMaxP(9999)
                .setMinP(-9999)
                .setTargetP(100)
                .setTargetQ(50);
        NetworkModification addBatteryModification = new CreateFeederBayBuilder()
                .withInjectionAdder(batteryAdder)
                .withBusOrBusbarSectionId("bbs1")
                .withInjectionPositionOrder(71)
                .withInjectionDirection(BOTTOM)
                .build();
        addBatteryModification.apply(network);
        BoundaryLineAdder boundaryLineAdder = network.getVoltageLevel("vl2").newBoundaryLine()
                        .setId("newBoundaryLine")
                        .setR(10)
                        .setX(20)
                        .setG(30)
                        .setB(40)
                        .setP0(50)
                        .setQ0(60)
                        .setEnsureIdUnicity(false);
        Optional<Range<Integer>> unusedOrderPositionsAfter0 = TopologyModificationUtils.getUnusedOrderPositionsAfter(network.getBusbarSection("bbs5"));
        assertTrue(unusedOrderPositionsAfter0.isPresent());
        assertEquals(81, (int) unusedOrderPositionsAfter0.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter0.get().getMaximum());
        int boundaryLinePositionOrder = unusedOrderPositionsAfter0.get().getMinimum();
        NetworkModification addBoundaryLineModification = new CreateFeederBayBuilder()
                .withInjectionAdder(boundaryLineAdder)
                .withBusOrBusbarSectionId("bbs5")
                .withInjectionPositionOrder(boundaryLinePositionOrder)
                .withInjectionDirection(BOTTOM)
                .build();
        addBoundaryLineModification.apply(network);

        ShuntCompensatorAdder shuntCompensatorAdder = network.getVoltageLevel("vl2").newShuntCompensator()
                        .setId("newShuntCompensator")
                        .setSectionCount(0)
                        .newLinearModel()
                            .setBPerSection(1e-5)
                            .setMaximumSectionCount(1)
                            .add();
        Optional<Range<Integer>> unusedOrderPositionsAfter1 = TopologyModificationUtils.getUnusedOrderPositionsAfter(network.getBusbarSection("bbs5"));
        assertTrue(unusedOrderPositionsAfter1.isPresent());
        assertEquals(82, (int) unusedOrderPositionsAfter1.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter1.get().getMaximum());
        int shuntCompensatorPositionOrder = unusedOrderPositionsAfter1.get().getMinimum();
        NetworkModification addShuntCompensatorModification = new CreateFeederBayBuilder()
                .withInjectionAdder(shuntCompensatorAdder)
                .withBusOrBusbarSectionId("bbs5")
                .withInjectionPositionOrder(shuntCompensatorPositionOrder)
                .withInjectionDirection(BOTTOM)
                .build();
        addShuntCompensatorModification.apply(network);

        StaticVarCompensatorAdder staticVarCompensatorAdder = network.getVoltageLevel("vl2").newStaticVarCompensator()
                        .setId("newStaticVarCompensator")
                        .setBmin(0.0002)
                        .setBmax(0.0008)
                        .newVoltageRegulation()
                            .withMode(RegulationMode.VOLTAGE)
                            .withRegulating(true)
                            .withTargetValue(390.0)
                            .add()
                        .setTargetQ(1.0)
                        .setEnsureIdUnicity(false);
        Optional<Range<Integer>> unusedOrderPositionsAfter2 = TopologyModificationUtils.getUnusedOrderPositionsAfter(network.getBusbarSection("bbs5"));
        assertTrue(unusedOrderPositionsAfter2.isPresent());
        assertEquals(83, (int) unusedOrderPositionsAfter2.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter2.get().getMaximum());
        int staticVarCompensatorPositionOrder = unusedOrderPositionsAfter2.get().getMinimum();
        NetworkModification addSVCompensatorModification = new CreateFeederBayBuilder()
                .withInjectionAdder(staticVarCompensatorAdder)
                .withBusOrBusbarSectionId("bbs5")
                .withInjectionPositionOrder(staticVarCompensatorPositionOrder)
                .withInjectionDirection(BOTTOM)
                .build();
        addSVCompensatorModification.apply(network);

        LccConverterStationAdder lccConverterStationAdder = network.getVoltageLevel("vl2").newLccConverterStation()
                        .setId("newLccConverterStation")
                        .setLossFactor(0.011f)
                        .setPowerFactor(0.5f)
                        .setEnsureIdUnicity(false);
        Optional<Range<Integer>> unusedOrderPositionsAfter3 = TopologyModificationUtils.getUnusedOrderPositionsAfter(network.getBusbarSection("bbs5"));
        assertTrue(unusedOrderPositionsAfter3.isPresent());
        assertEquals(84, (int) unusedOrderPositionsAfter3.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter3.get().getMaximum());
        int lccConverterStationPositionOrder = unusedOrderPositionsAfter3.get().getMinimum();
        NetworkModification addLccConverterStationModification = new CreateFeederBayBuilder()
                .withInjectionAdder(lccConverterStationAdder)
                .withBusOrBusbarSectionId("bbs5")
                .withInjectionPositionOrder(lccConverterStationPositionOrder)
                .withInjectionDirection(BOTTOM)
                .build();
        addLccConverterStationModification.apply(network);

        VscConverterStationAdder vscConverterStationAdder = network.getVoltageLevel("vl2").newVscConverterStation()
                .setId("newVscConverterStation")
                .setLossFactor(1.1f)
                .newVoltageRegulation()
                    .withTargetValue(405.0)
                    .withMode(RegulationMode.VOLTAGE)
                    .add()
                .setEnsureIdUnicity(false);
        Optional<Range<Integer>> unusedOrderPositionsAfter4 = TopologyModificationUtils.getUnusedOrderPositionsAfter(network.getBusbarSection("bbs5"));
        assertTrue(unusedOrderPositionsAfter4.isPresent());
        assertEquals(85, (int) unusedOrderPositionsAfter4.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter4.get().getMaximum());
        int vscConverterStationPositionOrder = unusedOrderPositionsAfter4.get().getMinimum();
        NetworkModification addVscConverterStationModification = new CreateFeederBayBuilder()
                .withInjectionAdder(vscConverterStationAdder)
                .withBusOrBusbarSectionId("bbs5")
                .withInjectionPositionOrder(vscConverterStationPositionOrder)
                .withInjectionDirection(BOTTOM)
                .build();
        addVscConverterStationModification.apply(network);
        writeXmlTest(network, "/network-node-breaker-with-new-equipments-bbs1.xml");
    }

    @Test
    void testWithoutExtension() {
        Network network = Network.read("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("bbs4")
                .withInjectionPositionOrder(115)
                .build()
                .apply(network, true, ReportNode.NO_OP);

        Load load = network.getLoad("newLoad");
        assertNotNull(load);

        ConnectablePosition<Load> position = load.getExtension(ConnectablePosition.class);
        assertNull(position);

        BusbarSection bbs = network.getBusbarSection("bbs2");
        PowsyblException exception = assertThrows(PowsyblException.class, () -> getUnusedOrderPositionsBefore(bbs));
        assertEquals("busbarSection has no BusbarSectionPosition extension", exception.getMessage());

        PowsyblException exception2 = assertThrows(PowsyblException.class, () -> getUnusedOrderPositionsAfter(bbs));
        assertEquals("busbarSection has no BusbarSectionPosition extension", exception2.getMessage());
    }

    @Test
    void testExtensionsAreCreatedIfNoOtherConnectables() {
        Network network = Network.read("network_one_voltage_level.xiidm", getClass().getResourceAsStream("/network_one_voltage_level.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("VLTEST").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("VLTEST12")
                .withInjectionPositionOrder(10)
                .build()
                .apply(network, true, ReportNode.NO_OP);

        Load load = network.getLoad("newLoad");
        assertNotNull(load);
        ConnectablePosition<Load> position = load.getExtension(ConnectablePosition.class);
        assertNotNull(position);
        assertEquals(BOTTOM, position.getFeeder().getDirection());
        assertEquals(Optional.of(10), position.getFeeder().getOrder());
    }

    @Test
    void testNoExtensionCreatedIfOrderPositionIsOutOfRange() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);

        // order position is too high
        new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("bbs1")
                .withInjectionPositionOrder(101)
                .build().apply(network);
        assertNull(network.getLoad("newLoad").getExtension(ConnectablePosition.class));

        // order position is too low
        loadAdder.setId("newLoad2");
        new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("bbs4")
                .withInjectionPositionOrder(61)
                .build().apply(network);
        assertNull(network.getLoad("newLoad2").getExtension(ConnectablePosition.class));

        // Add extra bbs to leave no space on bbs1
        addIncoherentPositionBusBarSection(network, loadAdder);
        new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("bbs1")
                .withInjectionPositionOrder(51)
                .build().apply(network);
        assertNull(network.getLoad("newLoad3").getExtension(ConnectablePosition.class));

        loadAdder.setId("newLoad4");
        new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("bbs3")
                .withInjectionPositionOrder(101)
                .build().apply(network);
        assertNull(network.getLoad("newLoad4").getExtension(ConnectablePosition.class));
    }

    @Test
    void testNoConnectableCreatedIfOrderPositionIsOutOfRangeAndLogOrThrowIfIncorrectPositionOrder() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
            .setId("newLoad")
            .setLoadType(LoadType.UNDEFINED)
            .setP0(0)
            .setQ0(0);

        // order position is too high
        new CreateFeederBayBuilder()
            .withInjectionAdder(loadAdder)
            .withBusOrBusbarSectionId("bbs1")
            .withInjectionPositionOrder(101)
            .withLogOrThrowIfIncorrectPositionOrder(true)
            .build().apply(network);
        assertNull(network.getLoad("newLoad"));

        // order position is too low
        loadAdder.setId("newLoad2");
        new CreateFeederBayBuilder()
            .withInjectionAdder(loadAdder)
            .withBusOrBusbarSectionId("bbs4")
            .withInjectionPositionOrder(61)
            .withLogOrThrowIfIncorrectPositionOrder(true)
            .build().apply(network);
        assertNull(network.getLoad("newLoad2"));

        // Add extra bbs to leave no space on bbs1
        addIncoherentPositionBusBarSection(network, loadAdder);
        new CreateFeederBayBuilder()
            .withInjectionAdder(loadAdder)
            .withBusOrBusbarSectionId("bbs1")
            .withInjectionPositionOrder(51)
            .withLogOrThrowIfIncorrectPositionOrder(true)
            .build().apply(network);
        assertNull(network.getLoad("newLoad3"));

        loadAdder.setId("newLoad4");
        new CreateFeederBayBuilder()
            .withInjectionAdder(loadAdder)
            .withBusOrBusbarSectionId("bbs3")
            .withInjectionPositionOrder(101)
            .withLogOrThrowIfIncorrectPositionOrder(true)
            .build().apply(network);
        assertNull(network.getLoad("newLoad4"));
    }

    private static void addIncoherentPositionBusBarSection(Network network, LoadAdder loadAdder) {
        loadAdder.setId("newLoad3");
        VoltageLevel vl1 = network.getVoltageLevel("vl1");
        BusbarSection bbs = vl1.getNodeBreakerView().newBusbarSection().setId("extraBbs").setNode(40).add();
        Load load = vl1.newLoad().setId("extraLoad").setP0(0).setQ0(0).setNode(41).add();
        vl1.getNodeBreakerView().newSwitch().setId("extraDisconnector").setKind(SwitchKind.DISCONNECTOR).setNode1(40).setNode2(41).add();

        bbs.addExtension(BusbarSectionPosition.class, new BusbarSectionPositionImpl(bbs, 1, 0));
        ConnectablePosition<Load> connectablePosition = new ConnectablePositionImpl<>(load,
                new ConnectablePositionImpl.FeederImpl("", 79), null, null, null);
        load.addExtension(ConnectablePosition.class, connectablePosition);
    }

    @Test
    void testExceptionInvalidValue() {
        ComputationManager computationManager = LocalComputationManager.getDefault();
        Network network = Network.create("test", "test");
        VoltageLevel vl = network.newVoltageLevel().setId("vl").setNominalV(1.0).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        BusbarSection bbs = vl.getNodeBreakerView().newBusbarSection().setId("bbs").setNode(0).add();
        LoadAdder loadAdder = vl.newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(10)
                .setQ0(10);

        //negative order position
        ReportNode reportNode1 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestNegativeOrderPosition")
                .build();
        CreateFeederBay negativeOrderCreate = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId(bbs.getId())
                .withInjectionPositionOrder(-2)
                .build();
        assertDoesNotThrow(() -> negativeOrderCreate.apply(network, false, computationManager, ReportNode.NO_OP));
        PowsyblException eNeg = assertThrows(PowsyblException.class, () -> negativeOrderCreate.apply(network, true, computationManager, reportNode1));
        assertEquals("Position order is negative for attachment in node-breaker voltage level vl: -2", eNeg.getMessage());
        assertEquals("core.iidm.modification.unexpectedNegativePositionOrder", reportNode1.getChildren().get(0).getMessageKey());

        //null order position
        ReportNode reportNode2 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestNullOrderPosition")
                .build();
        CreateFeederBay nullOrderCreate = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId(bbs.getId())
                .build();
        assertDoesNotThrow(() -> nullOrderCreate.apply(network, false, computationManager, ReportNode.NO_OP));
        PowsyblException eNull = assertThrows(PowsyblException.class, () -> nullOrderCreate.apply(network, true, computationManager, reportNode2));
        assertEquals("Position order is null for attachment in node-breaker voltage level vl", eNull.getMessage());
        assertEquals("core.iidm.modification.unexpectedNullPositionOrder", reportNode2.getChildren().get(0).getMessageKey());
    }

    @Test
    void testCreateLoadWithReportNode() throws IOException {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestCreateLoad")
                .build();
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("bbs4")
                .withInjectionPositionOrder(115)
                .withInjectionFeederName("newLoadFeeder")
                .withInjectionDirection(BOTTOM)
                .build().apply(network, reportNode);
        testReportNode(reportNode, "/reportNode/create-load-NB-report.txt");
    }

    @Test
    void testCreateLoadWithReportNodeWithoutExtensions() throws IOException {
        Network network = Network.read("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestCreateLoadWithoutExtensions")
                .build();
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("bbs4")
                .withInjectionPositionOrder(115)
                .build()
                .apply(network, reportNode);
        testReportNode(reportNode, "/reportNode/create-load-NB-without-extensions-report.txt");
    }

    @Test
    void testGetName() {
        Network network = Network.read("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
            .setId("newLoad")
            .setLoadType(LoadType.UNDEFINED)
            .setP0(0)
            .setQ0(0);
        AbstractNetworkModification networkModification = new CreateFeederBayBuilder()
            .withInjectionAdder(loadAdder)
            .withBusOrBusbarSectionId("bbs4")
            .withInjectionPositionOrder(115)
            .build();
        assertEquals("CreateFeederBay", networkModification.getName());
    }

    @Test
    void testHasImpact() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
            .setId("newLoad")
            .setLoadType(LoadType.UNDEFINED)
            .setP0(0)
            .setQ0(0);
        NetworkModification modification1 = new CreateFeederBayBuilder()
            .withInjectionAdder(loadAdder)
            .withBusOrBusbarSectionId("bbs4")
            .withInjectionPositionOrder(115)
            .withInjectionFeederName("newLoadFeeder")
            .withInjectionDirection(BOTTOM)
            .build();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new CreateFeederBayBuilder()
            .withInjectionAdder(loadAdder)
            .withBusOrBusbarSectionId("bbs4")
            .withInjectionFeederName("newLoadFeeder")
            .withInjectionDirection(BOTTOM)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification2.hasImpactOnNetwork(network));

        NetworkModification modification3 = new CreateFeederBayBuilder()
            .withInjectionAdder(loadAdder)
            .withBusOrBusbarSectionId("bbs4")
            .withInjectionPositionOrder(-5)
            .withInjectionFeederName("newLoadFeeder")
            .withInjectionDirection(BOTTOM)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification3.hasImpactOnNetwork(network));

        NetworkModification modification4 = new CreateFeederBayBuilder()
            .withInjectionAdder(loadAdder)
            .withBusOrBusbarSectionId("WRONG_BBS")
            .withInjectionPositionOrder(115)
            .withInjectionFeederName("newLoadFeeder")
            .withInjectionDirection(BOTTOM)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification4.hasImpactOnNetwork(network));

        Network networkBus = EurostagTutorialExample1Factory.create().setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
        NetworkModification modification5 = new CreateFeederBayBuilder()
            .withInjectionAdder(loadAdder)
            .withBusOrBusbarSectionId("NGEN")
            .withInjectionPositionOrder(115)
            .withInjectionFeederName("newLoadFeeder")
            .withInjectionDirection(BOTTOM)
            .build();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification5.hasImpactOnNetwork(networkBus));
    }

    @Test
    void testCreateBranchFeederBaysWithInternalLine() {
        Network network = createNetworkWithInternalLine();

        // Modification
        GeneratorAdder adder = network.getVoltageLevel("S1VL2").newGenerator()
            .setId("GEN")
            .setTargetP(0.0)
            .setTargetQ(0)
            .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(100.0)
                .add()
            .setMinP(0)
            .setMaxP(1000);
        new CreateFeederBayBuilder()
            .withInjectionPositionOrder(55)
            .withInjectionDirection(BOTTOM)
            .withInjectionAdder(adder)
            .withBusOrBusbarSectionId("S1VL1_BBS2")
            .build()
            .apply(network, true, ReportNode.NO_OP);
        Generator generator = network.getGenerator("GEN");
        ConnectablePosition<Generator> position = generator.getExtension(ConnectablePosition.class);
        assertNotNull(position);
        assertEquals(BOTTOM, position.getFeeder().getDirection());
        assertEquals(Optional.of(55), position.getFeeder().getOrder());
    }

    private static Network createNetworkWithInternalLine() {
        Network network = Network.create("testNetwork", "test");

        // Substations
        createSubstation1(network);
        createSubstation2(network);

        // Create elements
        createLine1(network);
        createTwt1(network);
        createTwt2(network);
        createLine2(network);
        createTwt3(network);
        createLine3(network);

        return network;
    }

    private static void createTwt3(Network network) {
        Substation s1 = network.getSubstation("S1");
        VoltageLevel s1vl1 = network.getVoltageLevel("S1VL1");
        VoltageLevel s1vl2 = network.getVoltageLevel("S1VL2");

        // Connect a TWT between s1vl1 and s1vl2
        createSwitch(s1vl1, "S1VL1_BBS2_TWT3_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 1, 11);
        createSwitch(s1vl1, "S1VL1_BBS4_TWT3_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 3, 11);
        createSwitch(s1vl1, "S1VL1_TWT3_BREAKER", SwitchKind.BREAKER, false, 11, 18);
        createSwitch(s1vl2, "S1VL2_BBS_TWT3_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 3);
        createSwitch(s1vl2, "S1VL2_TWT3_BREAKER", SwitchKind.BREAKER, false, 3, 4);
        TwoWindingsTransformer twt = s1.newTwoWindingsTransformer()
            .setId("TWT3")
            .setR(2.0)
            .setX(14.745)
            .setG(0.0)
            .setB(3.2E-5)
            .setRatedU1(400.0)
            .setRatedU2(225.0)
            .setNode1(18)
            .setVoltageLevel1("S1VL1")
            .setNode2(4)
            .setVoltageLevel2("S1VL2")
            .add();
        twt.newPhaseTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(2)
            .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
            .setRegulating(false)
            .setRegulationTerminal(twt.getTerminal(TwoSides.ONE))
            .beginStep().setR(39.78473).setX(29.784725).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-42.8).endStep()
            .beginStep().setR(31.720245).setX(21.720242).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-40.18).endStep()
            .beginStep().setR(23.655737).setX(13.655735).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-37.54).endStep()
            .add();
        twt.newRatioTapChanger()
            .beginStep().setR(0.0).setX(0.0).setB(0.0).setG(0.0).setRho(0.85).endStep()
            .beginStep().setR(0.0).setX(0.0).setB(0.0).setG(0.0).setRho(1).endStep()
            .beginStep().setR(0.0).setX(0.0).setB(0.0).setG(0.0).setRho(1.15).endStep()
            .setLowTapPosition(0)
            .setTapPosition(1)
            .setLoadTapChangingCapabilities(true)
            .setRegulating(true)
            .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
            .setRegulationValue(225.0)
            .setTargetDeadband(0)
            .setRegulationTerminal(twt.getTerminal(TwoSides.ONE))
            .add();
        twt.getTerminal1().setP(-80.0).setQ(-10.0);
        twt.getTerminal2().setP(80.0809).setQ(5.4857);
        twt.newExtension(ConnectablePositionAdder.class)
            .newFeeder1()
            .withName("TWT3")
            .withDirection(ConnectablePosition.Direction.BOTTOM)
            .withOrder(50)
            .add()
            .newFeeder2()
            .withName("TWT3")
            .withDirection(ConnectablePosition.Direction.TOP)
            .withOrder(20)
            .add()
            .add();
    }

    private static void createTwt2(Network network) {
        Substation s1 = network.getSubstation("S1");
        VoltageLevel s1vl1 = network.getVoltageLevel("S1VL1");
        VoltageLevel s1vl3 = network.getVoltageLevel("S1VL3");

        // Connect a TWT between s1vl1 and s1vl3
        createSwitch(s1vl1, "S1VL1_BBS5_TWT2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 4, 9);
        createSwitch(s1vl1, "S1VL1_TWT2_BREAKER", SwitchKind.BREAKER, false, 9, 16);
        createSwitch(s1vl3, "S1VL3_BBS_TWT2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s1vl3, "S1VL3_TWT2_BREAKER", SwitchKind.BREAKER, false, 1, 2);
        TwoWindingsTransformer twt = s1.newTwoWindingsTransformer()
            .setId("TWT2")
            .setR(2.0)
            .setX(14.745)
            .setG(0.0)
            .setB(3.2E-5)
            .setRatedU1(400.0)
            .setRatedU2(110.0)
            .setNode1(16)
            .setVoltageLevel1("S1VL1")
            .setNode2(2)
            .setVoltageLevel2("S1VL3")
            .add();
        twt.newPhaseTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(2)
            .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
            .setRegulating(false)
            .setRegulationTerminal(twt.getTerminal(TwoSides.ONE))
            .beginStep().setR(39.78473).setX(29.784725).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-42.8).endStep()
            .beginStep().setR(31.720245).setX(21.720242).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-40.18).endStep()
            .beginStep().setR(23.655737).setX(13.655735).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-37.54).endStep()
            .add();
        twt.newRatioTapChanger()
            .beginStep().setR(0.0).setX(0.0).setB(0.0).setG(0.0).setRho(0.85).endStep()
            .beginStep().setR(0.0).setX(0.0).setB(0.0).setG(0.0).setRho(1).endStep()
            .beginStep().setR(0.0).setX(0.0).setB(0.0).setG(0.0).setRho(1.15).endStep()
            .setLowTapPosition(0)
            .setTapPosition(1)
            .setLoadTapChangingCapabilities(true)
            .setRegulating(true)
            .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
            .setRegulationValue(110.0)
            .setTargetDeadband(0)
            .setRegulationTerminal(twt.getTerminal(TwoSides.ONE))
            .add();
        twt.getTerminal1().setP(-80.0).setQ(-10.0);
        twt.getTerminal2().setP(80.0809).setQ(5.4857);
        twt.newExtension(ConnectablePositionAdder.class)
            .newFeeder1()
            .withName("TWT2")
            .withDirection(ConnectablePosition.Direction.BOTTOM)
            .withOrder(30)
            .add()
            .newFeeder2()
            .withName("TWT2")
            .withDirection(ConnectablePosition.Direction.TOP)
            .withOrder(10)
            .add()
            .add();
    }

    private static void createTwt1(Network network) {
        Substation s1 = network.getSubstation("S1");
        VoltageLevel s1vl1 = network.getVoltageLevel("S1VL1");
        VoltageLevel s1vl2 = network.getVoltageLevel("S1VL2");

        // Connect a TWT between s1vl1 and s1vl2
        createSwitch(s1vl1, "S1VL1_BBS1_TWT1_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 0, 8);
        createSwitch(s1vl1, "S1VL1_BBS3_TWT1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 2, 8);
        createSwitch(s1vl1, "S1VL1_TWT1_BREAKER", SwitchKind.BREAKER, false, 8, 15);
        createSwitch(s1vl2, "S1VL2_BBS_TWT1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s1vl2, "S1VL2_TWT1_BREAKER", SwitchKind.BREAKER, false, 1, 2);
        TwoWindingsTransformer twt = s1.newTwoWindingsTransformer()
            .setId("TWT1")
            .setR(2.0)
            .setX(14.745)
            .setG(0.0)
            .setB(3.2E-5)
            .setRatedU1(400.0)
            .setRatedU2(225.0)
            .setNode1(15)
            .setVoltageLevel1("S1VL1")
            .setNode2(2)
            .setVoltageLevel2("S1VL2")
            .add();
        twt.newPhaseTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(2)
            .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
            .setRegulating(false)
            .setRegulationTerminal(twt.getTerminal(TwoSides.ONE))
            .beginStep().setR(39.78473).setX(29.784725).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-42.8).endStep()
            .beginStep().setR(31.720245).setX(21.720242).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-40.18).endStep()
            .beginStep().setR(23.655737).setX(13.655735).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-37.54).endStep()
            .add();
        twt.newRatioTapChanger()
            .beginStep().setR(0.0).setX(0.0).setB(0.0).setG(0.0).setRho(0.85).endStep()
            .beginStep().setR(0.0).setX(0.0).setB(0.0).setG(0.0).setRho(1).endStep()
            .beginStep().setR(0.0).setX(0.0).setB(0.0).setG(0.0).setRho(1.15).endStep()
            .setLowTapPosition(0)
            .setTapPosition(1)
            .setLoadTapChangingCapabilities(true)
            .setRegulating(true)
            .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
            .setRegulationValue(225.0)
            .setTargetDeadband(0)
            .setRegulationTerminal(twt.getTerminal(TwoSides.ONE))
            .add();
        twt.getTerminal1().setP(-80.0).setQ(-10.0);
        twt.getTerminal2().setP(80.0809).setQ(5.4857);
        twt.newExtension(ConnectablePositionAdder.class)
            .newFeeder1()
            .withName("TWT1")
            .withDirection(ConnectablePosition.Direction.BOTTOM)
            .withOrder(20)
            .add()
            .newFeeder2()
            .withName("TWT1")
            .withDirection(ConnectablePosition.Direction.TOP)
            .withOrder(10)
            .add()
            .add();
    }

    private static void createLine3(Network network) {
        VoltageLevel s1vl1 = network.getVoltageLevel("S1VL1");
        VoltageLevel s2vl1 = network.getVoltageLevel("S2VL1");

        // Connect a line between s1vl1 and s2vl1
        createSwitch(s1vl1, "S1VL1_BBS1_LINE3_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 1, 13);
        createSwitch(s1vl1, "S1VL1_BBS3_LINE3_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 3, 13);
        createSwitch(s1vl1, "S1VL1_LINE3_BREAKER", SwitchKind.BREAKER, false, 13, 20);
        createSwitch(s2vl1, "S2VL1_BBS_LINE3_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 3);
        createSwitch(s2vl1, "S2VL1_LINE3_BREAKER", SwitchKind.BREAKER, false, 3, 4);
        Line line1 = network.newLine()
            .setId("LINE3")
            .setR(0.009999999)
            .setX(19.100000024)
            .setG1(0.0)
            .setB1(0.0)
            .setG2(0.0)
            .setB2(0.0)
            .setNode1(20)
            .setVoltageLevel1("S1VL1")
            .setNode2(4)
            .setVoltageLevel2("S2VL1")
            .add();
        line1.newExtension(ConnectablePositionAdder.class)
            .newFeeder1()
            .withName("LINE3")
            .withDirection(ConnectablePosition.Direction.TOP)
            .withOrder(70)
            .add()
            .newFeeder2()
            .withName("LINE3")
            .withDirection(ConnectablePosition.Direction.TOP)
            .withOrder(20)
            .add()
            .add();
    }

    private static void createLine2(Network network) {
        VoltageLevel s1vl1 = network.getVoltageLevel("S1VL1");

        // Connect an internal line in s1vl1
        createSwitch(s1vl1, "S1VL1_BBS1_LINE2_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 0, 10);
        createSwitch(s1vl1, "S1VL1_BBS3_LINE2_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 2, 10);
        createSwitch(s1vl1, "S1VL1_BBS5_LINE2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 4, 10);
        createSwitch(s1vl1, "S1VL1_BBS5_LINE2_BREAKER", SwitchKind.BREAKER, false, 10, 17);
        createSwitch(s1vl1, "S1VL1_BBS2_LINE2_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 1, 12);
        createSwitch(s1vl1, "S1VL1_BBS4_LINE2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 3, 12);
        createSwitch(s1vl1, "S1VL1_LINE2_BREAKER", SwitchKind.BREAKER, false, 12, 19);
        Line line2 = network.newLine()
            .setId("LINE2")
            .setR(0.009999999)
            .setX(19.100000024)
            .setG1(0.0)
            .setB1(0.0)
            .setG2(0.0)
            .setB2(0.0)
            .setNode1(17)
            .setVoltageLevel1("S1VL1")
            .setNode2(19)
            .setVoltageLevel2("S1VL1")
            .add();
        line2.newExtension(ConnectablePositionAdder.class)
            .newFeeder1()
            .withName("LINE2")
            .withDirection(ConnectablePosition.Direction.TOP)
            .withOrder(40)
            .add()
            .newFeeder2()
            .withName("LINE2")
            .withDirection(ConnectablePosition.Direction.TOP)
            .withOrder(60)
            .add()
            .add();
    }

    private static void createLine1(Network network) {
        VoltageLevel s1vl1 = network.getVoltageLevel("S1VL1");
        VoltageLevel s2vl1 = network.getVoltageLevel("S2VL1");

        // Connect a line between s1vl1 and s2vl1
        createSwitch(s1vl1, "S1VL1_BBS1_LINE1_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 0, 7);
        createSwitch(s1vl1, "S1VL1_BBS3_LINE1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 2, 7);
        createSwitch(s1vl1, "S1VL1_LINE1_BREAKER", SwitchKind.BREAKER, false, 7, 14);
        createSwitch(s2vl1, "S2VL1_BBS_LINE1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s2vl1, "S2VL1_LINE1_BREAKER", SwitchKind.BREAKER, false, 1, 2);
        Line line1 = network.newLine()
            .setId("LINE1")
            .setR(0.009999999)
            .setX(19.100000024)
            .setG1(0.0)
            .setB1(0.0)
            .setG2(0.0)
            .setB2(0.0)
            .setNode1(14)
            .setVoltageLevel1("S1VL1")
            .setNode2(2)
            .setVoltageLevel2("S2VL1")
            .add();
        line1.newExtension(ConnectablePositionAdder.class)
            .newFeeder1()
            .withName("LINE1")
            .withDirection(ConnectablePosition.Direction.BOTTOM)
            .withOrder(10)
            .add()
            .newFeeder2()
            .withName("LINE1")
            .withDirection(ConnectablePosition.Direction.TOP)
            .withOrder(10)
            .add()
            .add();
    }

    private static void createSubstation2(Network network) {
        // First voltage level
        Substation s2 = network.newSubstation()
            .setId("S2")
            .add();
        VoltageLevel s2vl1 = s2.newVoltageLevel()
            .setId("S2VL1")
            .setNominalV(400.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        BusbarSection busbarSectionS2VL1 = s2vl1.getNodeBreakerView().newBusbarSection()
            .setId("S2VL1_BBS")
            .setName("S2VL1_BBS")
            .setNode(0)
            .add();
        busbarSectionS2VL1.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();

        // Second voltage level
        VoltageLevel s2vl2 = s2.newVoltageLevel()
            .setId("S2VL2")
            .setNominalV(230.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        BusbarSection busbarSectionS2VL2 = s2vl2.getNodeBreakerView().newBusbarSection()
            .setId("S2VL2_BBS")
            .setName("S2VL2_BBS")
            .setNode(0)
            .add();
        busbarSectionS2VL2.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();
    }

    private static void createSubstation1(Network network) {
        // First voltage level
        Substation s1 = network.newSubstation()
            .setId("S1")
            .add();
        VoltageLevel s1vl1 = s1.newVoltageLevel()
            .setId("S1VL1")
            .setNominalV(400.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        BusbarSection busbarSectionS1VL21 = s1vl1.getNodeBreakerView().newBusbarSection()
            .setId("S1VL1_BBS1")
            .setName("S1VL1_BBS1")
            .setNode(0)
            .add();
        busbarSectionS1VL21.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();
        BusbarSection busbarSectionS1VL22 = s1vl1.getNodeBreakerView().newBusbarSection()
            .setId("S1VL1_BBS2")
            .setName("S1VL1_BBS2")
            .setNode(1)
            .add();
        busbarSectionS1VL22.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(2)
            .add();
        BusbarSection busbarSectionS1VL23 = s1vl1.getNodeBreakerView().newBusbarSection()
            .setId("S1VL1_BBS3")
            .setName("S1VL1_BBS3")
            .setNode(2)
            .add();
        busbarSectionS1VL23.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(2)
            .withSectionIndex(1)
            .add();
        BusbarSection busbarSectionS1VL24 = s1vl1.getNodeBreakerView().newBusbarSection()
            .setId("S1VL1_BBS4")
            .setName("S1VL1_BBS4")
            .setNode(3)
            .add();
        busbarSectionS1VL24.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(2)
            .withSectionIndex(2)
            .add();
        BusbarSection busbarSectionS1VL25 = s1vl1.getNodeBreakerView().newBusbarSection()
            .setId("S1VL1_BBS5")
            .setName("S1VL1_BBS5")
            .setNode(4)
            .add();
        busbarSectionS1VL25.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(3)
            .withSectionIndex(1)
            .add();

        // Second voltage level
        VoltageLevel s1vl2 = s1.newVoltageLevel()
            .setId("S1VL2")
            .setNominalV(225.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        BusbarSection busbarSectionS1VL2 = s1vl2.getNodeBreakerView().newBusbarSection()
            .setId("S1VL2_BBS")
            .setName("S1VL2_BBS")
            .setNode(0)
            .add();
        busbarSectionS1VL2.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();

        // Third voltage level
        VoltageLevel s1vl3 = s1.newVoltageLevel()
            .setId("S1VL3")
            .setNominalV(110.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        BusbarSection busbarSectionS1VL3 = s1vl3.getNodeBreakerView().newBusbarSection()
            .setId("S1VL3_BBS")
            .setName("S1VL3_BBS")
            .setNode(0)
            .add();
        busbarSectionS1VL3.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();

        // Connect busbar sections
        createSwitch(s1vl1, "S1VL1_BBS1_BBS2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s1vl1, "S1VL1_BBS3_BBS4_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 2, 3);

        // Coupling device
        createSwitch(s1vl1, "S1VL1_BBS2_COUPLER_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 1, 5);
        createSwitch(s1vl1, "S1VL1_BBS4_COUPLER_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 3, 6);
        createSwitch(s1vl1, "S1VL1_COUPLER", SwitchKind.BREAKER, false, 5, 6);
    }

    private static void createSwitch(VoltageLevel vl, String id, SwitchKind kind, boolean open, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
            .setId(id)
            .setName(id)
            .setKind(kind)
            .setRetained(kind.equals(SwitchKind.BREAKER))
            .setOpen(open)
            .setFictitious(false)
            .setNode1(node1)
            .setNode2(node2)
            .add();
    }
}
