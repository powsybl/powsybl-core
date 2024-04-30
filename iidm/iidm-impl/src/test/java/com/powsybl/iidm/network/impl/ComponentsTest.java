/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class ComponentsTest {

    /**
     *
     * <pre>
     *  Gen1                               Gen2
     *    |                                  |
     *  Bus1--Lcc1-----(DCLine)------Lcc2--Bus2
     *    |                                  |
     *  Load1                             Load2
     *
     * </pre>
     */

    @Test
    void dcLineConnected() {
        final var network = createSmallDcNetwork();
        final var connectedComponents = network.getBusView().getConnectedComponents();
        final var synchronousComponents = network.getBusView().getSynchronousComponents();

        // one connected component of size 2
        assertEquals(1, connectedComponents.size());
        connectedComponents.forEach(cc -> assertEquals(2, cc.getSize()));
        // two synchronous components of size 1
        assertEquals(2, synchronousComponents.size());
        synchronousComponents.forEach(sc -> assertEquals(1, sc.getSize()));
    }

    @Test
    void dcLineDisconnected() {
        final var network = createSmallDcNetwork();
        network.getLccConverterStation("Lcc1").getTerminal().disconnect();
        network.getLccConverterStation("Lcc2").getTerminal().disconnect();
        final var connectedComponents = network.getBusView().getConnectedComponents();
        final var synchronousComponents = network.getBusView().getSynchronousComponents();

        // two connected components of size 1
        assertEquals(2, connectedComponents.size());
        connectedComponents.forEach(cc -> assertEquals(1, cc.getSize()));
        // two synchronous components of size 1
        assertEquals(2, synchronousComponents.size());
        synchronousComponents.forEach(sc -> assertEquals(1, sc.getSize()));
    }

    private Network createSmallDcNetwork() {
        final var network = Network.create("smallDc", "test");
        final var voltageLevel1 = network.newVoltageLevel()
                .setId("voltageLevel1")
                .setNominalV(400.0d)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        final var bus1 = voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        voltageLevel1.newLoad().setId("Load1")
                .setP0(100).setQ0(20)
                .setLoadType(LoadType.UNDEFINED)
                .setBus(bus1.getId())
                .setConnectableBus(bus1.getId())
                .add();
        voltageLevel1.newGenerator().setId("Gen1")
                .setMinP(-500)
                .setMaxP(500)
                .setTargetP(150)
                .setTargetV(405)
                .setVoltageRegulatorOn(true)
                .setBus(bus1.getId())
                .setConnectableBus(bus1.getId())
                .add();
        final var voltageLevel2 = network.newVoltageLevel()
                .setId("voltageLevel2")
                .setNominalV(400.0d)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        final var bus2 = voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        voltageLevel2.newLoad().setId("Load2")
                .setP0(100).setQ0(20)
                .setLoadType(LoadType.UNDEFINED)
                .setBus(bus2.getId())
                .setConnectableBus(bus2.getId())
                .add();
        voltageLevel2.newGenerator().setId("Gen2")
                .setMinP(-500)
                .setMaxP(500)
                .setTargetP(50)
                .setTargetV(405.0d)
                .setVoltageRegulatorOn(true)
                .setBus(bus2.getId())
                .setConnectableBus(bus2.getId())
                .add();
        voltageLevel1.newLccConverterStation()
                .setId("Lcc1")
                .setBus(bus1.getId())
                .setConnectableBus(bus1.getId())
                .setPowerFactor(0.95f)
                .setLossFactor(0.99f)
                .add();
        voltageLevel2.newLccConverterStation()
                .setId("Lcc2")
                .setBus(bus2.getId())
                .setConnectableBus(bus2.getId())
                .setPowerFactor(0.95f)
                .setLossFactor(0.99f)
                .add();
        network.newHvdcLine()
                .setId("DcLine")
                .setR(1)
                .setNominalV(300)
                .setConverterStationId1("Lcc1")
                .setConverterStationId2("Lcc2")
                .setMaxP(2000)
                .setActivePowerSetpoint(50)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                .add();
        return network;
    }
}
