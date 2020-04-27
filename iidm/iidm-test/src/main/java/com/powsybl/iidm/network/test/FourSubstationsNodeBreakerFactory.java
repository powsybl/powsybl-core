/**
 * Copyright (c) 2020, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

import java.util.Objects;

/**
 * @author Agnes Leroy <agnes.leroy at rte-france.com>
 */

public final class FourSubstationsNodeBreakerFactory {

    private FourSubstationsNodeBreakerFactory() {
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork("measured", "test");
        network.setCaseDate(DateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.setForecastDistance(0);

        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel s1vl1 = s1.newVoltageLevel()
                .setId("S1VL1")
                .setNominalV(225.0)
                .setLowVoltageLimit(0.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        BusbarSection busbarSectionFR1 = s1vl1.getNodeBreakerView().newBusbarSection()
                .setId("S1VL1_BBS")
                .setName("S1VL1_BBS")
                .setNode(0)
                .add();
        createSwitch(s1vl1, "S1VL1_A", "S1VL1_A", SwitchKind.DISCONNECTOR, false, false, true, 0, 1);
        createSwitch(s1vl1, "S1VL1_B", "S1VL1_B", SwitchKind.DISCONNECTOR, false, false, true, 0, 3);
        createSwitch(s1vl1, "S1VL1_C", "S1VL1_C", SwitchKind.BREAKER, true, false, true, 0, 2);
        createSwitch(s1vl1, "S1VL1_D", "S1VL1_D", SwitchKind.BREAKER, true, false, true, 0, 4);
        createSwitch(s1vl1, "S1VL1_E", "S1VL1_D", SwitchKind.BREAKER, false, false, true, 0, 5);

        Load load1 = s1vl1.newLoad()
                .setId("LOAD_1")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(25)
                .setQ0(10.168945)
                .setNode(4)
                .add();
        load1.getTerminal()
                .setP(25)
                .setQ(10.168945);

        VoltageLevel s1vl2 = s1.newVoltageLevel()
                .setId("S1VL2")
                .setNominalV(225.0)
                .setLowVoltageLimit(220.0)
                .setHighVoltageLimit(245.00002)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        BusbarSection busbarSectionS1VL21 = s1vl2.getNodeBreakerView().newBusbarSection()
                .setId("S1VL2_BBS_1")
                .setName("S1VL2_BBS_1")
                .setNode(0)
                .add();
        BusbarSection busbarSectionS1VL22 = s1vl2.getNodeBreakerView().newBusbarSection()
                .setId("S1VL2_BBS_2")
                .setName("S1VL2_BBS_2")
                .setNode(1)
                .add();
        createSwitch(s1vl2, "S1VL2_A", "S1VL2_A", SwitchKind.DISCONNECTOR, false, false, false, 0, 19);
        createSwitch(s1vl2, "S1VL2_B", "S1VL2_B", SwitchKind.DISCONNECTOR, false, false, false, 0, 17);
        createSwitch(s1vl2, "S1VL2_C", "S1VL2_C", SwitchKind.DISCONNECTOR, false, false, false, 0, 21);
        createSwitch(s1vl2, "S1VL2_D", "S1VL2_D", SwitchKind.DISCONNECTOR, false, false, false, 0, 11);
        createSwitch(s1vl2, "S1VL2_E", "S1VL2_E", SwitchKind.DISCONNECTOR, false, false, false, 0, 13);
        createSwitch(s1vl2, "S1VL2_F", "S1VL2_F", SwitchKind.DISCONNECTOR, false, false, false, 0, 15);
        createSwitch(s1vl2, "S1VL2_G", "S1VL2_G", SwitchKind.DISCONNECTOR, false, false, false, 0, 8);
        createSwitch(s1vl2, "S1VL2_H", "S1VL2_H", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        createSwitch(s1vl2, "S1VL2_I", "S1VL2_I", SwitchKind.DISCONNECTOR, false, false, false, 7, 0);
        createSwitch(s1vl2, "S1VL2_J", "S1VL2_J", SwitchKind.DISCONNECTOR, false, false, false, 1, 6);
        createSwitch(s1vl2, "S1VL2_K", "S1VL2_K", SwitchKind.DISCONNECTOR, false, false, false, 1, 19);
        createSwitch(s1vl2, "S1VL2_L", "S1VL2_L", SwitchKind.DISCONNECTOR, false, false, false, 1, 17);
        createSwitch(s1vl2, "S1VL2_M", "S1VL2_M", SwitchKind.DISCONNECTOR, false, false, false, 1, 21);
        createSwitch(s1vl2, "S1VL2_N", "S1VL2_N", SwitchKind.DISCONNECTOR, false, false, false, 1, 11);
        createSwitch(s1vl2, "S1VL2_O", "S1VL2_O", SwitchKind.DISCONNECTOR, false, false, false, 1, 13);
        createSwitch(s1vl2, "S1VL2_P", "S1VL2_P", SwitchKind.DISCONNECTOR, false, true, false, 1, 15);
        createSwitch(s1vl2, "S1VL2_Q", "S1VL2_Q", SwitchKind.DISCONNECTOR, false, false, false, 1, 8);
        createSwitch(s1vl2, "S1VL2_R", "S1VL2_R", SwitchKind.DISCONNECTOR, false, false, true, 1, 2);
        createSwitch(s1vl2, "S1VL2_S", "S1VL2_S", SwitchKind.BREAKER, true, true, true, 2, 3);
        createSwitch(s1vl2, "S1VL2_T", "S1VL2_T", SwitchKind.BREAKER, true, true, false, 3, 4);
        createSwitch(s1vl2, "S1VL2_U", "S1VL2_U", SwitchKind.DISCONNECTOR, false, false, false, 3, 5);
        createSwitch(s1vl2, "S1VL2_V", "S1VL2_V", SwitchKind.DISCONNECTOR, false, true, false, 9, 3);
        createSwitch(s1vl2, "S1VL2_W", "S1VL2_W", SwitchKind.BREAKER, true, false, false, 6, 7);
        createSwitch(s1vl2, "S1VL2_X", "S1VL2_X", SwitchKind.BREAKER, true, false, false, 8, 9);
        createSwitch(s1vl2, "S1VL2_Y", "S1VL2_Y", SwitchKind.DISCONNECTOR, false, false, false, 9, 10);
        createSwitch(s1vl2, "S1VL2_Z", "S1VL2_Z", SwitchKind.BREAKER, true, false, false, 11, 12);
        createSwitch(s1vl2, "S1VL2_AA", "S1VL2_AA", SwitchKind.BREAKER, true, false, false, 13, 14);
        createSwitch(s1vl2, "S1VL2_AB", "S1VL2_AB", SwitchKind.BREAKER, true, false, false, 15, 16);
        createSwitch(s1vl2, "S1VL2_AC", "S1VL2_AC", SwitchKind.BREAKER, true, false, false, 17, 18);
        createSwitch(s1vl2, "S1VL2_AD", "S1VL2_AD", SwitchKind.BREAKER, true, false, false, 0, 20);
        createSwitch(s1vl2, "S1VL2_AE", "S1VL2_AE", SwitchKind.BREAKER, true, false, false, 0, 22);
        createSwitch(s1vl2, "S1VL2_AF", "S1VL2_AF", SwitchKind.BREAKER, false, false, false, 0, 23);

        Generator generatorHydro1 = s1vl2.newGenerator()
                .setId("GEN_HYDRO_1")
                .setEnergySource(EnergySource.HYDRO)
                .setMinP(0.0)
                .setMaxP(100.0)
                .setVoltageRegulatorOn(false)
                .setTargetP(80.0)
                .setTargetV(0.0)
                .setTargetQ(50)
                .setNode(12)
                .add();
        generatorHydro1.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(0.0)
                .setMinQ(-59.3)
                .setMaxQ(160.0)
                .endPoint()
                .beginPoint()
                .setP(100.0)
                .setMinQ(-54.55)
                .setMaxQ(146.25)
                .endPoint()
                .add();

        Generator generatorHydro2 = s1vl2.newGenerator()
                .setId("GEN_HYDRO_2")
                .setEnergySource(EnergySource.HYDRO)
                .setMinP(0.0)
                .setMaxP(80.0)
                .setVoltageRegulatorOn(false)
                .setTargetP(40.0)
                .setTargetV(0.0)
                .setTargetQ(0.0)
                .setNode(14)
                .add();
        generatorHydro2.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(0.0)
                .setMinQ(-56.8)
                .setMaxQ(257.4)
                .endPoint()
                .beginPoint()
                .setP(80.0)
                .setMinQ(-53.514)
                .setMaxQ(236.4)
                .endPoint()
                .add();

        Generator generatorHydro3 = s1vl2.newGenerator()
                .setId("GEN_HYDRO_3")
                .setEnergySource(EnergySource.HYDRO)
                .setMinP(0.0)
                .setMaxP(35.0)
                .setVoltageRegulatorOn(true)
                .setTargetP(51.789589)
                .setTargetV(227)
                .setTargetQ(0.701546)
                .setNode(16)
                .add();
        generatorHydro3.getTerminal()
                .setP(-21.789589)
                .setQ(20.693394);
        generatorHydro3.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(0.0)
                .setMinQ(-20.6)
                .setMaxQ(218.1)
                .endPoint()
                .beginPoint()
                .setP(35.0)
                .setMinQ(-21.725)
                .setMaxQ(216.3500004)
                .endPoint()
                .add();

        Load loadCE = s1vl2.newLoad()
                .setId("LOAD_CE")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(42.18689)
                .setQ0(0.168945)
                .setNode(4)
                .add();
        loadCE.getTerminal()
                .setP(42.18689)
                .setQ(0.168945);

        s1vl2.newShuntCompensator()
                .setId("SHUNT")
                .setNode(13)
                .setMaximumSectionCount(1)
                .setCurrentSectionCount(1)
                .setbPerSection(-0.012)
                .add();

        Load loadCF = s1vl2.newLoad()
                .setId("CF")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(48.455854)
                .setQ0(3.695925)
                .setNode(18)
                .add();
        loadCF.getTerminal()
                .setP(48.455854)
                .setQ(3.695925);

        Load loadCG = s1vl2.newLoad()
                .setId("CG")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(40.39911)
                .setQ0(1.96869)
                .setNode(20)
                .add();
        loadCG.getTerminal()
                .setP(40.39911)
                .setQ(1.96869);

        Load loadCH = s1vl2.newLoad()
                .setId("CH")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(-5.102249)
                .setQ0(4.9081216)
                .setNode(22)
                .add();
        loadCH.getTerminal()
                .setP(-5.102249)
                .setQ(4.9081216);

        TwoWindingsTransformer twtCI = s1.newTwoWindingsTransformer()
                .setId("CI")
                .setR(2.0)
                .setX(14.745)
                .setG(0.0)
                .setB(3.2E-5)
                .setRatedU1(225.0)
                .setRatedU2(225.0)
                .setNode1(2)
                .setVoltageLevel1("S1VL1")
                .setNode2(10)
                .setVoltageLevel2("S1VL2")
                .add();
        twtCI.newCurrentLimits1()
                .setPermanentLimit(931.0)
                .add();
        twtCI.newCurrentLimits2()
                .setPermanentLimit(931.0)
                .add();
        twtCI.newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(22)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(930.6667)
                .setRegulating(false)
                .setRegulationTerminal(twtCI.getTerminal(Branch.Side.ONE))
                .beginStep().setR(39.78473).setX(39.784725).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-42.8).endStep()
                .beginStep().setR(31.720245).setX(31.720242).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-40.18).endStep()
                .beginStep().setR(23.655737).setX(23.655735).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-37.54).endStep()
                .beginStep().setR(16.263271).setX(16.263268).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-34.9).endStep()
                .beginStep().setR(9.542847).setX(9.542842).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-32.26).endStep()
                .beginStep().setR(3.4944773).setX(3.4944773).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-29.6).endStep()
                .beginStep().setR(-1.8818557).setX(-1.8818527).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-26.94).endStep()
                .beginStep().setR(-7.258195).setX(-7.2581954).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-24.26).endStep()
                .beginStep().setR(-11.962485).setX(-11.962484).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-21.58).endStep()
                .beginStep().setR(-15.994745).setX(-15.994745).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-18.9).endStep()
                .beginStep().setR(-19.354952).setX(-19.354952).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-16.22).endStep()
                .beginStep().setR(-22.043127).setX(-22.043129).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-13.52).endStep()
                .beginStep().setR(-24.73129).setX(-24.731287).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-10.82).endStep()
                .beginStep().setR(-26.747417).setX(-26.747417).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-8.12).endStep()
                .beginStep().setR(-28.091503).setX(-28.091503).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-5.42).endStep()
                .beginStep().setR(-28.763538).setX(-28.763536).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-2.7).endStep()
                .beginStep().setR(-28.763538).setX(-28.763536).setG(0.0).setB(0.0).setRho(1.0).setAlpha(0.0).endStep()
                .beginStep().setR(-28.763538).setX(-28.763536).setG(0.0).setB(0.0).setRho(1.0).setAlpha(2.7).endStep()
                .beginStep().setR(-28.091503).setX(-28.091503).setG(0.0).setB(0.0).setRho(1.0).setAlpha(5.42).endStep()
                .beginStep().setR(-26.747417).setX(-26.747417).setG(0.0).setB(0.0).setRho(1.0).setAlpha(8.12).endStep()
                .beginStep().setR(-24.73129).setX(-24.731287).setG(0.0).setB(0.0).setRho(1.0).setAlpha(10.82).endStep()
                .beginStep().setR(-22.043127).setX(-22.043129).setG(0.0).setB(0.0).setRho(1.0).setAlpha(13.52).endStep()
                .beginStep().setR(-19.354952).setX(-19.354952).setG(0.0).setB(0.0).setRho(1.0).setAlpha(16.22).endStep()
                .beginStep().setR(-15.994745).setX(-15.994745).setG(0.0).setB(0.0).setRho(1.0).setAlpha(18.9).endStep()
                .beginStep().setR(-11.962485).setX(-11.962484).setG(0.0).setB(0.0).setRho(1.0).setAlpha(21.58).endStep()
                .beginStep().setR(-7.258195).setX(-7.2581954).setG(0.0).setB(0.0).setRho(1.0).setAlpha(24.26).endStep()
                .beginStep().setR(-1.8818557).setX(-1.8818527).setG(0.0).setB(0.0).setRho(1.0).setAlpha(26.94).endStep()
                .beginStep().setR(3.4944773).setX(3.4944773).setG(0.0).setB(0.0).setRho(1.0).setAlpha(29.6).endStep()
                .beginStep().setR(9.542847).setX(9.542842).setG(0.0).setB(0.0).setRho(1.0).setAlpha(32.26).endStep()
                .beginStep().setR(16.263271).setX(16.263268).setG(0.0).setB(0.0).setRho(1.0).setAlpha(34.9).endStep()
                .beginStep().setR(23.655737).setX(23.655735).setG(0.0).setB(0.0).setRho(1.0).setAlpha(37.54).endStep()
                .beginStep().setR(31.720245).setX(31.720242).setG(0.0).setB(0.0).setRho(1.0).setAlpha(40.18).endStep()
                .beginStep().setR(39.78473).setX(39.784725).setG(0.0).setB(0.0).setRho(1.0).setAlpha(42.8).endStep()
                .add();

        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.ES)
                .add();
        VoltageLevel s2vl1 = s2.newVoltageLevel()
                .setId("S2VL1")
                .setNominalV(225.0)
                .setLowVoltageLimit(0.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        BusbarSection busbarSectionS2VL1 = s2vl1.getNodeBreakerView().newBusbarSection()
                .setId("S2VL1_BBS")
                .setName("S2VL1_BBS")
                .setNode(0)
                .add();
        createSwitch(s2vl1, "S2VL1_A", "S2VL1_A", SwitchKind.DISCONNECTOR, false, false, true, 0, 1);
        createSwitch(s2vl1, "S2VL1_B", "S2VL1_B", SwitchKind.DISCONNECTOR, false, false, true, 0, 3);
        createSwitch(s2vl1, "S2VL1_C", "S2VL1_C", SwitchKind.BREAKER, true, false, true, 1, 2);
        createSwitch(s2vl1, "S2VL1_D", "S2VL1_D", SwitchKind.BREAKER, true, false, true, 3, 4);
        createSwitch(s2vl1, "S2VL1_E", "S2VL1_F", SwitchKind.BREAKER, true, false, true, 4, 5);
        createSwitch(s2vl1, "S2VL1_F", "S2VL1_F", SwitchKind.BREAKER, false, false, true, 5, 6);

        Generator generatorThermal1 = s2vl1.newGenerator()
                .setId("GEN_THERMAL_1")
                .setEnergySource(EnergySource.THERMAL)
                .setMinP(0.0)
                .setMaxP(80.0)
                .setVoltageRegulatorOn(false)
                .setTargetP(70.0)
                .setTargetV(0.0)
                .setTargetQ(0.0)
                .setNode(1)
                .add();
        generatorThermal1.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(0.0)
                .setMinQ(-56.8)
                .setMaxQ(57.4)
                .endPoint()
                .beginPoint()
                .setP(80.0)
                .setMinQ(-53.514)
                .setMaxQ(36.4)
                .endPoint()
                .add();

        Substation s3 = network.newSubstation()
                .setId("S3")
                .setCountry(Country.ES)
                .add();
        VoltageLevel s3vl1 = s3.newVoltageLevel()
                .setId("S3VL1")
                .setNominalV(225.0)
                .setLowVoltageLimit(0.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        BusbarSection busbarSectionS3VL1 = s3vl1.getNodeBreakerView().newBusbarSection()
                .setId("S3VL1_BBS")
                .setName("S3VL1_BBS")
                .setNode(0)
                .add();
        createSwitch(s3vl1, "S3VL1_A", "S3VL1_A", SwitchKind.DISCONNECTOR, false, false, true, 0, 1);
        createSwitch(s3vl1, "S3VL1_B", "S3VL1_B", SwitchKind.DISCONNECTOR, false, false, true, 0, 3);
        createSwitch(s3vl1, "S3VL1_C", "S3VL1_C", SwitchKind.BREAKER, true, false, true, 1, 2);
        createSwitch(s3vl1, "S3VL1_D", "S3VL1_D", SwitchKind.BREAKER, true, false, true, 3, 4);
        createSwitch(s3vl1, "S3VL1_E", "S3VL1_E", SwitchKind.BREAKER, true, false, true, 4, 5);
        createSwitch(s3vl1, "S3VL1_F", "S3VL1_F", SwitchKind.BREAKER, true, false, true, 0, 6);

        Load loadES = s3vl1.newLoad()
                .setId("ES")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(50.989060)
                .setQ0(3.168945)
                .setNode(2)
                .add();
        loadES.getTerminal()
                .setP(50.09778)
                .setQ(3.309844);

        Generator generatorThermal2 = s3vl1.newGenerator()
                .setId("GEN_THERM_2")
                .setEnergySource(EnergySource.THERMAL)
                .setMinP(0.0)
                .setMaxP(70.0)
                .setVoltageRegulatorOn(false)
                .setTargetP(30.0)
                .setTargetV(0.0)
                .setTargetQ(0.0)
                .setNode(6)
                .add();
        generatorThermal2.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(0.0)
                .setMinQ(-59.3)
                .setMaxQ(60.0)
                .endPoint()
                .beginPoint()
                .setP(70.0)
                .setMinQ(-54.55)
                .setMaxQ(46.25)
                .endPoint()
                .add();

        Substation s4 = network.newSubstation()
                .setId("S4")
                .setCountry(Country.ES)
                .add();
        VoltageLevel s4vl1 = s4.newVoltageLevel()
                .setId("S4VL1")
                .setNominalV(225.0)
                .setLowVoltageLimit(0.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        BusbarSection busbarSectionS4VL1 = s4vl1.getNodeBreakerView().newBusbarSection()
                .setId("S4VL1_BBS")
                .setName("S4VL1_BBS")
                .setNode(0)
                .add();
        createSwitch(s4vl1, "S4VL1_A", "S4VL1_A", SwitchKind.DISCONNECTOR, false, false, true, 0, 1);
        createSwitch(s4vl1, "S4VL1_B", "S4VL1_B", SwitchKind.DISCONNECTOR, false, false, true, 1, 2);
        createSwitch(s4vl1, "S4VL1_C", "S4VL1_C", SwitchKind.DISCONNECTOR, false, false, true, 2, 3);
        createSwitch(s4vl1, "S4VL1_D", "S4VL1_D", SwitchKind.DISCONNECTOR, false, false, true, 3, 4);

        Load loadIT = s4vl1.newLoad()
                .setId("IT")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(112.18689)
                .setQ0(0.168945)
                .setNode(2)
                .add();
        loadIT.getTerminal()
                .setP(112.09778)
                .setQ(2.309844);

        s4vl1.newStaticVarCompensator()
                .setId("SVC")
                .setNode(1)
                .setBmin(-5e-2)
                .setBmax(5e-2)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetPoint(226)
                .add();

        Line lineIT = network.newLine()
                .setId("LINE_IT")
                .setR(0.009999999)
                .setX(0.100000024)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setNode1(4)
                .setVoltageLevel1("S3VL1")
                .setNode2(4)
                .setVoltageLevel2("S4VL1")
                .add();
        lineIT.newCurrentLimits1()
                .setPermanentLimit(931.0)
                .add();
        lineIT.newCurrentLimits2()
                .setPermanentLimit(931.0)
                .beginTemporaryLimit()
                .setName("IST")
                .setValue(1640.0)
                .setFictitious(true)
                .setAcceptableDuration(Integer.MAX_VALUE)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("IT1")
                .setValue(Double.MAX_VALUE)
                .setAcceptableDuration(60)
                .endTemporaryLimit()
                .add();

        VscConverterStation vsc1 = s1vl2.newVscConverterStation()
                .setId("VSC1")
                .setName("VSC1")
                .setNode(5)
                .setLossFactor(1.1f)
                .setReactivePowerSetpoint(0)
                .setVoltageSetpoint(227.0)
                .setVoltageRegulatorOn(true)
                .add();
        vsc1.getTerminal()
                .setP(100.0)
                .setQ(0.0);
        vsc1.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(5.0)
                .setMinQ(-200.0)
                .setMaxQ(100.0)
                .endPoint()
                .beginPoint()
                .setP(100.0)
                .setMinQ(-200.0)
                .setMaxQ(100.0)
                .endPoint()
                .add();

        VscConverterStation vsc2 = s2vl1.newVscConverterStation()
                .setId("VSC2")
                .setName("VSC2")
                .setNode(2)
                .setLossFactor(1.1f)
                .setReactivePowerSetpoint(123)
                .setVoltageSetpoint(0)
                .setVoltageRegulatorOn(false)
                .add();
        vsc2.newMinMaxReactiveLimits()
                .setMinQ(0.0)
                .setMaxQ(10.0)
                .add();

        network.newHvdcLine()
                .setId("HVDC1")
                .setName("HVDC1")
                .setConverterStationId1("VSC1")
                .setConverterStationId2("VSC2")
                .setR(1)
                .setNominalV(400)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                .setMaxP(300.0)
                .setActivePowerSetpoint(100)
                .add();

        LccConverterStation lcc1 = s1vl2.newLccConverterStation()
                .setId("LCC1")
                .setName("LCC1")
                .setNode(11)
                .setLossFactor(1.1f)
                .setPowerFactor(0.6f)
                .add();
        lcc1.getTerminal()
                .setP(100.0)
                .setQ(50.0);

        LccConverterStation lcc2 = s3vl1.newLccConverterStation()
                .setId("LCC2")
                .setName("LCC2")
                .setNode(1)
                .setLossFactor(1.1f)
                .setPowerFactor(0.6f)
                .add();
        lcc2.getTerminal()
                .setP(75.0)
                .setQ(25.0);

        network.newHvdcLine()
                .setId("HVDC2")
                .setName("HVDC2")
                .setConverterStationId1("LCC1")
                .setConverterStationId2("LCC2")
                .setR(1)
                .setNominalV(400)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                .setMaxP(300.0)
                .setActivePowerSetpoint(100)
                .add();

        network.newLine()
                .setId("LINE_S2S3")
                .setR(0.009999999)
                .setX(0.100000024)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setNode1(5)
                .setVoltageLevel1("S2VL1")
                .setNode2(5)
                .setVoltageLevel2("S3VL1")
                .add();

        network.newLine()
                .setId("LINE_S1S2")
                .setR(0.009999999)
                .setX(0.100000024)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setNode1(23)
                .setVoltageLevel1("S1VL2")
                .setNode2(6)
                .setVoltageLevel2("S2VL1")
                .add();

        busbarSectionFR1.getTerminal().getBusView().getBus()
                .setV(234.40912)
                .setAngle(0.0);
        busbarSectionS1VL21.getTerminal().getBusView().getBus()
                .setV(236.44736)
                .setAngle(15.250391);
        busbarSectionS1VL22.getTerminal().getBusView().getBus()
                .setV(236.44736)
                .setAngle(15.250391);
        busbarSectionS2VL1.getTerminal().getBusView().getBus()
                .setV(235.49015)
                .setAngle(18.075423);
        busbarSectionS3VL1.getTerminal().getBusView().getBus()
                .setV(233.232565)
                .setAngle(16.098765);
        busbarSectionS4VL1.getTerminal().getBusView().getBus()
                .setV(233.232565)
                .setAngle(16.098765);

        return network;

    }

    private static void createSwitch(VoltageLevel vl, String id, String name, SwitchKind kind, boolean retained, boolean open, boolean fictitious, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
                .setId(id)
                .setName(name)
                .setKind(kind)
                .setRetained(retained)
                .setOpen(open)
                .setFictitious(fictitious)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }
}
