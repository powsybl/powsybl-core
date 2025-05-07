/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import java.time.ZonedDateTime;

import java.util.Objects;

/**
 * This test network is constituted of four substations, with five voltage levels.
 * Below is a diagram of the network:
 * <div>
 *    <object data="doc-files/fourSubstationsNetwork.svg" type="image/svg+xml"></object>
 * </div>
 *
 *
 * @author Agnes Leroy {@literal <agnes.leroy at rte-france.com>}
 */

public final class FourSubstationsNodeBreakerFactory {

    public static final String S3VL1 = "S3VL1";

    private FourSubstationsNodeBreakerFactory() {
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork("fourSubstations", "test");
        network.setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.setForecastDistance(0);

        // First substation
        // It is constituted of 2 voltage levels
        // The second voltage levels comprises two busbar sections
        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel s1vl1 = s1.newVoltageLevel()
                .setId("S1VL1")
                .setNominalV(225.0)
                .setLowVoltageLimit(220.0)
                .setHighVoltageLimit(240.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        BusbarSection busbarSectionS1VL1 = s1vl1.getNodeBreakerView().newBusbarSection()
                .setId("S1VL1_BBS")
                .setName("S1VL1_BBS")
                .setNode(0)
                .add();
        VoltageLevel s1vl2 = s1.newVoltageLevel()
                .setId("S1VL2")
                .setNominalV(400.0)
                .setLowVoltageLimit(390.0)
                .setHighVoltageLimit(440.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        BusbarSection busbarSectionS1VL21 = s1vl2.getNodeBreakerView().newBusbarSection()
                .setId("S1VL2_BBS1")
                .setName("S1VL2_BBS1")
                .setNode(0)
                .add();
        BusbarSection busbarSectionS1VL22 = s1vl2.getNodeBreakerView().newBusbarSection()
                .setId("S1VL2_BBS2")
                .setName("S1VL2_BBS2")
                .setNode(1)
                .add();

        // Second substation
        Substation s2 = network.newSubstation()
                .setId("S2")
                .add();
        VoltageLevel s2vl1 = s2.newVoltageLevel()
                .setId("S2VL1")
                .setNominalV(400.0)
                .setLowVoltageLimit(390.0)
                .setHighVoltageLimit(440.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        BusbarSection busbarSectionS2VL1 = s2vl1.getNodeBreakerView().newBusbarSection()
                .setId("S2VL1_BBS")
                .setName("S2VL1_BBS")
                .setNode(0)
                .add();

        // Third substation
        Substation s3 = network.newSubstation()
                .setId("S3")
                .add();
        VoltageLevel s3vl1 = s3.newVoltageLevel()
                .setId(S3VL1)
                .setNominalV(400.0)
                .setLowVoltageLimit(390.0)
                .setHighVoltageLimit(440.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        BusbarSection busbarSectionS3VL1 = s3vl1.getNodeBreakerView().newBusbarSection()
                .setId("S3VL1_BBS")
                .setName("S3VL1_BBS")
                .setNode(0)
                .add();

        // Fourth substation
        Substation s4 = network.newSubstation()
                .setId("S4")
                .add();
        VoltageLevel s4vl1 = s4.newVoltageLevel()
                .setId("S4VL1")
                .setNominalV(400.0)
                .setLowVoltageLimit(390.0)
                .setHighVoltageLimit(440.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        BusbarSection busbarSectionS4VL1 = s4vl1.getNodeBreakerView().newBusbarSection()
                .setId("S4VL1_BBS")
                .setName("S4VL1_BBS")
                .setNode(0)
                .add();

        // Connect a load on the first voltage level of substation 1
        createSwitch(s1vl1, "S1VL1_BBS_LD1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s1vl1, "S1VL1_LD1_BREAKER", SwitchKind.BREAKER, false, 1, 2);
        Load load1 = s1vl1.newLoad()
                .setId("LD1")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(80)
                .setQ0(10)
                .setNode(2)
                .add();
        load1.getTerminal().setP(80.0).setQ(10.0);

        // Connect a TWT between the two voltage levels of substation 1, on the bus bar section 1 of the second voltage level
        // TWT could be connected to bbs 2 of the second VL through a second disconnector, which is open at the moment
        createSwitch(s1vl1, "S1VL1_BBS_TWT_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 3);
        createSwitch(s1vl1, "S1VL1_TWT_BREAKER", SwitchKind.BREAKER, false, 3, 4);
        createSwitch(s1vl2, "S1VL2_BBS1_TWT_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 2);
        createSwitch(s1vl2, "S1VL2_BBS2_TWT_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 1, 2);
        createSwitch(s1vl2, "S1VL2_TWT_BREAKER", SwitchKind.BREAKER, false, 2, 3);

        TwoWindingsTransformer twt = s1.newTwoWindingsTransformer()
                .setId("TWT")
                .setR(2.0)
                .setX(14.745)
                .setG(0.0)
                .setB(3.2E-5)
                .setRatedU1(225.0)
                .setRatedU2(400.0)
                .setNode1(4)
                .setVoltageLevel1("S1VL1")
                .setNode2(3)
                .setVoltageLevel2("S1VL2")
                .add();
        twt.newCurrentLimits1()
                .setPermanentLimit(1031.0)
                .add();
        twt.newCurrentLimits2()
                .setPermanentLimit(1031.0)
                .add();
        twt.newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(15)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulating(false)
                .setRegulationTerminal(twt.getTerminal(TwoSides.ONE))
                .beginStep().setR(39.78473).setX(29.784725).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-42.8).endStep()
                .beginStep().setR(31.720245).setX(21.720242).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-40.18).endStep()
                .beginStep().setR(23.655737).setX(13.655735).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-37.54).endStep()
                .beginStep().setR(16.263271).setX(6.263268).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-34.9).endStep()
                .beginStep().setR(9.542847).setX(4.542842).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-32.26).endStep()
                .beginStep().setR(3.4944773).setX(3.4944773).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-29.6).endStep()
                .beginStep().setR(-1.8818557).setX(-1.8818527).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-26.94).endStep()
                .beginStep().setR(-7.258195).setX(-3.2581954).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-24.26).endStep()
                .beginStep().setR(-11.962485).setX(-7.962484).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-21.58).endStep()
                .beginStep().setR(-15.994745).setX(-11.994745).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-18.9).endStep()
                .beginStep().setR(-19.354952).setX(-15.354952).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-16.22).endStep()
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
        twt.newRatioTapChanger()
                .beginStep().setR(0.0).setX(0.0).setB(0.0).setG(0.0).setRho(0.85).endStep()
                .beginStep().setR(0.0).setX(0.0).setB(0.0).setG(0.0).setRho(1).endStep()
                .beginStep().setR(0.0).setX(0.0).setB(0.0).setG(0.0).setRho(1.15).endStep()
                .setLowTapPosition(0)
                .setTapPosition(1)
                .setLoadTapChangingCapabilities(true)
                .setRegulating(true)
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setRegulationValue(225.0)
                .setTargetDeadband(0)
                .setRegulationTerminal(twt.getTerminal(TwoSides.ONE))
                .add();
        twt.getTerminal1().setP(-80.0).setQ(-10.0);
        twt.getTerminal2().setP(80.0809).setQ(5.4857);

        // Connect a VSC station to BBS2 of the second voltage level of substation 1, with a possibility to connect it to BBS1
        createSwitch(s1vl2, "S1VL2_BBS1_VSC1_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 0, 4);
        createSwitch(s1vl2, "S1VL2_BBS2_VSC1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 1, 4);
        createSwitch(s1vl2, "S1VL2_VSC1_BREAKER", SwitchKind.BREAKER, false, 4, 5);
        VscConverterStation vsc1 = s1vl2.newVscConverterStation()
                .setId("VSC1")
                .setName("VSC1")
                .setNode(5)
                .setLossFactor(1.1f)
                .setReactivePowerSetpoint(500)
                .setVoltageSetpoint(400)
                .setVoltageRegulatorOn(true)
                .add();
        vsc1.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(-100.0)
                .setMinQ(-550.0)
                .setMaxQ(570.0)
                .endPoint()
                .beginPoint()
                .setP(100.0)
                .setMinQ(-550.0)
                .setMaxQ(570.0)
                .endPoint()
                .add();
        vsc1.getTerminal().setP(10.1100).setQ(-512.0814);

        // Connect three hydro generators on the bbs 1 of the second voltage level of substation 1, with a possibility to connect them onto bbs 2
        createSwitch(s1vl2, "S1VL2_BBS1_GH1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 6);
        createSwitch(s1vl2, "S1VL2_BBS1_GH2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 8);
        createSwitch(s1vl2, "S1VL2_BBS1_GH3_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 10);
        createSwitch(s1vl2, "S1VL2_BBS2_GH1_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 1, 6);
        createSwitch(s1vl2, "S1VL2_BBS2_GH2_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 1, 8);
        createSwitch(s1vl2, "S1VL2_BBS2_GH3_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 1, 10);
        createSwitch(s1vl2, "S1VL2_GH1_BREAKER", SwitchKind.BREAKER, false, 6, 7);
        createSwitch(s1vl2, "S1VL2_GH2_BREAKER", SwitchKind.BREAKER, false, 8, 9);
        createSwitch(s1vl2, "S1VL2_GH3_BREAKER", SwitchKind.BREAKER, false, 10, 11);

        Generator generatorHydro1 = s1vl2.newGenerator()
                .setId("GH1")
                .setEnergySource(EnergySource.HYDRO)
                .setMinP(0.0)
                .setMaxP(100.0)
                .setVoltageRegulatorOn(true)
                .setTargetP(85.3570)
                .setTargetV(400.0)
                .setTargetQ(512.081)
                .setNode(7)
                .add();
        generatorHydro1.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(0.0)
                .setMinQ(-769.3)
                .setMaxQ(860.0)
                .endPoint()
                .beginPoint()
                .setP(100.0)
                .setMinQ(-864.55)
                .setMaxQ(946.25)
                .endPoint()
                .add();
        generatorHydro1.getTerminal().setP(-85.3570).setQ(-512.0814);

        Generator generatorHydro2 = s1vl2.newGenerator()
                .setId("GH2")
                .setEnergySource(EnergySource.HYDRO)
                .setMinP(0.0)
                .setMaxP(200.0)
                .setVoltageRegulatorOn(true)
                .setTargetP(90)
                .setTargetV(400.0)
                .setTargetQ(512.081)
                .setNode(9)
                .add();
        generatorHydro2.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(0.0)
                .setMinQ(-556.8)
                .setMaxQ(557.4)
                .endPoint()
                .beginPoint()
                .setP(200.0)
                .setMinQ(-553.514)
                .setMaxQ(536.4)
                .endPoint()
                .add();
        generatorHydro2.getTerminal().setP(-90.0).setQ(-512.0814);

        Generator generatorHydro3 = s1vl2.newGenerator()
                .setId("GH3")
                .setEnergySource(EnergySource.HYDRO)
                .setMinP(0.0)
                .setMaxP(200.0)
                .setVoltageRegulatorOn(true)
                .setTargetP(155.714)
                .setTargetV(400)
                .setTargetQ(512.081)
                .setNode(11)
                .add();
        generatorHydro3.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(0.0)
                .setMinQ(-680.6)
                .setMaxQ(688.1)
                .endPoint()
                .beginPoint()
                .setP(200.0)
                .setMinQ(-681.725)
                .setMaxQ(716.3500004)
                .endPoint()
                .add();
        generatorHydro3.getTerminal().setP(-155.7140).setQ(-512.0814);

        // Connect three loads on the bbs 2, with a possibility to connect them onto bbs 1
        createSwitch(s1vl2, "S1VL2_BBS1_LD2_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 0, 12);
        createSwitch(s1vl2, "S1VL2_BBS1_LD3_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 0, 14);
        createSwitch(s1vl2, "S1VL2_BBS1_LD4_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 0, 16);
        createSwitch(s1vl2, "S1VL2_BBS2_LD2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 1, 12);
        createSwitch(s1vl2, "S1VL2_BBS2_LD3_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 1, 14);
        createSwitch(s1vl2, "S1VL2_BBS2_LD4_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 1, 16);
        createSwitch(s1vl2, "S1VL2_LD2_BREAKER", SwitchKind.BREAKER, false, 12, 13);
        createSwitch(s1vl2, "S1VL2_LD3_BREAKER", SwitchKind.BREAKER, false, 14, 15);
        createSwitch(s1vl2, "S1VL2_LD4_BREAKER", SwitchKind.BREAKER, false, 16, 17);

        Load load2 = s1vl2.newLoad()
                .setId("LD2")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(60)
                .setQ0(5)
                .setNode(13)
                .add();
        load2.getTerminal().setP(60.0).setQ(5.0);

        Load load3 = s1vl2.newLoad()
                .setId("LD3")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(60)
                .setQ0(5)
                .setNode(15)
                .add();
        load3.getTerminal().setP(60.0).setQ(5.0);

        Load load4 = s1vl2.newLoad()
                .setId("LD4")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(40)
                .setQ0(5)
                .setNode(17)
                .add();
        load4.getTerminal().setP(40.0).setQ(5.0);

        // Connect a shunt on the BBS1, with a possibility to connect it to BBS2
        createSwitch(s1vl2, "S1VL2_BBS1_SHUNT_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 18);
        createSwitch(s1vl2, "S1VL2_BBS2_SHUNT_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 1, 18);
        createSwitch(s1vl2, "S1VL2_SHUNT_BREAKER", SwitchKind.BREAKER, false, 18, 19);

        ShuntCompensator shunt = s1vl2.newShuntCompensator()
                .setId("SHUNT")
                .setNode(19)
                .setSectionCount(1)
                .newLinearModel().setMaximumSectionCount(1).setBPerSection(-0.012).add()
                .add();
        shunt.getTerminal().setQ(1920.0);

        createSwitch(s1vl2, "S1VL2_BBS1_LCC1_DISCONNECTOR", SwitchKind.DISCONNECTOR, true, 0, 20);
        createSwitch(s1vl2, "S1VL2_BBS2_LCC1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 1, 20);
        createSwitch(s1vl2, "S1VL2_LCC1_BREAKER", SwitchKind.BREAKER, false, 20, 21);

        LccConverterStation lcc1 = s1vl2.newLccConverterStation()
                .setId("LCC1")
                .setName("LCC1")
                .setNode(21)
                .setLossFactor(1.1f)
                .setPowerFactor(0.6f)
                .add();
        lcc1.getTerminal().setP(80.8800);

        // Add a coupler between the two busbar sections
        createSwitch(s1vl2, "S1VL2_BBS1_COUPLER_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 22);
        createSwitch(s1vl2, "S1VL2_BBS2_COUPLER_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 1, 23);
        createSwitch(s1vl2, "S1VL2_COUPLER", SwitchKind.BREAKER, false, 22, 23);

        // Connect a thermal generator on the second substation
        createSwitch(s2vl1, "S2VL1_BBS_GTH1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s2vl1, "S2VL1_GTH1_BREAKER", SwitchKind.BREAKER, false, 1, 2);
        Generator generatorThermal1 = s2vl1.newGenerator()
                .setId("GTH1")
                .setEnergySource(EnergySource.THERMAL)
                .setMinP(0.0)
                .setMaxP(100.0)
                .setVoltageRegulatorOn(false)
                .setTargetP(100.0)
                .setTargetV(400)
                .setTargetQ(70)
                .setNode(2)
                .add();
        generatorThermal1.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(0.0)
                .setMinQ(-76.8)
                .setMaxQ(77.4)
                .endPoint()
                .beginPoint()
                .setP(100.0)
                .setMinQ(-73.514)
                .setMaxQ(76.4)
                .endPoint()
                .add();
        generatorThermal1.getTerminal().setP(-100.0).setQ(-70.0);

        // Connect a second VSC station on the second substation
        createSwitch(s2vl1, "S2VL1_BBS_VSC2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 3);
        createSwitch(s2vl1, "S2VL1_VSC2_BREAKER", SwitchKind.BREAKER, false, 3, 4);
        VscConverterStation vsc2 = s2vl1.newVscConverterStation()
                .setId("VSC2")
                .setName("VSC2")
                .setNode(4)
                .setLossFactor(1.1f)
                .setReactivePowerSetpoint(120)
                .setVoltageSetpoint(0)
                .setVoltageRegulatorOn(false)
                .add();
        vsc2.newMinMaxReactiveLimits()
                .setMinQ(-400.0)
                .setMaxQ(500.0)
                .add();
        vsc2.getTerminal().setP(-9.8900).setQ(-120.0);

        // The substations 1 and 2 are linked through an HVDC line
        network.newHvdcLine()
                .setId("HVDC1")
                .setName("HVDC1")
                .setConverterStationId1("VSC1")
                .setConverterStationId2("VSC2")
                .setR(1)
                .setNominalV(400)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                .setMaxP(300.0)
                .setActivePowerSetpoint(10)
                .add();

        // The substations 2 and 3 are connected through a line
        createSwitch(s2vl1, "S2VL1_BBS_LINES2S3_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 5);
        createSwitch(s2vl1, "S2VL1_LINES2S3_BREAKER", SwitchKind.BREAKER, false, 5, 6);
        createSwitch(s3vl1, "S3VL1_BBS_LINES2S3_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s3vl1, "S3VL1_LINES2S3_BREAKER", SwitchKind.BREAKER, false, 1, 2);
        Line lineS2S3 = network.newLine()
                .setId("LINE_S2S3")
                .setR(0.009999999)
                .setX(19.100000024)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setNode1(6)
                .setVoltageLevel1("S2VL1")
                .setNode2(2)
                .setVoltageLevel2(S3VL1)
                .add();
        lineS2S3.getTerminal1().setP(109.8893).setQ(190.0229);
        lineS2S3.getTerminal2().setP(-109.8864).setQ(-184.5171);

        // Connect a load onto the third substation
        createSwitch(s3vl1, "S3VL1_BBS_LD5_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 3);
        createSwitch(s3vl1, "S3VL1_LD5_BREAKER", SwitchKind.BREAKER, false, 3, 4);
        Load load5 = s3vl1.newLoad()
                .setId("LD5")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(200)
                .setQ0(5)
                .setNode(4)
                .add();
        load5.getTerminal().setP(200.0).setQ(5.0);

        // Connect a thermal generator onto the third substation
        createSwitch(s3vl1, "S3VL1_BBS_GTH2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 5);
        createSwitch(s3vl1, "S3VL1_GTH2_BREAKER", SwitchKind.BREAKER, false, 5, 6);
        Generator generatorThermal2 = s3vl1.newGenerator()
                .setId("GTH2")
                .setEnergySource(EnergySource.THERMAL)
                .setMinP(0.0)
                .setMaxP(400.0)
                .setVoltageRegulatorOn(true)
                .setTargetP(250.9944)
                .setTargetV(400)
                .setTargetQ(71.8487)
                .setNode(6)
                .add();
        generatorThermal2.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(0.0)
                .setMinQ(-169.3)
                .setMaxQ(200.0)
                .endPoint()
                .beginPoint()
                .setP(400.0)
                .setMinQ(-174.55)
                .setMaxQ(176.25)
                .endPoint()
                .add();
        generatorThermal2.getTerminal().setP(-250.9944).setQ(71.8487);

        // The stations 3 and 4 are linked by a line
        createSwitch(s3vl1, "S3VL1_BBS_LINES3S4_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 7);
        createSwitch(s3vl1, "S3VL1_LINES3S4_BREAKER", SwitchKind.BREAKER, false, 7, 8);
        createSwitch(s4vl1, "S4VL1_BBS_LINES3S4_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 5);
        createSwitch(s4vl1, "S4VL1_LINES3S4_BREAKER", SwitchKind.BREAKER, false, 5, 6);
        Line lineS3S4 = network.newLine()
                .setId("LINE_S3S4")
                .setR(0.009999999)
                .setX(13.100000024)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setNode1(8)
                .setVoltageLevel1(S3VL1)
                .setNode2(6)
                .setVoltageLevel2("S4VL1")
                .add();
        lineS3S4.getTerminal1().setP(240.0036).setQ(2.1751);
        lineS3S4.getTerminal2().setP(-240.0).setQ(2.5415);
        lineS3S4.newCurrentLimits1()
                .setPermanentLimit(931.0)
                .add();
        lineS3S4.newCurrentLimits2()
                .setPermanentLimit(931.0)
                .beginTemporaryLimit()
                .setName("IST")
                .setValue(1640.0)
                .setFictitious(true)
                .setAcceptableDuration(Integer.MAX_VALUE)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("LD71")
                .setValue(Double.MAX_VALUE)
                .setAcceptableDuration(60)
                .endTemporaryLimit()
                .add();

        // Connect an LCC station to the third substation
        createSwitch(s3vl1, "S3VL1_BBS_LCC2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 9);
        createSwitch(s3vl1, "S3VL1_LCC2_BREAKER", SwitchKind.BREAKER, false, 9, 10);
        LccConverterStation lcc2 = s3vl1.newLccConverterStation()
                .setId("LCC2")
                .setName("LCC2")
                .setNode(10)
                .setLossFactor(1.1f)
                .setPowerFactor(0.6f)
                .add();
        lcc2.getTerminal().setP(-79.1200);

        // The substations 1 and 3 are linked by an HVDC line
        network.newHvdcLine()
                .setId("HVDC2")
                .setName("HVDC2")
                .setConverterStationId1("LCC1")
                .setConverterStationId2("LCC2")
                .setR(1)
                .setNominalV(400)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                .setMaxP(300.0)
                .setActivePowerSetpoint(80)
                .add();

        // Connect a load to the fourth substation
        createSwitch(s4vl1, "S4VL1_BBS_LD6_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s4vl1, "S4VL1_LD6_BREAKER", SwitchKind.BREAKER, false, 1, 2);
        Load load6 = s4vl1.newLoad()
                .setId("LD6")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(240)
                .setQ0(10)
                .setNode(2)
                .add();
        load6.getTerminal().setP(240.0).setQ(10.0);

        // Connect a static var compensator to the fourth substation
        createSwitch(s4vl1, "S4VL1_BBS_SVC_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 3);
        createSwitch(s4vl1, "S4VL1_SVC_BREAKER", SwitchKind.BREAKER, false, 3, 4);
        StaticVarCompensator svc = s4vl1.newStaticVarCompensator()
                .setId("SVC")
                .setNode(4)
                .setBmin(-5e-2)
                .setBmax(5e-2)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(400)
                .add();
        svc.getTerminal().setQ(-12.5415);

        busbarSectionS1VL1.getTerminal().getBusView().getBus()
                .setV(224.6139)
                .setAngle(2.2822);
        busbarSectionS1VL21.getTerminal().getBusView().getBus()
                .setV(400.0)
                .setAngle(0.0);
        busbarSectionS1VL22.getTerminal().getBusView().getBus()
                .setV(400.0)
                .setAngle(0.0);
        busbarSectionS2VL1.getTerminal().getBusView().getBus()
                .setV(408.8470)
                .setAngle(0.7347);
        busbarSectionS3VL1.getTerminal().getBusView().getBus()
                .setV(400.0)
                .setAngle(0.0);
        busbarSectionS4VL1.getTerminal().getBusView().getBus()
                .setV(400.0)
                .setAngle(-1.1259);

        return network;

    }

    private static void createSwitch(VoltageLevel vl, String id, SwitchKind kind, boolean open, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
                .setId(id)
                .setName(id)
                .setKind(kind)
                .setRetained(kind.equals(SwitchKind.BREAKER))
                .setOpen(open)
                .setFictitious(false)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }
}
