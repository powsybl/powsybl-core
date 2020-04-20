/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

/**
 * This is a network test based on Eurostag tutorial example 1.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class EurostagTutorialExample1Factory {

    private static final String VLGEN = "VLGEN";

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
                .setId("VLHV1")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        VoltageLevel vlhv2 = p2.newVoltageLevel()
                .setId("VLHV2")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        VoltageLevel vlload = p2.newVoltageLevel()
                .setId("VLLOAD")
                .setNominalV(150.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        Bus ngen = vlgen.getBusBreakerView().newBus()
                .setId("NGEN")
            .add();
        Bus nhv1 = vlhv1.getBusBreakerView().newBus()
                .setId("NHV1")
            .add();
        Bus nhv2 = vlhv2.getBusBreakerView().newBus()
                .setId("NHV2")
            .add();
        Bus nload = vlload.getBusBreakerView().newBus()
                .setId("NLOAD")
            .add();
        network.newLine()
                .setId("NHV1_NHV2_1")
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
                .setId("NHV1_NHV2_2")
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
                .setId("NGEN_NHV1")
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
                .setId("NHV2_NLOAD")
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
                .setTargetV(158.0)
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

    public static Network createWithLFResults() {
        Network network = create();
        network.setCaseDate(DateTime.parse("2013-01-15T18:45:00.000+01:00"));

        network.getBusBreakerView().getBus("NGEN")
                .setV(24.500000610351563)
                .setAngle(2.3259763717651367);
        network.getBusBreakerView().getBus("NHV1")
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
        network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal1()
                .setP(605.558349609375)
                .setQ(225.2825164794922);
        network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal2()
                .setP(-604.8909301757812)
                .setQ(-197.48046875);
        network.getLoad("LOAD").getTerminal()
                .setP(600.0)
                .setQ(200.0);
        network.getTwoWindingsTransformer("NHV2_NLOAD").getTerminal1()
                .setP(600.8677978515625)
                .setQ(274.3769836425781);
        network.getTwoWindingsTransformer("NHV2_NLOAD").getTerminal2()
                .setP(-600.0)
                .setQ(-200.0);
        network.getLine("NHV1_NHV2_1").getTerminal1()
                .setP(302.4440612792969)
                .setQ(98.74027252197266);
        network.getLine("NHV1_NHV2_1").getTerminal2()
                .setP(-300.43389892578125)
                .setQ(-137.18849182128906);
        network.getLine("NHV1_NHV2_2").getTerminal1()
                .setP(302.4440612792969)
                .setQ(98.74027252197266);
        network.getLine("NHV1_NHV2_2").getTerminal2()
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
        Bus ngen = vlgen.getBusBreakerView().getBus("NGEN");

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
    @Deprecated
    public static Network createWithCurrentLimits() {
        Network network = createWithFixedCurrentLimits();
        Line line = network.getLine("NHV1_NHV2_1");
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

        line = network.getLine("NHV1_NHV2_2");
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

        network.setCaseDate(DateTime.parse("2018-01-01T11:00:00+01:00"));

        network.getSubstation("P2").setCountry(Country.BE);

        network.getVoltageLevel(VLGEN).newGenerator()
                .setId("GEN2")
                .setBus("NGEN")
                .setConnectableBus("NGEN")
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
                .add();

        ((Bus) network.getIdentifiable("NHV1")).setV(380).getVoltageLevel().setLowVoltageLimit(400).setHighVoltageLimit(500);
        ((Bus) network.getIdentifiable("NHV2")).setV(380).getVoltageLevel().setLowVoltageLimit(300).setHighVoltageLimit(500);

        Line line = network.getLine("NHV1_NHV2_1");
        line.getTerminal1().setP(560.0).setQ(550.0);
        line.getTerminal2().setP(560.0).setQ(550.0);
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

        line = network.getLine("NHV1_NHV2_2");
        line.getTerminal1().setP(560.0).setQ(550.0);
        line.getTerminal2().setP(560.0).setQ(550.0);
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
                .setMaximumSectionCount(1)
                .setCurrentSectionCount(1)
                .setbPerSection(1e-5)
                .add();

        return network;
    }

    public static Network createWithTerminalMockExt() {
        return createWithTerminalMockExt(NetworkFactory.findDefault());
    }

    public static Network createWithTerminalMockExt(NetworkFactory networkFactory) {
        Network network = create(networkFactory);
        network.setCaseDate(DateTime.parse("2013-01-15T18:45:00.000+01:00"));

        Load load = network.getLoad("LOAD");
        TerminalMockExt terminalMockExt = new TerminalMockExt(load);
        load.addExtension(TerminalMockExt.class, terminalMockExt);

        return network;
    }

}
