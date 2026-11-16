/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;

import java.time.ZonedDateTime;

/**
 * Test networks for the UCTE exporter tests.
 *
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
final class UcteExporterTestNetworks {

    private UcteExporterTestNetworks() {
    }

    /**
     * One French 380 kV voltage level holding one node {@code F<case>11} and one X-node {@code X<case>11} per case,
     * both with the same power generation limits:
     * <ul>
     *     <li>{@code CASE1}: normal P and Q limits;</li>
     *     <li>{@code CASE2}: out of bounds P and Q limits;</li>
     *     <li>{@code CASE3}: out of bounds P limits, normal Q limits;</li>
     *     <li>{@code CASE4}: normal P limits, out of bounds Q limits.</li>
     * </ul>
     */
    static Network createWithHighLimits() {
        Network network = Network.create("powerGenerationLimits", "test");
        network.setCaseDate(ZonedDateTime.parse("2026-01-01T00:00:00.000+01:00"));
        VoltageLevel voltageLevel = network.newSubstation()
                                           .setId("SUBSTATION")
                                           .setCountry(Country.FR)
                                           .add()
                                           .newVoltageLevel()
                                           .setId("VL")
                                           .setNominalV(380)
                                           .setTopologyKind(TopologyKind.BUS_BREAKER)
                                           .add();
        addNodeWithBoundaryLine(voltageLevel, "CASE1", 0, 500, -200, 300);
        addNodeWithBoundaryLine(voltageLevel, "CASE2", -9999, 9999, -Double.MAX_VALUE, Double.MAX_VALUE);
        addNodeWithBoundaryLine(voltageLevel, "CASE3", -Double.MAX_VALUE, 50_000_000, -200, 300);
        addNodeWithBoundaryLine(voltageLevel, "CASE4", 0, 500, -50_000_000, 9999);
        return network;
    }

    /**
     * Add a bus {@code F<name>11} with a generator, and a boundary line from this bus to an X-node {@code X<name>11}
     * whose generation regulates voltage. Both the generator and the X-node generation get the given power limits.
     *
     * @param voltageLevel the bus-breaker voltage level to add the bus, generator and boundary line to
     * @param name the case name used to build the node ids
     * @param minP the minimum active power of the generator and of the X-node generation
     * @param maxP the maximum active power of the generator and of the X-node generation
     * @param minQ the minimum reactive power of the generator and of the X-node generation
     * @param maxQ the maximum reactive power of the generator and of the X-node generation
     */
    private static void addNodeWithBoundaryLine(VoltageLevel voltageLevel,
                                                String name,
                                                double minP,
                                                double maxP,
                                                double minQ,
                                                double maxQ) {
        String busId = "F" + name + "11";
        String xnodeId = "X" + name + "11";
        voltageLevel.getBusBreakerView().newBus()
                    .setId(busId)
                    .add();
        voltageLevel.newGenerator()
                    .setId(busId + "_generator")
                    .setBus(busId)
                    .setMinP(minP)
                    .setMaxP(maxP)
                    .setTargetP(100)
                    .setLocalTargetQ(10)
                    .add()
                    .newMinMaxReactiveLimits()
                    .setMinQ(minQ)
                    .setMaxQ(maxQ)
                    .add();
        voltageLevel.newBoundaryLine()
                    .setId(busId + " " + xnodeId + " 1")
                    .setBus(busId)
                    .setR(1)
                    .setX(10)
                    .setG(0)
                    .setB(0)
                    .setP0(0)
                    .setQ0(0)
                    .setPairingKey(xnodeId)
                    .newGeneration()
                    .setTargetP(50)
                    .setTargetQ(5)
                    .setTargetV(400)
                    .setVoltageRegulationOn(true)
                    .setMinP(minP)
                    .setMaxP(maxP)
                    .add()
                    .add()
                    .getGeneration()
                    .newMinMaxReactiveLimits()
                    .setMinQ(minQ)
                    .setMaxQ(maxQ)
                    .add();
    }

    /**
     * One French 380 kV voltage level holding one node {@code FFFFFF11}, and a boundary line from this node to an
     * X-node {@code XXXXXX11} without generation part.
     */
    static Network createWithBoundaryLineWithoutGeneration() {
        Network network = Network.create("boundaryLineWithoutGeneration", "test");
        network.setCaseDate(ZonedDateTime.parse("2026-01-01T00:00:00.000+01:00"));
        VoltageLevel voltageLevel = network.newSubstation()
                                           .setId("SUBSTATION")
                                           .setCountry(Country.FR)
                                           .add()
                                           .newVoltageLevel()
                                           .setId("VL")
                                           .setNominalV(380)
                                           .setTopologyKind(TopologyKind.BUS_BREAKER)
                                           .add();
        voltageLevel.getBusBreakerView().newBus()
                    .setId("FFFFFF11")
                    .add();
        voltageLevel.newBoundaryLine()
                    .setId("FFFFFF11 XXXXXX11 1")
                    .setBus("FFFFFF11")
                    .setR(1)
                    .setX(10)
                    .setG(0)
                    .setB(0)
                    .setP0(50)
                    .setQ0(30)
                    .setPairingKey("XXXXXX11")
                    .add();
        return network;
    }
}
