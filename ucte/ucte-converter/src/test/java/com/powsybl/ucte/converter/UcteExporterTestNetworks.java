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
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;

import java.time.ZonedDateTime;
import java.util.stream.IntStream;

/**
 * Test networks for the UCTE exporter tests.
 *
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
public final class UcteExporterTestNetworks {

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

    /**
     * One French substation with two 380 kV voltage levels, and one transformer {@code F<case>11 F<case>12 1} per
     * case, each with a single tap changer. The neutral tap is the one found by the exporter.
     * <table>
     *     <caption>Tap changer cases</caption>
     *     <tr><th>Case</th><th>Tap changer</th><th>Positions, tap</th><th>Steps</th><th>Neutral tap, effect</th></tr>
     *     <tr><td>{@code RLINE}</td><td>ratio</td><td>[-2, 2], 0</td><td>linear model</td><td>0</td></tr>
     *     <tr><td>{@code RSHAP}</td><td>ratio</td><td>[1, 5], 3</td><td>rho 0.90, 0.95, 1.05, 0.98, 1.10</td>
     *     <td>4 (closest rho to 1, not exact), range extended on the high side, deviation</td></tr>
     *     <tr><td>{@code RTIEB}</td><td>ratio</td><td>[1, 2], 1</td><td>rho 0.95, 1.05</td>
     *     <td>1 (tie on the distance to 1, broken by the distance to the middle), range extended on the low side,
     *     deviation</td></tr>
     *     <tr><td>{@code RLOWX}</td><td>ratio</td><td>[-1, 3], 0</td><td>linear model</td>
     *     <td>0, range extended on the low side</td></tr>
     *     <tr><td>{@code RDEVI}</td><td>ratio</td><td>[-2, 2], 0</td><td>linear model, except rho 1.5 at 1</td>
     *     <td>0, deviation</td></tr>
     *     <tr><td>{@code PCLOS}</td><td>phase (SYMM)</td><td>[1, 5], 3</td><td>alpha -10, -5, 5, 2, -8</td>
     *     <td>4 (closest alpha to 0, not exact), range extended on the high side, deviation</td></tr>
     *     <tr><td>{@code PTIEB}</td><td>phase (ASYM)</td><td>[1, 5], 3</td>
     *     <td>alpha 0, rho 0.90, 0.95, 1.05, 0.98, 1.10</td>
     *     <td>3 (tie on the distance to 0, broken by the distance to the middle), deviation</td></tr>
     *     <tr><td>{@code SLINE}</td><td>phase (SYMM)</td><td>[-1, 1], 0</td><td>alpha 90, 0, -90</td><td>0</td></tr>
     *     <tr><td>{@code SDEVI}</td><td>phase (SYMM)</td><td>[-1, 1], 0</td><td>alpha 90, 0, -50</td>
     *     <td>0, deviation</td></tr>
     *     <tr><td>{@code ALINE}</td><td>phase (ASYM)</td><td>[-1, 1], 0</td><td>alpha 0, rho 4/3, 1, 0.8</td>
     *     <td>0</td></tr>
     *     <tr><td>{@code ADEVI}</td><td>phase (ASYM)</td><td>[-2, 2], 0</td><td>alpha 0, rho 2, 4/3, 1, 0.7, 2/3</td>
     *     <td>0, deviation (rho 0.8 expected at 1)</td></tr>
     *     <tr><td>{@code RROUN}</td><td>ratio</td><td>[-10, 10], 0</td>
     *     <td>linear model, δu = 1.23456 % (more than 4 decimals)</td><td>0</td></tr>
     *     <tr><td>{@code AROUN}</td><td>phase (ASYM)</td><td>[-10, 10], 0</td>
     *     <td>alpha 0, linear model, δu = 1.23456 % (more than 4 decimals)</td><td>0</td></tr>
     * </table>
     */
    public static Network createWithTapChangers() {
        Network network = Network.create("tapChangers", "test");
        network.setCaseDate(ZonedDateTime.parse("2026-01-01T00:00:00.000+01:00"));
        Substation substation = network.newSubstation()
                                       .setId("SUBSTATION")
                                       .setCountry(Country.FR)
                                       .add();
        for (String voltageLevelId : new String[] {"VL1", "VL2"}) {
            substation.newVoltageLevel()
                      .setId(voltageLevelId)
                      .setNominalV(380)
                      .setTopologyKind(TopologyKind.BUS_BREAKER)
                      .add();
        }

        addRatioTapChanger(addTransformer(substation, "RLINE"), -2, 0, 1 / 0.96, 1 / 0.98, 1, 1 / 1.02, 1 / 1.04);
        addRatioTapChanger(addTransformer(substation, "RSHAP"), 1, 3, 0.90, 0.95, 1.05, 0.98, 1.10);
        addRatioTapChanger(addTransformer(substation, "RTIEB"), 1, 1, 0.95, 1.05);
        addRatioTapChanger(addTransformer(substation, "RLOWX"), -1, 0, 1 / 0.98, 1, 1 / 1.02, 1 / 1.04, 1 / 1.06);
        addRatioTapChanger(addTransformer(substation, "RDEVI"), -2, 0, 1 / 0.96, 1 / 0.98, 1, 1.5, 1 / 1.04);
        addPhaseTapChanger(addTransformer(substation, "PCLOS"), 1, 3, new double[] {-10, -5, 5, 2, -8}, new double[] {1, 1, 1, 1, 1});
        addPhaseTapChanger(addTransformer(substation, "PTIEB"), 1, 3, new double[] {0, 0, 0, 0, 0}, new double[] {0.90, 0.95, 1.05, 0.98, 1.10});
        addPhaseTapChanger(addTransformer(substation, "SLINE"), -1, 0, new double[] {90, 0, -90}, new double[] {1, 1, 1});
        addPhaseTapChanger(addTransformer(substation, "SDEVI"), -1, 0, new double[] {90, 0, -50}, new double[] {1, 1, 1});
        addPhaseTapChanger(addTransformer(substation, "ALINE"), -1, 0, new double[] {0, 0, 0}, new double[] {4 / 3.0, 1, 0.8});
        addPhaseTapChanger(addTransformer(substation, "ADEVI"), -2, 0, new double[] {0, 0, 0, 0, 0}, new double[] {2, 4 / 3.0, 1, 0.7, 2 / 3.0});
        double[] roundingRhos = IntStream.rangeClosed(-10, 10).mapToDouble(k -> 1 / (1 + 1.23456 * k / 100)).toArray();
        addRatioTapChanger(addTransformer(substation, "RROUN"), -10, 0, roundingRhos);
        addPhaseTapChanger(addTransformer(substation, "AROUN"), -10, 0, new double[roundingRhos.length], roundingRhos);
        return network;
    }

    /**
     * Add buses {@code F<name>11} in {@code VL1} and {@code F<name>12} in {@code VL2}, and a transformer
     * {@code F<name>11 F<name>12 1} between them.
     */
    private static TwoWindingsTransformer addTransformer(Substation substation, String name) {
        String bus1 = "F" + name + "11";
        String bus2 = "F" + name + "12";
        substation.getNetwork().getVoltageLevel("VL1").getBusBreakerView().newBus().setId(bus1).add();
        substation.getNetwork().getVoltageLevel("VL2").getBusBreakerView().newBus().setId(bus2).add();
        return substation.newTwoWindingsTransformer()
                         .setId(bus1 + " " + bus2 + " 1")
                         .setVoltageLevel1("VL1").setBus1(bus1).setConnectableBus1(bus1)
                         .setVoltageLevel2("VL2").setBus2(bus2).setConnectableBus2(bus2)
                         .setRatedU1(380).setRatedU2(380).setRatedS(100)
                         .setR(2).setX(100)
                         .add();
    }

    /**
     * Add a non-regulating ratio tap changer with one step per given rho, starting at {@code lowTapPosition}.
     */
    private static void addRatioTapChanger(TwoWindingsTransformer transformer, int lowTapPosition, int tapPosition,
                                           double... rhos) {
        RatioTapChangerAdder adder = transformer.newRatioTapChanger()
                                                .setLoadTapChangingCapabilities(false)
                                                .setLowTapPosition(lowTapPosition)
                                                .setTapPosition(tapPosition);
        for (double rho : rhos) {
            adder.beginStep().setRho(rho).endStep();
        }
        adder.add();
    }

    /**
     * Add a non-regulating phase tap changer with one step per given (alpha, rho) pair, starting at
     * {@code lowTapPosition}.
     */
    private static void addPhaseTapChanger(TwoWindingsTransformer transformer, int lowTapPosition, int tapPosition,
                                           double[] alphas, double[] rhos) {
        PhaseTapChangerAdder adder = transformer.newPhaseTapChanger()
                                                .setLowTapPosition(lowTapPosition)
                                                .setTapPosition(tapPosition)
                                                .setRegulating(false);
        for (int i = 0; i < alphas.length; i++) {
            adder.beginStep().setAlpha(alphas[i]).setRho(rhos[i]).endStep();
        }
        adder.add();
    }
}
