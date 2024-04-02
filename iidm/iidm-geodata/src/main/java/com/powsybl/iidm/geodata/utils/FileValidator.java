/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.utils;

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
 * @author bendaamerahm <ahmed.bendaamer at rte-france.com>
 */
public final class FileValidator {

    private FileValidator() {
    }

    private static final String HEADERS_OF_FILE_HAS_CHANGED = "Invalid file, Headers of file {} has changed, header(s) not found: {}";
    private static final Logger LOGGER = LoggerFactory.getLogger(FileValidator.class);
    static final String COUNTRY_FR = "FR";
    static final CsvPreference CSV_PREFERENCE = new CsvPreference.Builder('"', ';', System.lineSeparator()).build();
    static final String TYPE = "text/csv";
    public static final String CODE_LIGNE_1 = "Code ligne 1";
    public static final String CODE_LIGNE_2 = "Code ligne 2";
    public static final String CODE_LIGNE_3 = "Code ligne 3";
    public static final String CODE_LIGNE_4 = "Code ligne 4";
    public static final String CODE_LIGNE_5 = "Code ligne 5";
    public static final String CODE_LIGNE_KEY_1 = "id1";
    public static final String CODE_LIGNE_KEY_2 = "id2";
    public static final String CODE_LIGNE_KEY_3 = "id3";
    public static final String CODE_LIGNE_KEY_4 = "id4";
    public static final String CODE_LIGNE_KEY_5 = "id5";
    static final Map<String, String> IDS_COLUMNS_NAME = Map.of("id1", CODE_LIGNE_1, "id2", CODE_LIGNE_2, "id3", CODE_LIGNE_3, "id4", CODE_LIGNE_4, "id5", CODE_LIGNE_5);
    public static final String GEO_SHAPE = "Geo Shape";
    static final String CODE_POSTE = "Code poste";
    static final String LONGITUDE_POSTE_DD = "Longitude poste (DD)";
    static final String LATITUDE_POSTE_DD = "Latitude poste (DD)";
    private static final List<String> SUBSTATIONS_EXPECTED_HEADERS = List.of(CODE_POSTE, LONGITUDE_POSTE_DD, LATITUDE_POSTE_DD);
    private static final List<String> AERIAL_LINES_EXPECTED_HEADERS = List.of(CODE_LIGNE_1, CODE_LIGNE_2, CODE_LIGNE_3, CODE_LIGNE_4, CODE_LIGNE_5, GEO_SHAPE);
    private static final List<String> UNDERGROUND_LINES_EXPECTED_HEADERS = List.of(CODE_LIGNE_1, CODE_LIGNE_2, CODE_LIGNE_3, CODE_LIGNE_4, CODE_LIGNE_5, GEO_SHAPE);
    private static final String SUBSTATIONS = "postes-electriques";
    private static final String AERIAL_LINES = "lignes-aeriennes";
    private static final String UNDERGROUND_LINES = "lignes-souterraines";

    public static boolean validateSubstations(Path path) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(path)), StandardCharsets.UTF_8));
             CsvMapReader mapReader = new CsvMapReader(fileReader, CSV_PREFERENCE)) {
            final List<String> headers = List.of(mapReader.getHeader(true));
            if (new HashSet<>(headers).containsAll(SUBSTATIONS_EXPECTED_HEADERS)) {
                return true;
            } else {
                List<String> notFoundHeaders = SUBSTATIONS_EXPECTED_HEADERS.stream().filter(isChangedHeaders(headers)).collect(Collectors.toList());
                LOGGER.error(HEADERS_OF_FILE_HAS_CHANGED, path.getFileName(), notFoundHeaders);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
        return false;
    }

    public static Map<String, BufferedReader> validateLines(List<Path> paths) {
        Map<String, BufferedReader> mapResult = new HashMap<>();
        paths.forEach(path -> {
            try (CsvMapReader mapReader = new CsvMapReader(new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(path)), StandardCharsets.UTF_8)), CSV_PREFERENCE)) {
                final String[] headersString = mapReader.getHeader(true);
                final List<String> headers = List.of(headersString);
                Map<String, String> row = mapReader.read(headersString);
                String typeOuvrage = row.get("Type ouvrage");
                switch ((typeOuvrage != null) ? typeOuvrage : "NULL") {
                    case "NULL":
                        getIfSubstationsOrLogError(mapResult, path, headers, typeOuvrage);
                        break;
                    case "AERIEN":
                        getResultOrLogError(headers, AERIAL_LINES_EXPECTED_HEADERS, mapResult, AERIAL_LINES, path);
                        break;
                    case "SOUTERRAIN":
                        getResultOrLogError(headers, UNDERGROUND_LINES_EXPECTED_HEADERS, mapResult, UNDERGROUND_LINES, path);
                        break;
                    default:
                        LOGGER.error("The file {} has no known equipment type : {}", path.getFileName(), typeOuvrage);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        return mapResult;
    }

    private static void getIfSubstationsOrLogError(Map<String, BufferedReader> mapResult, Path path, List<String> headers, String typeOuvrage) throws IOException {
        if (new HashSet<>(headers).containsAll(SUBSTATIONS_EXPECTED_HEADERS)) {
            mapResult.putIfAbsent(SUBSTATIONS, new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(path)), StandardCharsets.UTF_8)));
        } else if (isAerialOrUnderground(headers)) {
            LOGGER.error("The file {} has no equipment type : {}", path.getFileName(), typeOuvrage);
        } else {
            List<String> notFoundHeaders = SUBSTATIONS_EXPECTED_HEADERS.stream().filter(isChangedHeaders(headers)).collect(Collectors.toList());
            LOGGER.error(HEADERS_OF_FILE_HAS_CHANGED, path.getFileName(), notFoundHeaders);
        }
    }

    private static Predicate<String> isChangedHeaders(List<String> headers) {
        return h -> !headers.contains(h);
    }

    private static boolean isAerialOrUnderground(List<String> headers) {
        return new HashSet<>(headers).containsAll(AERIAL_LINES_EXPECTED_HEADERS) ||
                new HashSet<>(headers).containsAll(UNDERGROUND_LINES_EXPECTED_HEADERS);
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

