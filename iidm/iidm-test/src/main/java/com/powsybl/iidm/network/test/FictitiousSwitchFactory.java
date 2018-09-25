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
                .setNominalV(225.0)
                .setLowVoltageLimit(0.0)
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
                .setNominalV(225.0)
                .setLowVoltageLimit(220.0)
                .setHighVoltageLimit(245.00002)
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
                .setMinP(0.0)
                .setMaxP(70.0)
                .setVoltageRegulatorOn(false)
                .setTargetP(0.0)
                .setTargetV(0.0)
                .setTargetQ(0.0)
                .setNode(12)
                .add();
        generatorCB.newReactiveCapabilityCurve()
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

        Generator generatorCC = vlN.newGenerator()
                .setId("CC")
                .setEnergySource(EnergySource.HYDRO)
                .setMinP(0.0)
                .setMaxP(80.0)
                .setVoltageRegulatorOn(false)
                .setTargetP(0.0)
                .setTargetV(0.0)
                .setTargetQ(0.0)
                .setNode(14)
                .add();
        generatorCC.newReactiveCapabilityCurve()
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

        Generator generatorCD = vlN.newGenerator()
                .setId("CD")
                .setEnergySource(EnergySource.HYDRO)
                .setMinP(0.0)
                .setMaxP(35.0)
                .setVoltageRegulatorOn(true)
                .setTargetP(21.789589)
                .setTargetV(236.44736)
                .setTargetQ(-20.701546)
                .setNode(16)
                .add();
        generatorCD.getTerminal()
                .setP(-21.789589)
                .setQ(20.693394);
        generatorCD.newReactiveCapabilityCurve()
                .beginPoint()
                    .setP(0.0)
                    .setMinQ(-20.6)
                    .setMaxQ(18.1)
                .endPoint()
                .beginPoint()
                    .setP(35.0)
                    .setMinQ(-21.725)
                    .setMaxQ(6.3500004)
                .endPoint()
                .add();

        Load loadCE = vlN.newLoad()
                .setId("CE")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(-72.18689)
                .setQ0(50.168945)
                .setNode(4)
                .add();
        loadCE.getTerminal()
                .setP(-72.18689)
                .setQ(50.168945);

        Load loadCF = vlN.newLoad()
                .setId("CF")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(8.455854)
                .setQ0(-23.695925)
                .setNode(18)
                .add();
        loadCF.getTerminal()
                .setP(8.455854)
                .setQ(-23.695925);

        Load loadCG = vlN.newLoad()
                .setId("CG")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(90.39911)
                .setQ0(-51.96869)
                .setNode(20)
                .add();
        loadCG.getTerminal()
                .setP(90.39911)
                .setQ(-51.96869);

        Load loadCH = vlN.newLoad()
                .setId("CH")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(-5.102249)
                .setQ0(4.9081216)
                .setNode(22)
                .add();
        loadCH.getTerminal()
                .setP(-5.102249)
                .setQ(4.9081216);

        TwoWindingsTransformer twtCI = s.newTwoWindingsTransformer()
                .setId("CI")
                .setR(2.0)
                .setX(14.745)
                .setG(0.0)
                .setB(3.2E-5)
                .setRatedU1(225.0)
                .setRatedU2(225.0)
                .setNode1(2)
                .setVoltageLevel1("C")
                .setNode2(10)
                .setVoltageLevel2("N")
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
                .beginStep().setRdr(39.78473).setRdx(39.784725).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-42.8).endStep()
                .beginStep().setRdr(31.720245).setRdx(31.720242).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-40.18).endStep()
                .beginStep().setRdr(23.655737).setRdx(23.655735).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-37.54).endStep()
                .beginStep().setRdr(16.263271).setRdx(16.263268).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-34.9).endStep()
                .beginStep().setRdr(9.542847).setRdx(9.542842).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-32.26).endStep()
                .beginStep().setRdr(3.4944773).setRdx(3.4944773).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-29.6).endStep()
                .beginStep().setRdr(-1.8818557).setRdx(-1.8818527).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-26.94).endStep()
                .beginStep().setRdr(-7.258195).setRdx(-7.2581954).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-24.26).endStep()
                .beginStep().setRdr(-11.962485).setRdx(-11.962484).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-21.58).endStep()
                .beginStep().setRdr(-15.994745).setRdx(-15.994745).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-18.9).endStep()
                .beginStep().setRdr(-19.354952).setRdx(-19.354952).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-16.22).endStep()
                .beginStep().setRdr(-22.043127).setRdx(-22.043129).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-13.52).endStep()
                .beginStep().setRdr(-24.73129).setRdx(-24.731287).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-10.82).endStep()
                .beginStep().setRdr(-26.747417).setRdx(-26.747417).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-8.12).endStep()
                .beginStep().setRdr(-28.091503).setRdx(-28.091503).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-5.42).endStep()
                .beginStep().setRdr(-28.763538).setRdx(-28.763536).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(-2.7).endStep()
                .beginStep().setRdr(-28.763538).setRdx(-28.763536).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(0.0).endStep()
                .beginStep().setRdr(-28.763538).setRdx(-28.763536).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(2.7).endStep()
                .beginStep().setRdr(-28.091503).setRdx(-28.091503).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(5.42).endStep()
                .beginStep().setRdr(-26.747417).setRdx(-26.747417).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(8.12).endStep()
                .beginStep().setRdr(-24.73129).setRdx(-24.731287).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(10.82).endStep()
                .beginStep().setRdr(-22.043127).setRdx(-22.043129).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(13.52).endStep()
                .beginStep().setRdr(-19.354952).setRdx(-19.354952).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(16.22).endStep()
                .beginStep().setRdr(-15.994745).setRdx(-15.994745).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(18.9).endStep()
                .beginStep().setRdr(-11.962485).setRdx(-11.962484).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(21.58).endStep()
                .beginStep().setRdr(-7.258195).setRdx(-7.2581954).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(24.26).endStep()
                .beginStep().setRdr(-1.8818557).setRdx(-1.8818527).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(26.94).endStep()
                .beginStep().setRdr(3.4944773).setRdx(3.4944773).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(29.6).endStep()
                .beginStep().setRdr(9.542847).setRdx(9.542842).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(32.26).endStep()
                .beginStep().setRdr(16.263271).setRdx(16.263268).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(34.9).endStep()
                .beginStep().setRdr(23.655737).setRdx(23.655735).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(37.54).endStep()
                .beginStep().setRdr(31.720245).setRdx(31.720242).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(40.18).endStep()
                .beginStep().setRdr(39.78473).setRdx(39.784725).setRdg(0.0).setRdb(0.0).setRho(1.0).setAlpha(42.8).endStep()
                .add();

        Line lineCJ = network.newLine()
                .setId("CJ")
                .setR(0.009999999)
                .setX(0.100000024)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setNode1(4)
                .setVoltageLevel1("C")
                .setNode2(5)
                .setVoltageLevel2("N")
                .add();
        lineCJ.newCurrentLimits1()
                .setPermanentLimit(931.0)
                .add();
        lineCJ.newCurrentLimits2()
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

        busbarSectionD.getTerminal().getBusView().getBus()
                .setV(234.40912)
                .setAngle(0.0);
        busbarSectionO.getTerminal().getBusView().getBus()
                .setV(236.44736)
                .setAngle(15250391);
        busbarSectionP.getTerminal().getBusView().getBus()
                .setV(236.44736)
                .setAngle(15.250391);

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
