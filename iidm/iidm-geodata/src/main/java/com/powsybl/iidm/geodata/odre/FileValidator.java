/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.odre;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Ahmed Bendaamer {@literal <ahmed.bendaamer at rte-france.com>}
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public final class FileValidator {

    private FileValidator() {
    }

    private static final String HEADERS_OF_FILE_HAS_CHANGED = "Invalid {} file, header(s) not found: {}";
    private static final Logger LOGGER = LoggerFactory.getLogger(FileValidator.class);
    static final String COUNTRY_FR = "FR";

    static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setQuote('"')
            .setDelimiter(";")
            .setRecordSeparator(System.lineSeparator())
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();

    public static final String SUBSTATIONS = "substations";
    public static final String AERIAL_LINES = "aerial-lines";
    public static final String UNDERGROUND_LINES = "underground-lines";

    public static boolean validateSubstationsHeaders(CSVParser records, OdreConfig odreConfig) {
        return validateHeaders(records, odreConfig.substationsExpectedHeaders(), FileValidator.SUBSTATIONS);
    }

    public static boolean validateAerialLinesHeaders(CSVParser records, OdreConfig odreConfig) {
        return validateHeaders(records, odreConfig.aerialLinesExpectedHeaders(), FileValidator.AERIAL_LINES);
    }

    public static boolean validateUndergroundHeaders(CSVParser records, OdreConfig odreConfig) {
        return validateHeaders(records, odreConfig.undergroundLinesExpectedHeaders(), FileValidator.UNDERGROUND_LINES);
    }

    private static boolean validateHeaders(CSVParser csvParser, List<String> expectedHeaders, String fileType) {
        Map<String, Integer> headerMap = csvParser.getHeaderMap();
        if (headerMap.isEmpty()) {
            LOGGER.error("The substations file is empty");
            return false;
        }

        if (expectedHeaders.stream().allMatch(headerMap::containsKey)) {
            return true;
        } else {
            List<String> notFoundHeaders = expectedHeaders.stream().filter(h -> !headerMap.containsKey(h)).toList();
            LOGGER.error(HEADERS_OF_FILE_HAS_CHANGED, fileType, notFoundHeaders);
            return false;
        }
    }
}

