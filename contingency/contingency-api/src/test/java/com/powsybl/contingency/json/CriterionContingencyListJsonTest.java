/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.contingency.contingency.list.*;
import com.powsybl.iidm.criteria.*;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.IdentifiableType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
class CriterionContingencyListJsonTest extends AbstractSerDeTest {

    private static HvdcLineCriterionContingencyList createHvdcLineCriterionContingencyList() {
        TwoCountriesCriterion countriesCriterion = new TwoCountriesCriterion(Collections.singletonList(Country.ES),
                Collections.singletonList(Country.FR));
        TwoNominalVoltageCriterion nominalVoltageCriterion = new TwoNominalVoltageCriterion(
                VoltageInterval.between(200.0, 230.0, true, true),
                VoltageInterval.between(380.0, 400.0, true, true));
        PropertyCriterion propertyCriterion = new PropertyCriterion("propertyKey1", Collections.singletonList("value2"), PropertyCriterion.EquipmentToCheck.SELF);
        RegexCriterion regexCriterion = new RegexCriterion("regex");
        return new HvdcLineCriterionContingencyList("list1", countriesCriterion, nominalVoltageCriterion, Collections.singletonList(propertyCriterion), regexCriterion);
    }

    private static LineCriterionContingencyList createLineCriterionContingencyList() {
        TwoCountriesCriterion countriesCriterion = new TwoCountriesCriterion(Collections.singletonList(Country.FR),
                Collections.singletonList(Country.BE));
        TwoNominalVoltageCriterion twoNominalVoltageCriterion = new TwoNominalVoltageCriterion(
                VoltageInterval.between(200.0, 230.0, true, true),
                null);
        PropertyCriterion propertyCriterion = new PropertyCriterion("propertyKey", Collections.singletonList("value"),
                PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL, PropertyCriterion.SideToCheck.BOTH);
        RegexCriterion regexCriterion = new RegexCriterion("regex");
        return new LineCriterionContingencyList("list2", countriesCriterion, twoNominalVoltageCriterion, Collections.singletonList(propertyCriterion), regexCriterion);
    }

    private static TieLineCriterionContingencyList createTieLineCriterionContingencyList() {
        TwoCountriesCriterion countriesCriterion = new TwoCountriesCriterion(Collections.singletonList(Country.IT),
                Collections.singletonList(Country.FR));
        SingleNominalVoltageCriterion nominalVoltageCriterion = new SingleNominalVoltageCriterion(
                VoltageInterval.between(200.0, 230.0, true, true));
        PropertyCriterion propertyCriterion = new PropertyCriterion("propertyKey", Collections.singletonList("value"),
                PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL, PropertyCriterion.SideToCheck.BOTH);
        RegexCriterion regexCriterion = new RegexCriterion("regex");
        return new TieLineCriterionContingencyList("list2a", countriesCriterion, nominalVoltageCriterion, Collections.singletonList(propertyCriterion), regexCriterion);
    }

    private static InjectionCriterionContingencyList createInjectionCriterionContingencyList() {
        SingleCountryCriterion countriesCriterion = new SingleCountryCriterion(Collections.singletonList(Country.FR));
        SingleNominalVoltageCriterion nominalVoltageCriterion = new SingleNominalVoltageCriterion(
                VoltageInterval.between(200.0, 230.0, true, true));
        PropertyCriterion propertyCriterion = new PropertyCriterion("propertyKey", Collections.singletonList("value"),
                PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL);
        RegexCriterion regexCriterion = new RegexCriterion("regex");
        return new InjectionCriterionContingencyList("list2", IdentifiableType.GENERATOR,
                countriesCriterion, nominalVoltageCriterion, Collections.singletonList(propertyCriterion), regexCriterion);
    }

    private static InjectionCriterionContingencyList createInjectionCriterionContingencyListNoCountryCriterion() {
        SingleNominalVoltageCriterion nominalVoltageCriterion = new SingleNominalVoltageCriterion(
                VoltageInterval.between(200.0, 230.0, true, true));
        return new InjectionCriterionContingencyList("list2", IdentifiableType.GENERATOR,
                null, nominalVoltageCriterion, Collections.emptyList(), null);
    }

    private static InjectionCriterionContingencyList createInjectionCriterionContingencyListNoCountryMatch() {
        SingleCountryCriterion countriesCriterion = new SingleCountryCriterion(Collections.singletonList(Country.CI));
        SingleNominalVoltageCriterion nominalVoltageCriterion = new SingleNominalVoltageCriterion(
                VoltageInterval.between(200.0, 230.0, true, true));
        return new InjectionCriterionContingencyList("list2", IdentifiableType.GENERATOR,
                countriesCriterion, nominalVoltageCriterion, Collections.emptyList(), null);
    }

    private static InjectionCriterionContingencyList createInjectionCriterionContingencyListEmptyCountryList() {
        SingleCountryCriterion countriesCriterion = new SingleCountryCriterion(Collections.emptyList());
        return new InjectionCriterionContingencyList("list2", IdentifiableType.LINE,
                countriesCriterion, null, Collections.emptyList(), null);
    }

    private static TwoWindingsTransformerCriterionContingencyList createTwoWindingsTransformerCriterionContingencyList() {
        SingleCountryCriterion countryCriterion = new SingleCountryCriterion(Collections.singletonList(Country.FR));
        TwoNominalVoltageCriterion twoNominalVoltageCriterion = new TwoNominalVoltageCriterion(
                VoltageInterval.between(200.0, 230.0, true, true),
                VoltageInterval.between(380.0, 420.0, true, true));
        PropertyCriterion propertyCriterion = new PropertyCriterion("propertyKey", Collections.singletonList("value"),
                PropertyCriterion.EquipmentToCheck.SUBSTATION, PropertyCriterion.SideToCheck.BOTH);
        RegexCriterion regexCriterion = new RegexCriterion("regex");
        return new TwoWindingsTransformerCriterionContingencyList("list1", countryCriterion,
                twoNominalVoltageCriterion, Collections.singletonList(propertyCriterion), regexCriterion);
    }

    private static ThreeWindingsTransformerCriterionContingencyList createThreeWindingsTransformerCriterionContingencyList() {
        SingleCountryCriterion countryCriterion = new SingleCountryCriterion(Collections.singletonList(Country.FR));
        ThreeNominalVoltageCriterion threeNominalVoltageCriterion = new ThreeNominalVoltageCriterion(
                VoltageInterval.between(200.0, 230.0, true, true),
                VoltageInterval.between(380.0, 420.0, true, true),
                VoltageInterval.between(380.0, 420.0, true, true));
        PropertyCriterion propertyCriterion = new PropertyCriterion("propertyKey", Collections.singletonList("value"),
                PropertyCriterion.EquipmentToCheck.VOLTAGE_LEVEL, PropertyCriterion.SideToCheck.ALL_THREE);
        RegexCriterion regexCriterion = new RegexCriterion("regex");
        return new ThreeWindingsTransformerCriterionContingencyList("list1", countryCriterion,
                threeNominalVoltageCriterion, Collections.singletonList(propertyCriterion), regexCriterion);
    }

    @Test
    void lineCriterionContingencyListRoundTripTest() throws IOException {
        roundTripTest(createLineCriterionContingencyList(), CriterionContingencyListJsonTest::write,
                CriterionContingencyListJsonTest::readLineCriterionContingencyList, "/lineCriterionContingencyList.json");
    }

    @Test
    void tieLineCriterionContingencyListRoundTripTest() throws IOException {
        roundTripTest(createTieLineCriterionContingencyList(), CriterionContingencyListJsonTest::write,
                CriterionContingencyListJsonTest::readTieLineCriterionContingencyList, "/tieLineCriterionContingencyList.json");
    }

    @Test
    void injectionCriterionContingencyListRoundTripTest() throws IOException {
        roundTripTest(createInjectionCriterionContingencyList(), CriterionContingencyListJsonTest::write,
                CriterionContingencyListJsonTest::readInjectionCriterionContingencyList,
                "/injectionCriterionContingencyList.json");
    }

    @Test
    void createInjectionCriterionContingencyListNoCountryTest() throws IOException {
        roundTripTest(createInjectionCriterionContingencyListNoCountryCriterion(), CriterionContingencyListJsonTest::write,
                CriterionContingencyListJsonTest::readInjectionCriterionContingencyList,
                "/injectionCriterionContingencyListNoCountryCriterion.json");
    }

    @Test
    void createInjectionCriterionContingencyListNoCountryMatchTest() throws IOException {
        roundTripTest(createInjectionCriterionContingencyListNoCountryMatch(), CriterionContingencyListJsonTest::write,
                CriterionContingencyListJsonTest::readInjectionCriterionContingencyList,
                "/injectionCriterionContingencyListNoCountryMatch.json");
    }

    @Test
    void createInjectionCriterionContingencyListEmptyCountryListTest() throws IOException {
        roundTripTest(createInjectionCriterionContingencyListEmptyCountryList(), CriterionContingencyListJsonTest::write,
                CriterionContingencyListJsonTest::readInjectionCriterionContingencyList,
                "/injectionCriterionContingencyListEmptyCountryList.json");
    }

    @Test
    void hvdcLineCriterionContingencyListRoundTripTest() throws IOException {
        roundTripTest(createHvdcLineCriterionContingencyList(), CriterionContingencyListJsonTest::write,
                CriterionContingencyListJsonTest::readHvdcLineCriterionContingencyList,
                "/hvdclineCriterionContingencyList.json");
    }

    @Test
    void twoWindingsTransformerCriterionContingencyListRoundTripTest() throws IOException {
        roundTripTest(createTwoWindingsTransformerCriterionContingencyList(), CriterionContingencyListJsonTest::write,
                CriterionContingencyListJsonTest::readTwoWindingsTransformerCriterionContingencyList,
                "/twoWindingsTransformerCriterionContingencyList.json");
    }

    @Test
    void threeWindingsTransformerCriterionContingencyListRoundTripTest() throws IOException {
        roundTripTest(createThreeWindingsTransformerCriterionContingencyList(), CriterionContingencyListJsonTest::write,
                CriterionContingencyListJsonTest::readThreeWindingsTransformerCriterionContingencyList,
                "/threeWindingsTransformerCriterionContingencyList.json");
    }

    private static LineCriterionContingencyList readLineCriterionContingencyList(Path jsonFile) {
        return read(jsonFile, LineCriterionContingencyList.class);
    }

    private static TieLineCriterionContingencyList readTieLineCriterionContingencyList(Path jsonFile) {
        return read(jsonFile, TieLineCriterionContingencyList.class);
    }

    private static HvdcLineCriterionContingencyList readHvdcLineCriterionContingencyList(Path jsonFile) {
        return read(jsonFile, HvdcLineCriterionContingencyList.class);
    }

    private static InjectionCriterionContingencyList readInjectionCriterionContingencyList(Path jsonFile) {
        return read(jsonFile, InjectionCriterionContingencyList.class);
    }

    private static TwoWindingsTransformerCriterionContingencyList readTwoWindingsTransformerCriterionContingencyList(Path jsonFile) {
        return read(jsonFile, TwoWindingsTransformerCriterionContingencyList.class);
    }

    private static ThreeWindingsTransformerCriterionContingencyList readThreeWindingsTransformerCriterionContingencyList(Path jsonFile) {
        return read(jsonFile, ThreeWindingsTransformerCriterionContingencyList.class);
    }

    private static <T> T read(Path jsonFile, Class<T> clazz) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            ObjectMapper objectMapper = JsonUtil.createObjectMapper();
            ContingencyJsonModule module = new ContingencyJsonModule();
            objectMapper.registerModule(module);

            return (T) objectMapper.readValue(is, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> void write(T object, Path jsonFile) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(jsonFile);

        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            ObjectMapper mapper = JsonUtil.createObjectMapper();
            ContingencyJsonModule module = new ContingencyJsonModule();
            mapper.registerModule(module);

            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(os, object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
