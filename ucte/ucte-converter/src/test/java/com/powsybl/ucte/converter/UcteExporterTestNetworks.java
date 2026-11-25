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
     * Voltage levels without substation ("orphans") as met in CGMES imports, with UCTE ids.
     */
    static Network createWithOrphanVoltageLevelsUcteIds() {
        Network network = newNetwork();
        String a = newSubstationBus(network, "AAAAA", Country.NL, "NAAAAA11");
        String b = newSubstationBus(network, "BBBBB", Country.NL, "NBBBBB11");
        String c = newSubstationBus(network, "CCCCC", Country.NL, "NCCCCC11");
        // Not connected to anything: the network has two countries
        newSubstationBus(network, "DDDDD", Country.BE, "BDDDDD11");

        // T-line junction, linked to the three NL substations
        String junction = newBus(newOrphanVoltageLevel(network, "TJUNC"), "NTJUNC11");
        newLine(network, "NAAAAA11 NTJUNC11 1", a, junction);
        newLine(network, "NBBBBB11 NTJUNC11 1", b, junction);
        newLine(network, "NCCCCC11 NTJUNC11 1", c, junction);

        // Chained line segments A - SEGMT bus 1 - SEGMT bus 2 - B, both intermediate buses in one voltage level
        VoltageLevel segments = newOrphanVoltageLevel(network, "SEGMT");
        String segment1 = newBus(segments, "NSEGMT11");
        String segment2 = newBus(segments, "NSEGMT12");
        newLine(network, "NAAAAA11 NSEGMT11 1", a, segment1);
        newLine(network, "NSEGMT11 NSEGMT12 1", segment1, segment2);
        newLine(network, "NSEGMT12 NBBBBB11 1", segment2, b);

        // Chained line segments A - SEGAA - SEGBB - B, intermediate buses in two voltage levels
        String segmentA = newBus(newOrphanVoltageLevel(network, "SEGAA"), "NSEGAA11");
        String segmentB = newBus(newOrphanVoltageLevel(network, "SEGBB"), "NSEGBB11");
        newLine(network, "NAAAAA11 NSEGAA11 1", a, segmentA);
        newLine(network, "NSEGAA11 NSEGBB11 1", segmentA, segmentB);
        newLine(network, "NSEGBB11 NBBBBB11 1", segmentB, b);

        // Linked to A, two buses joined by a closed switch, a load on bus 1
        VoltageLevel orphan = newOrphanVoltageLevel(network, "ORPHA");
        String orphan1 = newBus(orphan, "NORPHA11");
        String orphan2 = newBus(orphan, "NORPHA12");
        orphan.getBusBreakerView().newSwitch().setId("NORPHA11 NORPHA12 1").setBus1(orphan1).setBus2(orphan2).setOpen(false).add();
        addLoad(orphan, "LOAD_ORPHA", orphan1, 42, 7);
        newLine(network, "NAAAAA11 NORPHA11 1", a, orphan1);

        // Isolated, one bus and no equipment
        newBus(newOrphanVoltageLevel(network, "EMPTY"), "NEMPTY11");
        return network;
    }

    /**
     * Voltage levels without substation ("orphans") as met in CGMES imports, with free ids.
     */
    static Network createWithOrphanVoltageLevels() {
        Network network = newNetwork();
        String a = newSubstationBus(network, "AAAAA", Country.NL, "BUS_AAAAA_1");
        String b = newSubstationBus(network, "BBBBB", Country.NL, "BUS_BBBBB_1");
        String c = newSubstationBus(network, "CCCCC", Country.NL, "BUS_CCCCC_1");
        // Not connected to anything: the network has two countries
        newSubstationBus(network, "DDDDD", Country.BE, "BUS_DDDDD_1");

        // T-line junction, linked to the three NL substations
        String junction = newBus(newOrphanVoltageLevel(network, "TJUNC"), "BUS_TJUNC_1");
        newLine(network, "BR_BUS_AAAAA_1_BUS_TJUNC_1", a, junction);
        newLine(network, "BR_BUS_BBBBB_1_BUS_TJUNC_1", b, junction);
        newLine(network, "BR_BUS_CCCCC_1_BUS_TJUNC_1", c, junction);

        // Chained line segments A - SEGMT bus 1 - SEGMT bus 2 - B, both intermediate buses in one voltage level
        VoltageLevel segments = newOrphanVoltageLevel(network, "SEGMT");
        String segment1 = newBus(segments, "BUS_SEGMT_1");
        String segment2 = newBus(segments, "BUS_SEGMT_2");
        newLine(network, "BR_BUS_AAAAA_1_BUS_SEGMT_1", a, segment1);
        newLine(network, "BR_BUS_SEGMT_1_BUS_SEGMT_2", segment1, segment2);
        newLine(network, "BR_BUS_SEGMT_2_BUS_BBBBB_1", segment2, b);

        // Chained line segments A - SEGAA - SEGBB - B, intermediate buses in two voltage levels
        String segmentA = newBus(newOrphanVoltageLevel(network, "SEGAA"), "BUS_SEGAA_1");
        String segmentB = newBus(newOrphanVoltageLevel(network, "SEGBB"), "BUS_SEGBB_1");
        newLine(network, "BR_BUS_AAAAA_1_BUS_SEGAA_1", a, segmentA);
        newLine(network, "BR_BUS_SEGAA_1_BUS_SEGBB_1", segmentA, segmentB);
        newLine(network, "BR_BUS_SEGBB_1_BUS_BBBBB_1", segmentB, b);

        // Linked to A, two buses joined by a closed switch, a load on bus 1
        VoltageLevel orphan = newOrphanVoltageLevel(network, "ORPHA");
        String orphan1 = newBus(orphan, "BUS_ORPHA_1");
        String orphan2 = newBus(orphan, "BUS_ORPHA_2");
        orphan.getBusBreakerView().newSwitch().setId("BR_BUS_ORPHA_1_BUS_ORPHA_2").setBus1(orphan1).setBus2(orphan2).setOpen(false).add();
        addLoad(orphan, "LOAD_ORPHA", orphan1, 42, 7);
        newLine(network, "BR_BUS_AAAAA_1_BUS_ORPHA_1", a, orphan1);

        // Isolated, one bus and no equipment
        newBus(newOrphanVoltageLevel(network, "EMPTY"), "BUS_EMPTY_1");
        return network;
    }

    /**
     * T-line around an orphan junction, with free ids.
     *
     * @param countryB the country of substation B, none if null
     */
    static Network createWithTLine(Country countryB) {
        Network network = newNetwork();
        String a = newSubstationBus(network, "AAAAA", Country.NL, "BUS_AAAAA_1");
        String b = newSubstationBus(network, "BBBBB", countryB, "BUS_BBBBB_1");
        String c = newSubstationBus(network, "CCCCC", Country.NL, "BUS_CCCCC_1");
        String junction = newBus(newOrphanVoltageLevel(network, "TJUNC"), "BUS_TJUNC_1");
        newLine(network, "BR_BUS_AAAAA_1_BUS_TJUNC_1", a, junction);
        newLine(network, "BR_BUS_BBBBB_1_BUS_TJUNC_1", b, junction);
        newLine(network, "BR_BUS_CCCCC_1_BUS_TJUNC_1", c, junction);
        return network;
    }

    static Network createWithSingleCountryAndUnconnectedOrphan() {
        Network network = newNetwork();
        newSubstationBus(network, "AAAAA", Country.NL, "BUS_AAAAA_1");
        // Unconnected orphan holding a load
        VoltageLevel alone = newOrphanVoltageLevel(network, "ALONE");
        addLoad(alone, "LOAD_ALONE", newBus(alone, "BUS_ALONE_1"), 42, 7);
        return network;
    }

    static Network createWithIsolatedOrphansHoldingEquipment() {
        Network network = createWithTwoCountries();

        // Two orphans linked to each other only, one holding a load
        VoltageLevel isolated1 = newOrphanVoltageLevel(network, "ISOL1");
        String i1 = newBus(isolated1, "BUS_ISOL1_1");
        String i2 = newBus(newOrphanVoltageLevel(network, "ISOL2"), "BUS_ISOL2_1");
        addLoad(isolated1, "LOAD_ISOL1", i1, 42, 7);
        newLine(network, "BR_BUS_ISOL1_1_BUS_ISOL2_1", i1, i2);

        // Orphan holding an unpaired boundary line
        VoltageLevel withBoundaryLine = newOrphanVoltageLevel(network, "XORPH");
        newBus(withBoundaryLine, "BUS_XORPH_1");
        newBoundaryLine(withBoundaryLine, "BL_X", null);

        // Orphan holding half of a tie line, the other half in substation A
        VoltageLevel withTieLine = newOrphanVoltageLevel(network, "XTIEL");
        newBus(withTieLine, "BUS_XTIEL_1");
        newBoundaryLine(withTieLine, "BL_1", "XKEY");
        newBoundaryLine(network.getVoltageLevel("VL_AAAAA"), "BL_2", "XKEY");
        network.newTieLine().setId("TL").setBoundaryLine1("BL_1").setBoundaryLine2("BL_2").add();
        return network;
    }

    static Network createWithIsolatedOrphansWithoutEquipment() {
        Network network = createWithTwoCountries();
        // Bus-breaker orphan holding one bus
        newBus(newOrphanVoltageLevel(network, "EMPTY"), "BUS_EMPTY_1");
        // Node-breaker orphan holding only a busbar section
        network.newVoltageLevel()
                .setId("VL_NBBBS")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add()
                .getNodeBreakerView().newBusbarSection().setId("BBS").setNode(0).add();
        return network;
    }

    private static Network createWithTwoCountries() {
        Network network = newNetwork();
        String a = newSubstationBus(network, "AAAAA", Country.NL, "BUS_AAAAA_1");
        String b = newSubstationBus(network, "BBBBB", Country.BE, "BUS_BBBBB_1");
        newLine(network, "BR_BUS_AAAAA_1_BUS_BBBBB_1", a, b);
        return network;
    }

    /**
     * Add a node-breaker T-line to a network created by {@link #createWithOrphanVoltageLevels()}.
     */
    static void addNodeBreakerTLine(Network network) {
        // Orphan node-breaker junction, its nodes 1, 2 and 3 linked to substations A, B and C
        VoltageLevel junction = network.newVoltageLevel()
                .setId("VL_NBJUN")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        int node = 1;
        for (String name : new String[] {"AAAAA", "BBBBB", "CCCCC"}) {
            String bus = "BUS_" + name + "_1";
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

    private static Network newNetwork() {
        Network network = Network.create("orphanVoltageLevels", "test");
        network.setCaseDate(ZonedDateTime.parse("2026-01-01T00:00:00.000+01:00"));
        return network;
    }

    /**
     * Add substation {@code S_<name>} holding 380 kV bus-breaker voltage level {@code VL_<name>} with one bus.
     */
    private static String newSubstationBus(Network network, String name, Country country, String busId) {
        SubstationAdder substationAdder = network.newSubstation().setId("S_" + name);
        if (country != null) {
            substationAdder.setCountry(country);
        }
        VoltageLevel voltageLevel = substationAdder.add().newVoltageLevel()
                .setId("VL_" + name)
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        return newBus(voltageLevel, busId);
    }

    /**
     * Add 380 kV bus-breaker voltage level {@code VL_<name>} without substation.
     */
    static VoltageLevel newOrphanVoltageLevel(Network network, String name) {
        return network.newVoltageLevel()
                .setId("VL_" + name)
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
    }

    static String newBus(VoltageLevel voltageLevel, String busId) {
        voltageLevel.getBusBreakerView().newBus().setId(busId).add();
        return busId;
    }

    static Line newLine(Network network, String id, String bus1, String bus2) {
        return network.newLine()
                .setId(id)
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

    private static void newBoundaryLine(VoltageLevel voltageLevel, String id, String pairingKey) {
        String bus = voltageLevel.getBusBreakerView().getBuses().iterator().next().getId();
        voltageLevel.newBoundaryLine().setId(id).setBus(bus).setConnectableBus(bus)
                .setR(1).setX(10).setG(0).setB(0).setP0(0).setQ0(0).setPairingKey(pairingKey).add();
    }
}
