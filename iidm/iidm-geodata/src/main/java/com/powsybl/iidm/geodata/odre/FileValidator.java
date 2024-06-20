/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.odre;

import com.powsybl.iidm.geodata.utils.InputUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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

    private static CSVFormat.Builder createCsvFormatBuilder() {
        return CSVFormat.DEFAULT.builder()
                .setQuote('"')
                .setDelimiter(";")
                .setRecordSeparator(System.lineSeparator());
    }

    static final CSVFormat CSV_FORMAT = createCsvFormatBuilder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();
    static final CSVFormat CSV_FORMAT_FOR_HEADER = createCsvFormatBuilder().build();
    public static final String SUBSTATIONS = "substations";
    public static final String AERIAL_LINES = "aerial-lines";
    public static final String UNDERGROUND_LINES = "underground-lines";
    static final String EQUIPMENT_TYPE = "type_ouvrage";
    static final String NULL_EQUIPMENT_TYPE = "NULL";
    static final String AERIAL_EQUIPMENT_TYPE = "AERIEN";
    static final String UNDERGROUND_EQUIPMENT_TYPE = "SOUTERRAIN";
    public static final String LINE_ID_1 = "code_ligne";
    public static final String LINE_ID_2 = "identification_2";
    public static final String LINE_ID_3 = "identification_3";
    public static final String LINE_ID_4 = "identification_4";
    public static final String LINE_ID_5 = "identification_5";
    public static final String LINE_ID_KEY_1 = "id1";
    public static final String LINE_ID_KEY_2 = "id2";
    public static final String LINE_ID_KEY_3 = "id3";
    public static final String LINE_ID_KEY_4 = "id4";
    public static final String LINE_ID_KEY_5 = "id5";
    static final Map<String, String> IDS_COLUMNS_NAME = Map.of("id1", LINE_ID_1, "id2", LINE_ID_2, "id3", LINE_ID_3, "id4", LINE_ID_4, "id5", LINE_ID_5);
    public static final String GEO_SHAPE = "geo_shape";
    static final String SUBSTATION_ID = "code_poste";
    static final String SUBSTATION_LONGITUDE = "longitude_poste";
    static final String SUBSTATION_LATITUDE = "latitude_poste";
    private static final List<String> SUBSTATIONS_EXPECTED_HEADERS = List.of(SUBSTATION_ID, SUBSTATION_LONGITUDE, SUBSTATION_LATITUDE);
    private static final List<String> AERIAL_LINES_EXPECTED_HEADERS = List.of(LINE_ID_1, LINE_ID_2, LINE_ID_3, LINE_ID_4, LINE_ID_5, GEO_SHAPE);
    private static final List<String> UNDERGROUND_LINES_EXPECTED_HEADERS = List.of(LINE_ID_1, LINE_ID_2, LINE_ID_3, LINE_ID_4, LINE_ID_5, GEO_SHAPE);

    public static boolean validateSubstations(Path path) {
        try (Reader reader = new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(path)), StandardCharsets.UTF_8))) {
            Iterator<CSVRecord> records = CSVParser.parse(reader, FileValidator.CSV_FORMAT_FOR_HEADER).iterator();

            List<String> headers;
            if (records.hasNext()) {
                CSVRecord headersRecord = records.next();
                headers = headersRecord.toList();
            } else {
                LOGGER.error("The file {} is empty", path.getFileName());
                return false;
            }

            if (new HashSet<>(headers).containsAll(SUBSTATIONS_EXPECTED_HEADERS)) {
                return true;
            } else {
                List<String> notFoundHeaders = SUBSTATIONS_EXPECTED_HEADERS.stream().filter(isChangedHeaders(headers)).collect(Collectors.toList());
                logHeaderError(path, notFoundHeaders);
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
            try (Reader reader = new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(path)), StandardCharsets.UTF_8))) {
                Iterator<CSVRecord> records = CSVParser.parse(reader, FileValidator.CSV_FORMAT_FOR_HEADER).iterator();

                final List<String> headers;
                if (records.hasNext()) {
                    CSVRecord headersRecord = records.next();
                    headers = headersRecord.toList();
                } else {
                    headers = null;
                    LOGGER.error("The file {} is empty", path.getFileName());
                }

                String equipmentType = null;
                if (headers != null && records.hasNext()) {
                    CSVRecord firstRow = records.next();
                    equipmentType = readEquipmentType(firstRow, headers);
                } else {
                    LOGGER.error("The file {} has no data", path.getFileName());
                }

                switch ((equipmentType != null) ? equipmentType : NULL_EQUIPMENT_TYPE) {
                    case NULL_EQUIPMENT_TYPE:
                        getIfSubstationsOrLogError(mapResult, path, headers, equipmentType);
                        break;
                    case AERIAL_EQUIPMENT_TYPE:
                        getResultOrLogError(headers, AERIAL_LINES_EXPECTED_HEADERS, mapResult, AERIAL_LINES, path);
                        break;
                    case UNDERGROUND_EQUIPMENT_TYPE:
                        getResultOrLogError(headers, UNDERGROUND_LINES_EXPECTED_HEADERS, mapResult, UNDERGROUND_LINES, path);
                        break;
                    default:
                        LOGGER.error("The file {} has no known equipment type : {}", path.getFileName(), equipmentType);
                }
            } catch (IOException e) {
                mapResult.values().forEach(IOUtils::closeQuietly);
                throw new UncheckedIOException(e);
            }
        });
        return mapResult;
    }

    private static String readEquipmentType(CSVRecord firstRow, List<String> headers) {
        String equipmentType = null;
        int index = headers.indexOf(EQUIPMENT_TYPE);
        if (index != -1) {
            equipmentType = firstRow.get(index);
        }
        return equipmentType;
    }

    private static void getIfSubstationsOrLogError(Map<String, BufferedReader> mapResult, Path path, List<String> headers, String typeOuvrage) throws IOException {
        if (new HashSet<>(headers).containsAll(SUBSTATIONS_EXPECTED_HEADERS)) {
            mapResult.putIfAbsent(SUBSTATIONS, new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(path)), StandardCharsets.UTF_8)));
        } else if (isAerialOrUnderground(headers)) {
            LOGGER.error("The file {} has no equipment type : {}", path.getFileName(), typeOuvrage);
        } else {
            List<String> notFoundHeaders = SUBSTATIONS_EXPECTED_HEADERS.stream().filter(isChangedHeaders(headers)).collect(Collectors.toList());
            logHeaderError(path, notFoundHeaders);
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
            logHeaderError(path, notFoundHeaders);
        }
    }

    private static void logHeaderError(Path path, List<String> notFoundHeaders) {
        LOGGER.error(HEADERS_OF_FILE_HAS_CHANGED, path.getFileName(), notFoundHeaders);
    }
}

