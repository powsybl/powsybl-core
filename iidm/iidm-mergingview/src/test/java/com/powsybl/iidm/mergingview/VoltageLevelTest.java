/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.powsybl.iidm.network.ContainerType;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class VoltageLevelTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MergingView mergingView;
    private Substation substation;

    @Before
    public void setUp() {
        mergingView = MergingView.create();
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
        assertEquals(TopologyKind.BUS_BREAKER, voltageLevel.getTopologyKind());

        // Not implemented yet !

        // Generator
        TestUtil.notImplemented(voltageLevel::newGenerator);
        TestUtil.notImplemented(voltageLevel::getGenerators);
        TestUtil.notImplemented(voltageLevel::getGeneratorStream);
        assertEquals(0, voltageLevel.getGeneratorCount());
        // Battery
        TestUtil.notImplemented(voltageLevel::newBattery);
        TestUtil.notImplemented(voltageLevel::getBatteries);
        TestUtil.notImplemented(voltageLevel::getBatteryStream);
        assertEquals(0, voltageLevel.getBatteryCount());
        // Load
        TestUtil.notImplemented(voltageLevel::newLoad);
        TestUtil.notImplemented(voltageLevel::getLoads);
        TestUtil.notImplemented(voltageLevel::getLoadStream);
        assertEquals(0, voltageLevel.getLoadCount());
        // Switch
        TestUtil.notImplemented(voltageLevel::getSwitches);
        assertEquals(0, voltageLevel.getSwitchCount());
        // ShuntCompensator
        TestUtil.notImplemented(voltageLevel::newShuntCompensator);
        TestUtil.notImplemented(voltageLevel::getShuntCompensators);
        TestUtil.notImplemented(voltageLevel::getShuntCompensatorStream);
        // DanglingLine
        TestUtil.notImplemented(voltageLevel::newDanglingLine);
        TestUtil.notImplemented(voltageLevel::getDanglingLines);
        TestUtil.notImplemented(voltageLevel::getDanglingLineStream);
        TestUtil.notImplemented(voltageLevel::getDanglingLineCount);
        // StaticVarCompensator
        TestUtil.notImplemented(voltageLevel::newStaticVarCompensator);
        TestUtil.notImplemented(voltageLevel::getStaticVarCompensators);
        TestUtil.notImplemented(voltageLevel::getStaticVarCompensatorStream);
        assertEquals(0, voltageLevel.getStaticVarCompensatorCount());
        // VscConverterStation
        TestUtil.notImplemented(voltageLevel::newVscConverterStation);
        TestUtil.notImplemented(voltageLevel::getVscConverterStations);
        TestUtil.notImplemented(voltageLevel::getVscConverterStationStream);
        assertEquals(0, voltageLevel.getVscConverterStationCount());
        // LccConverterStation
        TestUtil.notImplemented(voltageLevel::newLccConverterStation);
        TestUtil.notImplemented(voltageLevel::getLccConverterStations);
        TestUtil.notImplemented(voltageLevel::getLccConverterStationStream);
        // Bus
        TestUtil.notImplemented(voltageLevel::getNodeBreakerView);
        TestUtil.notImplemented(voltageLevel::getBusBreakerView);
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
