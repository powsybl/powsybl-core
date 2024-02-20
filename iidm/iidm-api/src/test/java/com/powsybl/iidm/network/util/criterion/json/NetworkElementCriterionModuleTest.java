/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.util.criterion.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class NetworkElementCriterionModuleTest extends AbstractSerDeTest {

    private static final ObjectMapper MAPPER = JsonUtil.createObjectMapper().registerModule(new NetworkElementCriterionModule());
    private static final ObjectWriter WRITER = MAPPER.writerWithDefaultPrettyPrinter();

    @Test
    void lineCriterionRoundTripTest() throws IOException {
        LineCriterion lineCriterion = new LineCriterion("criterion1")
                .setTwoCountriesCriterion(new TwoCountriesCriterion(List.of(Country.FR, Country.BE)))
                        .setSingleNominalVoltageCriterion(new SingleNominalVoltageCriterion(
                                new SingleNominalVoltageCriterion.VoltageInterval(80., 100., true, true)));
        roundTripTest(lineCriterion, NetworkElementCriterionModuleTest::writeCriterion,
                NetworkElementCriterionModuleTest::readLineCriterion,
                "/criterion/line-criterion.json");
    }

    @Test
    void twoWindingsTransformerCriterionRoundTripTest() throws IOException {
        TwoWindingsTransformerCriterion criterion = new TwoWindingsTransformerCriterion("criterion2")
                .setSingleCountryCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE)))
                .setTwoNominalVoltageCriterion(new TwoNominalVoltageCriterion(
                        new SingleNominalVoltageCriterion.VoltageInterval(80., 100., true, true),
                        new SingleNominalVoltageCriterion.VoltageInterval(380., 420., true, false)));
        roundTripTest(criterion, NetworkElementCriterionModuleTest::writeCriterion,
                NetworkElementCriterionModuleTest::readTwoWindingsTransformerCriterion,
                "/criterion/two-windings-transformer-criterion.json");
    }

    @Test
    void threeWindingsTransformerCriterionRoundTripTest() throws IOException {
        ThreeWindingsTransformerCriterion criterion = new ThreeWindingsTransformerCriterion("criterion3")
                .setSingleCountryCriterion(new SingleCountryCriterion(List.of(Country.BE)))
                .setThreeNominalVoltageCriterion(new ThreeNominalVoltageCriterion(
                        new SingleNominalVoltageCriterion.VoltageInterval(80., 100., true, true),
                        new SingleNominalVoltageCriterion.VoltageInterval(190., 220., false, true),
                        new SingleNominalVoltageCriterion.VoltageInterval(380., 420., true, false)));
        roundTripTest(criterion, NetworkElementCriterionModuleTest::writeCriterion,
                NetworkElementCriterionModuleTest::readThreeWindingsTransformerCriterion,
                "/criterion/three-windings-transformer-criterion.json");
    }

    @Test
    void networkElementIdListCriterionRoundTripTest() throws IOException {
        NetworkElementIdListCriterion criterion = new NetworkElementIdListCriterion("criterion4", Set.of("lineId1", "lineId2"));
        roundTripTest(criterion, NetworkElementCriterionModuleTest::writeCriterion,
                NetworkElementCriterionModuleTest::readNetworkElementIdListCriterion,
                "/criterion/network-element-id-list-criterion.json");
    }

    private static NetworkElementCriterion readLineCriterion(Path jsonFile) {
        return read(jsonFile, LineCriterion.class);
    }

    private static NetworkElementCriterion readTwoWindingsTransformerCriterion(Path jsonFile) {
        return read(jsonFile, TwoWindingsTransformerCriterion.class);
    }

    private static NetworkElementCriterion readThreeWindingsTransformerCriterion(Path jsonFile) {
        return read(jsonFile, ThreeWindingsTransformerCriterion.class);
    }

    private static NetworkElementCriterion readNetworkElementIdListCriterion(Path jsonFile) {
        return read(jsonFile, NetworkElementIdListCriterion.class);
    }

    private static void writeCriterion(NetworkElementCriterion criterion, Path path) {
        write(criterion, path);
    }

    private static <T> T read(Path jsonFile, Class<T> clazz) {
        Objects.requireNonNull(jsonFile);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return (T) MAPPER.readValue(is, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> void write(T object, Path jsonFile) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(jsonFile);
        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            WRITER.writeValue(os, object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
