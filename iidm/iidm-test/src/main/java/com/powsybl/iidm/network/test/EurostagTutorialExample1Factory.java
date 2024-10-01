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

    private static final String VLGEN = "VLGEN";
    private static final String VLLOAD = "VLLOAD";
    public static final String CASE_DATE = "2018-01-01T11:00:00+01:00";
    public static final String DANGLING_LINE_XNODE1_1 = "NHV1_XNODE1";
    public static final String DANGLING_LINE_XNODE1_2 = "XNODE1_NHV2";
    public static final String DANGLING_LINE_XNODE2_1 = "NHV1_XNODE2";
    public static final String DANGLING_LINE_XNODE2_2 = "XNODE2_NHV2";
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
                .setId("NHV2")
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

        DanglingLine nhv1xnode1 = network.getVoltageLevel(VLHV1).newDanglingLine()
                .setId(DANGLING_LINE_XNODE1_1)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.5)
                .setX(20.0)
                .setG(1E-6)
                .setB(386E-6 / 2)
                .setPairingKey(XNODE_1)
                .setBus(NHV1)
                .setHasShuntAdmittanceLineEquivalentModel(true)
                .add();
        DanglingLine xnode1nhv2 = network.getVoltageLevel(VLHV2).newDanglingLine()
                .setId(DANGLING_LINE_XNODE1_2)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.5)
                .setX(13.0)
                .setG(2E-6)
                .setB(386E-6 / 2)
                .setBus("NHV2")
                .setPairingKey(XNODE_1)
                .setHasShuntAdmittanceLineEquivalentModel(true)
                .add();
        network.newTieLine()
                .setId(NHV1_NHV2_1)
                .setDanglingLine1(nhv1xnode1.getId())
                .setDanglingLine2(xnode1nhv2.getId())
                .add();
        DanglingLine nhv1xnode2 = network.getVoltageLevel(VLHV1).newDanglingLine()
                .setId(DANGLING_LINE_XNODE2_1)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.5)
                .setX(20.0)
                .setG(1E-6)
                .setB(386E-6 / 2)
                .setBus(NHV1)
                .setPairingKey("XNODE2")
                .setHasShuntAdmittanceLineEquivalentModel(true)
                .add();
        DanglingLine xnode2nhv2 = network.getVoltageLevel(VLHV2).newDanglingLine()
                .setId(DANGLING_LINE_XNODE2_2)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.5)
                .setX(13.0)
                .setG(2E-6)
                .setB(386E-6 / 2)
                .setBus("NHV2")
                .setPairingKey("XNODE2")
                .setHasShuntAdmittanceLineEquivalentModel(true)
                .add();
        network.newTieLine()
                .setId(NHV1_NHV2_2)
                .setDanglingLine1(nhv1xnode2.getId())
                .setDanglingLine2(xnode2nhv2.getId())
                .add();
        network.getTieLine(NHV1_NHV2_1).getDanglingLine1().getTerminal()
                .setP(302.4440612792969)
                .setQ(98.74027252197266);
        network.getTieLine(NHV1_NHV2_1).getDanglingLine2().getTerminal()
                .setP(-300.43389892578125)
                .setQ(-137.18849182128906);
        network.getTieLine(NHV1_NHV2_2).getDanglingLine1().getTerminal()
                .setP(302.4440612792969)
                .setQ(98.74027252197266);
        network.getTieLine(NHV1_NHV2_2).getDanglingLine2().getTerminal()
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
        network.getBusBreakerView().getBus("NHV2")
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

    /**
     * @deprecated Use {@link #createWithFixedCurrentLimits()} instead,
     *             here current limits do not respect the convention of having
     *             an infinite value temporary limit, which make overload detection
     *             malfunction.
     */
    @Deprecated(since = "2.5.0")
    public static Network createWithCurrentLimits() {
        Network network = createWithFixedCurrentLimits();
        Line line = network.getLine(NHV1_NHV2_1);
        line.newCurrentLimits2()
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

        line = network.getLine(NHV1_NHV2_2);
        line.newCurrentLimits1()
            .setPermanentLimit(1100)
            .beginTemporaryLimit()
            .setName("20'")
            .setAcceptableDuration(20 * 60)
            .setValue(1200)
            .endTemporaryLimit()
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
        ((Bus) network.getIdentifiable("NHV2")).setV(380).getVoltageLevel().setLowVoltageLimit(300).setHighVoltageLimit(500);

        Line line = network.getLine(NHV1_NHV2_1);
        line.getTerminal1().setP(560.0).setQ(550.0);
        line.getTerminal2().setP(-560.0).setQ(-550.0);
        line.newCurrentLimits1().setPermanentLimit(500).add();
        line.newCurrentLimits2()
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
        line.newCurrentLimits1()
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
        line.newCurrentLimits2().setPermanentLimit(500).add();

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
        ((Bus) network.getIdentifiable("NHV2")).setV(380).getVoltageLevel().setLowVoltageLimit(300).setHighVoltageLimit(500);

        Line line = network.getLine(NHV1_NHV2_1);
        line.getTerminal1().setP(560.0).setQ(550.0);
        line.getTerminal2().setP(560.0).setQ(550.0);
        line.newActivePowerLimits1().setPermanentLimit(500).add();
        line.newActivePowerLimits2()
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

        line.newApparentPowerLimits1().setPermanentLimit(500).add();
        line.newApparentPowerLimits2()
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
        line.newActivePowerLimits1()
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
        line.newActivePowerLimits2().setPermanentLimit(500).add();

        line.newApparentPowerLimits1()
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
        line.newApparentPowerLimits2().setPermanentLimit(500).add();

        return network;
    }

    public static Network createWithFixedCurrentLimitsOnDanglingLines() {
        return createWithFixedCurrentLimitsOnDanglingLines(NetworkFactory.findDefault());
    }

    public static Network createWithFixedCurrentLimitsOnDanglingLines(NetworkFactory networkFactory) {
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
        ((Bus) network.getIdentifiable("NHV2")).setV(380).getVoltageLevel().setLowVoltageLimit(300).setHighVoltageLimit(500);

        DanglingLine danglingLine1 = network.getDanglingLine(DANGLING_LINE_XNODE1_1);
        DanglingLine danglingLine2 = network.getDanglingLine(DANGLING_LINE_XNODE1_2);
        danglingLine1.getTerminal().setP(560.0).setQ(550.0);
        danglingLine2.getTerminal().setP(-560.0).setQ(-550.0);
        danglingLine1.newCurrentLimits().setPermanentLimit(500).add();
        danglingLine2.newCurrentLimits()
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

        danglingLine1 = network.getDanglingLine(DANGLING_LINE_XNODE2_1);
        danglingLine2 = network.getDanglingLine(DANGLING_LINE_XNODE2_2);
        danglingLine1.getTerminal().setP(560.0).setQ(550.0);
        danglingLine2.getTerminal().setP(-560.0).setQ(-550.0);
        danglingLine1.newCurrentLimits()
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
        danglingLine2.newCurrentLimits().setPermanentLimit(500).add();

        return network;
    }

    public static Network createWithFixedLimitsOnDanglingLines() {
        return createWithFixedLimitsOnDanglingLines(NetworkFactory.findDefault());
    }

    public static Network createWithFixedLimitsOnDanglingLines(NetworkFactory networkFactory) {
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
        ((Bus) network.getIdentifiable("NHV2")).setV(380).getVoltageLevel().setLowVoltageLimit(300).setHighVoltageLimit(500);

        DanglingLine danglingLine1 = network.getDanglingLine(DANGLING_LINE_XNODE1_1);
        DanglingLine danglingLine2 = network.getDanglingLine(DANGLING_LINE_XNODE1_2);
        danglingLine1.getTerminal().setP(560.0).setQ(550.0);
        danglingLine2.getTerminal().setP(560.0).setQ(550.0);
        danglingLine1.newActivePowerLimits().setPermanentLimit(500).add();
        danglingLine2.newActivePowerLimits()
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

        danglingLine1.newApparentPowerLimits().setPermanentLimit(500).add();
        danglingLine2.newApparentPowerLimits()
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

        danglingLine1 = network.getDanglingLine(DANGLING_LINE_XNODE2_1);
        danglingLine2 = network.getDanglingLine(DANGLING_LINE_XNODE2_2);
        danglingLine1.getTerminal().setP(560.0).setQ(550.0);
        danglingLine2.getTerminal().setP(560.0).setQ(550.0);
        danglingLine1.newActivePowerLimits()
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
        danglingLine2.newActivePowerLimits().setPermanentLimit(500).add();

        danglingLine1.newApparentPowerLimits()
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
        danglingLine2.newApparentPowerLimits().setPermanentLimit(500).add();

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

        // createWithTieLines sets non-zero G for dangling lines, while the load flow solution included is for zero G.
        // Here we set all DanglingLine's G to zero. Doing this the DanglingLine's P and Q at boundary side (calculated by iIDM)
        // are consistent with included load flow results. In particular, flows of the 2 DanglingLines of a tie-line are consistent
        // (verifying dl1.getBoundary().getP() ~= -1.0 * dl2.getBoundary().getP())
        network.getDanglingLineStream().forEach(dl -> dl.setG(0.0));

        network.newArea()
                .setId("ControlArea_A")
                .setName("Control Area A")
                .setAreaType("ControlArea")
                .setInterchangeTarget(-602.6)
                .addVoltageLevel(network.getVoltageLevel(VLGEN))
                .addVoltageLevel(network.getVoltageLevel(VLHV1))
                .addAreaBoundary(network.getDanglingLine(DANGLING_LINE_XNODE1_1).getBoundary(), true)
                .addAreaBoundary(network.getDanglingLine(DANGLING_LINE_XNODE2_1).getBoundary(), true)
                .add();
        network.newArea()
                .setId("ControlArea_B")
                .setName("Control Area B")
                .setAreaType("ControlArea")
                .setInterchangeTarget(+602.6)
                .addVoltageLevel(network.getVoltageLevel(VLHV2))
                .addVoltageLevel(network.getVoltageLevel(VLLOAD))
                .addAreaBoundary(network.getDanglingLine(DANGLING_LINE_XNODE1_2).getBoundary(), true)
                .addAreaBoundary(network.getDanglingLine(DANGLING_LINE_XNODE2_2).getBoundary(), true)
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
                .setRegulatingTerminal(network.getTwoWindingsTransformer(NHV2_NLOAD).getTerminal1());
        return network;
    }

    private static Network removeVoltageControlForGenerator(Network network) {
        Generator gen = network.getGenerator("GEN");
        gen.setVoltageRegulatorOn(false);
        gen.setTargetV(Double.NaN);
        return network;
    }
}
