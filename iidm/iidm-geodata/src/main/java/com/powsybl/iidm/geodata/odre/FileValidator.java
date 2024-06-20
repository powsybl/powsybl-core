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

    public static boolean validateSubstations(Path path, OdreConfig odreConfig) {
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

            if (new HashSet<>(headers).containsAll(odreConfig.substationsExpectedHeaders())) {
                return true;
            } else {
                List<String> notFoundHeaders = odreConfig.substationsExpectedHeaders().stream().filter(isChangedHeaders(headers)).collect(Collectors.toList());
                logHeaderError(path, notFoundHeaders);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
        return false;
    }

    public static Map<String, Reader> validateLines(List<Path> paths, OdreConfig odreConfig) {
        Map<String, Reader> mapResult = new HashMap<>();
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
                    equipmentType = readEquipmentType(firstRow, headers, odreConfig);
                } else {
                    LOGGER.error("The file {} has no data", path.getFileName());
                }

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
                mapResult.values().forEach(IOUtils::closeQuietly);
                throw new UncheckedIOException(e);
            }
        });
        return mapResult;
    }

    private static String readEquipmentType(CSVRecord firstRow, List<String> headers, OdreConfig odreConfig) {
        String equipmentType = null;
        int index = headers.indexOf(odreConfig.equipmentTypeColumn());
        if (index != -1) {
            equipmentType = firstRow.get(index);
        }
        return equipmentType;
    }

    private static void getIfSubstationsOrLogError(Map<String, Reader> mapResult, Path path, List<String> headers, String typeOuvrage, OdreConfig odreConfig) throws IOException {
        if (new HashSet<>(headers).containsAll(odreConfig.substationsExpectedHeaders())) {
            mapResult.putIfAbsent(SUBSTATIONS, new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(path)), StandardCharsets.UTF_8)));
        } else if (isAerialOrUnderground(headers, odreConfig)) {
            LOGGER.error("The file {} has no equipment type : {}", path.getFileName(), typeOuvrage);
        } else {
            List<String> notFoundHeaders = odreConfig.substationsExpectedHeaders().stream().filter(isChangedHeaders(headers)).collect(Collectors.toList());
            logHeaderError(path, notFoundHeaders);
        }
    }

    private static Predicate<String> isChangedHeaders(List<String> headers) {
        return h -> !headers.contains(h);
    }

    private static boolean isAerialOrUnderground(List<String> headers, OdreConfig odreConfig) {
        return new HashSet<>(headers).containsAll(odreConfig.aerialLinesExpectedHeaders()) ||
                new HashSet<>(headers).containsAll(odreConfig.undergroundLinesExpectedHeaders());
    }

    private static void getResultOrLogError(List<String> headers, List<String> expectedHeaders, Map<String, Reader> mapResult, String fileType, Path path) throws IOException {
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

