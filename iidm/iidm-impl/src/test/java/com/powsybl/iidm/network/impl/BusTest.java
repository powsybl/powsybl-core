/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class BusTest {

    @Test
    public void testSetterGetter() {
        Network network = NetworkFactory.create("test", "test");
        Substation substation = network.newSubstation()
                                    .setCountry(Country.AF)
                                    .setTso("tso")
                                    .setName("sub")
                                    .setId("subId")
                                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                                        .setTopologyKind(TopologyKind.BUS_BREAKER)
                                        .setId("bbVL")
                                        .setName("bbVL_name")
                                        .setNominalV(200.0f)
                                    .add();
        // ConfiguredBus
        Bus bus = voltageLevel.getBusBreakerView()
                    .newBus()
                    .setName("bus1")
                    .setId("bus1")
                .add();
        LccConverterStation lccConverterStation = voltageLevel.newLccConverterStation()
                                                    .setId("lcc")
                                                    .setName("lcc")
                                                    .setBus("bus1")
                                                    .setLossFactor(0.011f)
                                                    .setPowerFactor(0.5f)
                                                    .setConnectableBus("bus1")
                                                .add();
        VscConverterStation vscConverterStation = voltageLevel.newVscConverterStation()
                                                    .setId("vsc")
                                                    .setName("vsc")
                                                    .setBus("bus1")
                                                    .setLossFactor(0.011f)
                                                    .setVoltageRegulatorOn(false)
                                                    .setReactivePowerSetpoint(1.0)
                                                    .setConnectableBus("bus1")
                                                .add();
        assertEquals(HvdcConverterStation.HvdcType.LCC, lccConverterStation.getHvdcType());
        assertEquals(HvdcConverterStation.HvdcType.VSC, vscConverterStation.getHvdcType());
        double p1 = 1.0;
        double q1 = 2.0;
        double p2 = 10.0;
        double q2 = 20.0;
        lccConverterStation.getTerminal().setP(p1);
        lccConverterStation.getTerminal().setQ(q1);
        vscConverterStation.getTerminal().setP(p2);
        vscConverterStation.getTerminal().setQ(q2);

        assertSame(voltageLevel, bus.getVoltageLevel());
        try {
            bus.setV(-1.0);
            fail();
        } catch (ValidationException ignored) {
        }
        bus.setV(200.0);
        assertEquals(200.0, bus.getV(), 0.0);
        bus.setAngle(30.0);
        assertEquals(30.0, bus.getAngle(), 0.0);

        assertEquals(p1 + p2, bus.getP(), 0.0);
        assertEquals(q1 + q2, bus.getQ(), 0.0);
    }
}
