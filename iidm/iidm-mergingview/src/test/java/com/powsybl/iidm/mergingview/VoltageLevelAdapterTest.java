/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import com.powsybl.iidm.network.util.ShortIdDictionary;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import java.util.Objects;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VoltageLevelAdapterTest {

    private MergingView mergingView;
    private Network networkRef;

    @Before
    public void setUp() {
        mergingView = MergingView.create("VoltageLevelTest", "iidm");
        networkRef = NoEquipmentNetworkFactory.create();
        mergingView.merge(networkRef);
    }

    @Test
    public void baseTests() {
        final String vlId = "vl1";
        VoltageLevel vlExpected = networkRef.getVoltageLevel(vlId);
        VoltageLevel vlActual = mergingView.getVoltageLevel(vlId);

        // Setter / Getter
        assertTrue(vlActual instanceof VoltageLevelAdapter);
        assertSame(mergingView, vlActual.getNetwork());
        assertSame(mergingView.getSubstation("sub"), vlActual.getSubstation());
        assertSame(mergingView.getIdentifiable(vlId), vlActual);
        assertEquals(vlExpected.getContainerType(), vlActual.getContainerType());

        vlActual.setHighVoltageLimit(300.0);
        assertEquals(300.0, vlActual.getHighVoltageLimit(), 0.0);
        vlActual.setLowVoltageLimit(200.0);
        assertEquals(200.0, vlActual.getLowVoltageLimit(), 0.0);
        vlActual.setNominalV(500.0);
        assertEquals(500.0, vlActual.getNominalV(), 0.0);
        assertEquals(vlExpected.getShuntCompensatorCount(), vlActual.getShuntCompensatorCount());
        assertEquals(vlExpected.getLccConverterStationCount(), vlActual.getLccConverterStationCount());

        // VscConverterStation
        vlActual.newVscConverterStation()
                    .setId("C1")
                    .setName("Converter1")
                    .setConnectableBus("busA")
                    .setBus("busA")
                    .setLossFactor(0.011f)
                    .setVoltageSetpoint(405.0)
                    .setVoltageRegulatorOn(true)
                    .setReactivePowerSetpoint(123)
                    .setEnsureIdUnicity(false)
                 .add();
        vlActual.getVscConverterStations().forEach(b -> {
            assertTrue(b instanceof VscConverterStationAdapter);
            assertNotNull(b);
        });
        assertEquals(vlExpected.getVscConverterStationCount(), vlActual.getVscConverterStationCount());
        assertEquals(vlExpected.getVscConverterStationStream().count(), vlActual.getVscConverterStationStream().count());

        // Battery
        vlActual.newBattery()
                    .setId("BAT")
                    .setConnectableBus("busA")
                    .setBus("busA")
                    .setMaxP(9999.99)
                    .setMinP(-9999.99)
                    .setP0(15)
                    .setQ0(-15)
                .add();
        vlActual.getBatteries().forEach(b -> {
            assertTrue(b instanceof BatteryAdapter);
            assertNotNull(b);
        });
        assertEquals(vlExpected.getBatteryCount(), vlActual.getBatteryCount());
        assertEquals(vlExpected.getBatteryStream().count(), vlActual.getBatteryStream().count());

        // Generator
        vlActual.newGenerator()
                    .setId("GEN").setVoltageRegulatorOn(true)
                    .setConnectableBus("busA")
                    .setBus("busA")
                    .setMaxP(9999.99)
                    .setMinP(-9999.99)
                    .setTargetV(25.5)
                    .setTargetP(600.05)
                    .setTargetQ(300.5)
                    .setEnsureIdUnicity(true)
                .add();
        vlActual.getGenerators().forEach(g -> {
            assertTrue(g instanceof GeneratorAdapter);
            assertNotNull(g);
        });
        assertEquals(vlExpected.getGeneratorCount(), vlActual.getGeneratorCount());
        assertEquals(vlExpected.getGeneratorStream().count(), vlActual.getGeneratorStream().count());

        // Load
        vlActual.newLoad()
                    .setId("LOAD")
                    .setConnectableBus("busA")
                    .setBus("busA")
                    .setP0(9999.99)
                    .setQ0(-9999.99)
                    .setLoadType(LoadType.FICTITIOUS)
                    .setEnsureIdUnicity(true)
                .add();
        vlActual.getLoads().forEach(l -> {
            assertTrue(l instanceof LoadAdapter);
            assertNotNull(l);
        });
        assertEquals(vlExpected.getLoadCount(), vlActual.getLoadCount());
        assertEquals(vlExpected.getLoadStream().count(), vlActual.getLoadStream().count());

        // ShuntCompensator
        vlActual.newShuntCompensator()
                    .setId("SHUNT")
                    .setConnectableBus("busA")
                    .setBus("busA")
                    .setbPerSection(1e-5)
                    .setCurrentSectionCount(1)
                    .setMaximumSectionCount(1)
                .add();
        vlActual.getShuntCompensators().forEach(s -> {
            assertTrue(s instanceof ShuntCompensatorAdapter);
            assertNotNull(s);
        });
        assertEquals(vlExpected.getShuntCompensatorCount(), vlActual.getShuntCompensatorCount());
        assertEquals(vlExpected.getShuntCompensatorStream().count(), vlActual.getShuntCompensatorStream().count());

        // StaticVarCompensator
        vlActual.newStaticVarCompensator()
                    .setId("svc1")
                    .setName("scv1")
                    .setConnectableBus("busA")
                    .setBus("busA")
                    .setBmin(0.0002)
                    .setBmax(0.0008)
                    .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                    .setRegulatingTerminal(mergingView.getLoad("LOAD").getTerminal())
                    .setVoltageSetPoint(390.0)
                    .setReactivePowerSetPoint(1.0)
                    .setEnsureIdUnicity(false)
                .add();
        vlActual.getStaticVarCompensators().forEach(s -> {
            assertTrue(s instanceof StaticVarCompensatorAdapter);
            assertNotNull(s);
        });
        assertEquals(vlExpected.getStaticVarCompensatorCount(), vlActual.getStaticVarCompensatorCount());
        assertEquals(vlExpected.getStaticVarCompensatorStream().count(), vlActual.getStaticVarCompensatorStream().count());

        // LccConverterStation
        vlActual.newLccConverterStation()
                    .setId("C2")
                    .setName("Converter2")
                    .setConnectableBus("busA")
                    .setBus("busA")
                    .setLossFactor(0.011f)
                    .setPowerFactor(0.5f)
                    .setEnsureIdUnicity(false)
                .add();
        vlActual.getLccConverterStations().forEach(b -> {
            assertTrue(b instanceof LccConverterStationAdapter);
            assertNotNull(b);
        });
        assertEquals(vlExpected.getLccConverterStationCount(), vlActual.getLccConverterStationCount());
        assertEquals(vlExpected.getLccConverterStationStream().count(), vlActual.getLccConverterStationStream().count());

        // Switch
        vlActual.getSwitches().forEach(s -> {
            assertTrue(s instanceof SwitchAdapter);
            assertNotNull(s);
        });
        assertEquals(vlExpected.getSwitchCount(), vlActual.getSwitchCount());

        // DanglingLine
        vlActual.newDanglingLine()
                .setId("DLL1")
                .setName("DLL1")
                .setR(1)
                .setX(2)
                .setG(3)
                .setB(4)
                .setP0(5)
                .setQ0(6)
                .setUcteXnodeCode("code")
                .setBus("busA")
                .setConnectableBus("busA")
            .add();
        vlActual.getDanglingLines().forEach(s -> {
            assertTrue(s instanceof DanglingLineAdapter);
            assertNotNull(s);
        });
        assertEquals(vlExpected.getDanglingLineCount(), vlActual.getDanglingLineCount());
        assertEquals(vlExpected.getDanglingLineStream().count(), vlActual.getDanglingLineStream().count());

        // Topology : Bus kind
        assertEquals(vlExpected.getTopologyKind(), vlActual.getTopologyKind());
        TopologyVisitor visitor = mock(TopologyVisitor.class);
        vlActual.visitEquipments(visitor);
        verify(visitor, times(1)).visitBattery(any(Battery.class));
        verify(visitor, times(1)).visitGenerator(any(Generator.class));
        verify(visitor, times(1)).visitLoad(any(Load.class));
        verify(visitor, times(1)).visitShuntCompensator(any(ShuntCompensator.class));
        verify(visitor, times(1)).visitStaticVarCompensator(any(StaticVarCompensator.class));
        verify(visitor, times(2)).visitHvdcConverterStation(any(HvdcConverterStation.class));
        verify(visitor, times(1)).visitDanglingLine(any(DanglingLine.class));

        vlActual.printTopology();
        vlActual.printTopology(System.out, mock(ShortIdDictionary.class));
        try {
            vlActual.exportTopology(mock(Writer.class), mock(Random.class));
            vlActual.exportTopology(mock(Writer.class));
        } catch (IOException e) {
            // Ignored
        }

        // Not implemented yet !
        // Connectables
        TestUtil.notImplemented(() -> vlActual.getConnectable("", null));
        TestUtil.notImplemented(() -> vlActual.getConnectables(null));
        TestUtil.notImplemented(() -> vlActual.getConnectableStream(null));
        TestUtil.notImplemented(() -> vlActual.getConnectableCount(null));
        TestUtil.notImplemented(vlActual::getConnectables);
        TestUtil.notImplemented(vlActual::getConnectableStream);
        TestUtil.notImplemented(vlActual::getConnectableCount);
    }

    @Test
    public void busViewTests() {
        final VoltageLevel voltageLevelBB = mergingView.getVoltageLevel("vl1");
        final VoltageLevel.BusView bv = voltageLevelBB.getBusView();
        assertTrue(bv instanceof VoltageLevelAdapter.BusViewAdapter);

        // Not implemented
        TestUtil.notImplemented(bv::getBuses);
        TestUtil.notImplemented(bv::getBusStream);
        TestUtil.notImplemented(() -> bv.getBus(""));
        TestUtil.notImplemented(() -> bv.getMergedBus(""));
    }

    @Test
    public void busBreakerViewTests() {
        final VoltageLevel voltageLevelBB = mergingView.getVoltageLevel("vl1");
        final VoltageLevel.BusBreakerView bbv = voltageLevelBB.getBusBreakerView();
        assertTrue(bbv instanceof VoltageLevelAdapter.BusBreakerViewAdapter);

        final String switchId = "BBV_SW1";
        final String busId1 = "BBV_B1";
        final String busId2 = "BBV_B2";
        final Bus busB1 = voltageLevelBB.getBusBreakerView().newBus()
                    .setId(busId1)
                    .setName(busId1)
                    .setEnsureIdUnicity(false)
                .add();
        final Bus busB2 = voltageLevelBB.getBusBreakerView().newBus()
                    .setId(busId2)
                    .setName(busId2)
                    .setEnsureIdUnicity(false)
                .add();
        final Switch switchSW1 = bbv.newSwitch()
                                        .setId(switchId)
                                        .setName(switchId)
                                        .setEnsureIdUnicity(true)
                                        .setBus1(busId1)
                                        .setBus2(busId2)
                                        .setOpen(true)
                                        .setFictitious(true)
                                    .add();

        assertSame(busB1, bbv.getBus(busId1));
        bbv.getBuses().forEach(s -> {
            assertTrue(s instanceof BusAdapter);
            assertNotNull(s);
        });
        assertEquals(3, bbv.getBusStream().count());

        assertSame(switchSW1, bbv.getSwitch(switchId));
        bbv.getSwitches().forEach(s -> {
            assertTrue(s instanceof SwitchAdapter);
            assertNotNull(s);
        });
        assertEquals(bbv.getSwitchCount(), bbv.getSwitchStream().count());

        assertSame(busB1, bbv.getBus1(switchId));
        assertSame(busB2, bbv.getBus2(switchId));

        // Not implemented
        TestUtil.notImplemented(() -> bbv.removeBus(""));
        TestUtil.notImplemented(bbv::removeAllBuses);
        TestUtil.notImplemented(() -> bbv.removeSwitch(""));
        TestUtil.notImplemented(bbv::removeAllSwitches);
    }

    @Test
    public void nodeBreakerViewTests() {
        // adder
        final VoltageLevel voltageLevelNB = mergingView.getSubstation("sub").newVoltageLevel()
                                                                                    .setTopologyKind(TopologyKind.NODE_BREAKER)
                                                                                    .setTopologyKind(TopologyKind.NODE_BREAKER.name())
                                                                                    .setId("nbVL")
                                                                                    .setName("nbVL_name")
                                                                                    .setNominalV(200.0)
                                                                                    .setLowVoltageLimit(100.0)
                                                                                    .setHighVoltageLimit(200.0)
                                                                                    .setEnsureIdUnicity(false)
                                                                                 .add();

        final VoltageLevel.NodeBreakerView nbv = voltageLevelNB.getNodeBreakerView();
        assertTrue(nbv instanceof VoltageLevelAdapter.NodeBreakerViewAdapter);

        nbv.newInternalConnection()
               .setNode1(0)
               .setNode2(1)
            .add();
        nbv.getInternalConnections().forEach(Assert::assertNotNull);
        assertEquals(nbv.getInternalConnectionCount(), nbv.getInternalConnectionStream().count());

        final Switch switchSW1 = nbv.newSwitch()
                                        .setId("NBV_SW1")
                                        .setName("NBV_SW1")
                                        .setEnsureIdUnicity(true)
                                        .setNode1(0)
                                        .setNode2(1)
                                        .setKind(SwitchKind.LOAD_BREAK_SWITCH)
                                        .setKind(SwitchKind.LOAD_BREAK_SWITCH.name())
                                        .setOpen(true)
                                        .setRetained(true)
                                        .setFictitious(true)
                                    .add();

        final Switch breakerBK1 = nbv.newBreaker()
                                         .setId("NBV_BK1")
                                         .setName("NBV_BK1")
                                         .setEnsureIdUnicity(true)
                                         .setNode1(0)
                                         .setNode2(1)
                                         .setOpen(true)
                                         .setRetained(true)
                                         .setFictitious(true)
                                      .add();

        final Switch breakerDIS1 = nbv.newDisconnector()
                                         .setId("NBV_DIS1")
                                         .setName("NBV_DIS1")
                                         .setEnsureIdUnicity(true)
                                         .setNode1(0)
                                         .setNode2(1)
                                         .setOpen(true)
                                         .setRetained(true)
                                         .setFictitious(true)
                                      .add();

        assertEquals(0, nbv.getNode1("NBV_SW1"));
        assertEquals(1, nbv.getNode2("NBV_SW1"));
        assertSame(nbv.getTerminal(0), nbv.getTerminal1("NBV_SW1"));
        assertSame(nbv.getTerminal(1), nbv.getTerminal2("NBV_SW1"));

        assertSame(switchSW1, nbv.getSwitch("NBV_SW1"));
        assertSame(breakerBK1, nbv.getSwitch("NBV_BK1"));
        assertSame(breakerDIS1, nbv.getSwitch("NBV_DIS1"));
        nbv.getSwitches().forEach(sw -> {
            if (Objects.nonNull(sw)) {
                assertTrue(sw instanceof SwitchAdapter);
            }
        });

        assertEquals(nbv.getSwitchCount(), nbv.getSwitchStream().count());

        final BusbarSection sjb1 = nbv.newBusbarSection()
                                          .setId("NBV_SJB1")
                                          .setName("NBV_SJB1")
                                          .setNode(0)
                                      .add();
        assertSame(sjb1, nbv.getBusbarSection("NBV_SJB1"));
        nbv.getBusbarSections().forEach(Assert::assertNotNull);
        assertEquals(nbv.getBusbarSectionCount(), nbv.getBusbarSectionStream().count());

        // Not implemented
        TestUtil.notImplemented(() -> nbv.removeSwitch(""));
        TestUtil.notImplemented(() -> nbv.traverse(0, null));
    }
}
