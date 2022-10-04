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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class CriterionContingencyListTest {

    @Test
    public void testCountries() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        network.getSubstation("S1").setCountry(Country.FR);
        network.getSubstation("S2").setCountry(Country.BE);
        network.getSubstation("S3").setCountry(Country.FR);
        network.getSubstation("S4").setCountry(Country.LU);

        // lines in France
        TwoCountriesCriterion countriesCriterion = new TwoCountriesCriterion(Collections.singletonList(Country.FR),
                Collections.emptyList());
        LineCriterionContingencyList contingencyList = new LineCriterionContingencyList("list1", countriesCriterion,
                null, null);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));
        assertEquals(new Contingency("LINE_S3S4", new LineContingency("LINE_S3S4")), contingencies.get(1));

        // lines between France and Belgium
        countriesCriterion = new TwoCountriesCriterion(Collections.singletonList(Country.FR),
                Collections.singletonList(Country.BE));
        contingencyList = new LineCriterionContingencyList("list1", countriesCriterion,
                null, null);
        contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));

        // Generator in Belgium
        SingleCountryCriterion countryCriterion = new SingleCountryCriterion(Collections.singletonList("BE"));
        InjectionCriterionContingencyList generatorContingencyList = new InjectionCriterionContingencyList("list1", "GENERATOR", countryCriterion, null, null);
        contingencies = generatorContingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("GTH1", new GeneratorContingency("GTH1")), contingencies.get(0));

        // All generators
        generatorContingencyList = new InjectionCriterionContingencyList("list1", "GENERATOR", null, null, null);
        contingencies = generatorContingencyList.getContingencies(network);
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
                countriesCriterion, null, null);
        contingencies = hvdcLineCriterionContingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("HVDC1", new HvdcLineContingency("HVDC1")), contingencies.get(0));

        // transfo2 in France
        countryCriterion = new SingleCountryCriterion(Collections.singletonList("FR"));
        TwoWindingsTransformerCriterionContingencyList twoWindingsTransformerCriterionContingencyList = new TwoWindingsTransformerCriterionContingencyList("list",
                countryCriterion, null, null);
        contingencies = twoWindingsTransformerCriterionContingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("TWT", new TwoWindingsTransformerContingency("TWT")), contingencies.get(0));
    }

    @Test
    public void testNominalVoltage() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        // load on 225 kV
        SingleNominalVoltageCriterion singleNominalVoltageCriterion = new SingleNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(200.0, 230.0, true, true));
        InjectionCriterionContingencyList contingencyList = new InjectionCriterionContingencyList("list1",
                "LOAD", null, singleNominalVoltageCriterion, null);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("LD1", new LoadContingency("LD1")), contingencies.get(0));

        //  transformer between 225 kV and 400 kV
        TwoNominalVoltageCriterion twoNominalVoltageCriterion = new TwoNominalVoltageCriterion(
                new SingleNominalVoltageCriterion.VoltageInterval(200.0, 230.0,
                        true, true),
                new SingleNominalVoltageCriterion.VoltageInterval(380.0, 420.0,
                        true, true));
        TwoWindingsTransformerCriterionContingencyList transformerContingencyList = new TwoWindingsTransformerCriterionContingencyList("list1", null,
                twoNominalVoltageCriterion, null);
        contingencies = transformerContingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("TWT", new TwoWindingsTransformerContingency("TWT")), contingencies.get(0));

        // 400 kV lines
        singleNominalVoltageCriterion = new SingleNominalVoltageCriterion(new SingleNominalVoltageCriterion
                .VoltageInterval(380.0, 400.0, true, true));
        LineCriterionContingencyList lineContingencyList = new LineCriterionContingencyList("list1", null,
                singleNominalVoltageCriterion, null);
        contingencies = lineContingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));
        assertEquals(new Contingency("LINE_S3S4", new LineContingency("LINE_S3S4")), contingencies.get(1));
    }

    @Test
    public void testProperty() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        network.getGenerator("GH1").setProperty("property", "val1");
        network.getGenerator("GH3").setProperty("property", "val4");
        network.getGenerator("GTH1").setProperty("property", "val2");
        network.getGenerator("GTH2").setProperty("property", "val3");
        List<String> values = new ArrayList<>();
        values.add("val1");
        values.add("val4");
        PropertyCriterion propertyCriterion = new PropertyCriterion("property", values);
        ContingencyList contingencyList = new InjectionCriterionContingencyList("list1", "GENERATOR",
                null, null, propertyCriterion);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("GH1", new GeneratorContingency("GH1")), contingencies.get(0));
        assertEquals(new Contingency("GH3", new GeneratorContingency("GH3")), contingencies.get(1));
    }
}
