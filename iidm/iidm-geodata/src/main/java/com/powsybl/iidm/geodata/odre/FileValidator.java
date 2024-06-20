/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.odre;

import com.powsybl.iidm.geodata.utils.InputUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Ahmed Bendaamer {@literal <ahmed.bendaamer at rte-france.com>}
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public final class FileValidator {

    private FileValidator() {
    }

    private static final String HEADERS_OF_FILE_HAS_CHANGED = "Invalid file, Headers of file {} has changed, header(s) not found: {}";
    private static final Logger LOGGER = LoggerFactory.getLogger(FileValidator.class);
    static final String COUNTRY_FR = "FR";
    static final CsvPreference CSV_PREFERENCE = new CsvPreference.Builder('"', ';', System.lineSeparator()).build();
    public static final String SUBSTATIONS = "substations";
    public static final String AERIAL_LINES = "aerial-lines";
    public static final String UNDERGROUND_LINES = "underground-lines";

    public static boolean validateSubstations(Path path, OdreConfig odreConfig) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(path)), StandardCharsets.UTF_8));
             CsvMapReader mapReader = new CsvMapReader(fileReader, CSV_PREFERENCE)) {
            final List<String> headers = List.of(mapReader.getHeader(true));
            if (new HashSet<>(headers).containsAll(odreConfig.substationsExpectedHeaders())) {
                return true;
            } else {
                List<String> notFoundHeaders = odreConfig.substationsExpectedHeaders().stream().filter(isChangedHeaders(headers)).collect(Collectors.toList());
                LOGGER.error(HEADERS_OF_FILE_HAS_CHANGED, path.getFileName(), notFoundHeaders);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
        return false;
    }

    public static Map<String, BufferedReader> validateLines(List<Path> paths, OdreConfig odreConfig) {
        Map<String, BufferedReader> mapResult = new HashMap<>();
        paths.forEach(path -> {
            try (CsvMapReader mapReader = new CsvMapReader(new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(path)), StandardCharsets.UTF_8)), CSV_PREFERENCE)) {
                final String[] headersString = mapReader.getHeader(true);
                final List<String> headers = List.of(headersString);
                Map<String, String> row = mapReader.read(headersString);
                String equipmentType = row.get(odreConfig.equipmentTypeColumn());
                String type = equipmentType != null ? equipmentType : odreConfig.nullEquipmentType();
                if (type.equals(odreConfig.nullEquipmentType())) {
                    getIfSubstationsOrLogError(mapResult, path, headers, equipmentType, odreConfig);
                } else if (type.equals(odreConfig.aerialEquipmentType())) {
                    getResultOrLogError(headers, odreConfig.aerialLinesExpectedHeaders(), mapResult, AERIAL_LINES, path);
                } else if (type.equals(odreConfig.undergroundEquipmentType())) {
                    getResultOrLogError(headers, odreConfig.undergroundLinesExpectedHeaders(), mapResult, UNDERGROUND_LINES, path);
                } else {
                    LOGGER.error("The file {} has no known equipment type : {}", path.getFileName(), equipmentType);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        return mapResult;
    }

    private static void getIfSubstationsOrLogError(Map<String, BufferedReader> mapResult, Path path, List<String> headers, String typeOuvrage, OdreConfig odreConfig) throws IOException {
        if (new HashSet<>(headers).containsAll(odreConfig.substationsExpectedHeaders())) {
            mapResult.putIfAbsent(SUBSTATIONS, new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(path)), StandardCharsets.UTF_8)));
        } else if (isAerialOrUnderground(headers, odreConfig)) {
            LOGGER.error("The file {} has no equipment type : {}", path.getFileName(), typeOuvrage);
        } else {
            List<String> notFoundHeaders = odreConfig.substationsExpectedHeaders().stream().filter(isChangedHeaders(headers)).collect(Collectors.toList());
            LOGGER.error(HEADERS_OF_FILE_HAS_CHANGED, path.getFileName(), notFoundHeaders);
        }
    }

    private static Predicate<String> isChangedHeaders(List<String> headers) {
        return h -> !headers.contains(h);
    }

    private static boolean isAerialOrUnderground(List<String> headers, OdreConfig odreConfig) {
        return new HashSet<>(headers).containsAll(odreConfig.aerialLinesExpectedHeaders()) ||
                new HashSet<>(headers).containsAll(odreConfig.undergroundLinesExpectedHeaders());
    }

    private static void getResultOrLogError(List<String> headers, List<String> expectedHeaders, Map<String, BufferedReader> mapResult, String fileType, Path path) throws IOException {
        if (new HashSet<>(headers).containsAll(expectedHeaders)) {
            mapResult.putIfAbsent(fileType, new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(path)), StandardCharsets.UTF_8)));
        } else {
            List<String> notFoundHeaders = expectedHeaders.stream().filter(isChangedHeaders(headers)).collect(Collectors.toList());
            LOGGER.error(HEADERS_OF_FILE_HAS_CHANGED, path.getFileName(), notFoundHeaders);
        }
    }
}

