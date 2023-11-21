/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.impl.extensions.BusbarSectionPositionImpl;
import com.powsybl.iidm.network.impl.extensions.ConnectablePositionImpl;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.apache.commons.lang3.Range;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getUnusedOrderPositionsAfter;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getUnusedOrderPositionsBefore;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.testReporter;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.TOP;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class CreateFeederBayTest extends AbstractSerDeTest {

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
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/network-node-breaker-with-new-load-bbs4.xml");
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
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/eurostag-create-load-feeder-bay.xml");
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
        ReporterModel reporter1 = new ReporterModel("reportTestInvalidNetwork", "Testing reporter if network mismatch");
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
                .withInjectionPositionOrder(115)
                .withInjectionDirection(BOTTOM)
                .build();
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> modification0.apply(network1, true, reporter1));
        assertEquals("Network given in parameters and in connectableAdder are different. Connectable was added then removed", e0.getMessage());
        assertEquals("networkMismatch", reporter1.getReports().iterator().next().getReportKey());

        // not found id
        ReporterModel reporter2 = new ReporterModel("reportTestInvalidId", "Testing reporter if wrong feeder id");
        CreateFeederBay modification1 = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("bbs")
                .withInjectionPositionOrder(115)
                .withInjectionDirection(BOTTOM)
                .build();
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> modification1.apply(network, true, reporter2));
        assertEquals("Bus or busbar section bbs not found", e1.getMessage());
        assertEquals("notFoundBusOrBusbarSection", reporter2.getReports().iterator().next().getReportKey());

        // wrong identifiable type
        ReporterModel reporter3 = new ReporterModel("reportTestInvalidType", "Testing reporter if wrong feeder type");
        CreateFeederBay modification2 = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId("gen1")
                .withInjectionPositionOrder(115)
                .withInjectionDirection(BOTTOM)
                .build();
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> modification2.apply(network, true, reporter3));
        assertEquals("Unsupported type GENERATOR for identifiable gen1", e2.getMessage());
        assertEquals("unsupportedIdentifiableType", reporter3.getReports().iterator().next().getReportKey());
    }

    @Test
    void baseGeneratorTest() throws IOException {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        GeneratorAdder generatorAdder = network.getVoltageLevel("vl1").newGenerator()
                .setId("newGenerator")
                .setVoltageRegulatorOn(true)
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
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/network-node-breaker-with-new-generator-bbs1.xml");
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
        DanglingLineAdder danglingLineAdder = network.getVoltageLevel("vl2").newDanglingLine()
                        .setId("newDanglingLine")
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
        int danglingLinePositionOrder = unusedOrderPositionsAfter0.get().getMinimum();
        NetworkModification addDanglingLineModification = new CreateFeederBayBuilder()
                .withInjectionAdder(danglingLineAdder)
                .withBusOrBusbarSectionId("bbs5")
                .withInjectionPositionOrder(danglingLinePositionOrder)
                .withInjectionDirection(BOTTOM)
                .build();
        addDanglingLineModification.apply(network);

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
                        .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                        .setVoltageSetpoint(390.0)
                        .setReactivePowerSetpoint(1.0)
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
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
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
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/network-node-breaker-with-new-equipments-bbs1.xml");
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
                .apply(network, true, Reporter.NO_OP);

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
                .apply(network, true, Reporter.NO_OP);

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
        ReporterModel reporter1 = new ReporterModel("reportTestNegativeOrderPosition", "Testing reporter for a load creation with negative position order");
        CreateFeederBay negativeOrderCreate = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId(bbs.getId())
                .withInjectionPositionOrder(-2)
                .build();
        PowsyblException eNeg = assertThrows(PowsyblException.class, () -> negativeOrderCreate.apply(network, true, computationManager, reporter1));
        assertEquals("Position order is negative for attachment in node-breaker voltage level vl: -2", eNeg.getMessage());
        assertEquals("unexpectedNegativePositionOrder", reporter1.getReports().iterator().next().getReportKey());

        //null order position
        ReporterModel reporter2 = new ReporterModel("reportTestNullOrderPosition", "Testing reporter for a load creation with null order position");
        CreateFeederBay nullOrderCreate = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBusOrBusbarSectionId(bbs.getId())
                .build();
        PowsyblException eNull = assertThrows(PowsyblException.class, () -> nullOrderCreate.apply(network, true, computationManager, reporter2));
        assertEquals("Position order is null for attachment in node-breaker voltage level vl", eNull.getMessage());
        assertEquals("unexpectedNullPositionOrder", reporter2.getReports().iterator().next().getReportKey());
    }

    @Test
    void testCreateLoadWithReporter() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        ReporterModel reporter = new ReporterModel("reportTestCreateLoad", "Testing reporter for a load creation");
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
                .build().apply(network, reporter);
        testReporter(reporter, "/reporter/create-load-NB-report.txt");
    }

    @Test
    void testCreateLoadWithReporterWithoutExtensions() {
        Network network = Network.read("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        ReporterModel reporter = new ReporterModel("reportTestCreateLoadWithoutExtensions", "Testing reporter for a load creation in a network without extensions");
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
                .apply(network, reporter);
        testReporter(reporter, "/reporter/create-load-NB-without-extensions-report.txt");
    }
}
