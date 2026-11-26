/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.ucte.converter.util.UcteConverterConstants;

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
     *     <li>{@code CASE8}: generators {@code GA} and {@code GB}, both hydro;</li>
     *     <li>{@code CASE9}: connected load {@code LA} (10 MW, 2 MVar) and disconnected load {@code LB} (5 MW, 1 MVar),
     *     connected hydro generator {@code GA} (P = 100, P in [0, 200]) and disconnected thermal generator {@code GB}
     *     (P = 50, P in [0, 100]).</li>
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

        bus = addBus(vl21, "CASE9");
        addLoad(vl21, "CASE9_LA", bus, 10, 2);
        addLoad(vl21, "CASE9_LB", bus, 5, 1);
        network.getLoad("CASE9_LB").getTerminal().disconnect();
        addGenerator(vl21, "CASE9_GA", bus, 100, 0, 200).setEnergySource(EnergySource.HYDRO);
        addGenerator(vl21, "CASE9_GB", bus, 50, 0, 100).setEnergySource(EnergySource.THERMAL).getTerminal().disconnect();
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
     *     <li>{@code CASE9}: non-regulating generator {@code GA} (P = 10) without voltage target;</li>
     *     <li>{@code CASEA}: generator {@code GA} (P = 100) regulating {@code CASE3_L}'s terminal at 22.5 kV, on another
     *     bus with the same nominal voltage, without local target;</li>
     *     <li>{@code CASEB}: disconnected load {@code L} and generator {@code GA} (P = 100) regulating {@code L}'s
     *     disconnected terminal at 22.5 kV, without local target;</li>
     *     <li>{@code CASEC}: disconnected load {@code L}, generator {@code GA} (P = 100) regulating {@code L}'s
     *     disconnected terminal at 22.5 kV, without local target, and non-regulating generator {@code GB} (P = 10,
     *     23 kV).</li>
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

        bus = addBus(vl21, "CASEA");
        addRemoteRegulatingGenerator(vl21, "CASEA_GA", bus, 100, network.getLoad("CASE3_L").getTerminal(), 22.5);

        bus = addBus(vl21, "CASEB");
        addLoad(vl21, "CASEB_L", bus, 1, 1);
        network.getLoad("CASEB_L").getTerminal().disconnect();
        addRemoteRegulatingGenerator(vl21, "CASEB_GA", bus, 100, network.getLoad("CASEB_L").getTerminal(), 22.5);

        bus = addBus(vl21, "CASEC");
        addLoad(vl21, "CASEC_L", bus, 1, 1);
        network.getLoad("CASEC_L").getTerminal().disconnect();
        addRemoteRegulatingGenerator(vl21, "CASEC_GA", bus, 100, network.getLoad("CASEC_L").getTerminal(), 22.5);
        addGenerator(vl21, "CASEC_GB", bus, 10, 0, 100).setLocalTargetV(23.0);
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
     * case, each with a single tap changer. Each case is described by a comment: the kind of tap changer, its steps,
     * the neutral tap found by the exporter, and the expected effects of the export.
     *
     * @return the network
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

        // Ratio, constant-step model on [-2, 2]: neutral at 0
        addRatioTapChanger(addTransformer(substation, "RLINE"), -2, 0, 1 / 0.96, 1 / 0.98, 1, 1 / 1.02, 1 / 1.04);
        // Ratio on [1, 5], closest rho to 1 not exact: neutral at 4, range extended on the high side, deviation
        addRatioTapChanger(addTransformer(substation, "RSHAP"), 1, 3, 0.90, 0.95, 1.05, 0.98, 1.10);
        // Ratio on [1, 2], tie on the distance to 1 broken by the distance to the middle: neutral at 1, range extended
        // on the low side, deviation
        addRatioTapChanger(addTransformer(substation, "RTIEB"), 1, 1, 0.95, 1.05);
        // Ratio, constant-step model on [-1, 3]: neutral at 0, range extended on the low side
        addRatioTapChanger(addTransformer(substation, "RLOWX"), -1, 0, 1 / 0.98, 1, 1 / 1.02, 1 / 1.04, 1 / 1.06);
        // Ratio, constant-step model on [-2, 2] except rho 1.5 at tap 1: neutral at 0, deviation
        addRatioTapChanger(addTransformer(substation, "RDEVI"), -2, 0, 1 / 0.96, 1 / 0.98, 1, 1.5, 1 / 1.04);
        // SYMM on [1, 5], closest alpha to 0 not exact: neutral at 4, range extended on the high side, deviation
        addPhaseTapChanger(addTransformer(substation, "PCLOS"),
                1,
                3,
                new double[]{-10, -5, 5, 2, -8},
                new double[]{1, 1, 1, 1, 1});
        // ASYM on [1, 5], tie on the distance of alpha to 0 (taps 2 to 4) broken by the distance to the middle: neutral
        // at 3, deviation
        addPhaseTapChanger(addTransformer(substation, "PTIEB"),
                1,
                3,
                new double[]{-10, 5, -5, 5, 10},
                new double[]{0.90, 0.95, 1.05, 0.98, 1.10});
        // SYMM, constant-step model on [-1, 1]: neutral at 0
        addPhaseTapChanger(addTransformer(substation, "SLINE"), -1, 0, new double[]{90, 0, -90}, new double[]{1, 1, 1});
        // SYMM, constant-step model on [-1, 1] except alpha -50 at tap 1: neutral at 0, deviation
        addPhaseTapChanger(addTransformer(substation, "SDEVI"), -1, 0, new double[]{90, 0, -50}, new double[]{1, 1, 1});
        // ASYM, constant-step model on [-1, 1], δu = 25 %, θ = 60°: neutral at 0
        addAsymPhaseTapChanger(addTransformer(substation, "ALINE"), -1, 1, 25, 60);
        // ASYM, constant-step model on [-2, 2], δu = 25 %, θ = 60°, except rho reduced by 10 % at tap 1: neutral at 0,
        // deviation
        TwoWindingsTransformer adevi = addTransformer(substation, "ADEVI");
        addAsymPhaseTapChanger(adevi, -2, 2, 25, 60);
        PhaseTapChangerStep adeviStep = adevi.getPhaseTapChanger().getStep(1);
        adeviStep.setRho(adeviStep.getRho() * 0.9);
        // Ratio, constant-step model on [-10, 10], δu = 1.23456 % (more than 4 decimals): neutral at 0, no deviation
        double[] roundingRhos = IntStream.rangeClosed(-10, 10).mapToDouble(k -> 1 / (1 + 1.23456 * k / 100)).toArray();
        addRatioTapChanger(addTransformer(substation, "RROUN"), -10, 0, roundingRhos);
        // ASYM, constant-step model on [-10, 10], δu = 1.23456 % (more than 4 decimals), θ = 60°: neutral at 0, no
        // deviation
        addAsymPhaseTapChanger(addTransformer(substation, "AROUN"), -10, 10, 1.23456, 60);
        return network;
    }

    /**
     * Add a non-regulating asymmetrical phase tap changer following the UCTE-DEF constant-step model, with its neutral
     * and current tap at position 0: in the complex plane, the point {@code (1 / rho).e^(-i.alpha)} of tap {@code k}
     * is {@code 1 + k.(du / 100).e^(i.theta)}.
     *
     * @param transformer the transformer to add the phase tap changer to
     * @param lowTapPosition the low tap position, negative or zero
     * @param highTapPosition the high tap position, positive or zero
     * @param du the δu (%) between two consecutive taps
     * @param thetaDegrees the angle θ of the regulation, in degrees
     */
    private static void addAsymPhaseTapChanger(TwoWindingsTransformer transformer, int lowTapPosition, int highTapPosition,
                                               double du, double thetaDegrees) {
        int stepCount = highTapPosition - lowTapPosition + 1;
        double[] alphas = new double[stepCount];
        double[] rhos = new double[stepCount];
        double theta = Math.toRadians(thetaDegrees);
        for (int i = 0; i < stepCount; i++) {
            double distance = (lowTapPosition + i) * du / 100;
            double x = 1 + distance * Math.cos(theta);
            double y = distance * Math.sin(theta);
            alphas[i] = -Math.toDegrees(Math.atan2(y, x));
            rhos[i] = 1 / Math.hypot(x, y);
        }
        addPhaseTapChanger(transformer, lowTapPosition, 0, alphas, rhos);
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
     * Two Dutch 380 kV node-breaker voltage levels {@code VL_A} and {@code VL_B}, joined by the line {@code LINE}. Each
     * voltage level holds two busbar sections joined by a closed retained breaker ({@code COUPLER_A} and
     * {@code COUPLER_B}), and the line is connected to the second busbar section through a breaker.
     */
    static Network createNodeBreakerWithCouplers() {
        Network network = Network.create("nodeBreakerWithCouplers", "test");
        network.setCaseDate(ZonedDateTime.parse("2026-01-01T00:00:00.000+01:00"));
        addNodeBreakerVoltageLevelWithCoupler(network, "A");
        addNodeBreakerVoltageLevelWithCoupler(network, "B");
        network.newLine()
               .setId("LINE")
               .setVoltageLevel1("VL_A")
               .setNode1(2)
               .setVoltageLevel2("VL_B")
               .setNode2(2)
               .setR(1)
               .setX(10)
               .setG1(0)
               .setB1(0)
               .setG2(0)
               .setB2(0)
               .add();
        return network;
    }

    /**
     * Add a substation {@code S_<name>} and a 380 kV node-breaker voltage level {@code VL_<name>}, with busbar sections
     * {@code BBS_<name>1} (node 0) and {@code BBS_<name>2} (node 1), the closed retained breaker {@code COUPLER_<name>}
     * between them, and the breaker {@code BREAKER_<name>} between node 1 and node 2, left free for a branch.
     *
     * @param network the network to add the substation to
     * @param name the suffix of the ids
     */
    private static void addNodeBreakerVoltageLevelWithCoupler(Network network, String name) {
        VoltageLevel voltageLevel = network.newSubstation()
                                           .setId("S_" + name)
                                           .setCountry(Country.NL)
                                           .add()
                                           .newVoltageLevel()
                                           .setId("VL_" + name)
                                           .setNominalV(380)
                                           .setTopologyKind(TopologyKind.NODE_BREAKER)
                                           .add();
        voltageLevel.getNodeBreakerView().newBusbarSection()
                    .setId("BBS_" + name + "1")
                    .setNode(0)
                    .add();
        voltageLevel.getNodeBreakerView().newBusbarSection()
                    .setId("BBS_" + name + "2")
                    .setNode(1)
                    .add();
        voltageLevel.getNodeBreakerView().newBreaker()
                    .setId("COUPLER_" + name)
                    .setNode1(0)
                    .setNode2(1)
                    .setRetained(true)
                    .setOpen(false)
                    .add();
        voltageLevel.getNodeBreakerView().newBreaker()
                    .setId("BREAKER_" + name)
                    .setNode1(1)
                    .setNode2(2)
                    .setOpen(false)
                    .add();
    }

    /**
     * Three bus-breaker areas, with load flow results (bus voltages) where only the Dutch main area is energized.
     *
     * @return the network
     */
    static Network createWithDeenergizedBranches() {
        Network network = Network.create("deenergizedBranches", "test");
        network.setCaseDate(ZonedDateTime.parse("2026-01-01T00:00:00.000+01:00"));

        // Dutch main area, energized
        Substation mainSubstation = addSubstation(network, "NA", Country.NL);
        VoltageLevel main380 = addBusBreakerVoltageLevel(mainSubstation, "NA1", 380, "NA____11", "NA____12", "NA____13");
        VoltageLevel main220 = addBusBreakerVoltageLevel(mainSubstation, "NA2", 220, "NA____21");
        addLine(network, "NA____11", "NA____12", "1");
        addSwitch(main380, "NA____11", "NA____13", false);
        addTransformer(mainSubstation, "NA____11", "NA____21");
        main380.getBusBreakerView().getBus("NA____11").setV(380);
        main380.getBusBreakerView().getBus("NA____12").setV(380);
        // NA____13 has no voltage, but it is merged with NA____11 through the closed coupler
        main220.getBusBreakerView().getBus("NA____21").setV(220);

        // Belgian area, without voltage
        Substation belgianSubstation = addSubstation(network, "BD", Country.BE);
        VoltageLevel belgian380 = addBusBreakerVoltageLevel(belgianSubstation, "BD1", 380, "BD____11", "BD____12", "BD____13");
        addBusBreakerVoltageLevel(belgianSubstation, "BD2", 220, "BD____21");
        addLine(network, "BD____11", "BD____12", "1");
        // Equivalent element
        addLine(network, "BD____11", "BD____12", "2").setFictitious(true);
        // Already out of operation
        addLine(network, "BD____11", "BD____12", "3").getTerminal2().disconnect();
        addSwitch(belgian380, "BD____11", "BD____13", false);
        // Already out of operation
        addSwitch(belgian380, "BD____12", "BD____13", true);
        addTransformer(belgianSubstation, "BD____11", "BD____21");
        // Unpaired boundary lines, the second one exported as a coupler
        addBoundaryLine(belgian380, "BD____12", "XD____11");
        addBoundaryLine(belgian380, "BD____13", "XC____11").setProperty(UcteConverterConstants.IS_COUPLER_PROPERTY_KEY, "true");

        // Tie line between the Dutch main area and the Belgian area, through the X-node XT____11
        addBoundaryLine(main380, "NA____11", "XT____11");
        addBoundaryLine(belgian380, "BD____11", "XT____11");
        network.newTieLine()
               .setId("XT____11")
               .setBoundaryLine1("NA____11 XT____11 1")
               .setBoundaryLine2("BD____11 XT____11 1")
               .add();

        // Dutch island, without voltage
        Substation islandSubstation = addSubstation(network, "NI", Country.NL);
        addBusBreakerVoltageLevel(islandSubstation, "NI1", 380, "NI____11", "NI____12");
        addLine(network, "NI____11", "NI____12", "1");
        return network;
    }

    /**
     * Add a substation.
     *
     * @param network the network to add the substation to
     * @param id the id of the substation
     * @param country the country of the substation
     * @return the substation
     */
    private static Substation addSubstation(Network network, String id, Country country) {
        return network.newSubstation()
                      .setId(id)
                      .setCountry(country)
                      .add();
    }

    /**
     * Add a bus-breaker voltage level and its buses.
     *
     * @param substation the substation to add the voltage level to
     * @param id the id of the voltage level
     * @param nominalV the nominal voltage of the voltage level, in kV
     * @param busIds the ids of the buses of the voltage level
     * @return the voltage level
     */
    private static VoltageLevel addBusBreakerVoltageLevel(Substation substation, String id, double nominalV,
                                                          String... busIds) {
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                                              .setId(id)
                                              .setNominalV(nominalV)
                                              .setTopologyKind(TopologyKind.BUS_BREAKER)
                                              .add();
        for (String busId : busIds) {
            voltageLevel.getBusBreakerView().newBus()
                        .setId(busId)
                        .add();
        }
        return voltageLevel;
    }

    /**
     * Add a line {@code <bus1> <bus2> <orderCode>} between two buses of the bus-breaker view.
     *
     * @param network the network to add the line to
     * @param bus1 the id of the bus of side 1
     * @param bus2 the id of the bus of side 2
     * @param orderCode the order code of the line
     * @return the line
     */
    private static Line addLine(Network network, String bus1, String bus2, String orderCode) {
        return network.newLine()
                      .setId(bus1 + " " + bus2 + " " + orderCode)
                      .setVoltageLevel1(network.getBusBreakerView().getBus(bus1).getVoltageLevel().getId())
                      .setBus1(bus1)
                      .setVoltageLevel2(network.getBusBreakerView().getBus(bus2).getVoltageLevel().getId())
                      .setBus2(bus2)
                      .setR(1)
                      .setX(10)
                      .setG1(0)
                      .setB1(0)
                      .setG2(0)
                      .setB2(0)
                      .add();
    }

    /**
     * Add a switch {@code <bus1> <bus2> 1} between two buses of a bus-breaker voltage level.
     *
     * @param voltageLevel the voltage level to add the switch to
     * @param bus1 the id of the bus of side 1
     * @param bus2 the id of the bus of side 2
     * @param open true if the switch is open
     */
    private static void addSwitch(VoltageLevel voltageLevel, String bus1, String bus2, boolean open) {
        voltageLevel.getBusBreakerView().newSwitch()
                    .setId(bus1 + " " + bus2 + " 1")
                    .setBus1(bus1)
                    .setBus2(bus2)
                    .setOpen(open)
                    .add();
    }

    /**
     * Add a 380/220 kV two-winding transformer {@code <bus1> <bus2> 1}.
     *
     * @param substation the substation to add the transformer to
     * @param bus1 the id of the 380 kV bus
     * @param bus2 the id of the 220 kV bus
     */
    private static void addTransformer(Substation substation, String bus1, String bus2) {
        Network network = substation.getNetwork();
        substation.newTwoWindingsTransformer()
                  .setId(bus1 + " " + bus2 + " 1")
                  .setVoltageLevel1(network.getBusBreakerView().getBus(bus1).getVoltageLevel().getId())
                  .setBus1(bus1)
                  .setVoltageLevel2(network.getBusBreakerView().getBus(bus2).getVoltageLevel().getId())
                  .setBus2(bus2)
                  .setRatedU1(380)
                  .setRatedU2(220)
                  .setR(1)
                  .setX(10)
                  .setG(0)
                  .setB(0)
                  .add();
    }

    /**
     * Add a boundary line {@code <bus> <xnode> 1} from a bus to an X-node.
     *
     * @param voltageLevel the voltage level to add the boundary line to
     * @param bus the id of the bus
     * @param xnode the code of the X-node, used as pairing key
     * @return the boundary line
     */
    private static BoundaryLine addBoundaryLine(VoltageLevel voltageLevel, String bus, String xnode) {
        return voltageLevel.newBoundaryLine()
                           .setId(bus + " " + xnode + " 1")
                           .setBus(bus)
                           .setR(1)
                           .setX(10)
                           .setG(0)
                           .setB(0)
                           .setP0(0)
                           .setQ0(0)
                           .setPairingKey(xnode)
                           .add();
    }
}
