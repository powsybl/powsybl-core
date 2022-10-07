/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.contingency.list.*;
import com.powsybl.contingency.contingency.list.criterion.*;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class CriterionContingencyListTest {

    Network fourSubstationNetwork;

    @Before
    public void setup() {
        fourSubstationNetwork = FourSubstationsNodeBreakerFactory.create();
        fourSubstationNetwork.getSubstation("S1").setCountry(Country.FR);
        fourSubstationNetwork.getSubstation("S2").setCountry(Country.BE);
        fourSubstationNetwork.getSubstation("S3").setCountry(Country.FR);
        fourSubstationNetwork.getSubstation("S4").setCountry(Country.LU);
    }

    @Test
    public void testCountries() {

        // lines in France
        TwoCountriesCriterion countriesCriterion = new TwoCountriesCriterion(Collections.singletonList(Country.FR),
                Collections.emptyList());
        LineCriterionContingencyList contingencyList = new LineCriterionContingencyList("list1", countriesCriterion,
                null, null, null);
        List<Contingency> contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));
        assertEquals(new Contingency("LINE_S3S4", new LineContingency("LINE_S3S4")), contingencies.get(1));

        // lines between France and Belgium
        countriesCriterion = new TwoCountriesCriterion(Collections.singletonList(Country.FR),
                Collections.singletonList(Country.BE));
        contingencyList = new LineCriterionContingencyList("list1", countriesCriterion,
                null, null, null);
        contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));

        // Generator in Belgium
        SingleCountryCriterion countryCriterion = new SingleCountryCriterion(Collections.singletonList(Country.BE));
        InjectionCriterionContingencyList generatorContingencyList = new InjectionCriterionContingencyList("list1",
                "GENERATOR", countryCriterion, null, null, null);
        contingencies = generatorContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("GTH1", new GeneratorContingency("GTH1")), contingencies.get(0));

        // All generators
        generatorContingencyList = new InjectionCriterionContingencyList("list1", "GENERATOR", null, null, null, null);
        contingencies = generatorContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(5, contingencies.size());
        assertEquals(new Contingency("GH1", new GeneratorContingency("GH1")), contingencies.get(0));
        assertEquals(new Contingency("GH2", new GeneratorContingency("GH2")), contingencies.get(1));
        assertEquals(new Contingency("GH3", new GeneratorContingency("GH3")), contingencies.get(2));
        assertEquals(new Contingency("GTH1", new GeneratorContingency("GTH1")), contingencies.get(3));
        assertEquals(new Contingency("GTH2", new GeneratorContingency("GTH2")), contingencies.get(4));

        // hvdc between Belgium and France
        countriesCriterion = new TwoCountriesCriterion(Collections.singletonList(Country.FR),
                Collections.singletonList(Country.BE));
        HvdcLineCriterionContingencyList hvdcLineCriterionContingencyList = new HvdcLineCriterionContingencyList("list",
                countriesCriterion, null, null, null);
        contingencies = hvdcLineCriterionContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("HVDC1", new HvdcLineContingency("HVDC1")), contingencies.get(0));

        // transfo2 in France
        countryCriterion = new SingleCountryCriterion(Collections.singletonList(Country.FR));
        TwoWindingsTransformerCriterionContingencyList twoWindingsTransformerCriterionContingencyList = new TwoWindingsTransformerCriterionContingencyList("list",
                countryCriterion, null, null, null);
        contingencies = twoWindingsTransformerCriterionContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("TWT", new TwoWindingsTransformerContingency("TWT")), contingencies.get(0));
    }

    @Test
    public void testNominalVoltage() {

        // load on 225 kV
        SingleNominalVoltageCriterion singleNominalVoltageCriterion = new SingleNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(200.0, 230.0, true, true));
        InjectionCriterionContingencyList contingencyList = new InjectionCriterionContingencyList("list1",
                "LOAD", null, singleNominalVoltageCriterion, null, null);
        List<Contingency> contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("LD1", new LoadContingency("LD1")), contingencies.get(0));

        //  transformer between 225 kV and 400 kV
        TwoNominalVoltageCriterion twoNominalVoltageCriterion = new TwoNominalVoltageCriterion(
                new SingleNominalVoltageCriterion.VoltageInterval(200.0, 230.0,
                        true, true),
                new SingleNominalVoltageCriterion.VoltageInterval(380.0, 420.0,
                        true, true));
        TwoWindingsTransformerCriterionContingencyList transformerContingencyList = new TwoWindingsTransformerCriterionContingencyList("list1", null,
                twoNominalVoltageCriterion, null, null);
        contingencies = transformerContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("TWT", new TwoWindingsTransformerContingency("TWT")), contingencies.get(0));

        // 400 kV lines
        singleNominalVoltageCriterion = new SingleNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(380.0, 400.0, true, true));
        LineCriterionContingencyList lineContingencyList = new LineCriterionContingencyList("list1", null,
                singleNominalVoltageCriterion, null, null);
        contingencies = lineContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));
        assertEquals(new Contingency("LINE_S3S4", new LineContingency("LINE_S3S4")), contingencies.get(1));

        // Hvdc
        twoNominalVoltageCriterion = new TwoNominalVoltageCriterion(new SingleNominalVoltageCriterion.VoltageInterval(380.0, 420.0,
                true, true), null);
        HvdcLineCriterionContingencyList hvdcLineCriterionContingencyList = new HvdcLineCriterionContingencyList("list",
                null, twoNominalVoltageCriterion, null, null);
        contingencies = hvdcLineCriterionContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("HVDC1", new HvdcLineContingency("HVDC1")), contingencies.get(0));
        assertEquals(new Contingency("HVDC2", new HvdcLineContingency("HVDC2")), contingencies.get(1));
    }

    @Test
    public void testProperty() {
        fourSubstationNetwork.getGenerator("GH1").setProperty("property", "val1");
        fourSubstationNetwork.getGenerator("GH3").setProperty("property", "val4");
        fourSubstationNetwork.getGenerator("GTH1").setProperty("property", "val2");
        fourSubstationNetwork.getGenerator("GTH2").setProperty("property", "val3");
        List<String> values = new ArrayList<>();
        values.add("val1");
        values.add("val4");
        PropertyCriterion propertyCriterion = new PropertyCriterion("property", values);
        ContingencyList contingencyList = new InjectionCriterionContingencyList("list1", "GENERATOR",
                null, null, propertyCriterion, null);
        List<Contingency> contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("GH1", new GeneratorContingency("GH1")), contingencies.get(0));
        assertEquals(new Contingency("GH3", new GeneratorContingency("GH3")), contingencies.get(1));
    }

    @Test
    public void testRegex() {
        RegexCriterion regexCriterion = new RegexCriterion("3$");
        ContingencyList contingencyList = new LineCriterionContingencyList("list1",
                null, null, null, regexCriterion);
        List<Contingency> contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));
    }

    @Test
    public void testThreeWindingsTransformer() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.getSubstation("SUBSTATION").setCountry(Country.FR);
        SingleCountryCriterion countryCriterion = new SingleCountryCriterion(Collections.singletonList(Country.FR));
        ThreeNominalVoltageCriterion criterion = new ThreeNominalVoltageCriterion(
                new SingleNominalVoltageCriterion.VoltageInterval(110.0, 150.0,
                        true, true),
                new SingleNominalVoltageCriterion.VoltageInterval(20.0, 40.0,
                        true, true),
                new SingleNominalVoltageCriterion.VoltageInterval(5.0, 20.0,
                        true, true));
        ThreeWindingsTransformerCriterionContingencyList contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list",
                countryCriterion, criterion, null, null);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("3WT", new ThreeWindingsTransformerContingency("3WT")), contingencies.get(0));

        //

        // no filter
        criterion = new ThreeNominalVoltageCriterion(null, null, null);
        contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list",
                null, criterion, null, null);
        contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("3WT", new ThreeWindingsTransformerContingency("3WT")), contingencies.get(0));

        criterion = new ThreeNominalVoltageCriterion(new SingleNominalVoltageCriterion.VoltageInterval(110.0, 150.0,
                true, true),
                new SingleNominalVoltageCriterion.VoltageInterval(20.0, 40.0,
                        true, true), null);
        contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list",
                null, criterion, null, null);
        contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("3WT", new ThreeWindingsTransformerContingency("3WT")), contingencies.get(0));

        criterion = new ThreeNominalVoltageCriterion(new SingleNominalVoltageCriterion.VoltageInterval(110.0, 150.0,
                true, true), null, null);
        contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list",
                null, criterion, null, null);
        contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("3WT", new ThreeWindingsTransformerContingency("3WT")), contingencies.get(0));

        criterion = new ThreeNominalVoltageCriterion(new SingleNominalVoltageCriterion.VoltageInterval(1000.0, 1200.0,
                true, true), null, null);
        contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list",
                null, criterion, null, null);
        contingencies = contingencyList.getContingencies(network);
        assertEquals(0, contingencies.size());
    }

    @Test
    public void testDanglingLines() {
        // dangling lines
        Network network = DanglingLineNetworkFactory.create();
        SingleCountryCriterion countriesCriterion = new SingleCountryCriterion(Collections.singletonList(Country.FR));
        SingleNominalVoltageCriterion nominalVoltageCriterion = new SingleNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(90.0, 130.0, true, false));
        InjectionCriterionContingencyList contingencyList = new InjectionCriterionContingencyList("list2",
                IdentifiableType.DANGLING_LINE, countriesCriterion, nominalVoltageCriterion, null, null);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("DL", new DanglingLineContingency("DL")), contingencies.get(0));
    }

    @Test
    public void testSomeInjections() {
        SingleCountryCriterion countriesCriterion = new SingleCountryCriterion(Collections.singletonList(Country.FR));
        SingleNominalVoltageCriterion nominalVoltageCriterion = new SingleNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(390.0, 440.0, false, false));
        InjectionCriterionContingencyList contingencyList = new InjectionCriterionContingencyList("list2",
                IdentifiableType.SWITCH, countriesCriterion, nominalVoltageCriterion, null, null);
        List<Contingency> contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(43, contingencies.size());
        assertEquals(new Contingency("S1VL2_BBS1_TWT_DISCONNECTOR", new SwitchContingency("S1VL2_BBS1_TWT_DISCONNECTOR")),
                contingencies.get(0));
        assertEquals(new Contingency("S1VL2_BBS2_TWT_DISCONNECTOR", new SwitchContingency("S1VL2_BBS2_TWT_DISCONNECTOR")),
                contingencies.get(1));
        assertEquals(new Contingency("S1VL2_TWT_BREAKER", new SwitchContingency("S1VL2_TWT_BREAKER")),
                contingencies.get(2));

        // shunt
        contingencyList = new InjectionCriterionContingencyList("list2",
                IdentifiableType.SHUNT_COMPENSATOR, countriesCriterion, nominalVoltageCriterion, null, null);
        contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("SHUNT", new ShuntCompensatorContingency("SHUNT")), contingencies.get(0));

        // static var compensator
        countriesCriterion = new SingleCountryCriterion(Collections.singletonList(Country.LU));
        contingencyList = new InjectionCriterionContingencyList("list2",
                IdentifiableType.STATIC_VAR_COMPENSATOR, countriesCriterion, nominalVoltageCriterion, null, null);
        contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("SVC", new StaticVarCompensatorContingency("SVC")), contingencies.get(0));

        // bus bar section
        countriesCriterion = new SingleCountryCriterion(Collections.singletonList(Country.FR));
        contingencyList = new InjectionCriterionContingencyList("list2",
                IdentifiableType.BUSBAR_SECTION, countriesCriterion, nominalVoltageCriterion, null, null);
        contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(3, contingencies.size());
        assertEquals(new Contingency("S1VL2_BBS1", new BusbarSectionContingency("S1VL2_BBS1")), contingencies.get(0));
        assertEquals(new Contingency("S1VL2_BBS2", new BusbarSectionContingency("S1VL2_BBS2")), contingencies.get(1));
        assertEquals(new Contingency("S3VL1_BBS", new BusbarSectionContingency("S3VL1_BBS")), contingencies.get(2));
    }
}
