/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json.duration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.criteria.duration.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class LimitDurationCriterionModuleTest extends AbstractSerDeTest {

    private static final ObjectMapper MAPPER = JsonUtil.createObjectMapper().registerModule(new LimitDurationCriterionModule());
    private static final ObjectWriter WRITER = MAPPER.writerWithDefaultPrettyPrinter();

    @Test
    void permanentDurationCriterionRoundTripTest() throws IOException {
        PermanentDurationCriterion criterion = new PermanentDurationCriterion();

        List<LimitDurationCriterion> criteria = List.of(criterion);
        roundTripTest(criteria, LimitDurationCriterionModuleTest::writeCriteria,
                LimitDurationCriterionModuleTest::readPermanentDurationCriterion,
                "/criterion/duration/permanent-duration-criteria.json");
    }

    @Test
    void allTemporaryDurationCriterionRoundTripTest() throws IOException {
        AllTemporaryDurationCriterion criterion = new AllTemporaryDurationCriterion();

        List<LimitDurationCriterion> criteria = List.of(criterion);
        roundTripTest(criteria, LimitDurationCriterionModuleTest::writeCriteria,
                LimitDurationCriterionModuleTest::readAllTemporaryDurationCriterion,
                "/criterion/duration/all-temporary-duration-criteria.json");
    }

    @Test
    void equalityTemporaryDurationCriterionRoundTripTest() throws IOException {
        EqualityTemporaryDurationCriterion criterion = new EqualityTemporaryDurationCriterion(20 * 60);

        List<LimitDurationCriterion> criteria = List.of(criterion);
        roundTripTest(criteria, LimitDurationCriterionModuleTest::writeCriteria,
                LimitDurationCriterionModuleTest::readEqualityTemporaryDurationCriterion,
                "/criterion/duration/equality-temporary-duration-criteria.json");
    }

    @Test
    void missingValueAtEqualityTemporaryDurationCriterionReading() {
        String jsonString = """
                {
                  "type" : "TEMPORARY_EQUALITY",
                  "version" : "1.0"
                }
                """;
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> readCriterion(jsonString, EqualityTemporaryDurationCriterion.class));
        Assertions.assertEquals("\"value\" attribute is expected for \"EQUALITY\" temporary limit duration criteria", e.getMessage());
    }

    @Test
    void intervalTemporaryDurationCriterionRoundTripTest() throws IOException {
        IntervalTemporaryDurationCriterion criterion1 = IntervalTemporaryDurationCriterion.builder()
                .setLowBound(60, true)
                .setHighBound(5 * 60, true)
                .build();
        IntervalTemporaryDurationCriterion criterion2 = IntervalTemporaryDurationCriterion.builder()
                .setLowBound(10 * 60, false)
                .build();
        IntervalTemporaryDurationCriterion criterion3 = IntervalTemporaryDurationCriterion.builder()
                .setHighBound(20 * 60, false)
                .build();
        List<LimitDurationCriterion> criteria = List.of(criterion1, criterion2, criterion3);
        roundTripTest(criteria, LimitDurationCriterionModuleTest::writeCriteria,
                LimitDurationCriterionModuleTest::readIntervalTemporaryDurationCriterion,
                "/criterion/duration/interval-temporary-duration-criteria.json");
    }

    @Test
    void missingBoundClosedIndicatorAtIntervalTemporaryDurationCriterionReading() {
        String jsonString = """
                {
                  "type" : "TEMPORARY_INTERVAL",
                  "version" : "1.0",
                  "lowBound" : 600
                }
                """;
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> readCriterion(jsonString, IntervalTemporaryDurationCriterion.class));
        Assertions.assertEquals("Missing \"lowClosed\" attribute for \"INTERVAL\" temporary limit duration criterion with non-null \"lowBound\" attribute.", e.getMessage());
    }

    @Test
    void missingBoundValueAtIntervalTemporaryDurationCriterionReading() {
        String jsonString = """
                {
                  "type" : "TEMPORARY_INTERVAL",
                  "version" : "1.0",
                  "highClosed" : true
                }
                """;
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> readCriterion(jsonString, IntervalTemporaryDurationCriterion.class));
        Assertions.assertEquals("Missing \"highBound\" attribute for \"INTERVAL\" temporary limit duration criterion with non-null \"highClosed\" attribute.", e.getMessage());
    }

    private static List<LimitDurationCriterion> readPermanentDurationCriterion(Path jsonFile) {
        return convert(readCriteria(jsonFile, new TypeReference<List<PermanentDurationCriterion>>() { }));
    }

    private static List<LimitDurationCriterion> readAllTemporaryDurationCriterion(Path jsonFile) {
        return convert(readCriteria(jsonFile, new TypeReference<List<AllTemporaryDurationCriterion>>() { }));
    }

    private static List<LimitDurationCriterion> readEqualityTemporaryDurationCriterion(Path jsonFile) {
        return convert(readCriteria(jsonFile, new TypeReference<List<EqualityTemporaryDurationCriterion>>() { }));
    }

    private static List<LimitDurationCriterion> readIntervalTemporaryDurationCriterion(Path jsonFile) {
        return convert(readCriteria(jsonFile, new TypeReference<List<IntervalTemporaryDurationCriterion>>() { }));
    }

    private static List<LimitDurationCriterion> convert(List<? extends LimitDurationCriterion> list) {
        return List.of(list.toArray(new LimitDurationCriterion[0]));
    }

    private static <T extends LimitDurationCriterion> List<T> readCriteria(Path jsonFile, TypeReference<List<T>> typeReference) {
        Objects.requireNonNull(jsonFile);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return MAPPER.readValue(is, typeReference);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T extends LimitDurationCriterion> T readCriterion(String jsonString, Class<T> clazz) {
        try {
            return MAPPER.readValue(jsonString, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeCriteria(List<LimitDurationCriterion> criteria, Path path) {
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
