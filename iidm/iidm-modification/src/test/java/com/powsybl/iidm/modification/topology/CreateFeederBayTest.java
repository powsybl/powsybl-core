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
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.TOP;
import static org.junit.Assert.*;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CreateFeederBayTest extends AbstractXmlConverterTest  {

    @Test
    public void createLoadTestWithBbsId() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        NetworkModification modification = new CreateFeederBay(loadAdder, "vl1", "bb4", 115);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-load-bbs1.xml");
    }

    @Test
    public void createLoadTestWithBbsId2() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        int loadPositionOrder = TopologyModificationUtils.getLastUnusedOrderPosition(network.getVoltageLevel("vl1"), network.getBusbarSection("bbs2"));
        NetworkModification modification = new CreateFeederBay(loadAdder, "vl1", "bbs1", loadPositionOrder, TOP);
        modification.apply(network);
        assertEquals(TOP, network.getLoad("newLoad").getExtension(ConnectablePosition.class).getFeeder().getDirection());
        assertEquals(111, loadPositionOrder);
    }

    @Test
    public void createLoadTestWithBbsId3() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        int loadPositionOrder = TopologyModificationUtils.getFirstUnusedOrderPosition(network.getVoltageLevel("vl1"), network.getBusbarSection("bbs2"));
        NetworkModification modification = new CreateFeederBay(loadAdder, "vl1", "bbs2", loadPositionOrder);
        modification.apply(network);
        assertEquals(Optional.of(39), network.getLoad("newLoad").getExtension(ConnectablePosition.class).getFeeder().getOrder());
        assertEquals(39, loadPositionOrder);
    }

    @Test
    public void createLoadTestWithVoltageLevelId() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        BusbarSection bbs = TopologyModificationUtils.getFirstBusbarSection(network.getVoltageLevel("vl1"));
        assertEquals("bbs1", bbs.getId());
        NetworkModification modification = new CreateFeederBay(loadAdder, "vl1", bbs.getId(), 71);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-load-vl1.xml");
    }

    @Test
    public void testConstructor() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        CreateFeederBay modification = new CreateFeederBay(loadAdder, "vl1", "bb4", 115);
        assertEquals(loadAdder, modification.getInjectionAdder());
        assertEquals("vl1", modification.getVoltageLevelId());
        assertEquals("bb4", modification.getBbsId());
        assertEquals(115, modification.getInjectionPositionOrder());
        assertEquals(BOTTOM, modification.getInjectionDirection());
    }

    @Test
    public void testBuilder() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        CreateFeederBay modification = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withVoltageLevelId("vl1")
                .withBbsId("bb4")
                .withInjectionPositionOrder(115)
                .build();
        assertEquals(loadAdder, modification.getInjectionAdder());
        assertEquals("vl1", modification.getVoltageLevelId());
        assertEquals("bb4", modification.getBbsId());
        assertEquals(115, modification.getInjectionPositionOrder());
        assertEquals(BOTTOM, modification.getInjectionDirection());

        CreateFeederBay modification1 = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withVoltageLevelId("vl1")
                .withInjectionPositionOrder(115)
                .withInjectionDirection(TOP)
                .build();
        assertEquals(loadAdder, modification1.getInjectionAdder());
        assertEquals("vl1", modification.getVoltageLevelId());
        assertNull(modification1.getBbsId());
        assertEquals(115, modification1.getInjectionPositionOrder());
        assertEquals(TOP, modification1.getInjectionDirection());
    }

    @Test
    public void testException() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        //wrong voltageLevel
        CreateFeederBay modification = new CreateFeederBay(loadAdder, "vl", "bbs4", 115);
        assertThrows(PowsyblException.class, () -> modification.apply(network, true, Reporter.NO_OP));
        //bbsId not in voltageLevel
        CreateFeederBay modification1 = new CreateFeederBay(loadAdder, "vl1", "bbs5", 115);
        assertThrows(PowsyblException.class, () -> modification1.apply(network, true, Reporter.NO_OP));
        //wrong network
        Network network1 = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        modification1.setBbsId("bbs1");
        assertThrows(PowsyblException.class, () -> modification1.apply(network1, true, Reporter.NO_OP));
        //wrong bbsId
        modification1.setBbsId("bbs");
        assertThrows(PowsyblException.class, () -> modification1.apply(network, true, Reporter.NO_OP));
        //wrong injectionPositionOrder
        CreateFeederBay modification3 = new CreateFeederBay(loadAdder, "vl1", "bbs4", 0);
        assertThrows(PowsyblException.class, () -> modification3.apply(network, true, Reporter.NO_OP));
    }

    @Test
    public void createGeneratorTest() throws IOException {
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
        NetworkModification modification = new CreateFeederBay(generatorAdder, "vl1", "bbs1", 115);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-generator-bbs1.xml");
    }

    @Test
    public void createEquipmentsTest() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        BatteryAdder batteryAdder = network.getVoltageLevel("vl1").newBattery()
                .setId("newBattery")
                .setMaxP(9999)
                .setMinP(-9999)
                .setTargetP(100)
                .setTargetQ(50);
        NetworkModification addBatteryModification = new CreateFeederBay(batteryAdder, "vl1", "bbs1", 115);
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
        int danglingLinePositionOrder = TopologyModificationUtils.getLastUnusedOrderPosition(network.getVoltageLevel("vl2"), network.getBusbarSection("bbs5"));
        NetworkModification addDanglingLineModification = new CreateFeederBay(danglingLineAdder, "vl2", "bbs5", danglingLinePositionOrder);
        addDanglingLineModification.apply(network);
        ShuntCompensatorAdder shuntCompensatorAdder = network.getVoltageLevel("vl2").newShuntCompensator()
                        .setId("newShuntCompensator")
                        .setSectionCount(0)
                        .newLinearModel()
                            .setBPerSection(1e-5)
                            .setMaximumSectionCount(1)
                            .add();
        int shuntCompensatorPositionOrder = TopologyModificationUtils.getLastUnusedOrderPosition(network.getVoltageLevel("vl2"), network.getBusbarSection("bbs5"));
        NetworkModification addShuntCompensatorModification = new CreateFeederBay(shuntCompensatorAdder, "vl2", "bbs5", shuntCompensatorPositionOrder);
        addShuntCompensatorModification.apply(network);
        StaticVarCompensatorAdder staticVarCompensatorAdder = network.getVoltageLevel("vl1").newStaticVarCompensator()
                        .setId("newStaticVarCompensator")
                        .setBmin(0.0002)
                        .setBmax(0.0008)
                        .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                        .setVoltageSetpoint(390.0)
                        .setReactivePowerSetpoint(1.0)
                        .setEnsureIdUnicity(false);
        int staticVarCompensatorPositionOrder = TopologyModificationUtils.getLastUnusedOrderPosition(network.getVoltageLevel("vl2"), network.getBusbarSection("bbs5"));
        NetworkModification addSVCompensatorModification = new CreateFeederBay(staticVarCompensatorAdder, "vl2", "bbs5", staticVarCompensatorPositionOrder);
        addSVCompensatorModification.apply(network);
        LccConverterStationAdder lccConverterStationAdder = network.getVoltageLevel("vl2").newLccConverterStation()
                        .setId("newLccConverterStation")
                        .setLossFactor(0.011f)
                        .setPowerFactor(0.5f)
                        .setEnsureIdUnicity(false);
        int lccConverterStationPositionOrder = TopologyModificationUtils.getLastUnusedOrderPosition(network.getVoltageLevel("vl2"), network.getBusbarSection("bbs5"));
        NetworkModification addLccConverterStationModification = new CreateFeederBay(lccConverterStationAdder, "vl2", "bbs5", lccConverterStationPositionOrder);
        addLccConverterStationModification.apply(network);
        VscConverterStationAdder vscConverterStationAdder = network.getVoltageLevel("vl2").newVscConverterStation()
                .setId("newVscConverterStation")
                .setLossFactor(1.1f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .setEnsureIdUnicity(false);
        int vscConverterStationPositionOrder = TopologyModificationUtils.getLastUnusedOrderPosition(network.getVoltageLevel("vl2"), network.getBusbarSection("bbs5"));
        NetworkModification addVscConverterStationModification = new CreateFeederBay(vscConverterStationAdder, "vl2", "bbs5", vscConverterStationPositionOrder);
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
}
