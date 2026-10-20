/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.SubstationAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.ucte.network.UcteCountryCode;

import java.time.ZonedDateTime;

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
     * One French 380 kV node {@code FREMOT11} holding load {@code L380}, and one 21 kV node {@code F<case>71} per case
     * holding several loads or generators. Equipment ids are prefixed by the case name.
     * <ul>
     *     <li>{@code CASE1}: loads {@code LA} (10 MW, 2 MVar) and {@code LB} (5 MW, 1 MVar);</li>
     *     <li>{@code CASE2}: generators {@code GA} (P = 100, Q = 5, P in [10, 200], Q in [-50, 50]) and {@code GB}
     *     (P = 50, Q = 3, P in [5, 100], Q in [-20, 20]);</li>
     *     <li>{@code CASE3}: generators {@code GA} (P = 50) and {@code GB} (P = 150), both with the reactive capability
     *     curve {@code minQ = -10 - P, maxQ = 10 + P} on [0, 200]: Q in [-60, 60] and [-160, 160] at their own P;</li>
     *     <li>{@code CASE4}: generators {@code GA} (maxP = 9999, the "no limit" value) and {@code GB} (maxP = 100);</li>
     *     <li>{@code CASE5}: same as {@code CASE4}, with {@code GA}'s maxP = {@code Double.MAX_VALUE};</li>
     *     <li>{@code CASE6}: generators {@code GA} and {@code GB}, both with maxP = 6000 (sum out of bounds);</li>
     *     <li>{@code CASE7}: generators {@code GA} (hydro) and {@code GB} (thermal);</li>
     *     <li>{@code CASE8}: generators {@code GA} and {@code GB}, both hydro.</li>
     * </ul>
     */
    static Network createWithMultipleInjections() {
        Network network = createWithRemoteNode("multipleInjections");
        VoltageLevel vl21 = network.getVoltageLevel("VL21");

        String bus = addBus(vl21, "CASE1");
        addLoad(vl21, "CASE1_LA", bus, 10, 2);
        addLoad(vl21, "CASE1_LB", bus, 5, 1);

        bus = addBus(vl21, "CASE2");
        addGenerator(vl21, "CASE2_GA", bus, 100, 10, 200).setLocalTargetQ(5)
                .newMinMaxReactiveLimits().setMinQ(-50).setMaxQ(50).add();
        addGenerator(vl21, "CASE2_GB", bus, 50, 5, 100).setLocalTargetQ(3)
                .newMinMaxReactiveLimits().setMinQ(-20).setMaxQ(20).add();

        bus = addBus(vl21, "CASE3");
        for (Generator generator : new Generator[] {addGenerator(vl21, "CASE3_GA", bus, 50, 0, 200),
                                                    addGenerator(vl21, "CASE3_GB", bus, 150, 0, 200)}) {
            generator.newReactiveCapabilityCurve()
                     .beginPoint().setP(0).setMinQ(-10).setMaxQ(10).endPoint()
                     .beginPoint().setP(200).setMinQ(-210).setMaxQ(210).endPoint()
                     .add();
        }

        bus = addBus(vl21, "CASE4");
        addGenerator(vl21, "CASE4_GA", bus, 10, 1, 9999);
        addGenerator(vl21, "CASE4_GB", bus, 10, 2, 100);

        bus = addBus(vl21, "CASE5");
        addGenerator(vl21, "CASE5_GA", bus, 10, 1, Double.MAX_VALUE);
        addGenerator(vl21, "CASE5_GB", bus, 10, 2, 100);

        bus = addBus(vl21, "CASE6");
        addGenerator(vl21, "CASE6_GA", bus, 10, 0, 6000);
        addGenerator(vl21, "CASE6_GB", bus, 10, 0, 6000);

        bus = addBus(vl21, "CASE7");
        addGenerator(vl21, "CASE7_GA", bus, 10, 0, 100).setEnergySource(EnergySource.HYDRO);
        addGenerator(vl21, "CASE7_GB", bus, 10, 0, 100).setEnergySource(EnergySource.THERMAL);

        bus = addBus(vl21, "CASE8");
        addGenerator(vl21, "CASE8_GA", bus, 10, 0, 100).setEnergySource(EnergySource.HYDRO);
        addGenerator(vl21, "CASE8_GB", bus, 10, 0, 100).setEnergySource(EnergySource.HYDRO);
        return network;
    }

    /**
     * One French 380 kV node {@code FREMOT11} holding load {@code L380}, and one 21 kV node {@code F<case>71} per case
     * holding several generators providing voltage targets. Equipment ids are prefixed by the case name.
     * <ul>
     *     <li>{@code CASE1}: regulating generators {@code GA} (P = 100, 22 kV) and {@code GB} (P = 200, 23 kV);</li>
     *     <li>{@code CASE2}: regulating generators {@code GB} (P = 100, 23 kV) and {@code GA} (P = 100, 22 kV), added in
     *     this order to rule out an iteration-order effect on the tie-break;</li>
     *     <li>{@code CASE3}: load {@code L} and generator {@code GA} (P = 100) regulating {@code L}'s terminal at 22.5 kV,
     *     without local target;</li>
     *     <li>{@code CASE4}: generator {@code GA} (P = 100) regulating {@code FREMOT11} at 405 kV;</li>
     *     <li>{@code CASE5}: regulating generator {@code GA} (P = 10, 22 kV) and generator {@code GB} (P = 500)
     *     regulating {@code FREMOT11} at 405 kV;</li>
     *     <li>{@code CASE6}: non-regulating generators {@code GA} (P = 10, 22 kV) and {@code GB} (P = 20, 23 kV);</li>
     *     <li>{@code CASE7}: regulating generator {@code GA} (P = 10, 22 kV) and non-regulating generator {@code GB}
     *     (P = 500, 23 kV);</li>
     *     <li>{@code CASE8}: generator {@code GA} (P = 10) regulating {@code FREMOT11} at 405 kV and non-regulating
     *     generator {@code GB} (P = 500, 23 kV);</li>
     *     <li>{@code CASE9}: non-regulating generator {@code GA} (P = 10) without voltage target.</li>
     * </ul>
     */
    public static Network createWithMultipleVoltageTargets() {
        Network network = createWithRemoteNode("multipleVoltageTargets");
        VoltageLevel vl21 = network.getVoltageLevel("VL21");
        Terminal remoteTerminal = network.getLoad("L380").getTerminal();

        String bus = addBus(vl21, "CASE1");
        addRegulatingGenerator(vl21, "CASE1_GA", bus, 100, 22.0);
        addRegulatingGenerator(vl21, "CASE1_GB", bus, 200, 23.0);

        bus = addBus(vl21, "CASE2");
        addRegulatingGenerator(vl21, "CASE2_GB", bus, 100, 23.0);
        addRegulatingGenerator(vl21, "CASE2_GA", bus, 100, 22.0);

        bus = addBus(vl21, "CASE3");
        addLoad(vl21, "CASE3_L", bus, 1, 1);
        addRemoteRegulatingGenerator(vl21, "CASE3_GA", bus, 100, network.getLoad("CASE3_L").getTerminal(), 22.5);

        bus = addBus(vl21, "CASE4");
        addRemoteRegulatingGenerator(vl21, "CASE4_GA", bus, 100, remoteTerminal, 405);

        bus = addBus(vl21, "CASE5");
        addRegulatingGenerator(vl21, "CASE5_GA", bus, 10, 22.0);
        addRemoteRegulatingGenerator(vl21, "CASE5_GB", bus, 500, remoteTerminal, 405);

        bus = addBus(vl21, "CASE6");
        addGenerator(vl21, "CASE6_GA", bus, 10, 0, 100).setLocalTargetV(22.0);
        addGenerator(vl21, "CASE6_GB", bus, 20, 0, 100).setLocalTargetV(23.0);

        bus = addBus(vl21, "CASE7");
        addRegulatingGenerator(vl21, "CASE7_GA", bus, 10, 22.0);
        addGenerator(vl21, "CASE7_GB", bus, 500, 0, 1000).setLocalTargetV(23.0);

        bus = addBus(vl21, "CASE8");
        addRemoteRegulatingGenerator(vl21, "CASE8_GA", bus, 10, remoteTerminal, 405);
        addGenerator(vl21, "CASE8_GB", bus, 500, 0, 1000).setLocalTargetV(23.0);

        bus = addBus(vl21, "CASE9");
        addGenerator(vl21, "CASE9_GA", bus, 10, 0, 100);
        return network;
    }

    /**
     * A French substation with a 380 kV voltage level holding node {@code FREMOT11} and its load {@code L380}, and an
     * empty 21 kV voltage level {@code VL21}.
     */
    private static Network createWithRemoteNode(String networkId) {
        Network network = Network.create(networkId, "test");
        network.setCaseDate(ZonedDateTime.parse("2026-01-01T00:00:00.000+01:00"));
        Substation substation = network.newSubstation()
                                       .setId("SUBSTATION")
                                       .setCountry(Country.FR)
                                       .add();
        VoltageLevel vl380 = substation.newVoltageLevel()
                                       .setId("VL380")
                                       .setNominalV(380)
                                       .setTopologyKind(TopologyKind.BUS_BREAKER)
                                       .add();
        vl380.getBusBreakerView().newBus()
             .setId("FREMOT11")
             .add();
        addLoad(vl380, "L380", "FREMOT11", 1, 1);
        substation.newVoltageLevel()
                  .setId("VL21")
                  .setNominalV(21)
                  .setTopologyKind(TopologyKind.BUS_BREAKER)
                  .add();
        return network;
    }

    /**
     * Add a bus {@code F<name>71} to the given voltage level and return its id.
     */
    private static String addBus(VoltageLevel voltageLevel, String name) {
        String busId = "F" + name + "71";
        voltageLevel.getBusBreakerView().newBus()
                    .setId(busId)
                    .add();
        return busId;
    }

    private static void addLoad(VoltageLevel voltageLevel, String id, String busId, double p0, double q0) {
        voltageLevel.newLoad().setId(id).setBus(busId).setConnectableBus(busId).setP0(p0).setQ0(q0).add();
    }

    private static GeneratorAdder generatorAdder(VoltageLevel voltageLevel, String id, String busId, double targetP,
                                                 double minP, double maxP) {
        return voltageLevel.newGenerator()
                           .setId(id)
                           .setBus(busId)
                           .setConnectableBus(busId)
                           .setEnergySource(EnergySource.OTHER)
                           .setTargetP(targetP)
                           .setMinP(minP)
                           .setMaxP(maxP)
                           .setLocalTargetQ(0);
    }

    /**
     * Add a generator without voltage regulation (PQ).
     */
    private static Generator addGenerator(VoltageLevel voltageLevel, String id, String busId, double targetP,
                                          double minP, double maxP) {
        return generatorAdder(voltageLevel, id, busId, targetP, minP, maxP).add();
    }

    /**
     * Add a voltage-regulating generator with a local target.
     */
    private static void addRegulatingGenerator(VoltageLevel voltageLevel, String id, String busId, double targetP,
                                               double targetV) {
        GeneratorAdder adder = generatorAdder(voltageLevel, id, busId, targetP, 0, 1000).setLocalTargetV(targetV);
        adder.newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withRegulating(true).add();
        adder.add();
    }

    /**
     * Add a voltage-regulating generator with no local target, regulating {@code terminal} at {@code targetValue}.
     */
    private static void addRemoteRegulatingGenerator(VoltageLevel voltageLevel, String id, String busId,
                                                     double targetP, Terminal terminal, double targetValue) {
        GeneratorAdder adder = generatorAdder(voltageLevel, id, busId, targetP, 0, 1000);
        adder.newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withRegulating(true)
             .withTerminal(terminal).withTargetValue(targetValue).add();
        adder.add();
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

    /**
     * Ids of the networks with voltage levels without substation ("orphans"), mimicking CGMES imports: valid UCTE
     * codes (for the Default naming strategy) or free ids (Counter naming strategy).
     * All their voltage levels are 380 kV. Substation "S_&lt;name&gt;" holds voltage level "VL_&lt;name&gt;"; orphan
     * voltage levels are named "VL_&lt;name&gt;".
     */
    enum Ids {
        UCTE,
        FREE;

        /**
         * Build the id of a bus: {@code <country code><name>1<busbar>} for UCTE ids, {@code BUS_<name>_<busbar>} for
         * free ids.
         *
         * @param country the country of the bus, only used for UCTE ids
         * @param name the name of the bus, 5 characters
         * @param busbar the busbar character, last character of UCTE ids
         * @return the bus id
         * @throws com.powsybl.ucte.network.UcteException if {@code country} has no UCTE country code, for UCTE ids
         */
        String bus(Country country, String name, char busbar) {
            return this == UCTE
                    ? UcteCountryCode.fromCountry(country).getUcteCode() + name + "1" + busbar
                    : "BUS_" + name + "_" + busbar;
        }

        /**
         * Build the id of a branch: {@code <bus1> <bus2> 1} for UCTE ids, {@code BR_<bus1>_<bus2>} for free ids.
         *
         * @param bus1 the id of the bus on side 1
         * @param bus2 the id of the bus on side 2
         * @return the branch id
         */
        String branch(String bus1, String bus2) {
            return this == UCTE ? bus1 + " " + bus2 + " 1" : "BR_" + bus1 + "_" + bus2;
        }
    }

    /**
     * Create a network with voltage levels without substation, one per topology met in CGMES imports, around
     * substations {@code AAAAA}, {@code BBBBB}, {@code CCCCC} (NL) and {@code DDDDD} (BE, unconnected):
     * <ul>
     *     <li>{@code TJUNC}: T-line junction, linked to {@code AAAAA}, {@code BBBBB} and {@code CCCCC};</li>
     *     <li>{@code SEGMT}: chained line segments {@code AAAAA} - {@code SEGMT} bus 1 - {@code SEGMT} bus 2 -
     *     {@code BBBBB}, both intermediate buses in one voltage level;</li>
     *     <li>{@code SEGAA}, {@code SEGBB}: chained line segments {@code AAAAA} - {@code SEGAA} - {@code SEGBB} -
     *     {@code BBBBB}, intermediate buses in two voltage levels;</li>
     *     <li>{@code ORPHA}: linked to {@code AAAAA}, two buses joined by a closed switch, and load {@code LOAD_ORPHA}
     *     (p0 = 42, q0 = 7) on bus 1;</li>
     *     <li>{@code EMPTY}: isolated, one bus and no equipment.</li>
     * </ul>
     *
     * @param ids the kind of ids to use for buses and branches
     * @return the network
     */
    static Network createWithOrphanVoltageLevels(Ids ids) {
        Network network = newNetwork();
        String a = addSubstationBus(network, ids, Country.NL, "AAAAA");
        String b = addSubstationBus(network, ids, Country.NL, "BBBBB");
        String c = addSubstationBus(network, ids, Country.NL, "CCCCC");
        addSubstationBus(network, ids, Country.BE, "DDDDD");

        String junction = addBus(addOrphanVoltageLevel(network, "TJUNC"), ids, "TJUNC", '1');
        addLine(network, ids, a, junction);
        addLine(network, ids, b, junction);
        addLine(network, ids, c, junction);

        VoltageLevel segments = addOrphanVoltageLevel(network, "SEGMT");
        String segment1 = addBus(segments, ids, "SEGMT", '1');
        String segment2 = addBus(segments, ids, "SEGMT", '2');
        addLine(network, ids, a, segment1);
        addLine(network, ids, segment1, segment2);
        addLine(network, ids, segment2, b);

        String segmentA = addBus(addOrphanVoltageLevel(network, "SEGAA"), ids, "SEGAA", '1');
        String segmentB = addBus(addOrphanVoltageLevel(network, "SEGBB"), ids, "SEGBB", '1');
        addLine(network, ids, a, segmentA);
        addLine(network, ids, segmentA, segmentB);
        addLine(network, ids, segmentB, b);

        VoltageLevel orphan = addOrphanVoltageLevel(network, "ORPHA");
        String orphan1 = addBus(orphan, ids, "ORPHA", '1');
        String orphan2 = addBus(orphan, ids, "ORPHA", '2');
        orphan.getBusBreakerView().newSwitch()
                .setId(ids.branch(orphan1, orphan2))
                .setBus1(orphan1)
                .setBus2(orphan2)
                .setOpen(false)
                .add();
        addLoad(network, orphan1, "LOAD_ORPHA");
        addLine(network, ids, a, orphan1);

        addBus(addOrphanVoltageLevel(network, "EMPTY"), ids, "EMPTY", '1');
        return network;
    }

    /**
     * Create a T-line network: substations {@code AAAAA} (NL), {@code BBBBB} and {@code CCCCC} (NL), each linked to the
     * orphan junction {@code TJUNC}.
     *
     * @param ids the kind of ids to use for buses and branches
     * @param countryB the country of substation {@code BBBBB}, none if null
     * @return the network
     */
    static Network createWithTLine(Ids ids, Country countryB) {
        Network network = newNetwork();
        String a = addSubstationBus(network, ids, Country.NL, "AAAAA");
        String b = addSubstationBus(network, ids, countryB, "BBBBB");
        String c = addSubstationBus(network, ids, Country.NL, "CCCCC");
        String junction = addBus(addOrphanVoltageLevel(network, "TJUNC"), ids, "TJUNC", '1');
        addLine(network, ids, a, junction);
        addLine(network, ids, b, junction);
        addLine(network, ids, c, junction);
        return network;
    }

    /**
     * Create a network with free ids, holding substation {@code AAAAA} (NL) and the unconnected orphan voltage level
     * {@code ALONE}, with load {@code LOAD_ALONE} (p0 = 42, q0 = 7): the network has a single country.
     *
     * @return the network
     */
    static Network createWithSingleCountryAndUnconnectedOrphan() {
        Network network = newNetwork();
        addSubstationBus(network, Ids.FREE, Country.NL, "AAAAA");
        String alone = addBus(addOrphanVoltageLevel(network, "ALONE"), Ids.FREE, "ALONE", '1');
        addLoad(network, alone, "LOAD_ALONE");
        return network;
    }

    /**
     * Create a network with free ids, with two countries, and isolated orphan voltage levels holding equipment:
     * <ul>
     *     <li>{@code ISOL1} and {@code ISOL2}: linked to each other by a line, load {@code LOAD_ISOL1} on {@code ISOL1};</li>
     *     <li>{@code XORPH}: boundary line {@code BL_X};</li>
     *     <li>{@code XTIEL}: boundary line {@code BL_1}, paired with boundary line {@code BL_2} of {@code AAAAA} in tie line
     *     {@code TL}.</li>
     * </ul>
     *
     * @return the network
     * @see #createWithTwoCountries()
     */
    static Network createWithIsolatedOrphansHoldingEquipment() {
        Network network = createWithTwoCountries();
        String i1 = addBus(addOrphanVoltageLevel(network, "ISOL1"), Ids.FREE, "ISOL1", '1');
        String i2 = addBus(addOrphanVoltageLevel(network, "ISOL2"), Ids.FREE, "ISOL2", '1');
        addLoad(network, i1, "LOAD_ISOL1");
        addLine(network, Ids.FREE, i1, i2);
        VoltageLevel withBoundaryLine = addOrphanVoltageLevel(network, "XORPH");
        addBus(withBoundaryLine, Ids.FREE, "XORPH", '1');
        addBoundaryLine(withBoundaryLine, "BL_X", null);
        VoltageLevel withTieLine = addOrphanVoltageLevel(network, "XTIEL");
        addBus(withTieLine, Ids.FREE, "XTIEL", '1');
        addBoundaryLine(withTieLine, "BL_1", "XKEY");
        addBoundaryLine(network.getVoltageLevel("VL_AAAAA"), "BL_2", "XKEY");
        network.newTieLine().setId("TL").setBoundaryLine1("BL_1").setBoundaryLine2("BL_2").add();
        return network;
    }

    /**
     * Create a network with free ids, with two countries, and isolated orphan voltage levels without equipment:
     * bus-breaker {@code VL_EMPTY} holding one bus, and node-breaker {@code VL_NBBBS} holding only busbar section
     * {@code BBS}.
     *
     * @return the network
     * @see #createWithTwoCountries()
     */
    static Network createWithIsolatedOrphansWithoutEquipment() {
        Network network = createWithTwoCountries();
        addBus(addOrphanVoltageLevel(network, "EMPTY"), Ids.FREE, "EMPTY", '1');
        network.newVoltageLevel()
                .setId("VL_NBBBS")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add()
                .getNodeBreakerView().newBusbarSection().setId("BBS").setNode(0).add();
        return network;
    }

    /**
     * Create a network with free ids, holding substations {@code AAAAA} (NL) and {@code BBBBB} (BE), linked by a line.
     *
     * @return the network
     */
    private static Network createWithTwoCountries() {
        Network network = newNetwork();
        String a = addSubstationBus(network, Ids.FREE, Country.NL, "AAAAA");
        String b = addSubstationBus(network, Ids.FREE, Country.BE, "BBBBB");
        addLine(network, Ids.FREE, a, b);
        return network;
    }

    /**
     * Add a node-breaker T-line, with free ids: orphan node-breaker voltage level {@code VL_NBJUN}, its nodes 1, 2 and 3
     * linked to the buses of {@code VL_AAAAA}, {@code VL_BBBBB} and {@code VL_CCCCC}.
     *
     * @param network a network created by {@link #createWithOrphanVoltageLevels(Ids)} with free ids
     */
    static void addNodeBreakerTLine(Network network) {
        VoltageLevel junction = network.newVoltageLevel()
                .setId("VL_NBJUN")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        int node = 1;
        for (String name : new String[] {"AAAAA", "BBBBB", "CCCCC"}) {
            String bus = Ids.FREE.bus(Country.NL, name, '1');
            junction.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(node).add();
            network.newLine()
                    .setId("BR_NBJUN_" + name)
                    .setVoltageLevel1("VL_" + name)
                    .setBus1(bus)
                    .setConnectableBus1(bus)
                    .setVoltageLevel2("VL_NBJUN")
                    .setNode2(node)
                    .setR(1)
                    .setX(10)
                    .add();
            node++;
        }
    }

    /**
     * Add orphan voltage level {@code VL_DISCO}, with free ids, linked to the bus of {@code VL_AAAAA} by a line
     * disconnected on both sides.
     *
     * @param network a network created by {@link #createWithOrphanVoltageLevels(Ids)} with free ids
     */
    static void addOrphanWithDisconnectedLine(Network network) {
        String disconnected = addBus(addOrphanVoltageLevel(network, "DISCO"), Ids.FREE, "DISCO", '1');
        Line line = addLine(network, Ids.FREE, Ids.FREE.bus(Country.NL, "AAAAA", '1'), disconnected);
        line.getTerminal1().disconnect();
        line.getTerminal2().disconnect();
    }

    /**
     * Add two orphan voltage levels to the network of {@code network.xiidm}, with free ids, and set the country of
     * substation {@code P2} to BE, so that the network has two countries:
     * <ul>
     *     <li>{@code VL_LINKED}: linked to bus {@code NHV1} by line {@code BR_NHV1_BUS_LINKED_1};</li>
     *     <li>{@code VL_ISOLATED}: isolated, one bus and no equipment.</li>
     * </ul>
     *
     * @param network the network of {@code network.xiidm}
     */
    static void addLinkedAndIsolatedOrphans(Network network) {
        network.getSubstation("P2").setCountry(Country.BE);
        String linked = addBus(addOrphanVoltageLevel(network, "LINKED"), Ids.FREE, "LINKED", '1');
        addLine(network, Ids.FREE, "NHV1", linked);
        addBus(addOrphanVoltageLevel(network, "ISOLATED"), Ids.FREE, "ISOLATED", '1');
    }

    /**
     * Create an empty network, with a fixed case date.
     *
     * @return the network
     */
    static Network newNetwork() {
        Network network = Network.create("orphanVoltageLevels", "test");
        network.setCaseDate(ZonedDateTime.parse("2026-01-01T00:00:00.000+01:00"));
        return network;
    }

    /**
     * Add substation {@code S_<name>} with bus-breaker voltage level {@code VL_<name>} holding one bus.
     *
     * @param network the network to add the substation to
     * @param ids the kind of ids to use for the bus
     * @param country the country of the substation, none if null (UCTE ids then use NL)
     * @param name the name of the substation, 5 characters
     * @return the id of the bus
     * @throws com.powsybl.ucte.network.UcteException if the country has no UCTE country code, for UCTE ids
     */
    static String addSubstationBus(Network network, Ids ids, Country country, String name) {
        SubstationAdder substationAdder = network.newSubstation().setId("S_" + name);
        if (country != null) {
            substationAdder.setCountry(country);
        }
        VoltageLevel voltageLevel = substationAdder.add().newVoltageLevel()
                .setId("VL_" + name)
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        return addBus(voltageLevel, ids, name, '1');
    }

    /**
     * Add bus-breaker voltage level {@code VL_<name>} without substation.
     *
     * @param network the network to add the voltage level to
     * @param name the name of the voltage level
     * @return the voltage level
     */
    static VoltageLevel addOrphanVoltageLevel(Network network, String name) {
        return network.newVoltageLevel()
                .setId("VL_" + name)
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
    }

    /**
     * Add a bus to a bus-breaker voltage level. UCTE ids use the country of the substation, NL for orphan voltage
     * levels.
     *
     * @param voltageLevel the bus-breaker voltage level to add the bus to
     * @param ids the kind of ids to use for the bus
     * @param name the name of the bus, 5 characters
     * @param busbar the busbar character, last character of UCTE ids
     * @return the id of the bus
     * @throws com.powsybl.ucte.network.UcteException if the country has no UCTE country code, for UCTE ids
     */
    static String addBus(VoltageLevel voltageLevel, Ids ids, String name, char busbar) {
        Country country = voltageLevel.getSubstation().flatMap(Substation::getCountry).orElse(Country.NL);
        String busId = ids.bus(country, name, busbar);
        voltageLevel.getBusBreakerView().newBus().setId(busId).add();
        return busId;
    }

    /**
     * Add a line (r = 1, x = 10) between two buses.
     *
     * @param network the network to add the line to
     * @param ids the kind of ids to use for the line
     * @param bus1 the id of the bus on side 1
     * @param bus2 the id of the bus on side 2
     * @return the line
     */
    static Line addLine(Network network, Ids ids, String bus1, String bus2) {
        return network.newLine()
                .setId(ids.branch(bus1, bus2))
                .setVoltageLevel1(network.getBusBreakerView().getBus(bus1).getVoltageLevel().getId())
                .setBus1(bus1)
                .setConnectableBus1(bus1)
                .setVoltageLevel2(network.getBusBreakerView().getBus(bus2).getVoltageLevel().getId())
                .setBus2(bus2)
                .setConnectableBus2(bus2)
                .setR(1)
                .setX(10)
                .add();
    }

    /**
     * Add a load (p0 = 42, q0 = 7) on a bus.
     *
     * @param network the network holding the bus
     * @param busId the id of the bus
     * @param loadId the id of the load
     */
    static void addLoad(Network network, String busId, String loadId) {
        addLoad(network.getBusBreakerView().getBus(busId).getVoltageLevel(), loadId, busId, 42, 7);
    }

    /**
     * Add a boundary line (r = 1, x = 10) on the single bus of a bus-breaker voltage level.
     *
     * @param voltageLevel the bus-breaker voltage level holding one bus
     * @param id the id of the boundary line
     * @param pairingKey the pairing key of the boundary line, none if null
     */
    private static void addBoundaryLine(VoltageLevel voltageLevel, String id, String pairingKey) {
        String bus = voltageLevel.getBusBreakerView().getBuses().iterator().next().getId();
        voltageLevel.newBoundaryLine().setId(id).setBus(bus).setConnectableBus(bus)
                .setR(1).setX(10).setG(0).setB(0).setP0(0).setQ0(0).setPairingKey(pairingKey).add();
    }
}
