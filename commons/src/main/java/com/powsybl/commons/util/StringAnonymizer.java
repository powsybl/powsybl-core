/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.powsybl.commons.PowsyblException;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRecord;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.LineDelimiter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class StringAnonymizer {

    private static final char DEFAULT_SEPARATOR = ';';

    private final BiMap<String, String> mapping = HashBiMap.create();

    public static String getAlpha(int num) {
        StringBuilder result = new StringBuilder();
        int n = num;
        while (n > 0) {
            n--;
            int remainder = n % 26;
            char digit = (char) (remainder + 'A');
            result.insert(0, digit);
            n = (n - remainder) / 26;
        }
        return result.toString();
    }

    public int getStringCount() {
        return mapping.size();
    }

    public String anonymize(String str) {
        if (str == null) {
            return null;
        }

        return mapping.computeIfAbsent(str, k -> getAlpha(mapping.size() + 1));
    }

    public String deanonymize(String str) {
        if (str == null) {
            return null;
        }
        String str2 = mapping.inverse().get(str);
        if (str2 == null) {
            throw new PowsyblException("Mapping not found for anonymized string '" + str + "'");
        }
        return str2;
    }

    public void readCsv(BufferedReader reader) {
        readCsv(reader, DEFAULT_SEPARATOR);
    }

    public void readCsv(BufferedReader reader, char separator) {
        try (CsvReader<CsvRecord> csvReader = CsvReader.builder()
            .fieldSeparator(separator)
            .quoteCharacter('"')
            .ofCsvRecord(reader)) {
            csvReader.forEach(csvRecord -> {
                if (csvRecord.getFieldCount() != 2) {
                    throw new PowsyblException("Invalid line '" + csvRecord + "'");
                }
                mapping.put(csvRecord.getField(0), csvRecord.getField(1));
            });
        } catch (IOException e) {
            throw new PowsyblException("Failed to read the CSV", e);
        }
    }

    public void writeCsv(BufferedWriter writer) {
        writeCsv(writer, DEFAULT_SEPARATOR);
    }

    public void writeCsv(BufferedWriter writer, char separator) {
        try (CsvWriter csvWriter1 = CsvWriter.builder()
            .fieldSeparator(separator)
            .quoteCharacter('"')
            .lineDelimiter(LineDelimiter.PLATFORM)
            .build(writer)) {
            mapping.forEach(csvWriter1::writeRecord);
        } catch (IOException e) {
            throw new PowsyblException("Failed to write the CSV", e);
        }
    }
}
