/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.contingency.contingency.list.*;
import com.powsybl.iidm.criteria.*;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
class CriterionContingencyListTest {

    Network fourSubstationNetwork;

    @BeforeEach
    void setup() {
        fourSubstationNetwork = FourSubstationsNodeBreakerFactory.create();
        fourSubstationNetwork.getSubstation("S1").setCountry(Country.FR);
        fourSubstationNetwork.getSubstation("S2").setCountry(Country.BE);
        fourSubstationNetwork.getSubstation("S3").setCountry(Country.FR);
        fourSubstationNetwork.getSubstation("S4").setCountry(Country.LU);

        fourSubstationNetwork.getSubstation("S2").setProperty("property", "valueA");
        fourSubstationNetwork.getSubstation("S3").setProperty("property", "valueB");
        fourSubstationNetwork.getSubstation("S4").setProperty("property", "valueB");

        fourSubstationNetwork.getGenerator("GH1").setProperty("property", "val1");
        fourSubstationNetwork.getGenerator("GH3").setProperty("property", "val4");
        fourSubstationNetwork.getGenerator("GTH1").setProperty("property", "val2");
        fourSubstationNetwork.getGenerator("GTH2").setProperty("property", "val3");

        fourSubstationNetwork.getVoltageLevel("S1VL1").setProperty("property", "value1");
        fourSubstationNetwork.getVoltageLevel("S1VL2").setProperty("property", "value2");
    }

    @Test
    void testCountries() {
        // lines in France
        TwoCountriesCriterion countriesCriterion = new TwoCountriesCriterion(Collections.singletonList(Country.FR));
        LineCriterionContingencyList contingencyList = new LineCriterionContingencyList("list1", countriesCriterion,
                null, Collections.emptyList(), null);
        List<Contingency> contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));
        assertEquals(new Contingency("LINE_S3S4", new LineContingency("LINE_S3S4")), contingencies.get(1));

        // lines between France and Belgium
        countriesCriterion = new TwoCountriesCriterion(Collections.singletonList(Country.FR),
                Collections.singletonList(Country.BE));
        contingencyList = new LineCriterionContingencyList("list1", countriesCriterion,
                null, Collections.emptyList(), null);
        contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));

        // Generator in Belgium
        SingleCountryCriterion countryCriterion = new SingleCountryCriterion(Collections.singletonList(Country.BE));
        InjectionCriterionContingencyList generatorContingencyList = new InjectionCriterionContingencyList("list1",
                "GENERATOR", countryCriterion, null, Collections.emptyList(), null);
        contingencies = generatorContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("GTH1", new GeneratorContingency("GTH1")), contingencies.get(0));

        // All generators
        generatorContingencyList = new InjectionCriterionContingencyList("list1", "GENERATOR",
                null, null, Collections.emptyList(), null);
        contingencies = generatorContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(5, contingencies.size());
        assertEquals(new Contingency("GH1", new GeneratorContingency("GH1")), contingencies.get(0));
        assertEquals(new Contingency("GH2", new GeneratorContingency("GH2")), contingencies.get(1));
        assertEquals(new Contingency("GH3", new GeneratorContingency("GH3")), contingencies.get(2));
        assertEquals(new Contingency("GTH1", new GeneratorContingency("GTH1")), contingencies.get(3));
        assertEquals(new Contingency("GTH2", new GeneratorContingency("GTH2")), contingencies.get(4));

        countryCriterion = new SingleCountryCriterion(Collections.emptyList());
        // All generators
        generatorContingencyList = new InjectionCriterionContingencyList("list1", "GENERATOR",
                countryCriterion, null, Collections.emptyList(), null);
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
                countriesCriterion, null, Collections.emptyList(), null);
        contingencies = hvdcLineCriterionContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("HVDC1", new HvdcLineContingency("HVDC1")), contingencies.get(0));

        // transfo2 in France
        countryCriterion = new SingleCountryCriterion(Collections.singletonList(Country.FR));
        TwoWindingsTransformerCriterionContingencyList twoWindingsTransformerCriterionContingencyList = new TwoWindingsTransformerCriterionContingencyList("list",
                countryCriterion, null, Collections.emptyList(), null);
        contingencies = twoWindingsTransformerCriterionContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("TWT", new TwoWindingsTransformerContingency("TWT")), contingencies.get(0));

        fourSubstationNetwork.getSubstation("S1").setCountry(null);
        countryCriterion = new SingleCountryCriterion(Collections.singletonList(Country.FR));
        twoWindingsTransformerCriterionContingencyList = new TwoWindingsTransformerCriterionContingencyList("list",
                countryCriterion, null, Collections.emptyList(), null);
        contingencies = twoWindingsTransformerCriterionContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(0, contingencies.size());
    }

    public List<Contingency> getContingenciesForHvdcTwoCountriesCriterion(List<Country> countries1, List<Country> countries2) {
        TwoCountriesCriterion countriesCriterion = new TwoCountriesCriterion(countries1, countries2);
        HvdcLineCriterionContingencyList hvdcLineCriterionContingencyList = new HvdcLineCriterionContingencyList("list",
                countriesCriterion, null, Collections.emptyList(), null);
        return hvdcLineCriterionContingencyList.getContingencies(fourSubstationNetwork);
    }

    @Test
    void testTwoCountriesCriterion() {
        List<Contingency> contingencies = getContingenciesForHvdcTwoCountriesCriterion(Collections.singletonList(Country.FR), Collections.singletonList(Country.BE));
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("HVDC1", new HvdcLineContingency("HVDC1")), contingencies.get(0));

        contingencies = getContingenciesForHvdcTwoCountriesCriterion(Collections.singletonList(Country.FR), Collections.emptyList());
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("HVDC1", new HvdcLineContingency("HVDC1")), contingencies.get(0));

        contingencies = getContingenciesForHvdcTwoCountriesCriterion(Collections.emptyList(), Collections.emptyList());
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("HVDC1", new HvdcLineContingency("HVDC1")), contingencies.get(0));

        contingencies = getContingenciesForHvdcTwoCountriesCriterion(Collections.singletonList(Country.FR), Collections.singletonList(Country.CI));
        assertEquals(0, contingencies.size());

        fourSubstationNetwork.getSubstation("S1").setCountry(null);
        contingencies = getContingenciesForHvdcTwoCountriesCriterion(Collections.singletonList(Country.FR), Collections.singletonList(Country.BE));
        assertEquals(0, contingencies.size());

        contingencies = getContingenciesForHvdcTwoCountriesCriterion(Collections.singletonList(Country.FR), Collections.emptyList());
        assertEquals(0, contingencies.size());

        contingencies = getContingenciesForHvdcTwoCountriesCriterion(Collections.emptyList(), Collections.singletonList(Country.BE));
        assertEquals(1, contingencies.size());

        fourSubstationNetwork.getSubstation("S2").setCountry(null);
        contingencies = getContingenciesForHvdcTwoCountriesCriterion(Collections.singletonList(Country.FR), Collections.singletonList(Country.BE));
        assertEquals(0, contingencies.size());
    }

    public List<Contingency> getContingenciesForGeneratorSingleCountryCriterion(SingleCountryCriterion singleCountryCriterion) {
        InjectionCriterionContingencyList generatorContingencyList = new InjectionCriterionContingencyList("list1",
                "GENERATOR", singleCountryCriterion, null, Collections.emptyList(), null);
        return generatorContingencyList.getContingencies(fourSubstationNetwork);
    }

    @Test
    void testSingleCountryCriterionEmptyCountries() {
        List<Contingency> contingencies = getContingenciesForGeneratorSingleCountryCriterion(null);
        assertEquals(5, contingencies.size());

        contingencies = getContingenciesForGeneratorSingleCountryCriterion(new SingleCountryCriterion(Collections.emptyList()));
        assertEquals(5, contingencies.size());

        fourSubstationNetwork.getSubstation("S1").setCountry(null);
        contingencies = getContingenciesForGeneratorSingleCountryCriterion(new SingleCountryCriterion(Collections.emptyList()));
        assertEquals(5, contingencies.size());

        contingencies = getContingenciesForGeneratorSingleCountryCriterion(new SingleCountryCriterion(Collections.singletonList(Country.FR)));
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("GTH2", new GeneratorContingency("GTH2")), contingencies.get(0));
    }

    @Test
    void testNominalVoltage() {

        // load on 225 kV
        SingleNominalVoltageCriterion singleNominalVoltageCriterion = new SingleNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(200.0, 230.0, true, true));
        InjectionCriterionContingencyList contingencyList = new InjectionCriterionContingencyList("list1",
                "LOAD", null, singleNominalVoltageCriterion, Collections.emptyList(),
                null);
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
                twoNominalVoltageCriterion, Collections.emptyList(), null);
        contingencies = transformerContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("TWT", new TwoWindingsTransformerContingency("TWT")), contingencies.get(0));

        // 400 kV lines
        TwoNominalVoltageCriterion twoNominalVoltageCriterion1 = new TwoNominalVoltageCriterion(
                new SingleNominalVoltageCriterion.VoltageInterval(380.0, 420.0,
                        true, true), null);
        LineCriterionContingencyList lineContingencyList = new LineCriterionContingencyList("list1", null,
                twoNominalVoltageCriterion1, Collections.emptyList(), null);
        contingencies = lineContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));
        assertEquals(new Contingency("LINE_S3S4", new LineContingency("LINE_S3S4")), contingencies.get(1));

        // Hvdc
        twoNominalVoltageCriterion = new TwoNominalVoltageCriterion(new SingleNominalVoltageCriterion.VoltageInterval(380.0, 420.0,
                true, true), null);
        HvdcLineCriterionContingencyList hvdcLineCriterionContingencyList = new HvdcLineCriterionContingencyList("list",
                null, twoNominalVoltageCriterion, Collections.emptyList(), null);
        contingencies = hvdcLineCriterionContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("HVDC1", new HvdcLineContingency("HVDC1")), contingencies.get(0));
        assertEquals(new Contingency("HVDC2", new HvdcLineContingency("HVDC2")), contingencies.get(1));
    }

    @Test
    void testProperty() {
        // self
        List<String> values = new ArrayList<>();
        values.add("val1");
        values.add("val4");
        PropertyCriterion propertyCriterion = new PropertyCriterion("property", values,
                PropertyCriterion.EquipmentToCheck.SELF);
        ContingencyList contingencyList = new InjectionCriterionContingencyList("list1", "GENERATOR",
                null, null, Collections.singletonList(propertyCriterion), null);
        List<Contingency> contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("GH1", new GeneratorContingency("GH1")), contingencies.get(0));
        assertEquals(new Contingency("GH3", new GeneratorContingency("GH3")), contingencies.get(1));

        // voltage level
        values = new ArrayList<>();
        values.add("value2");
        propertyCriterion = new PropertyCriterion("property", values, PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL);
        contingencyList = new InjectionCriterionContingencyList("list1", "GENERATOR",
                null, null, Collections.singletonList(propertyCriterion), null);
        contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(3, contingencies.size());
        assertEquals(new Contingency("GH1", new GeneratorContingency("GH1")), contingencies.get(0));
        assertEquals(new Contingency("GH2", new GeneratorContingency("GH2")), contingencies.get(1));
        assertEquals(new Contingency("GH3", new GeneratorContingency("GH3")), contingencies.get(2));

        // substations
        values = new ArrayList<>();
        values.add("valueA");
        propertyCriterion = new PropertyCriterion("property", values, PropertyCriterion.EquipmentToCheck.SUBSTATION);
        contingencyList = new InjectionCriterionContingencyList("list1", "GENERATOR",
                null, null, Collections.singletonList(propertyCriterion), null);
        contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("GTH1", new GeneratorContingency("GTH1")), contingencies.get(0));

        // three-windings transformers
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.getVoltageLevel("VL_132").setProperty("property", "value1");
        network.getVoltageLevel("VL_33").setProperty("property", "value2");
        network.getVoltageLevel("VL_11").setProperty("property", "value3");
        values = List.of("value");
        propertyCriterion = new PropertyCriterion("property", values,
                PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL);
        contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list1", null,
                null, Collections.singletonList(propertyCriterion), null);
        var cl = contingencyList;
        Exception e = assertThrows(IllegalArgumentException.class, () -> cl.getContingencies(network));
        assertTrue(e.getMessage().contains("enum to check side can not be null for threeWindingsTransformer to check their voltage level"));

        assertThreeWindingsTransformerContingencies(false, network, "value0", PropertyCriterion.SideToCheck.ONE);
        assertThreeWindingsTransformerContingencies(true, network, "value1", PropertyCriterion.SideToCheck.ONE);
        assertThreeWindingsTransformerContingencies(true, network, "value2", PropertyCriterion.SideToCheck.ONE);
        assertThreeWindingsTransformerContingencies(true, network, "value3", PropertyCriterion.SideToCheck.ONE);
        assertThreeWindingsTransformerContingencies(false, network, "value1", PropertyCriterion.SideToCheck.BOTH);
        assertThreeWindingsTransformerContingencies(false, network, "value1", PropertyCriterion.SideToCheck.ALL_THREE);

        network.getVoltageLevel("VL_11").setProperty("property", "value2");
        assertThreeWindingsTransformerContingencies(true, network, "value2", PropertyCriterion.SideToCheck.BOTH);
        assertThreeWindingsTransformerContingencies(false, network, "value2", PropertyCriterion.SideToCheck.ALL_THREE);

        network.getVoltageLevel("VL_132").setProperty("property", "value2");
        assertThreeWindingsTransformerContingencies(true, network, "value2", PropertyCriterion.SideToCheck.BOTH);
        assertThreeWindingsTransformerContingencies(true, network, "value2", PropertyCriterion.SideToCheck.ALL_THREE);

    }

    private void assertThreeWindingsTransformerContingencies(boolean successExpected, Network network, String value,
                                                             PropertyCriterion.SideToCheck sideToCheck) {
        PropertyCriterion propertyCriterion = new PropertyCriterion("property", List.of(value),
                PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL, sideToCheck);
        ContingencyList contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list1", null,
                null, Collections.singletonList(propertyCriterion), null);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        if (successExpected) {
            assertEquals(1, contingencies.size());
            assertEquals(new Contingency("3WT", new ThreeWindingsTransformerContingency("3WT")), contingencies.get(0));
        } else {
            assertEquals(0, contingencies.size());
        }
    }

    @Test
    void testRegex() {
        RegexCriterion regexCriterion = new RegexCriterion("3$");
        ContingencyList contingencyList = new LineCriterionContingencyList("list1",
                null, null, Collections.emptyList(), regexCriterion);
        List<Contingency> contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));
    }

    @Test
    void testThreeWindingsTransformer() {
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
                countryCriterion, criterion, Collections.emptyList(), null);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("3WT", new ThreeWindingsTransformerContingency("3WT")), contingencies.get(0));

        // no filter
        criterion = new ThreeNominalVoltageCriterion(null, null, null);
        contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list",
                null, criterion, Collections.emptyList(), null);
        contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("3WT", new ThreeWindingsTransformerContingency("3WT")), contingencies.get(0));

        criterion = new ThreeNominalVoltageCriterion(new SingleNominalVoltageCriterion.VoltageInterval(110.0, 150.0,
                true, true),
                new SingleNominalVoltageCriterion.VoltageInterval(20.0, 40.0,
                        true, true), null);
        contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list",
                null, criterion, Collections.emptyList(), null);
        contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("3WT", new ThreeWindingsTransformerContingency("3WT")), contingencies.get(0));

        criterion = new ThreeNominalVoltageCriterion(new SingleNominalVoltageCriterion.VoltageInterval(110.0, 150.0,
                true, true), null, null);
        contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list",
                null, criterion, Collections.emptyList(), null);
        contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("3WT", new ThreeWindingsTransformerContingency("3WT")), contingencies.get(0));

        criterion = new ThreeNominalVoltageCriterion(new SingleNominalVoltageCriterion.VoltageInterval(1000.0, 1200.0,
                true, true), null, null);
        contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list",
                null, criterion, Collections.emptyList(), null);
        contingencies = contingencyList.getContingencies(network);
        assertEquals(0, contingencies.size());

        // property
        network.getVoltageLevel("VL_33").setProperty("property", "value1");
        network.getVoltageLevel("VL_11").setProperty("property", "value2");
        network.getVoltageLevel("VL_132").setProperty("property", "value1");

        // two sides with property equals to value1
        PropertyCriterion propertyCriterion = new PropertyCriterion("property", Collections.singletonList("value1"),
                PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL, PropertyCriterion.SideToCheck.BOTH);
        contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list",
                null, null, Collections.singletonList(propertyCriterion),
                null);
        contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("3WT", new ThreeWindingsTransformerContingency("3WT")), contingencies.get(0));

        // all three sides with property equals to value1
        propertyCriterion = new PropertyCriterion("property", Collections.singletonList("value1"),
                PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL, PropertyCriterion.SideToCheck.ALL_THREE);
        contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list",
                null, null, Collections.singletonList(propertyCriterion),
                null);
        contingencies = contingencyList.getContingencies(network);
        assertEquals(0, contingencies.size());

        // check all three sides with their property's value
        network.getVoltageLevel("VL_132").setProperty("property", "value3");
        PropertyCriterion propertyCriterion1 = new PropertyCriterion("property", Collections.singletonList("value1"),
                PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL, PropertyCriterion.SideToCheck.ONE);
        PropertyCriterion propertyCriterion2 = new PropertyCriterion("property", Collections.singletonList("value1"),
                PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL, PropertyCriterion.SideToCheck.ONE);
        PropertyCriterion propertyCriterion3 = new PropertyCriterion("property", Collections.singletonList("value1"),
                PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL, PropertyCriterion.SideToCheck.ONE);
        List<PropertyCriterion> propertyCriteria = new ArrayList<>();
        propertyCriteria.add(propertyCriterion1);
        propertyCriteria.add(propertyCriterion2);
        propertyCriteria.add(propertyCriterion3);
        contingencyList = new ThreeWindingsTransformerCriterionContingencyList("list",
                null, null, propertyCriteria,
                null);
        contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("3WT", new ThreeWindingsTransformerContingency("3WT")), contingencies.get(0));
    }

    @Test
    void testBranchesProperties() {
        // transfo
        // one side
        PropertyCriterion transformerPropertyCriterion = new PropertyCriterion("property",
                Collections.singletonList("value1"), PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL,
                PropertyCriterion.SideToCheck.ONE);
        TwoWindingsTransformerCriterionContingencyList twoWindingsTransformerCriterionContingencyList =
                new TwoWindingsTransformerCriterionContingencyList("list",
                        null, null, Collections.singletonList(transformerPropertyCriterion),
                        null);
        List<Contingency> contingencies = twoWindingsTransformerCriterionContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("TWT", new TwoWindingsTransformerContingency("TWT")), contingencies.get(0));

        // both sides
        transformerPropertyCriterion = new PropertyCriterion("property",
                Collections.singletonList("value1"), PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL,
                PropertyCriterion.SideToCheck.BOTH);
        twoWindingsTransformerCriterionContingencyList =
                new TwoWindingsTransformerCriterionContingencyList("list",
                        null, null, Collections.singletonList(transformerPropertyCriterion),
                        null);
        contingencies = twoWindingsTransformerCriterionContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(0, contingencies.size());

        // both sides with their value
        PropertyCriterion transformerPropertyCriterion1 = new PropertyCriterion("property",
                Collections.singletonList("value1"), PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL,
                PropertyCriterion.SideToCheck.ONE);

        PropertyCriterion transformerPropertyCriterion2 = new PropertyCriterion("property",
                Collections.singletonList("value2"), PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL,
                PropertyCriterion.SideToCheck.ONE);

        List<PropertyCriterion> propertyCriteria = new ArrayList<>();
        propertyCriteria.add(transformerPropertyCriterion1);
        propertyCriteria.add(transformerPropertyCriterion2);
        twoWindingsTransformerCriterionContingencyList =
                new TwoWindingsTransformerCriterionContingencyList("list",
                        null, null, propertyCriteria, null);
        contingencies = twoWindingsTransformerCriterionContingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("TWT", new TwoWindingsTransformerContingency("TWT")), contingencies.get(0));

        // lines
        PropertyCriterion linePropertyCriterion = new PropertyCriterion("property",
                Collections.singletonList("valueA"), PropertyCriterion.EquipmentToCheck.SUBSTATION,
                PropertyCriterion.SideToCheck.ONE);
        LineCriterionContingencyList contingencyList = new LineCriterionContingencyList("list1", null,
                null, Collections.singletonList(linePropertyCriterion), null);
        contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));

        // lines
        linePropertyCriterion = new PropertyCriterion("property",
                Collections.singletonList("valueB"), PropertyCriterion.EquipmentToCheck.SUBSTATION,
                PropertyCriterion.SideToCheck.BOTH);
        contingencyList = new LineCriterionContingencyList("list1", null,
                null, Collections.singletonList(linePropertyCriterion), null);
        contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("LINE_S3S4", new LineContingency("LINE_S3S4")), contingencies.get(0));

        // error
        linePropertyCriterion = new PropertyCriterion("property",
                Collections.singletonList("valueB"), PropertyCriterion.EquipmentToCheck.SUBSTATION,
                PropertyCriterion.SideToCheck.ALL_THREE);
        contingencyList = new LineCriterionContingencyList("list1", null,
                null, Collections.singletonList(linePropertyCriterion), null);
        LineCriterionContingencyList finalContingencyList = contingencyList;
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                finalContingencyList.getContingencies(fourSubstationNetwork)
        );
        assertEquals("only ONE or BOTH sides can be checked when filtering properties on branches",
                exception.getMessage());
    }

    @Test
    void testDanglingLines() {
        // dangling lines
        Network network = DanglingLineNetworkFactory.create();
        SingleCountryCriterion countriesCriterion = new SingleCountryCriterion(Collections.singletonList(Country.FR));
        SingleNominalVoltageCriterion nominalVoltageCriterion = new SingleNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(90.0, 130.0, true, false));
        InjectionCriterionContingencyList contingencyList = new InjectionCriterionContingencyList("list2",
                IdentifiableType.DANGLING_LINE, countriesCriterion, nominalVoltageCriterion,
                Collections.emptyList(), null);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("DL", new DanglingLineContingency("DL")), contingencies.get(0));
    }

    @Test
    void testSomeInjections() {
        SingleCountryCriterion countriesCriterion = new SingleCountryCriterion(Collections.singletonList(Country.FR));
        SingleNominalVoltageCriterion nominalVoltageCriterion = new SingleNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(390.0, 440.0, false, false));
        InjectionCriterionContingencyList contingencyList = new InjectionCriterionContingencyList("list2",
                IdentifiableType.SWITCH, countriesCriterion, nominalVoltageCriterion, Collections.emptyList(), null);
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
                IdentifiableType.SHUNT_COMPENSATOR, countriesCriterion, nominalVoltageCriterion, Collections.emptyList(), null);
        contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("SHUNT", new ShuntCompensatorContingency("SHUNT")), contingencies.get(0));

        // static var compensator
        countriesCriterion = new SingleCountryCriterion(Collections.singletonList(Country.LU));
        contingencyList = new InjectionCriterionContingencyList("list2",
                IdentifiableType.STATIC_VAR_COMPENSATOR, countriesCriterion, nominalVoltageCriterion, Collections.emptyList(), null);
        contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("SVC", new StaticVarCompensatorContingency("SVC")), contingencies.get(0));

        // bus bar section
        countriesCriterion = new SingleCountryCriterion(Collections.singletonList(Country.FR));
        contingencyList = new InjectionCriterionContingencyList("list2",
                IdentifiableType.BUSBAR_SECTION, countriesCriterion, nominalVoltageCriterion, Collections.emptyList(), null);
        contingencies = contingencyList.getContingencies(fourSubstationNetwork);
        assertEquals(3, contingencies.size());
        assertEquals(new Contingency("S1VL2_BBS1", new BusbarSectionContingency("S1VL2_BBS1")), contingencies.get(0));
        assertEquals(new Contingency("S1VL2_BBS2", new BusbarSectionContingency("S1VL2_BBS2")), contingencies.get(1));
        assertEquals(new Contingency("S3VL1_BBS", new BusbarSectionContingency("S3VL1_BBS")), contingencies.get(2));
    }
}
