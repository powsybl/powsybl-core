/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;
import java.time.ZonedDateTime;

/**
 * This is a network test based on Eurostag tutorial example 1.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class EurostagTutorialExample1Factory {

    public static final String VLGEN = "VLGEN";
    public static final String VLLOAD = "VLLOAD";
    public static final String CASE_DATE = "2018-01-01T11:00:00+01:00";
    public static final String BOUNDARY_LINE_XNODE1_1 = "NHV1_XNODE1";
    public static final String BOUNDARY_LINE_XNODE1_2 = "XNODE1_NHV2";
    public static final String BOUNDARY_LINE_XNODE2_1 = "NHV1_XNODE2";
    public static final String BOUNDARY_LINE_XNODE2_2 = "XNODE2_NHV2";
    public static final String VLHV1 = "VLHV1";
    public static final String VLHV2 = "VLHV2";
    public static final String NHV1_NHV2_1 = "NHV1_NHV2_1";
    public static final String NHV1_NHV2_2 = "NHV1_NHV2_2";
    public static final String NGEN_NHV1 = "NGEN_NHV1";
    public static final String NHV2_NLOAD = "NHV2_NLOAD";
    public static final String XNODE_1 = "XNODE1";
    public static final String NGEN_V2_NHV1 = "NGEN_V2_NHV1";
    public static final String NGEN = "NGEN";
    public static final String NHV1 = "NHV1";
    public static final String NHV2 = "NHV2";

    private EurostagTutorialExample1Factory() {
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Network network = networkFactory.createNetwork("sim1", "test");
        Substation p1 = network.newSubstation()
                .setId("P1")
                .setCountry(Country.FR)
                .setTso("RTE")
                .setGeographicalTags("A")
            .add();
        Substation p2 = network.newSubstation()
                .setId("P2")
                .setCountry(Country.FR)
                .setTso("RTE")
                .setGeographicalTags("B")
            .add();
        VoltageLevel vlgen = p1.newVoltageLevel()
                .setId(VLGEN)
                .setNominalV(24.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        VoltageLevel vlhv1 = p1.newVoltageLevel()
                .setId(VLHV1)
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        VoltageLevel vlhv2 = p2.newVoltageLevel()
                .setId(VLHV2)
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        VoltageLevel vlload = p2.newVoltageLevel()
                .setId(VLLOAD)
                .setNominalV(150.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        Bus ngen = vlgen.getBusBreakerView().newBus()
                .setId(NGEN)
            .add();
        Bus nhv1 = vlhv1.getBusBreakerView().newBus()
                .setId(NHV1)
            .add();
        Bus nhv2 = vlhv2.getBusBreakerView().newBus()
                .setId(NHV2)
            .add();
        Bus nload = vlload.getBusBreakerView().newBus()
                .setId("NLOAD")
            .add();
        network.newLine()
                .setId(NHV1_NHV2_1)
                .setVoltageLevel1(vlhv1.getId())
                .setBus1(nhv1.getId())
                .setConnectableBus1(nhv1.getId())
                .setVoltageLevel2(vlhv2.getId())
                .setBus2(nhv2.getId())
                .setConnectableBus2(nhv2.getId())
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
            .add();
        network.newLine()
                .setId(NHV1_NHV2_2)
                .setVoltageLevel1(vlhv1.getId())
                .setBus1(nhv1.getId())
                .setConnectableBus1(nhv1.getId())
                .setVoltageLevel2(vlhv2.getId())
                .setBus2(nhv2.getId())
                .setConnectableBus2(nhv2.getId())
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
            .add();
        int zb380 = 380 * 380 / 100;
        p1.newTwoWindingsTransformer()
                .setId(NGEN_NHV1)
                .setVoltageLevel1(vlgen.getId())
                .setBus1(ngen.getId())
                .setConnectableBus1(ngen.getId())
                .setRatedU1(24.0)
                .setVoltageLevel2(vlhv1.getId())
                .setBus2(nhv1.getId())
                .setConnectableBus2(nhv1.getId())
                .setRatedU2(400.0)
                .setR(0.24 / 1300 * zb380)
                .setX(Math.sqrt(10 * 10 - 0.24 * 0.24) / 1300 * zb380)
                .setG(0.0)
                .setB(0.0)
            .add();
        int zb150 = 150 * 150 / 100;
        TwoWindingsTransformer nhv2Nload = p2.newTwoWindingsTransformer()
                .setId(NHV2_NLOAD)
                .setVoltageLevel1(vlhv2.getId())
                .setBus1(nhv2.getId())
                .setConnectableBus1(nhv2.getId())
                .setRatedU1(400.0)
                .setVoltageLevel2(vlload.getId())
                .setBus2(nload.getId())
                .setConnectableBus2(nload.getId())
                .setRatedU2(158.0)
                .setR(0.21 / 1000 * zb150)
                .setX(Math.sqrt(18 * 18 - 0.21 * 0.21) / 1000 * zb150)
                .setG(0.0)
                .setB(0.0)
            .add();
        double a = (158.0 / 150.0) / (400.0 / 380.0);
        nhv2Nload.newRatioTapChanger()
                .beginStep()
                    .setRho(0.85f * a)
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                .endStep()
                .beginStep()
                    .setRho(a)
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                .endStep()
                .beginStep()
                    .setRho(1.15f * a)
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                .endStep()
                .setTapPosition(1)
                .setLoadTapChangingCapabilities(true)
                .setRegulating(true)
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setRegulationValue(158.0)
                .setTargetDeadband(0)
                .setRegulationTerminal(nhv2Nload.getTerminal2())
            .add();
        vlload.newLoad()
                .setId("LOAD")
                .setBus(nload.getId())
                .setConnectableBus(nload.getId())
                .setP0(600.0)
                .setQ0(200.0)
            .add();
        Generator generator = vlgen.newGenerator()
                .setId("GEN")
                .setBus(ngen.getId())
                .setConnectableBus(ngen.getId())
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
            .add();
        generator.newMinMaxReactiveLimits()
                .setMinQ(-9999.99)
                .setMaxQ(9999.99)
            .add();
        return network;
    }

    public static Network createWithTieLine() {
        return createWithTieLines(NetworkFactory.findDefault());
    }

    public static Network createWithTieLines(NetworkFactory networkFactory) {
        Network network = createWithLFResults(networkFactory);
        network.getLine(NHV1_NHV2_1).remove();
        network.getLine(NHV1_NHV2_2).remove();

        BoundaryLine nhv1xnode1 = network.getVoltageLevel(VLHV1).newBoundaryLine()
                .setId(BOUNDARY_LINE_XNODE1_1)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.5)
                .setX(20.0)
                .setG(1E-6)
                .setB(386E-6 / 2)
                .setPairingKey(XNODE_1)
                .setBus(NHV1)
                .add();
        BoundaryLine xnode1nhv2 = network.getVoltageLevel(VLHV2).newBoundaryLine()
                .setId(BOUNDARY_LINE_XNODE1_2)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.5)
                .setX(13.0)
                .setG(2E-6)
                .setB(386E-6 / 2)
                .setBus(NHV2)
                .setPairingKey(XNODE_1)
                .add();
        network.newTieLine()
                .setId(NHV1_NHV2_1)
                .setBoundaryLine1(nhv1xnode1.getId())
                .setBoundaryLine2(xnode1nhv2.getId())
                .add();
        BoundaryLine nhv1xnode2 = network.getVoltageLevel(VLHV1).newBoundaryLine()
                .setId(BOUNDARY_LINE_XNODE2_1)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.5)
                .setX(20.0)
                .setG(1E-6)
                .setB(386E-6 / 2)
                .setBus(NHV1)
                .setPairingKey("XNODE2")
                .add();
        BoundaryLine xnode2nhv2 = network.getVoltageLevel(VLHV2).newBoundaryLine()
                .setId(BOUNDARY_LINE_XNODE2_2)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.5)
                .setX(13.0)
                .setG(2E-6)
                .setB(386E-6 / 2)
                .setBus(NHV2)
                .setPairingKey("XNODE2")
                .add();
        network.newTieLine()
                .setId(NHV1_NHV2_2)
                .setBoundaryLine1(nhv1xnode2.getId())
                .setBoundaryLine2(xnode2nhv2.getId())
                .add();
        network.getTieLine(NHV1_NHV2_1).getBoundaryLine1().getTerminal()
                .setP(302.4440612792969)
                .setQ(98.74027252197266);
        network.getTieLine(NHV1_NHV2_1).getBoundaryLine2().getTerminal()
                .setP(-300.43389892578125)
                .setQ(-137.18849182128906);
        network.getTieLine(NHV1_NHV2_2).getBoundaryLine1().getTerminal()
                .setP(302.4440612792969)
                .setQ(98.74027252197266);
        network.getTieLine(NHV1_NHV2_2).getBoundaryLine2().getTerminal()
                .setP(-300.43389892578125)
                .setQ(-137.188491821289060);

        return network;
    }

    public static Network createWithLFResults() {
        return createWithLFResults(NetworkFactory.findDefault());
    }

    public static Network createWithLFResults(NetworkFactory factory) {
        Network network = create(factory);
        network.setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));

        network.getBusBreakerView().getBus(NGEN)
                .setV(24.500000610351563)
                .setAngle(2.3259763717651367);
        network.getBusBreakerView().getBus(NHV1)
                .setV(402.1428451538086)
                .setAngle(0.0);
        network.getBusBreakerView().getBus(NHV2)
                .setV(389.9526763916016)
                .setAngle(-3.5063576698303223);
        network.getBusBreakerView().getBus("NLOAD")
                .setV(147.57861328125)
                .setAngle(-9.614486694335938);

        network.getGenerator("GEN").getTerminal()
                .setP(-605.558349609375)
                .setQ(-225.2825164794922);
        network.getTwoWindingsTransformer(NGEN_NHV1).getTerminal1()
                .setP(605.558349609375)
                .setQ(225.2825164794922);
        network.getTwoWindingsTransformer(NGEN_NHV1).getTerminal2()
                .setP(-604.8909301757812)
                .setQ(-197.48046875);
        network.getLoad("LOAD").getTerminal()
                .setP(600.0)
                .setQ(200.0);
        network.getTwoWindingsTransformer(NHV2_NLOAD).getTerminal1()
                .setP(600.8677978515625)
                .setQ(274.3769836425781);
        network.getTwoWindingsTransformer(NHV2_NLOAD).getTerminal2()
                .setP(-600.0)
                .setQ(-200.0);
        network.getLine(NHV1_NHV2_1).getTerminal1()
                .setP(302.4440612792969)
                .setQ(98.74027252197266);
        network.getLine(NHV1_NHV2_1).getTerminal2()
                .setP(-300.43389892578125)
                .setQ(-137.18849182128906);
        network.getLine(NHV1_NHV2_2).getTerminal1()
                .setP(302.4440612792969)
                .setQ(98.74027252197266);
        network.getLine(NHV1_NHV2_2).getTerminal2()
                .setP(-300.43389892578125)
                .setQ(-137.188491821289060);

        return network;
    }

    public static Network createWithMoreGenerators() {
        return createWithMoreGenerators(NetworkFactory.findDefault());
    }

    public static Network createWithMoreGenerators(NetworkFactory networkFactory) {
        Network network = create(networkFactory);

        VoltageLevel vlgen = network.getVoltageLevel(VLGEN);
        Bus ngen = vlgen.getBusBreakerView().getBus(NGEN);

        Generator generator2 = vlgen.newGenerator()
                .setId("GEN2")
                .setBus(ngen.getId())
                .setConnectableBus(ngen.getId())
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
                .add();
        generator2.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(3.0)
                .setMaxQ(5.0)
                .setMinQ(4.0)
                .endPoint()
                .beginPoint()
                .setP(0.0)
                .setMaxQ(7.0)
                .setMinQ(6.0)
                .endPoint()
                .beginPoint()
                .setP(1.0)
                .setMaxQ(5.0)
                .setMinQ(4.0)
                .endPoint()
                .add();

        return network;
    }

    public static Network createWithFixedCurrentLimits() {
        return createWithFixedCurrentLimits(NetworkFactory.findDefault());
    }

    public static Network createWithFixedCurrentLimits(NetworkFactory networkFactory) {
        Network network = create(networkFactory);

        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));

        network.getSubstation("P2").setCountry(Country.BE);

        network.getVoltageLevel(VLGEN).newGenerator()
                .setId("GEN2")
                .setBus(NGEN)
                .setConnectableBus(NGEN)
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
                .add();

        ((Bus) network.getIdentifiable(NHV1)).setV(380).getVoltageLevel().setLowVoltageLimit(400).setHighVoltageLimit(500);
        ((Bus) network.getIdentifiable(NHV2)).setV(380).getVoltageLevel().setLowVoltageLimit(300).setHighVoltageLimit(500);

        Line line = network.getLine(NHV1_NHV2_1);
        line.getTerminal1().setP(560.0).setQ(550.0);
        line.getTerminal2().setP(-560.0).setQ(-550.0);
        line.getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits().setPermanentLimit(500).add();
        line.getOrCreateSelectedOperationalLimitsGroup2().newCurrentLimits()
                .setPermanentLimit(1100)
                .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(1500)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("N/A")
                .setAcceptableDuration(0)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();

        line = network.getLine(NHV1_NHV2_2);
        line.getTerminal1().setP(560.0).setQ(550.0);
        line.getTerminal2().setP(-560.0).setQ(-550.0);
        line.getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits()
                .setPermanentLimit(1100)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("N/A")
                .setAcceptableDuration(60)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();
        line.getOrCreateSelectedOperationalLimitsGroup2().newCurrentLimits().setPermanentLimit(500).add();

        return network;
    }

    public static final String ACTIVATED_ONE_ONE = "activated_1_1";
    public static final String ACTIVATED_ONE_TWO = "activated_1_2";
    public static final String ACTIVATED_TWO_ONE = "activated_2_1";
    public static final String ACTIVATED_TWO_TWO = "activated_2_2";
    public static final String ACTIVATED_THREE_ONE = "activated_3_1";
    public static final String NOT_ACTIVATED = "not_activated";

    /**
     * Selected limits:
     * On {@link #NHV1_NHV2_1}, side 1 (also has a {@link #NOT_ACTIVATED} group)
     * <pre>
     *      Default      activated_1_1       activated_1_2
     *      |                                ---- IT0.5 1600 (30s)
     *      |            ---- IT1 1500 (60s)
     *      |            ---- IT10 1200 (600s)
     *      |            ---- ITP 1100
     *      |                                ---- IT40 700 (2400s)
     *      | ---- ITP 500
     *      |                                ---- ITP 300
     * </pre>
     * Side 2
     * <pre>
     *      Default             activated_2_1
     *      |                   ---- IT10 1000 (600s)
     *      | ---- ITP 600      ---- ITP 600
     * </pre>
     *
     * On {@link #NHV1_NHV2_2}, side 1
     * <pre>
     *      Default
     *      | ---- IT20 1200 (1200s)
     *      | ---- ITP 1100
     * </pre>
     * Side 2 (also has a {@link #NOT_ACTIVATED} group)
     * <pre>
     *     Default              activated_2_1       activated_2_2
     *     |                    ---- IT20 600 (1200s)
     *     | ---- ITP 500
     *     |                                        ---- ITP 300
     *     |                    ---- ITP 200
     * </pre>
     */
    public static Network createWithMultipleSelectedFixedCurrentLimits() {
        return createWithMultipleSelectedFixedCurrentLimits(NetworkFactory.findDefault());
    }

    /**
     * Selected limits:
     * On {@link #NHV1_NHV2_1} a line, side 1 (also has a {@link #NOT_ACTIVATED} group)
     * <pre>
     *      Default      activated_1_1       activated_1_2
     *      |                                ---- IT0.5 1600 (30s)
     *      |            ---- IT1 1500 (60s)
     *      |            ---- IT10 1200 (600s)
     *      |            ---- ITP 1100
     *      |                                ---- IT40 700 (2400s)
     *      | ---- ITP 500
     *      |                                ---- ITP 300
     * </pre>
     * Side 2
     * <pre>
     *      Default             activated_2_1
     *      |                   ---- IT10 1000 (600s)
     *      | ---- ITP 600      ---- ITP 600
     * </pre>
     *
     * On {@link #NHV1_NHV2_2}, a line, side 1
     * <pre>
     *      Default
     *      | ---- IT20 1200 (1200s)
     *      | ---- ITP 1100
     * </pre>
     * Side 2 (also has a {@link #NOT_ACTIVATED} group)
     * <pre>
     *     Default              activated_2_1       activated_2_2
     *     |                    ---- IT20 600 (1200s)
     *     | ---- ITP 500
     *     |                                        ---- ITP 300
     *     |                    ---- ITP 200
     * </pre>
     */
    public static Network createWithMultipleSelectedFixedCurrentLimits(NetworkFactory networkFactory) {
        Network network = create(networkFactory);

        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));

        Line line = network.getLine(NHV1_NHV2_1);
        line.getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits().setPermanentLimit(500).add();
        line.newOperationalLimitsGroup1(ACTIVATED_ONE_ONE).newCurrentLimits()
                .setPermanentLimit(1100)
                .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(1500)
                .endTemporaryLimit()
                .add();

        line.newOperationalLimitsGroup1(ACTIVATED_ONE_TWO).newCurrentLimits()
                .setPermanentLimit(300)
                .beginTemporaryLimit()
                .setName("40'")
                .setAcceptableDuration(40 * 60)
                .setValue(700)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("0.5'")
                .setAcceptableDuration(30)
                .setValue(1600)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("N/A")
                .setAcceptableDuration(0)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();

        line.addSelectedOperationalLimitsGroups(TwoSides.ONE, ACTIVATED_ONE_ONE, ACTIVATED_ONE_TWO);

        line.newOperationalLimitsGroup1(NOT_ACTIVATED).newCurrentLimits()
            .setPermanentLimit(400)
            .beginTemporaryLimit()
            .setValue(600)
            .setName("30'")
            .setAcceptableDuration(30 * 60)
            .endTemporaryLimit()
            .add();

        line.getOrCreateSelectedOperationalLimitsGroup2().newCurrentLimits().setPermanentLimit(600);

        line.newOperationalLimitsGroup2(ACTIVATED_TWO_ONE).newCurrentLimits()
                .setPermanentLimit(600)
                .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(60 * 10)
                .setValue(1000)
                .endTemporaryLimit()
                .add();

        line.addSelectedOperationalLimitsGroups(TwoSides.TWO, ACTIVATED_TWO_ONE);

        line = network.getLine(NHV1_NHV2_2);
        line.getTerminal1().setP(560.0).setQ(550.0);
        line.getTerminal2().setP(-560.0).setQ(-550.0);
        line.getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits()
                .setPermanentLimit(1100)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("N/A")
                .setAcceptableDuration(60)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();
        line.getOrCreateSelectedOperationalLimitsGroup2().newCurrentLimits().setPermanentLimit(500).add();

        line.newOperationalLimitsGroup2(ACTIVATED_TWO_ONE).newCurrentLimits()
                .setPermanentLimit(200)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(600)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("N/A")
                .setAcceptableDuration(0)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();

        line.newOperationalLimitsGroup2(ACTIVATED_TWO_TWO).newCurrentLimits()
                .setPermanentLimit(300)
                .beginTemporaryLimit()
                .setName("N/A")
                .setAcceptableDuration(0)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();

        line.newOperationalLimitsGroup2(NOT_ACTIVATED).newCurrentLimits()
                .setPermanentLimit(400)
                .add();

        line.addSelectedOperationalLimitsGroups(TwoSides.TWO, ACTIVATED_TWO_ONE, ACTIVATED_TWO_TWO);

        return network;
    }

    /**
     * Selected limits:
     * On {@link #NGEN_V2_NHV1}, a three-winding transformer, side 3 (also has a {@link #NOT_ACTIVATED} group)
     * <pre>
     *      Default      activated_3_1
     *      |            ---- IT45 400 (2700s)
     *      |            ---- ITP 350
     *      | ---- ITP 250
     * </pre>
     */
    public static Network createWithMultipleSelectedFixedActivePowerLimits() {
        Network network = createWith3wTransformer();

        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));

        ThreeWindingsTransformer threeWindingsTransformer = network.getThreeWindingsTransformer(NGEN_V2_NHV1);

        ThreeWindingsTransformer.Leg legThree = threeWindingsTransformer.getLeg(ThreeSides.THREE);

        legThree.getOrCreateSelectedOperationalLimitsGroup()
                .newActivePowerLimits()
                .setPermanentLimit(250)
                .add();

        legThree.newOperationalLimitsGroup(ACTIVATED_THREE_ONE)
                .newActivePowerLimits()
                .setPermanentLimit(350)
                .beginTemporaryLimit()
                .setValue(400)
                .setName("45'")
                .setAcceptableDuration(45 * 60)
                .endTemporaryLimit()
                .add();

        legThree.addSelectedOperationalLimitsGroups(ACTIVATED_THREE_ONE);

        legThree.newOperationalLimitsGroup(NOT_ACTIVATED)
            .newActivePowerLimits()
            .setPermanentLimit(300)
            .beginTemporaryLimit()
            .setName("25'")
            .setValue(550)
            .setAcceptableDuration(25 * 60)
            .endTemporaryLimit()
            .add();

        return network;
    }

    /**
     * Selected limits:
     * On {@link #NGEN_NHV1}, a two-winding transformer, side 2
     * <pre>
     *      activated_2_1             activated_2_2
     *                                ---- IT20 250 (1200s)
     *      ---- IT10 240 (600s)      ---- ITP 240
     *      ---- ITP 230
     * </pre>
     */
    public static Network createWithMultipleSelectedFixedApparentPowerLimits() {
        return createWithMultipleSelectedFixedApparentPowerLimits(NetworkFactory.findDefault());
    }

    /**
     * Selected limits:
     * On {@link #NGEN_NHV1}, a two-winding transformer, side 2
     * <pre>
     *      activated_2_1             activated_2_2
     *                                ---- IT20 250 (1200s)
     *      ---- IT10 240 (600s)      ---- ITP 240
     *      ---- ITP 230
     * </pre>
     */
    public static Network createWithMultipleSelectedFixedApparentPowerLimits(NetworkFactory networkFactory) {
        Network network = create(networkFactory);

        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));

        TwoWindingsTransformer twoWindingsTransformer = network.getTwoWindingsTransformer(NGEN_NHV1);

        twoWindingsTransformer.newOperationalLimitsGroup2(ACTIVATED_TWO_ONE)
                .newApparentPowerLimits()
                .setPermanentLimit(230)
                .beginTemporaryLimit()
                .setValue(240)
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .endTemporaryLimit()
                .add();

        twoWindingsTransformer.newOperationalLimitsGroup2(ACTIVATED_TWO_TWO)
                .newApparentPowerLimits()
                .setPermanentLimit(240)
                .beginTemporaryLimit()
                .setValue(250)
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .endTemporaryLimit()
                .add();

        twoWindingsTransformer.addSelectedOperationalLimitsGroups(TwoSides.TWO, ACTIVATED_TWO_ONE, ACTIVATED_TWO_TWO);

        return network;
    }

    public static Network createWithFixedLimits() {
        return createWithFixedLimits(NetworkFactory.findDefault());
    }

    public static Network createWithFixedLimits(NetworkFactory networkFactory) {
        Network network = create(networkFactory);

        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));

        network.getSubstation("P2").setCountry(Country.BE);

        network.getVoltageLevel(VLGEN).newGenerator()
               .setId("GEN2")
               .setBus(NGEN)
               .setConnectableBus(NGEN)
               .setMinP(-9999.99)
               .setMaxP(9999.99)
               .setVoltageRegulatorOn(true)
               .setTargetV(24.5)
               .setTargetP(607.0)
               .setTargetQ(301.0)
               .add();

        ((Bus) network.getIdentifiable(NHV1)).setV(380).getVoltageLevel().setLowVoltageLimit(400).setHighVoltageLimit(500);
        ((Bus) network.getIdentifiable(NHV2)).setV(380).getVoltageLevel().setLowVoltageLimit(300).setHighVoltageLimit(500);

        Line line = network.getLine(NHV1_NHV2_1);
        line.getTerminal1().setP(560.0).setQ(550.0);
        line.getTerminal2().setP(560.0).setQ(550.0);
        line.getOrCreateSelectedOperationalLimitsGroup1().newActivePowerLimits().setPermanentLimit(500).add();
        line.getOrCreateSelectedOperationalLimitsGroup2().newActivePowerLimits()
            .setPermanentLimit(1100)
            .beginTemporaryLimit()
            .setName("10'")
            .setAcceptableDuration(10 * 60)
            .setValue(1200)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName("1'")
            .setAcceptableDuration(60)
            .setValue(1500)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName("N/A")
            .setAcceptableDuration(0)
            .setValue(Double.MAX_VALUE)
            .endTemporaryLimit()
            .add();

        line.getOrCreateSelectedOperationalLimitsGroup1().newApparentPowerLimits().setPermanentLimit(500).add();
        line.getOrCreateSelectedOperationalLimitsGroup2().newApparentPowerLimits()
            .setPermanentLimit(1100)
            .beginTemporaryLimit()
            .setName("10'")
            .setAcceptableDuration(10 * 60)
            .setValue(1200)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName("1'")
            .setAcceptableDuration(60)
            .setValue(1500)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName("N/A")
            .setAcceptableDuration(0)
            .setValue(Double.MAX_VALUE)
            .endTemporaryLimit()
            .add();

        line = network.getLine(NHV1_NHV2_2);
        line.getTerminal1().setP(560.0).setQ(550.0);
        line.getTerminal2().setP(560.0).setQ(550.0);
        line.getOrCreateSelectedOperationalLimitsGroup1().newActivePowerLimits()
            .setPermanentLimit(1100)
            .beginTemporaryLimit()
            .setName("20'")
            .setAcceptableDuration(20 * 60)
            .setValue(1200)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName("N/A")
            .setAcceptableDuration(60)
            .setValue(Double.MAX_VALUE)
            .endTemporaryLimit()
            .add();
        line.getOrCreateSelectedOperationalLimitsGroup2().newActivePowerLimits().setPermanentLimit(500).add();

        line.getOrCreateSelectedOperationalLimitsGroup1().newApparentPowerLimits()
            .setPermanentLimit(1100)
            .beginTemporaryLimit()
            .setName("20'")
            .setAcceptableDuration(20 * 60)
            .setValue(1200)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName("N/A")
            .setAcceptableDuration(60)
            .setValue(Double.MAX_VALUE)
            .endTemporaryLimit()
            .add();
        line.getOrCreateSelectedOperationalLimitsGroup2().newApparentPowerLimits().setPermanentLimit(500).add();

        return network;
    }

    public static Network createWithFixedCurrentLimitsOnBoundaryLines() {
        return createWithFixedCurrentLimitsOnBoundaryLines(NetworkFactory.findDefault());
    }

    public static Network createWithFixedCurrentLimitsOnBoundaryLines(NetworkFactory networkFactory) {
        Network network = createWithTieLines(networkFactory);

        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));

        network.getSubstation("P2").setCountry(Country.BE);

        network.getVoltageLevel(VLGEN).newGenerator()
                .setId("GEN2")
                .setBus(NGEN)
                .setConnectableBus(NGEN)
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
                .add();

        ((Bus) network.getIdentifiable(NHV1)).setV(380).getVoltageLevel().setLowVoltageLimit(400).setHighVoltageLimit(500);
        ((Bus) network.getIdentifiable(NHV2)).setV(380).getVoltageLevel().setLowVoltageLimit(300).setHighVoltageLimit(500);

        BoundaryLine boundaryLine1 = network.getBoundaryLine(BOUNDARY_LINE_XNODE1_1);
        BoundaryLine boundaryLine2 = network.getBoundaryLine(BOUNDARY_LINE_XNODE1_2);
        boundaryLine1.getTerminal().setP(560.0).setQ(550.0);
        boundaryLine2.getTerminal().setP(-560.0).setQ(-550.0);
        boundaryLine1.getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits().setPermanentLimit(500).add();
        boundaryLine2.getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits()
                .setPermanentLimit(1100)
                .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(1500)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("N/A")
                .setAcceptableDuration(0)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();

        boundaryLine1 = network.getBoundaryLine(BOUNDARY_LINE_XNODE2_1);
        boundaryLine2 = network.getBoundaryLine(BOUNDARY_LINE_XNODE2_2);
        boundaryLine1.getTerminal().setP(560.0).setQ(550.0);
        boundaryLine2.getTerminal().setP(-560.0).setQ(-550.0);
        boundaryLine1.getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits()
                .setPermanentLimit(1100)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("N/A")
                .setAcceptableDuration(60)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();
        boundaryLine2.getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits().setPermanentLimit(500).add();

        return network;
    }

    public static Network createWithFixedLimitsOnBoundaryLines() {
        return createWithFixedLimitsOnBoundaryLines(NetworkFactory.findDefault());
    }

    public static Network createWithFixedLimitsOnBoundaryLines(NetworkFactory networkFactory) {
        Network network = createWithTieLines(networkFactory);

        network.setCaseDate(ZonedDateTime.parse(CASE_DATE));

        network.getSubstation("P2").setCountry(Country.BE);

        network.getVoltageLevel(VLGEN).newGenerator()
                .setId("GEN2")
                .setBus(NGEN)
                .setConnectableBus(NGEN)
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
                .add();

        ((Bus) network.getIdentifiable(NHV1)).setV(380).getVoltageLevel().setLowVoltageLimit(400).setHighVoltageLimit(500);
        ((Bus) network.getIdentifiable(NHV2)).setV(380).getVoltageLevel().setLowVoltageLimit(300).setHighVoltageLimit(500);

        BoundaryLine boundaryLine1 = network.getBoundaryLine(BOUNDARY_LINE_XNODE1_1);
        BoundaryLine boundaryLine2 = network.getBoundaryLine(BOUNDARY_LINE_XNODE1_2);
        boundaryLine1.getTerminal().setP(560.0).setQ(550.0);
        boundaryLine2.getTerminal().setP(560.0).setQ(550.0);
        boundaryLine1.getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits().setPermanentLimit(500).add();
        boundaryLine2.getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits()
                .setPermanentLimit(1100)
                .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(1500)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("N/A")
                .setAcceptableDuration(0)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();

        boundaryLine1.getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits().setPermanentLimit(500).add();
        boundaryLine2.getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits()
                .setPermanentLimit(1100)
                .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(1500)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("N/A")
                .setAcceptableDuration(0)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();

        boundaryLine1 = network.getBoundaryLine(BOUNDARY_LINE_XNODE2_1);
        boundaryLine2 = network.getBoundaryLine(BOUNDARY_LINE_XNODE2_2);
        boundaryLine1.getTerminal().setP(560.0).setQ(550.0);
        boundaryLine2.getTerminal().setP(560.0).setQ(550.0);
        boundaryLine1.getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits()
                .setPermanentLimit(1100)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("N/A")
                .setAcceptableDuration(60)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();
        boundaryLine2.getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits().setPermanentLimit(500).add();

        boundaryLine1.getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits()
                .setPermanentLimit(1100)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("N/A")
                .setAcceptableDuration(60)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();
        boundaryLine2.getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits().setPermanentLimit(500).add();

        return network;
    }

    public static Network createWithMultipleConnectedComponents() {
        return createWithMultipleConnectedComponents(NetworkFactory.findDefault());
    }

    public static Network createWithMultipleConnectedComponents(NetworkFactory networkFactory) {
        Network network = create(networkFactory);

        Substation p3 = network.newSubstation()
                .setId("P3")
                .setCountry(Country.FR)
                .add();

        VoltageLevel vlhv3 = p3.newVoltageLevel()
                .setId("VLHV3")
                .setNominalV(24.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        Bus nconnected = vlhv3.getBusBreakerView().newBus().setId("N1").add();
        Bus ndisconnected = vlhv3.getBusBreakerView().newBus().setId("N2").add();
        Bus nshunt = vlhv3.getBusBreakerView().newBus().setId("NSHUNT").add();

        vlhv3.newLoad().setId("LOAD2")
                .setBus(nconnected.getId())
                .setConnectableBus(nconnected.getId())
                .setP0(600.0)
                .setQ0(200.0)
                .add();
        vlhv3.newLoad().setId("LOAD3")
                .setConnectableBus(ndisconnected.getId())
                .setP0(600.0)
                .setQ0(200.0)
                .add();

        vlhv3.newGenerator().setId("GEN2")
                .setBus(nconnected.getId())
                .setConnectableBus(nconnected.getId())
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
                .add();
        vlhv3.newGenerator().setId("GEN3")
                .setConnectableBus(ndisconnected.getId())
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
                .add();

        vlhv3.newShuntCompensator()
                .setId("SHUNT")
                .setConnectableBus(nshunt.getId())
                .setSectionCount(1)
                .newLinearModel()
                    .setBPerSection(1e-5)
                    .setMaximumSectionCount(1)
                    .add()
                .add();

        return network;
    }

    public static Network createWithTerminalMockExt() {
        return createWithTerminalMockExt(NetworkFactory.findDefault());
    }

    public static Network createWithTerminalMockExt(NetworkFactory networkFactory) {
        Network network = create(networkFactory);
        network.setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));

        Load load = network.getLoad("LOAD");
        TerminalMockExt terminalMockExt = new TerminalMockExt(load);
        load.addExtension(TerminalMockExt.class, terminalMockExt);

        return network;
    }

    public static Network createWithVoltageAngleLimit() {
        Network network = create(NetworkFactory.findDefault());
        network.setCaseDate(ZonedDateTime.parse("2023-06-28T23:11:51.614+02:00"));

        network.newVoltageAngleLimit()
            .setId("VOLTAGE_ANGLE_LIMIT_NHV1_NHV2_1")
            .from(network.getLine(NHV1_NHV2_1).getTerminal1())
            .to(network.getLine(NHV1_NHV2_1).getTerminal2())
            .setHighLimit(0.25)
            .add();

        network.newVoltageAngleLimit()
            .setId("VOLTAGE_ANGLE_LIMIT_NHV1_NHV2_2")
            .from(network.getLine(NHV1_NHV2_2).getTerminal1())
            .to(network.getLine(NHV1_NHV2_2).getTerminal2())
            .setLowLimit(0.20)
            .add();

        network.newVoltageAngleLimit()
            .setId("VOLTAGE_ANGLE_LIMIT_NGEN_NHV1")
            .from(network.getGenerator("GEN").getTerminal())
            .to(network.getTwoWindingsTransformer(NGEN_NHV1).getTerminal2())
            .setLowLimit(-0.20)
            .setHighLimit(0.35)
            .add();

        return network;
    }

    public static Network createWithTieLinesAndAreas() {
        return createWithTieLinesAndAreas(NetworkFactory.findDefault());
    }

    public static Network createWithTieLinesAndAreas(NetworkFactory networkFactory) {
        Network network = createWithTieLines(networkFactory);

        // createWithTieLines sets non-zero G for boundary lines, while the load flow solution included is for zero G.
        // Here we set all BoundaryLine's G to zero. Doing this the BoundaryLine's P and Q at boundary side (calculated by iIDM)
        // are consistent with included load flow results. In particular, flows of the 2 BoundaryLines of a tie-line are consistent
        // (verifying dl1.getBoundary().getP() ~= -1.0 * dl2.getBoundary().getP())
        network.getBoundaryLineStream().forEach(dl -> dl.setG(0.0));

        network.newArea()
                .setId("ControlArea_A")
                .setName("Control Area A")
                .setAreaType("ControlArea")
                .setInterchangeTarget(-602.6)
                .addVoltageLevel(network.getVoltageLevel(VLGEN))
                .addVoltageLevel(network.getVoltageLevel(VLHV1))
                .addAreaBoundary(network.getBoundaryLine(BOUNDARY_LINE_XNODE1_1).getBoundary(), true)
                .addAreaBoundary(network.getBoundaryLine(BOUNDARY_LINE_XNODE2_1).getBoundary(), true)
                .add();
        network.newArea()
                .setId("ControlArea_B")
                .setName("Control Area B")
                .setAreaType("ControlArea")
                .setInterchangeTarget(+602.6)
                .addVoltageLevel(network.getVoltageLevel(VLHV2))
                .addVoltageLevel(network.getVoltageLevel(VLLOAD))
                .addAreaBoundary(network.getBoundaryLine(BOUNDARY_LINE_XNODE1_2).getBoundary(), true)
                .addAreaBoundary(network.getBoundaryLine(BOUNDARY_LINE_XNODE2_2).getBoundary(), true)
                .add();
        network.newArea()
                .setId("Region_AB")
                .setName("Region AB")
                .setAreaType("Region")
                .addVoltageLevel(network.getVoltageLevel(VLGEN))
                .addVoltageLevel(network.getVoltageLevel(VLHV1))
                .addVoltageLevel(network.getVoltageLevel(VLHV2))
                .addVoltageLevel(network.getVoltageLevel(VLLOAD))
                .add();
        return network;
    }

    public static Network createWithReactiveTcc() {
        Network network = create();
        network.getTwoWindingsTransformer(NHV2_NLOAD)
                .getRatioTapChanger()
                .setRegulationMode(RatioTapChanger.RegulationMode.REACTIVE_POWER)
                .setRegulationValue(100);
        return network;
    }

    public static Network createRemoteReactiveTcc() {
        return createRemoteTcc(createWithReactiveTcc());
    }

    public static Network createRemoteVoltageTcc() {
        return createRemoteTcc(create());
    }

    private static Network createRemoteTcc(Network network) {
        network.getTwoWindingsTransformer(NHV2_NLOAD)
                .getRatioTapChanger()
                .setRegulationTerminal(network.getGenerator("GEN").getTerminal());

        return network;
    }

    public static Network createWithoutRtcControl() {
        Network network = create();
        TwoWindingsTransformer nhv2Nload = network.getTwoWindingsTransformer(NHV2_NLOAD);
        RatioTapChanger rtc = nhv2Nload.getRatioTapChanger();
        rtc.remove();
        nhv2Nload.newRatioTapChanger()
                .beginStep()
                .setRho(0.85f)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setRho(1)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setRho(1.15f)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .setTapPosition(1)
                .setLoadTapChangingCapabilities(false)
            .add();
        return network;
    }

    public static Network createWith3wTransformer() {
        Network network = create();
        Substation p1 = network.getSubstation("P1");
        VoltageLevel v2 = p1.newVoltageLevel()
                .setId("V2")
                .setNominalV(150.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        v2.getBusBreakerView().newBus()
                .setId("N2")
            .add();
        network.getTwoWindingsTransformer(NHV2_NLOAD).remove();
        ThreeWindingsTransformerAdder threeWindingsTransformerAdder1 = p1.newThreeWindingsTransformer()
                .setId(NGEN_V2_NHV1)
                .setRatedU0(400);
        threeWindingsTransformerAdder1.newLeg1()
                .setBus(NHV1)
                .setR(0.001)
                .setX(0.000001)
                .setB(0)
                .setG(0)
                .setRatedU(400)
                .setVoltageLevel(VLHV1)
                .add();
        threeWindingsTransformerAdder1.newLeg2()
                .setBus("N2")
                .setR(0.1)
                .setX(0.00001)
                .setB(0)
                .setG(0)
                .setRatedU(150.0)
                .setVoltageLevel("V2")
                .add();
        threeWindingsTransformerAdder1.newLeg3()
                .setBus(NGEN)
                .setR(0.01)
                .setX(0.0001)
                .setB(0)
                .setG(0)
                .setRatedU(24)
                .setVoltageLevel(VLGEN)
                .add();
        threeWindingsTransformerAdder1.add();
        return network;
    }

    public static Network createWith3wWithVoltageControl() {
        Network network = createWith3wTransformer();
        add3wRtcWithVoltageControl(network);
        return network;
    }

    public static Network createWith3wWithoutControl() {
        Network network = createWith3wTransformer();
        add3wRtcWithoutControl(network);
        return network;
    }

    private static void add3wRtcWithVoltageControl(Network network) {
        network.getThreeWindingsTransformer(NGEN_V2_NHV1).getLeg1().newRatioTapChanger()
                .beginStep()
                .setRho(0.85f)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setRho(1.0)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setRho(1.15f)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .setTapPosition(1)
                .setLoadTapChangingCapabilities(true)
                .setRegulating(true)
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setRegulationValue(158.0)
                .setTargetDeadband(0)
                .setRegulationTerminal(network.getThreeWindingsTransformer(NGEN_V2_NHV1).getLeg1().getTerminal())
            .add();
    }

    private static void add3wRtcWithoutControl(Network network) {
        network.getThreeWindingsTransformer(NGEN_V2_NHV1).getLeg1().newRatioTapChanger()
                .beginStep()
                .setRho(0.85f)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setRho(1.0)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setRho(1.15f)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .setTapPosition(1)
                .setLoadTapChangingCapabilities(false)
            .add();
    }

    public static Network create3wWithReactiveTcc() {
        Network network = createWith3wWithVoltageControl();
        network.getThreeWindingsTransformer(NGEN_V2_NHV1).getLeg1()
                .getRatioTapChanger()
                .setRegulationMode(RatioTapChanger.RegulationMode.REACTIVE_POWER)
                .setRegulationValue(100);
        return network;
    }

    private static Network create3wRemoteTcc(Network network) {
        network.getThreeWindingsTransformer(NGEN_V2_NHV1).getLeg1()
                .getRatioTapChanger()
                .setRegulationTerminal(network.getGenerator("GEN").getTerminal());
        return network;
    }

    public static Network create3wRemoteReactiveTcc() {
        return create3wRemoteTcc(create3wWithReactiveTcc());
    }

    public static Network create3wRemoteVoltageTcc() {
        return create3wRemoteTcc(createWith3wWithVoltageControl());
    }

    public static Network createWithRemoteVoltageGenerator() {
        return addRemoteVoltageGenerator(create());
    }

    public static Network createWithRemoteReactiveGenerator() {
        return removeVoltageControlForGenerator(addRemoteReactiveGenerator(create()));
    }

    public static Network createWithLocalReactiveGenerator() {
        return removeVoltageControlForGenerator(addLocalReactiveGenerator(create()));
    }

    public static Network createWithRemoteReactiveAndVoltageGenerators() {
        return addRemoteVoltageGenerator(addRemoteReactiveGenerator(create()));
    }

    public static Network createWithLocalReactiveAndVoltageGenerator() {
        return addLocalReactiveGenerator(create());
    }

    public static Network createWithoutControl() {
        return removeVoltageControlForGenerator(create());
    }

    public static Network createRemoteWithoutControl() {
        return removeVoltageControlForGenerator(createWithRemoteVoltageGenerator());
    }

    private static Network addLocalReactiveGenerator(Network network) {
        return addReactiveGenerator(network, network.getGenerator("GEN").getRegulatingTerminal());
    }

    private static Network addRemoteReactiveGenerator(Network network) {
        return addReactiveGenerator(network, network.getTwoWindingsTransformer(NHV2_NLOAD).getTerminal1());
    }

    private static Network addReactiveGenerator(Network network, Terminal terminal) {
        network.getGenerator("GEN").newExtension(RemoteReactivePowerControlAdder.class)
                .withRegulatingTerminal(terminal)
                .withTargetQ(200)
                .withEnabled(true).add();
        return network;
    }

    private static Network addRemoteVoltageGenerator(Network network) {
        network.getGenerator("GEN")
                .setRegulatingTerminal(network.getTwoWindingsTransformer(NHV2_NLOAD).getTerminal1())
                .setTargetV(399);
        return network;
    }

    private static Network removeVoltageControlForGenerator(Network network) {
        Generator gen = network.getGenerator("GEN");
        gen.setVoltageRegulatorOn(false);
        gen.setTargetV(Double.NaN);
        return network;
    }

    public static Network createReducibleVoltageLevelNetworkBusBreaker() {
        Network network = NetworkFactory.findDefault().createNetwork("sim1", "test");
        Substation p1 = network.newSubstation().setId("P1").add();
        VoltageLevel vlFlow = p1.newVoltageLevel()
            .setId("VLFLOW")
            .setNominalV(400)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        vlFlow.getBusBreakerView().newBus().setId("BF").add();

        VoltageLevel vlGen = p1.newVoltageLevel()
            .setId("VLGEN")
            .setNominalV(20)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        String bus11Id = "Bus_1_1";
        String bus12Id = "Bus_1_2";
        vlGen.getBusBreakerView().newBus().setId(bus11Id).add();
        vlGen.getBusBreakerView().newBus().setId(bus12Id).add();
        createBusGenerator(vlGen, "B1GEN1", bus12Id);
        createBusSwitch(vlGen, "B1B2SW1", bus11Id, bus12Id);
        createBusLoad(vlGen, "B1L1", bus11Id);
        createBusTransformer(p1, "T1_1_BF", "BF", bus11Id);

        String bus21Id = "Bus_2_1";
        String bus22Id = "Bus_2_2";
        vlGen.getBusBreakerView().newBus().setId(bus21Id).add();
        vlGen.getBusBreakerView().newBus().setId(bus22Id).add();
        createBusGenerator(vlGen, "B2GEN1", bus22Id);
        createBusGenerator(vlGen, "B2GEN2", bus21Id);
        createBusSwitch(vlGen, "B1B2SW2", bus21Id, bus22Id);
        createBusLoad(vlGen, "B2L1", bus21Id);
        createBusTransformer(p1, "T2_1_BF", "BF", bus21Id);

        String bus31Id = "Bus_3_1";
        vlGen.getBusBreakerView().newBus().setId(bus31Id).add();
        createBusGenerator(vlGen, "B3GEN1", bus31Id);
        createBusTransformer(p1, "T3_1_BF", "BF", bus31Id);

        VoltageLevel vlInvalid = p1.newVoltageLevel()
            .setId("VLINV")
            .setNominalV(20)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        String bus41Id = "Bus_4_1";
        String bus42Id = "Bus_4_2";
        String bus43Id = "Bus_4_3";
        vlInvalid.getBusBreakerView().newBus().setId(bus41Id).add();
        vlInvalid.getBusBreakerView().newBus().setId(bus42Id).add();
        vlInvalid.getBusBreakerView().newBus().setId(bus43Id).add();
        createBusGenerator(vlInvalid, "B4GEN1", bus42Id);
        createBusGenerator(vlInvalid, "B4GEN2", bus43Id);
        createBusSwitch(vlInvalid, "B1B2SW4", bus41Id, bus42Id);
        createBusSwitch(vlInvalid, "B2B3SW1", bus42Id, bus43Id);
        createBusLoad(vlInvalid, "B4L1", bus41Id);
        createBusTransformer(p1, "T4_1_BF", "BF", bus41Id);
        createBusTransformer(p1, "T4_3_BF", "BF", bus43Id);

        String bus51Id = "Bus_5_1";
        vlInvalid.getBusBreakerView().newBus().setId(bus51Id).add();
        createBusGenerator(vlInvalid, "B5GEN1", bus51Id);
        createBusTransformer(p1, "T5_1_BF", "BF", bus51Id);

        return network;
    }

    private static void createBusTransformer(Substation substation, String transformerId, String bus1Id, String bus2Id) {
        substation.newTwoWindingsTransformer()
            .setConnectableBus1(bus1Id)
            .setBus1(bus1Id)
            .setConnectableBus2(bus2Id)
            .setBus2(bus2Id)
            .setRatedU1(400)
            .setRatedU2(20)
            .setId(transformerId)
            .setR(0.1)
            .setX(0.1)
            .add();
    }

    private static void createBusLoad(VoltageLevel vl, String loadId, String busId) {
        vl.newLoad()
            .setId(loadId)
            .setP0(-10)
            .setQ0(15)
            .setBus(busId)
            .setConnectableBus(busId)
            .add();
    }

    private static void createBusSwitch(VoltageLevel vl, String switchId, String bus1Id, String bus2Id) {
        vl.getBusBreakerView().newSwitch()
            .setId(switchId)
            .setOpen(false)
            .setBus1(bus1Id)
            .setBus2(bus2Id)
            .add();
    }

    private static void createBusGenerator(VoltageLevel vl, String generatorId, String busId) {
        vl.newGenerator()
            .setId(generatorId)
            .setBus(busId)
            .setConnectableBus(busId)
            .setMinP(100)
            .setMaxP(900)
            .setTargetP(750)
            .setVoltageRegulatorOn(true)
            .setTargetV(20)
            .add();
    }

    public static Network createReducibleVoltageLevelNetworkNodeBreaker() {
        Network network = NetworkFactory.findDefault().createNetwork("sim1", "test");
        Substation p1 = network.newSubstation().setId("P1").add();
        VoltageLevel vlFlow = p1.newVoltageLevel()
            .setId("VLFLOW")
            .setNominalV(400)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        String busbar01Id = "Busbar_01";
        String busbar02Id = "Busbar_02";
        String busbar03Id = "Busbar_03";
        String busbar04Id = "Busbar_04";
        String busbar05Id = "Busbar_05";
        String busbar06Id = "Busbar_06";
        VoltageLevel.NodeBreakerView vlFlowNodeView = vlFlow.getNodeBreakerView();
        vlFlowNodeView.newBusbarSection().setId(busbar01Id).setNode(0).add();
        vlFlowNodeView.newBusbarSection().setId(busbar02Id).setNode(1).add();
        vlFlowNodeView.newBusbarSection().setId(busbar03Id).setNode(2).add();
        vlFlowNodeView.newBusbarSection().setId(busbar04Id).setNode(20).add();
        vlFlowNodeView.newBusbarSection().setId(busbar05Id).setNode(21).add();
        vlFlowNodeView.newBusbarSection().setId(busbar06Id).setNode(22).add();

        vlFlowNodeView.newDisconnector().setId("DF12").setNode1(0).setNode2(1).setOpen(true).add();
        vlFlowNodeView.newDisconnector().setId("DF23").setNode1(1).setNode2(2).setOpen(true).add();
        vlFlowNodeView.newDisconnector().setId("DF34").setNode1(2).setNode2(20).setOpen(true).add();
        vlFlowNodeView.newDisconnector().setId("DF45").setNode1(20).setNode2(21).setOpen(true).add();
        vlFlowNodeView.newDisconnector().setId("DF56").setNode1(21).setNode2(22).setOpen(true).add();

        VoltageLevel vlGen = p1.newVoltageLevel()
            .setId("VLGEN")
            .setNominalV(20)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        String busbar11Id = "Busbar_11";
        String busbar12Id = "Busbar_12";
        String busbar13Id = "Busbar_13";

        VoltageLevel.NodeBreakerView vlGenNodeView = vlGen.getNodeBreakerView();

        vlGenNodeView.newBusbarSection().setId(busbar11Id).setNode(4).add();
        vlGenNodeView.newBusbarSection().setId(busbar12Id).setNode(5).add();
        vlGenNodeView.newBusbarSection().setId(busbar13Id).setNode(6).add();

        vlGenNodeView.newDisconnector().setNode1(4).setNode2(5).setId("D12").setOpen(true).add();
        vlGenNodeView.newDisconnector().setNode1(5).setNode2(6).setId("D23").setOpen(true).add();

        //create a load on the busbar with a disconnector and a generator behind a breaker
        createNodeLoad(vlGen, "L1", 7);
        vlGenNodeView.newDisconnector().setId("DL1").setNode1(4).setNode2(7).setOpen(false).add();
        createNodeGenerator(vlGen, "G1", 8);
        vlGenNodeView.newBreaker().setId("BRG1").setNode1(4).setNode2(8).setOpen(false).setRetained(true).add();
        createNodeTransformer(p1, "TBB1BF1", 4, vlGen, 0, vlFlow, 13, 14);

        //create a load on the busbar with a disconnector, a generator behind a breaker and a generator on the busbar (with disconnector)
        createNodeLoad(vlGen, "L2", 9);
        vlGenNodeView.newDisconnector().setId("DL2").setNode1(5).setNode2(9).setOpen(false).add();
        createNodeGenerator(vlGen, "G21", 10);
        vlGenNodeView.newBreaker().setId("BRG2").setNode1(5).setNode2(10).setOpen(false).setRetained(true).add();
        createNodeTransformer(p1, "TBB2BF2", 5, vlGen, 1, vlFlow, 15, 16);
        createNodeGenerator(vlGen, "G22", 11);
        vlGenNodeView.newDisconnector().setId("DG1").setNode1(5).setNode2(11).setOpen(false).add();

        // create a generator directly on the third busbar
        createNodeGenerator(vlGen, "G3", 12);
        vlGenNodeView.newInternalConnection().setNode1(6).setNode2(12).add();
        createNodeTransformer(p1, "TBB3BF3", 6, vlGen, 2, vlFlow, 17, 18);

        VoltageLevel vlInvalid = p1.newVoltageLevel()
            .setId("VLINV")
            .setNominalV(20)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        VoltageLevel.NodeBreakerView vlInvNodeView = vlInvalid.getNodeBreakerView();
        vlInvNodeView.newBusbarSection().setId("Busbar_20").setNode(30).add();
        vlInvNodeView.newBusbarSection().setId("Busbar_21").setNode(35).add();
        createNodeGenerator(vlInvalid, "G41", 32);
        vlInvNodeView.newBreaker().setId("BRG41").setNode1(30).setNode2(32).setOpen(false).setRetained(true).add();
        createNodeLoad(vlInvalid, "L4", 31);
        vlInvNodeView.newDisconnector().setId("DL4").setNode1(31).setNode2(30).setOpen(false).add();
        createNodeTransformer(p1, "TBB20BF4", 30, vlInvalid, 20, vlFlow, 33, 34);
        vlInvNodeView.newBreaker().setId("BRBB20BB21").setNode1(30).setNode2(35).setOpen(false).setRetained(true).add();
        createNodeGenerator(vlInvalid, "G42", 36);
        vlInvNodeView.newInternalConnection().setNode1(36).setNode2(35).add();
        createNodeTransformer(p1, "TBB21BF5", 35, vlInvalid, 21, vlFlow, 37, 38);

        // create a generator directly on the busbar
        vlInvNodeView.newBusbarSection().setId("Busbar_30").setNode(40).add();
        vlInvNodeView.newDisconnector().setId("D2130").setNode1(35).setNode2(40).setOpen(true).add();
        createNodeGenerator(vlInvalid, "G5", 41);
        vlInvNodeView.newInternalConnection().setNode1(40).setNode2(41).add();
        createNodeTransformer(p1, "TBB30BF6", 40, vlInvalid, 22, vlFlow, 42, 43);

        return network;
    }

    private static void createNodeLoad(VoltageLevel vl, String loadId, int node) {
        vl.newLoad()
            .setId(loadId)
            .setP0(-10)
            .setQ0(15)
            .setNode(node)
            .add();
    }

    private static void createNodeGenerator(VoltageLevel vl, String generatorId, int node) {
        vl.newGenerator()
            .setId(generatorId)
            .setNode(node)
            .setMinP(100)
            .setMaxP(900)
            .setTargetP(750)
            .setVoltageRegulatorOn(true)
            .setTargetV(20)
            .add();
    }

    private static void createNodeTransformer(Substation substation, String transformerId, int node1, VoltageLevel vl1, int node2, VoltageLevel vl2, int disconnectorNode1, int disconnectorNode2) {
        vl1.getNodeBreakerView().newDisconnector().setNode1(node1).setNode2(disconnectorNode1).setOpen(false).setId(transformerId + vl1.getId()).add();
        vl2.getNodeBreakerView().newDisconnector().setNode1(node2).setNode2(disconnectorNode2).setOpen(false).setId(transformerId + vl2.getId()).add();
        substation.newTwoWindingsTransformer()
            .setNode1(disconnectorNode1)
            .setVoltageLevel1(vl1.getId())
            .setNode2(disconnectorNode2)
            .setVoltageLevel2(vl2.getId())
            .setRatedU1(400)
            .setRatedU2(20)
            .setId(transformerId)
            .setR(0.1)
            .setX(0.1)
            .add();
    }

}
