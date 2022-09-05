/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.NetworkXml;
import org.apache.commons.lang3.Range;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getUnusedOrderPositionsAfter;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getUnusedOrderPositionsBefore;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.TOP;
import static org.junit.Assert.*;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CreateFeederBayTest extends AbstractXmlConverterTest  {

    @Test
    public void baseLoadTest() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        NetworkModification modification = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBbsId("bbs4")
                .withInjectionPositionOrder(115)
                .withInjectionDirection(BOTTOM)
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-load-bbs4.xml");
    }

    @Test
    public void getUnusedOrderPositionAfter() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        Optional<Range<Integer>> unusedOrderPositionsAfter = TopologyModificationUtils.getUnusedOrderPositionsAfter(
                network.getVoltageLevel("vl1"), network.getBusbarSection("bbs2"));
        assertTrue(unusedOrderPositionsAfter.isPresent());
        assertEquals(121, (int) unusedOrderPositionsAfter.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter.get().getMaximum());

        int loadPositionOrder = unusedOrderPositionsAfter.get().getMinimum();
        NetworkModification modification = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBbsId("bbs1")
                .withInjectionPositionOrder(loadPositionOrder)
                .withInjectionDirection(TOP)
                .build();
        modification.apply(network);

        ConnectablePosition<Load> newLoad = network.getLoad("newLoad").getExtension(ConnectablePosition.class);
        assertEquals(TOP, newLoad.getFeeder().getDirection());
        assertEquals(Optional.of(121), newLoad.getFeeder().getOrder());
    }

    @Test
    public void getUnusedOrderPositionBefore() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        Optional<Range<Integer>> unusedOrderPositionsBefore = getUnusedOrderPositionsBefore(
                network.getVoltageLevel("vl1"), network.getBusbarSection("bbs2"));
        assertTrue(unusedOrderPositionsBefore.isPresent());
        assertEquals(71, (int) unusedOrderPositionsBefore.get().getMinimum());
        assertEquals(79, (int) unusedOrderPositionsBefore.get().getMaximum());
        int loadPositionOrder = unusedOrderPositionsBefore.get().getMaximum();

        NetworkModification modification = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBbsId("bbs2")
                .withInjectionPositionOrder(loadPositionOrder)
                .withInjectionDirection(BOTTOM)
                .build();
        modification.apply(network);

        ConnectablePosition<Load> newLoad = network.getLoad("newLoad").getExtension(ConnectablePosition.class);
        assertEquals(BOTTOM, newLoad.getFeeder().getDirection());
        assertEquals(Optional.of(79), newLoad.getFeeder().getOrder());
    }

    @Test
    public void testException() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        //wrong network
        Network network1 = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        CreateFeederBay modification0 = new CreateFeederBayBuilder().
                withInjectionAdder(loadAdder)
                .withBbsId("bbs1")
                .withInjectionPositionOrder(115)
                .withInjectionDirection(BOTTOM)
                .build();
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> modification0.apply(network1, true, Reporter.NO_OP));
        assertEquals("Network given in parameters and in injectionAdder are different. Injection was added then removed", e0.getMessage());

        //wrong bbsId
        CreateFeederBay modification1 = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBbsId("bbs")
                .withInjectionPositionOrder(115)
                .withInjectionDirection(BOTTOM)
                .build();
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> modification1.apply(network, true, Reporter.NO_OP));
        assertEquals("Busbar section bbs not found.", e1.getMessage());

        //wrong injectionPositionOrder
        CreateFeederBay modification2 = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBbsId("bbs4")
                .withInjectionPositionOrder(0)
                .withInjectionDirection(BOTTOM)
                .build();
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> modification2.apply(network, true, Reporter.NO_OP));
        assertEquals("InjectionPositionOrder 0 already taken.", e2.getMessage());
    }

    @Test
    public void baseGeneratorTest() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
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
                .withBbsId("bbs1")
                .withInjectionPositionOrder(115)
                .withInjectionDirection(BOTTOM)
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-generator-bbs1.xml");
    }

    @Test
    public void baseEquipmentsTest() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        BatteryAdder batteryAdder = network.getVoltageLevel("vl1").newBattery()
                .setId("newBattery")
                .setMaxP(9999)
                .setMinP(-9999)
                .setTargetP(100)
                .setTargetQ(50);
        NetworkModification addBatteryModification = new CreateFeederBayBuilder()
                .withInjectionAdder(batteryAdder)
                .withBbsId("bbs1")
                .withInjectionPositionOrder(115)
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
        Optional<Range<Integer>> unusedOrderPositionsAfter0 = TopologyModificationUtils.getUnusedOrderPositionsAfter(
                network.getVoltageLevel("vl2"), network.getBusbarSection("bbs5"));
        assertTrue(unusedOrderPositionsAfter0.isPresent());
        assertEquals(81, (int) unusedOrderPositionsAfter0.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter0.get().getMaximum());
        int danglingLinePositionOrder = unusedOrderPositionsAfter0.get().getMinimum();
        NetworkModification addDanglingLineModification = new CreateFeederBayBuilder()
                .withInjectionAdder(danglingLineAdder)
                .withBbsId("bbs5")
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
        Optional<Range<Integer>> unusedOrderPositionsAfter1 = TopologyModificationUtils.getUnusedOrderPositionsAfter(
                network.getVoltageLevel("vl2"), network.getBusbarSection("bbs5"));
        assertTrue(unusedOrderPositionsAfter1.isPresent());
        assertEquals(82, (int) unusedOrderPositionsAfter1.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter1.get().getMaximum());
        int shuntCompensatorPositionOrder = unusedOrderPositionsAfter1.get().getMinimum();
        NetworkModification addShuntCompensatorModification = new CreateFeederBayBuilder()
                .withInjectionAdder(shuntCompensatorAdder)
                .withBbsId("bbs5")
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
        Optional<Range<Integer>> unusedOrderPositionsAfter2 = TopologyModificationUtils.getUnusedOrderPositionsAfter(
                network.getVoltageLevel("vl2"), network.getBusbarSection("bbs5"));
        assertTrue(unusedOrderPositionsAfter2.isPresent());
        assertEquals(83, (int) unusedOrderPositionsAfter2.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter2.get().getMaximum());
        int staticVarCompensatorPositionOrder = unusedOrderPositionsAfter2.get().getMinimum();
        NetworkModification addSVCompensatorModification = new CreateFeederBayBuilder()
                .withInjectionAdder(staticVarCompensatorAdder)
                .withBbsId("bbs5")
                .withInjectionPositionOrder(staticVarCompensatorPositionOrder)
                .withInjectionDirection(BOTTOM)
                .build();
        addSVCompensatorModification.apply(network);

        LccConverterStationAdder lccConverterStationAdder = network.getVoltageLevel("vl2").newLccConverterStation()
                        .setId("newLccConverterStation")
                        .setLossFactor(0.011f)
                        .setPowerFactor(0.5f)
                        .setEnsureIdUnicity(false);
        Optional<Range<Integer>> unusedOrderPositionsAfter3 = TopologyModificationUtils.getUnusedOrderPositionsAfter(
                network.getVoltageLevel("vl2"), network.getBusbarSection("bbs5"));
        assertTrue(unusedOrderPositionsAfter3.isPresent());
        assertEquals(84, (int) unusedOrderPositionsAfter3.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter3.get().getMaximum());
        int lccConverterStationPositionOrder = unusedOrderPositionsAfter3.get().getMinimum();
        NetworkModification addLccConverterStationModification = new CreateFeederBayBuilder()
                .withInjectionAdder(lccConverterStationAdder)
                .withBbsId("bbs5")
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
        Optional<Range<Integer>> unusedOrderPositionsAfter4 = TopologyModificationUtils.getUnusedOrderPositionsAfter(
                network.getVoltageLevel("vl2"), network.getBusbarSection("bbs5"));
        assertTrue(unusedOrderPositionsAfter4.isPresent());
        assertEquals(85, (int) unusedOrderPositionsAfter4.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter4.get().getMaximum());
        int vscConverterStationPositionOrder = unusedOrderPositionsAfter4.get().getMinimum();
        NetworkModification addVscConverterStationModification = new CreateFeederBayBuilder()
                .withInjectionAdder(vscConverterStationAdder)
                .withBbsId("bbs5")
                .withInjectionPositionOrder(vscConverterStationPositionOrder)
                .withInjectionDirection(BOTTOM)
                .build();
        addVscConverterStationModification.apply(network);
        roundTripTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-equipments-bbs1.xml");
    }

    @Test
    public void testFeederOrders() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        Set<Integer> feederOrders = TopologyModificationUtils.getFeederPositions(network.getVoltageLevel("vl1"));
        assertEquals(13, feederOrders.size());
        assertTrue(feederOrders.contains(100));
        Set<Integer> feederOrders2 = TopologyModificationUtils.getFeederPositions(network.getVoltageLevel("vl2"));
        assertEquals(9, feederOrders2.size());
        Set<Integer> feederOrders3 = TopologyModificationUtils.getFeederPositions(network.getVoltageLevel("vlSubst2"));
        assertEquals(1, feederOrders3.size());
    }

    @Test
    public void testWithoutExtension() {
        Network network = Importers.loadNetwork("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBbsId("bbs4")
                .withInjectionPositionOrder(115)
                .build()
                .apply(network, true, Reporter.NO_OP);

        Load load = network.getLoad("newLoad");
        assertNotNull(load);

        ConnectablePosition<Load> position = load.getExtension(ConnectablePosition.class);
        assertNull(position);

        VoltageLevel vl = network.getVoltageLevel("vl1");
        BusbarSection bbs = network.getBusbarSection("bbs2");
        PowsyblException exception = assertThrows(PowsyblException.class, () -> getUnusedOrderPositionsBefore(vl, bbs));
        assertEquals("busbarSection has no BusbarSectionPosition extension", exception.getMessage());

        PowsyblException exception2 = assertThrows(PowsyblException.class, () -> getUnusedOrderPositionsAfter(vl, bbs));
        assertEquals("busbarSection has no BusbarSectionPosition extension", exception.getMessage());
    }
}
