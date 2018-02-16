/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.powsybl.commons.PowsyblException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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

    private static CsvPreference createPreference(char separator) {
        return new CsvPreference.Builder('"', separator, System.lineSeparator()).build();
    }

    public void readCsv(BufferedReader reader, char separator) {
        CsvListReader csvReader = new CsvListReader(reader, createPreference(separator));
        List<String> nextLine;
        try {
            while ((nextLine = csvReader.read()) != null) {
                if (nextLine.size() != 2) {
                    throw new PowsyblException("Invalid line '" + nextLine + "'");
                }
                mapping.put(nextLine.get(0), nextLine.get(1));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeCsv(BufferedWriter writer) {
        writeCsv(writer, DEFAULT_SEPARATOR);
    }

    public void writeCsv(BufferedWriter writer, char separator) {
        CsvListWriter csvWriter = new CsvListWriter(writer, createPreference(separator));
        String[] nextLine = new String[2];
        try {
            try {
                for (Map.Entry<String, String> e : mapping.entrySet()) {
                    nextLine[0] = e.getKey();
                    nextLine[1] = e.getValue();
                    csvWriter.write(nextLine);
                }
            } finally {
                csvWriter.flush();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
