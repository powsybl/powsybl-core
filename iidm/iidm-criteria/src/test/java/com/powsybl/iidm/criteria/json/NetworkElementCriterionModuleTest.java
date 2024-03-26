/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.criteria.*;
import com.powsybl.iidm.network.Country;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class NetworkElementCriterionModuleTest extends AbstractSerDeTest {

    private static final ObjectMapper MAPPER = JsonUtil.createObjectMapper().registerModule(new NetworkElementCriterionModule());
    private static final ObjectWriter WRITER = MAPPER.writerWithDefaultPrettyPrinter();

    @Test
    void lineCriterionRoundTripTest() throws IOException {
        LineCriterion criterion = new LineCriterion("criterion1", new TwoCountriesCriterion(List.of(Country.FR, Country.BE)),
                        new TwoNominalVoltageCriterion(
                                new VoltageInterval(190., 210., true, true),
                                new VoltageInterval(220., 230., true, true)));
        LineCriterion empty = new LineCriterion(null, null);
        List<NetworkElementCriterion> criteria = List.of(criterion, empty);
        roundTripTest(criteria, NetworkElementCriterionModuleTest::writeCriteria,
                NetworkElementCriterionModuleTest::readLineCriteria,
                "/criterion/line-criteria.json");
    }

    @Test
    void tieLineCriterionRoundTripTest() throws IOException {
        TieLineCriterion criterion = new TieLineCriterion("criterion5", new TwoCountriesCriterion(List.of(Country.FR, Country.DE)),
                new TwoNominalVoltageCriterion(
                        new VoltageInterval(190., 210., true, true),
                        new VoltageInterval(220., 230., true, true)));
        TieLineCriterion empty = new TieLineCriterion(null, null);
        List<NetworkElementCriterion> criteria = List.of(criterion, empty);
        roundTripTest(criteria, NetworkElementCriterionModuleTest::writeCriteria,
                NetworkElementCriterionModuleTest::readTieLineCriteria,
                "/criterion/tie-line-criteria.json");
    }

    @Test
    void danglingLineCriterionRoundTripTest() throws IOException {
        DanglingLineCriterion criterion = new DanglingLineCriterion("criterion6", new SingleCountryCriterion(List.of(Country.FR, Country.DE)),
                new SingleNominalVoltageCriterion(
                        new VoltageInterval(80., 100., true, true)));
        DanglingLineCriterion empty = new DanglingLineCriterion(null, null);
        List<NetworkElementCriterion> criteria = List.of(criterion, empty);
        roundTripTest(criteria, NetworkElementCriterionModuleTest::writeCriteria,
                NetworkElementCriterionModuleTest::readDanglingLineCriteria,
                "/criterion/dangling-line-criteria.json");
    }

    @Test
    void twoWindingsTransformerCriterionRoundTripTest() throws IOException {
        TwoWindingsTransformerCriterion criterion = new TwoWindingsTransformerCriterion("criterion2",
                new SingleCountryCriterion(List.of(Country.FR, Country.BE)),
                new TwoNominalVoltageCriterion(
                        new VoltageInterval(80., 100., true, true),
                        new VoltageInterval(380., 420., true, false)));
        TwoWindingsTransformerCriterion empty = new TwoWindingsTransformerCriterion(null, null);
        List<NetworkElementCriterion> criteria = List.of(criterion, empty);
        roundTripTest(criteria, NetworkElementCriterionModuleTest::writeCriteria,
                NetworkElementCriterionModuleTest::readTwoWindingsTransformerCriteria,
                "/criterion/two-windings-transformer-criteria.json");
    }

    @Test
    void threeWindingsTransformerCriterionRoundTripTest() throws IOException {
        ThreeWindingsTransformerCriterion criterion = new ThreeWindingsTransformerCriterion("criterion3",
                new SingleCountryCriterion(List.of(Country.BE)),
                new ThreeNominalVoltageCriterion(
                        new VoltageInterval(80., 100., true, true),
                        new VoltageInterval(190., 220., false, true),
                        new VoltageInterval(380., 420., true, false)));
        ThreeWindingsTransformerCriterion empty = new ThreeWindingsTransformerCriterion(null, null);
        List<NetworkElementCriterion> criteria = List.of(criterion, empty);
        roundTripTest(criteria, NetworkElementCriterionModuleTest::writeCriteria,
                NetworkElementCriterionModuleTest::readThreeWindingsTransformerCriteria,
                "/criterion/three-windings-transformer-criteria.json");
    }

    @Test
    void identifiableCriterionRoundTripTest() throws IOException {
        IdentifiableCriterion criterion = new IdentifiableCriterion("criterion7", new AtLeastOneCountryCriterion(List.of(Country.FR, Country.DE)),
                new AtLeastOneNominalVoltageCriterion(
                        new VoltageInterval(80., 100., true, true)));
        IdentifiableCriterion small1 = new IdentifiableCriterion(new AtLeastOneCountryCriterion(List.of(Country.BE)));
        IdentifiableCriterion small2 = new IdentifiableCriterion(new AtLeastOneNominalVoltageCriterion(
                new VoltageInterval(80., 100., true, true)));
        List<NetworkElementCriterion> criteria = List.of(criterion, small1, small2);
        roundTripTest(criteria, NetworkElementCriterionModuleTest::writeCriteria,
                NetworkElementCriterionModuleTest::readIdentifiableCriteria,
                "/criterion/identifiable-criteria.json");
    }

    @Test
    void rejectEmptyIdentifiableCriterion() throws IOException {
        String empty = """
                {
                  "type" : "identifiableCriterion",
                  "version" : "1.0"
                }
                """;
        PowsyblException pex = assertThrows(PowsyblException.class, () -> {
            try (InputStream is = new ByteArrayInputStream(empty.getBytes(StandardCharsets.UTF_8))) {
                MAPPER.readValue(is, new TypeReference<IdentifiableCriterion>() {
                });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        assertEquals("Criterion of type 'identifiableCriterion' should have at least one sub-criterion", pex.getMessage());
    }

    @Test
    void networkElementIdListCriterionRoundTripTest() throws IOException {
        NetworkElementIdListCriterion criterion = new NetworkElementIdListCriterion("criterion4", Set.of("lineId1", "lineId2"));
        NetworkElementIdListCriterion empty = new NetworkElementIdListCriterion(Set.of());
        List<NetworkElementCriterion> criteria = List.of(criterion, empty);
        roundTripTest(criteria, NetworkElementCriterionModuleTest::writeCriteria,
                NetworkElementCriterionModuleTest::readNetworkElementIdListCriteria,
                "/criterion/network-element-id-list-criteria.json");
    }

    private static List<NetworkElementCriterion> readLineCriteria(Path jsonFile) {
        return convert(readCriteria(jsonFile, new TypeReference<List<LineCriterion>>() { }));
    }

    private static List<NetworkElementCriterion> readTieLineCriteria(Path jsonFile) {
        return convert(readCriteria(jsonFile, new TypeReference<List<TieLineCriterion>>() { }));
    }

    private static List<NetworkElementCriterion> readDanglingLineCriteria(Path jsonFile) {
        return convert(readCriteria(jsonFile, new TypeReference<List<DanglingLineCriterion>>() { }));
    }

    private static List<NetworkElementCriterion> readTwoWindingsTransformerCriteria(Path jsonFile) {
        return convert(readCriteria(jsonFile, new TypeReference<List<TwoWindingsTransformerCriterion>>() { }));
    }

    private static List<NetworkElementCriterion> readThreeWindingsTransformerCriteria(Path jsonFile) {
        return convert(readCriteria(jsonFile, new TypeReference<List<ThreeWindingsTransformerCriterion>>() { }));
    }

    private static List<NetworkElementCriterion> readIdentifiableCriteria(Path jsonFile) {
        return convert(readCriteria(jsonFile, new TypeReference<List<IdentifiableCriterion>>() { }));
    }

    private static List<NetworkElementCriterion> readNetworkElementIdListCriteria(Path jsonFile) {
        return convert(readCriteria(jsonFile, new TypeReference<List<NetworkElementIdListCriterion>>() { }));
    }

    private static List<NetworkElementCriterion> convert(List<? extends NetworkElementCriterion> list) {
        return List.of(list.toArray(new NetworkElementCriterion[0]));
    }

    private static <T extends NetworkElementCriterion> List<T> readCriteria(Path jsonFile,
                                                              TypeReference<List<T>> typeReference) {
        Objects.requireNonNull(jsonFile);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return MAPPER.readValue(is, typeReference);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeCriteria(List<NetworkElementCriterion> criteria, Path path) {
        write(criteria, path);
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
