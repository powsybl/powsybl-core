/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class FictitiousSwitchFactory {

    private FictitiousSwitchFactory() {
    }

    public static Network create() {
        Network network = NetworkFactory.create("fictitious", "test");
        network.setCaseDate(DateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.setForecastDistance(0);

        Substation s = network.newSubstation()
                .setId("A")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vlC = s.newVoltageLevel()
                .setId("C")
                .setNominalV(225.0f)
                .setLowVoltageLimit(0.0f)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vlC.getNodeBreakerView().setNodeCount(5);
        BusbarSection busbarSectionD = vlC.getNodeBreakerView().newBusbarSection()
                .setId("D")
                .setName("E")
                .setNode(0)
                .add();
        createSwitch(vlC, "F", "G", SwitchKind.DISCONNECTOR, false, false, true, 0, 1);
        createSwitch(vlC, "H", "I", SwitchKind.DISCONNECTOR, false, false, true, 0, 3);
        createSwitch(vlC, "J", "K", SwitchKind.BREAKER, true, false, true, 1, 2);
        createSwitch(vlC, "L", "M", SwitchKind.BREAKER, true, false, true, 3, 4);

        VoltageLevel vlN = s.newVoltageLevel()
                .setId("N")
                .setNominalV(225.0f)
                .setLowVoltageLimit(220.0f)
                .setHighVoltageLimit(245.00002f)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vlN.getNodeBreakerView().setNodeCount(23);
        BusbarSection busbarSectionO = vlN.getNodeBreakerView().newBusbarSection()
                .setId("O")
                .setName("E")
                .setNode(0)
                .add();
        BusbarSection busbarSectionP = vlN.getNodeBreakerView().newBusbarSection()
                .setId("P")
                .setName("Q")
                .setNode(1)
                .add();
        createSwitch(vlN, "R", "S", SwitchKind.DISCONNECTOR, false, true, false, 0, 19);
        createSwitch(vlN, "T", "U", SwitchKind.DISCONNECTOR, false, true, false, 0, 17);
        createSwitch(vlN, "V", "W", SwitchKind.DISCONNECTOR, false, true, false, 0, 21);
        createSwitch(vlN, "X", "Y", SwitchKind.DISCONNECTOR, false, true, false, 0, 11);
        createSwitch(vlN, "Z", "AA", SwitchKind.DISCONNECTOR, false, true, false, 0, 13);
        createSwitch(vlN, "AB", "AC", SwitchKind.DISCONNECTOR, false, false, false, 0, 15);
        createSwitch(vlN, "AD", "AE", SwitchKind.DISCONNECTOR, false, true, false, 0, 8);
        createSwitch(vlN, "AF", "AG", SwitchKind.DISCONNECTOR, false, true, true, 0, 2);
        createSwitch(vlN, "AH", "AI", SwitchKind.DISCONNECTOR, false, false, false, 7, 0);
        createSwitch(vlN, "AJ", "AK", SwitchKind.DISCONNECTOR, false, false, false, 1, 6);
        createSwitch(vlN, "AL", "AM", SwitchKind.DISCONNECTOR, false, false, false, 1, 19);
        createSwitch(vlN, "AN", "AO", SwitchKind.DISCONNECTOR, false, false, false, 1, 17);
        createSwitch(vlN, "AP", "AQ", SwitchKind.DISCONNECTOR, false, false, false, 1, 21);
        createSwitch(vlN, "AR", "AS", SwitchKind.DISCONNECTOR, false, true, false, 1, 11);
        createSwitch(vlN, "AT", "AU", SwitchKind.DISCONNECTOR, false, true, false, 1, 13);
        createSwitch(vlN, "AV", "AW", SwitchKind.DISCONNECTOR, false, true, false, 1, 15);
        createSwitch(vlN, "AX", "AY", SwitchKind.DISCONNECTOR, false, false, false, 1, 8);
        createSwitch(vlN, "AZ", "BA", SwitchKind.DISCONNECTOR, false, true, true, 1, 2);
        createSwitch(vlN, "BB", "BC", SwitchKind.BREAKER, true, true, true, 2, 3);
        createSwitch(vlN, "BD", "BE", SwitchKind.BREAKER, true, false, false, 3, 4);
        createSwitch(vlN, "BF", "BG", SwitchKind.DISCONNECTOR, false, false, false, 3, 5);
        createSwitch(vlN, "BH", "BI", SwitchKind.DISCONNECTOR, false, true, false, 9, 3);
        createSwitch(vlN, "BJ", "BK", SwitchKind.BREAKER, true, false, false, 6, 7);
        createSwitch(vlN, "BL", "BM", SwitchKind.BREAKER, true, false, false, 8, 9);
        createSwitch(vlN, "BN", "BO", SwitchKind.DISCONNECTOR, false, false, false, 9, 10);
        createSwitch(vlN, "BP", "BQ", SwitchKind.BREAKER, true, true, false, 11, 12);
        createSwitch(vlN, "BR", "BS", SwitchKind.BREAKER, true, true, false, 13, 14);
        createSwitch(vlN, "BT", "BU", SwitchKind.BREAKER, true, false, false, 15, 16);
        createSwitch(vlN, "BV", "BW", SwitchKind.BREAKER, true, false, false, 17, 18);
        createSwitch(vlN, "BX", "BY", SwitchKind.BREAKER, true, false, false, 19, 20);
        createSwitch(vlN, "BZ", "CA", SwitchKind.BREAKER, true, false, false, 21, 22);

        Generator generatorCB = vlN.newGenerator()
                .setId("CB")
                .setEnergySource(EnergySource.HYDRO)
                .setMinP(0.0f)
                .setMaxP(70.0f)
                .setVoltageRegulatorOn(false)
                .setTargetP(0.0f)
                .setTargetV(0.0f)
                .setTargetQ(0.0f)
                .setNode(12)
                .add();
        generatorCB.newReactiveCapabilityCurve()
                .beginPoint()
                    .setP(0.0f)
                    .setMinQ(-59.3f)
                    .setMaxQ(60.0f)
                .endPoint()
                .beginPoint()
                    .setP(70.0f)
                    .setMinQ(-54.55f)
                    .setMaxQ(46.25f)
                .endPoint()
                .add();

        Generator generatorCC = vlN.newGenerator()
                .setId("CC")
                .setEnergySource(EnergySource.HYDRO)
                .setMinP(0.0f)
                .setMaxP(80.0f)
                .setVoltageRegulatorOn(false)
                .setTargetP(0.0f)
                .setTargetV(0.0f)
                .setTargetQ(0.0f)
                .setNode(14)
                .add();
        generatorCC.newReactiveCapabilityCurve()
                .beginPoint()
                    .setP(0.0f)
                    .setMinQ(-56.8f)
                    .setMaxQ(57.4f)
                .endPoint()
                .beginPoint()
                    .setP(80.0f)
                    .setMinQ(-53.514f)
                    .setMaxQ(36.4f)
                .endPoint()
                .add();

        Generator generatorCD = vlN.newGenerator()
                .setId("CD")
                .setEnergySource(EnergySource.HYDRO)
                .setMinP(0.0f)
                .setMaxP(35.0f)
                .setVoltageRegulatorOn(true)
                .setTargetP(21.789589f)
                .setTargetV(236.44736f)
                .setTargetQ(-20.701546f)
                .setNode(16)
                .add();
        generatorCD.getTerminal()
                .setP(-21.789589f)
                .setQ(20.693394f);
        generatorCD.newReactiveCapabilityCurve()
                .beginPoint()
                    .setP(0.0f)
                    .setMinQ(-20.6f)
                    .setMaxQ(18.1f)
                .endPoint()
                .beginPoint()
                    .setP(35.0f)
                    .setMinQ(-21.725f)
                    .setMaxQ(6.3500004f)
                .endPoint()
                .add();

        Load loadCE = vlN.newLoad()
                .setId("CE")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(-72.18689f)
                .setQ0(50.168945f)
                .setNode(4)
                .add();
        loadCE.getTerminal()
                .setP(-72.18689f)
                .setQ(50.168945f);

        Load loadCF = vlN.newLoad()
                .setId("CF")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(8.455854f)
                .setQ0(-23.695925f)
                .setNode(18)
                .add();
        loadCF.getTerminal()
                .setP(8.455854f)
                .setQ(-23.695925f);

        Load loadCG = vlN.newLoad()
                .setId("CG")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(90.39911f)
                .setQ0(-51.96869f)
                .setNode(20)
                .add();
        loadCG.getTerminal()
                .setP(90.39911f)
                .setQ(-51.96869f);

        Load loadCH = vlN.newLoad()
                .setId("CH")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(-5.102249f)
                .setQ0(4.9081216f)
                .setNode(22)
                .add();
        loadCH.getTerminal()
                .setP(-5.102249f)
                .setQ(4.9081216f);

        TwoWindingsTransformer twtCI = s.newTwoWindingsTransformer()
                .setId("CI")
                .setR(2.0f)
                .setX(14.745f)
                .setG(0.0f)
                .setB(3.2E-5f)
                .setRatedU1(225.0f)
                .setRatedU2(225.0f)
                .setNode1(2)
                .setVoltageLevel1("C")
                .setNode2(10)
                .setVoltageLevel2("N")
                .add();
        twtCI.newCurrentLimits1()
                .setPermanentLimit(931.0f)
                .add();
        twtCI.newCurrentLimits2()
                .setPermanentLimit(931.0f)
                .add();
        twtCI.newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(22)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(930.6667f)
                .setRegulating(false)
                .setRegulationTerminal(twtCI.getTerminal(Branch.Side.ONE))
                .beginStep().setR(39.78473f).setX(39.784725f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-42.8f).endStep()
                .beginStep().setR(31.720245f).setX(31.720242f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-40.18f).endStep()
                .beginStep().setR(23.655737f).setX(23.655735f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-37.54f).endStep()
                .beginStep().setR(16.263271f).setX(16.263268f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-34.9f).endStep()
                .beginStep().setR(9.542847f).setX(9.542842f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-32.26f).endStep()
                .beginStep().setR(3.4944773f).setX(3.4944773f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-29.6f).endStep()
                .beginStep().setR(-1.8818557f).setX(-1.8818527f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-26.94f).endStep()
                .beginStep().setR(-7.258195f).setX(-7.2581954f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-24.26f).endStep()
                .beginStep().setR(-11.962485f).setX(-11.962484f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-21.58f).endStep()
                .beginStep().setR(-15.994745f).setX(-15.994745f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-18.9f).endStep()
                .beginStep().setR(-19.354952f).setX(-19.354952f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-16.22f).endStep()
                .beginStep().setR(-22.043127f).setX(-22.043129f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-13.52f).endStep()
                .beginStep().setR(-24.73129f).setX(-24.731287f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-10.82f).endStep()
                .beginStep().setR(-26.747417f).setX(-26.747417f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-8.12f).endStep()
                .beginStep().setR(-28.091503f).setX(-28.091503f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-5.42f).endStep()
                .beginStep().setR(-28.763538f).setX(-28.763536f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(-2.7f).endStep()
                .beginStep().setR(-28.763538f).setX(-28.763536f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(0.0f).endStep()
                .beginStep().setR(-28.763538f).setX(-28.763536f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(2.7f).endStep()
                .beginStep().setR(-28.091503f).setX(-28.091503f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(5.42f).endStep()
                .beginStep().setR(-26.747417f).setX(-26.747417f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(8.12f).endStep()
                .beginStep().setR(-24.73129f).setX(-24.731287f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(10.82f).endStep()
                .beginStep().setR(-22.043127f).setX(-22.043129f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(13.52f).endStep()
                .beginStep().setR(-19.354952f).setX(-19.354952f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(16.22f).endStep()
                .beginStep().setR(-15.994745f).setX(-15.994745f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(18.9f).endStep()
                .beginStep().setR(-11.962485f).setX(-11.962484f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(21.58f).endStep()
                .beginStep().setR(-7.258195f).setX(-7.2581954f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(24.26f).endStep()
                .beginStep().setR(-1.8818557f).setX(-1.8818527f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(26.94f).endStep()
                .beginStep().setR(3.4944773f).setX(3.4944773f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(29.6f).endStep()
                .beginStep().setR(9.542847f).setX(9.542842f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(32.26f).endStep()
                .beginStep().setR(16.263271f).setX(16.263268f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(34.9f).endStep()
                .beginStep().setR(23.655737f).setX(23.655735f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(37.54f).endStep()
                .beginStep().setR(31.720245f).setX(31.720242f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(40.18f).endStep()
                .beginStep().setR(39.78473f).setX(39.784725f).setG(0.0f).setB(0.0f).setRho(1.0f).setAlpha(42.8f).endStep()
                .add();

        Line lineCJ = network.newLine()
                .setId("CJ")
                .setR(0.009999999f)
                .setX(0.100000024f)
                .setG1(0.0f)
                .setB1(0.0f)
                .setG2(0.0f)
                .setB2(0.0f)
                .setNode1(4)
                .setVoltageLevel1("C")
                .setNode2(5)
                .setVoltageLevel2("N")
                .add();
        lineCJ.newCurrentLimits1()
                .setPermanentLimit(931.0f)
                .add();
        lineCJ.newCurrentLimits2()
                .setPermanentLimit(931.0f)
                .beginTemporaryLimit()
                    .setName("IST")
                    .setValue(1640.0f)
                    .setFictitious(true)
                    .setAcceptableDuration(Integer.MAX_VALUE)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("IT1")
                    .setValue(Float.MAX_VALUE)
                    .setAcceptableDuration(60)
                .endTemporaryLimit()
                .add();

        busbarSectionD.getTerminal().getBusView().getBus()
                .setV(234.40912f)
                .setAngle(0.0f);
        busbarSectionO.getTerminal().getBusView().getBus()
                .setV(236.44736f)
                .setAngle(15250391f);
        busbarSectionP.getTerminal().getBusView().getBus()
                .setV(236.44736f)
                .setAngle(15.250391f);

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
