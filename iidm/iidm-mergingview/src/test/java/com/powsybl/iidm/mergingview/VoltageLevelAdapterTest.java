/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ShortIdDictionary;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VoltageLevelAdapterTest {

    private MergingView mergingView;
    private Substation substation;

    @Before
    public void setUp() {
        mergingView = MergingView.create("VoltageLevelTest", "iidm");
        substation = mergingView.newSubstation()
                    .setCountry(Country.AF)
                    .setTso("tso")
                    .setName("sub")
                    .setId("subId")
                .add();
    }

    @Test
    public void baseTests() {
        // adder
        final VoltageLevel voltageLevel = substation.newVoltageLevel()
                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                    .setTopologyKind(TopologyKind.BUS_BREAKER.name())
                    .setId("bbVL")
                    .setName("bbVL_name")
                    .setNominalV(200.0)
                    .setLowVoltageLimit(100.0)
                    .setHighVoltageLimit(200.0)
                    .setEnsureIdUnicity(false)
                .add();
        assertTrue(voltageLevel instanceof VoltageLevelAdapter);
        assertSame(mergingView, voltageLevel.getNetwork());
        assertSame(substation, voltageLevel.getSubstation());
        assertSame(voltageLevel, mergingView.getIdentifiable("bbVL"));
        assertEquals(ContainerType.VOLTAGE_LEVEL, voltageLevel.getContainerType());

        // setter getter
        voltageLevel.setHighVoltageLimit(300.0);
        assertEquals(300.0, voltageLevel.getHighVoltageLimit(), 0.0);
        voltageLevel.setLowVoltageLimit(200.0);
        assertEquals(200.0, voltageLevel.getLowVoltageLimit(), 0.0);
        voltageLevel.setNominalV(500.0);
        assertEquals(500.0, voltageLevel.getNominalV(), 0.0);

        assertEquals(0, voltageLevel.getShuntCompensatorCount());
        assertEquals(0, voltageLevel.getLccConverterStationCount());

        // Topology
        assertEquals(TopologyKind.BUS_BREAKER, voltageLevel.getTopologyKind());
        voltageLevel.visitEquipments(Mockito.mock(TopologyVisitor.class));
        voltageLevel.printTopology();
        voltageLevel.printTopology(System.out, Mockito.mock(ShortIdDictionary.class));
        try {
            voltageLevel.exportTopology(Mockito.mock(Writer.class), Mockito.mock(Random.class));
            voltageLevel.exportTopology(Mockito.mock(Writer.class));
        } catch (IOException e) {
            // Ignored
        }

        // Bus
        voltageLevel.getBusBreakerView().newBus()
            .setId("B1")
            .setName("B1")
            .setEnsureIdUnicity(false)
            .add();

        // VscConverterStation
        final VscConverterStation cs1 = voltageLevel.newVscConverterStation()
            .setId("C1")
            .setName("Converter1")
            .setConnectableBus("B1")
            .setBus("B1")
            .setLossFactor(0.011f)
            .setVoltageSetpoint(405.0)
            .setVoltageRegulatorOn(true)
            .setReactivePowerSetpoint(123)
            .setEnsureIdUnicity(false)
            .add();
        assertTrue(cs1 instanceof VscConverterStationAdapter);
        assertTrue(voltageLevel.getVscConverterStations().iterator().hasNext());
        assertEquals(1, voltageLevel.getVscConverterStationCount());

        // Battery
        voltageLevel.newBattery()
                    .setId("BAT")
                    .setConnectableBus("B1")
                    .setMaxP(9999.99)
                    .setMinP(-9999.99)
                    .setP0(15)
                    .setQ0(-15)
                .add();
        voltageLevel.getBatteries().forEach(b -> {
            assertTrue(b instanceof BatteryAdapter);
            assertNotNull(b);
        });
        assertEquals(1, voltageLevel.getBatteryCount());

        // Generator
        voltageLevel.newGenerator()
                    .setId("GEN").setVoltageRegulatorOn(true)
                    .setConnectableBus("B1")
                    .setMaxP(9999.99)
                    .setMinP(-9999.99)
                    .setTargetV(25.5)
                    .setTargetP(600.05)
                    .setTargetQ(300.5)
                    .setEnsureIdUnicity(true)
                .add();
        voltageLevel.getGenerators().forEach(g -> {
            assertTrue(g instanceof GeneratorAdapter);
            assertNotNull(g);
        });
        assertEquals(1, voltageLevel.getGeneratorCount());

        // Load
        voltageLevel.newLoad()
                    .setId("LOAD")
                    .setBus("B1")
                    .setConnectableBus("B1")
                    .setP0(9999.99)
                    .setQ0(-9999.99)
                    .setLoadType(LoadType.FICTITIOUS)
                    .setEnsureIdUnicity(true)
                .add();
        voltageLevel.getLoads().forEach(l -> {
            assertTrue(l instanceof LoadAdapter);
            assertNotNull(l);
        });
        assertEquals(voltageLevel.getLoadStream().count(), voltageLevel.getLoadCount());

        // ShuntCompensator
        voltageLevel.newShuntCompensator()
                    .setId("SHUNT")
                    .setConnectableBus("B1")
                    .setBus("B1")
                    .setbPerSection(1e-5)
                    .setCurrentSectionCount(1)
                    .setMaximumSectionCount(1)
                .add();
        voltageLevel.getShuntCompensators().forEach(s -> {
            assertTrue(s instanceof ShuntCompensatorAdapter);
            assertNotNull(s);
        });
        assertEquals(voltageLevel.getShuntCompensatorStream().count(), voltageLevel.getShuntCompensatorCount());

        // StaticVarCompensator
        voltageLevel.newStaticVarCompensator()
                    .setId("svc1")
                    .setName("scv1")
                    .setConnectableBus("B1")
                    .setBus("B1")
                    .setBmin(0.0002)
                    .setBmax(0.0008)
                    .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                    .setVoltageSetPoint(390.0)
                    .setReactivePowerSetPoint(1.0)
                    .setEnsureIdUnicity(false)
                .add();
        voltageLevel.getStaticVarCompensators().forEach(s -> {
            assertTrue(s instanceof StaticVarCompensatorAdapter);
            assertNotNull(s);
        });
        assertEquals(voltageLevel.getStaticVarCompensatorStream().count(), voltageLevel.getStaticVarCompensatorCount());

        // Not implemented yet !
        // Switch
        TestUtil.notImplemented(voltageLevel::getSwitches);
        assertEquals(0, voltageLevel.getSwitchCount());
        // DanglingLine
        TestUtil.notImplemented(voltageLevel::newDanglingLine);
        TestUtil.notImplemented(voltageLevel::getDanglingLines);
        TestUtil.notImplemented(voltageLevel::getDanglingLineStream);
        TestUtil.notImplemented(voltageLevel::getDanglingLineCount);
        // LccConverterStation
        TestUtil.notImplemented(voltageLevel::newLccConverterStation);
        TestUtil.notImplemented(voltageLevel::getLccConverterStations);
        TestUtil.notImplemented(voltageLevel::getLccConverterStationStream);
        // Bus
        TestUtil.notImplemented(voltageLevel::getNodeBreakerView);
        TestUtil.notImplemented(voltageLevel::getBusView);
        // Connectables
        TestUtil.notImplemented(() -> voltageLevel.getConnectable("", null));
        TestUtil.notImplemented(() -> voltageLevel.getConnectables(null));
        TestUtil.notImplemented(() -> voltageLevel.getConnectableStream(null));
        TestUtil.notImplemented(() -> voltageLevel.getConnectableCount(null));
        TestUtil.notImplemented(voltageLevel::getConnectables);
        TestUtil.notImplemented(voltageLevel::getConnectableStream);
        TestUtil.notImplemented(voltageLevel::getConnectableCount);
    }
}
