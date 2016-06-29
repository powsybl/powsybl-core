/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.test;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.Substation;
import eu.itesla_project.iidm.network.NetworkFactory;
import eu.itesla_project.iidm.network.VoltageLevel;
import eu.itesla_project.iidm.network.TopologyKind;
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.TwoWindingsTransformer;
import eu.itesla_project.iidm.network.Load;
import eu.itesla_project.iidm.network.Generator;

/**
 * This is a network test based on Eurostag tutorial example 1.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagTutorialExample1Factory {

    private EurostagTutorialExample1Factory() {
    }

    public static Network create() {
        Network network = NetworkFactory.create("sim1", "test");
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
                .setId("VLGEN")
                .setNominalV(24)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        VoltageLevel vlhv1 = p1.newVoltageLevel()
                .setId("VLHV1")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        VoltageLevel vlhv2 = p2.newVoltageLevel()
                .setId("VLHV2")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        VoltageLevel vlload = p2.newVoltageLevel()
                .setId("VLLOAD")
                .setNominalV(150)
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
                .setVoltageLevel1("VLHV1")
                .setBus1("NHV1")
                .setConnectableBus1("NHV1")
                .setVoltageLevel2("VLHV2")
                .setBus2("NHV2")
                .setConnectableBus2("NHV2")
                .setR(3)
                .setX(33)
                .setG1(0)
                .setB1(386E-6f / 2)
                .setG2(0f)
                .setB2(386E-6f / 2)
            .add();
        network.newLine()
                .setId("NHV1_NHV2_2")
                .setVoltageLevel1("VLHV1")
                .setBus1("NHV1")
                .setConnectableBus1("NHV1")
                .setVoltageLevel2("VLHV2")
                .setBus2("NHV2")
                .setConnectableBus2("NHV2")
                .setR(3)
                .setX(33)
                .setG1(0)
                .setB1(386E-6f / 2)
                .setG2(0f)
                .setB2(386E-6f / 2)
            .add();
        float zb380 = 380 * 380 / 100;
        TwoWindingsTransformer ngen_nhv1 = p1.newTwoWindingsTransformer()
                .setId("NGEN_NHV1")
                .setVoltageLevel1("VLGEN")
                .setBus1("NGEN")
                .setConnectableBus1("NGEN")
                .setRatedU1(24f)
                .setVoltageLevel2("VLHV1")
                .setBus2("NHV1")
                .setConnectableBus2("NHV1")
                .setRatedU2(400f)
                .setR(0.24f / 1300 * zb380)
                .setX(((float) Math.sqrt(10 * 10 - 0.24 * 0.24)) / 1300 * zb380)
                .setG(0)
                .setB(0)
            .add();
        float zb150 = 150 * 150 / 100;
        TwoWindingsTransformer nhv2_nload = p2.newTwoWindingsTransformer()
                .setId("NHV2_NLOAD")
                .setVoltageLevel1("VLHV2")
                .setBus1("NHV2")
                .setConnectableBus1("NHV2")
                .setRatedU1(400f)
                .setVoltageLevel2("VLLOAD")
                .setBus2("NLOAD")
                .setConnectableBus2("NLOAD")
                .setRatedU2(158f)
                .setR(0.21f / 1000 * zb150)
                .setX(((float) Math.sqrt(18 * 18 - 0.21 * 0.21)) / 1000 * zb150)
                .setG(0)
                .setB(0)
            .add();
        float a = (158f / 150f) / (400f / 380f);
        nhv2_nload.newRatioTapChanger()
                .beginStep()
                    .setRho(0.85f * a)
                    .setR(0f)
                    .setX(0f)
                    .setG(0f)
                    .setB(0f)
                .endStep()
                .beginStep()
                    .setRho(a)
                    .setR(0f)
                    .setX(0f)
                    .setG(0f)
                    .setB(0f)
                .endStep()
                .beginStep()
                    .setRho(1.15f * a)
                    .setR(0f)
                    .setX(0f)
                    .setG(0f)
                    .setB(0f)
                .endStep()
                .setTapPosition(1)
                .setLoadTapChangingCapabilities(true)
                .setRegulating(true)
                .setTargetV(158)
                .setTerminal(nhv2_nload.getTerminal2())
            .add();
        Load load = vlload.newLoad()
                .setId("LOAD")
                .setBus("NLOAD")
                .setConnectableBus("NLOAD")
                .setP0(600f)
                .setQ0(200f)
            .add();
        Generator generator = vlgen.newGenerator()
                .setId("GEN")
                .setBus("NGEN")
                .setConnectableBus("NGEN")
                .setMinP(-9999.99f)
                .setMaxP(9999.99f)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5f)
                .setTargetP(607f)
                .setTargetQ(301f)
            .add();
        generator.newMinMaxReactiveLimits()
                .setMinQ(-9999.99f)
                .setMaxQ(9999.99f)
            .add();
        return network;
    }

}
