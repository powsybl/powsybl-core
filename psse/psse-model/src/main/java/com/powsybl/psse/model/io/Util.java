/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.io;

import com.powsybl.psse.model.PsseException;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static com.powsybl.psse.model.io.FileFormat.LEGACY_TEXT;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public final class Util {

    private Util() {
    }

    public static List<String> readRecords(BufferedReader reader) throws IOException {
        List<String> records = new ArrayList<>();
        String line = readLineAndRemoveComment(reader);
        while (!line.trim().equals("0")) {
            records.add(line);
            line = readLineAndRemoveComment(reader);
        }
        return records;
    }

    static String removeComment(String line) {
        int slashIndex = line.indexOf('/');
        if (slashIndex == -1) {
            return line;
        }
        return line.substring(0, slashIndex);
    }

    public static String readLineAndRemoveComment(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new PsseException("PSSE. Unexpected end of file");
        }
        StringBuffer newLine = new StringBuffer();
        Matcher m = FileFormat.LEGACY_TEXT_QUOTED_OR_WHITESPACE.matcher(removeComment(line));
        char quote = FileFormat.getQuote(LEGACY_TEXT);
        while (m.find()) {
            // If current group is quoted, keep it as it is
            if (m.group().indexOf(quote) >= 0) {
                m.appendReplacement(newLine, m.group());
            } else {
                // current group is whitespace, keep a single whitespace
                m.appendReplacement(newLine, " ");
            }
        }
        m.appendTail(newLine);
        return newLine.toString().trim();
    }

    public static void writeListString(List<String> records, OutputStream outputStream) {
        CsvWriter writer = new CsvWriter(outputStream, new CsvWriterSettings());
        records.forEach(writer::writeRow);
        writer.flush();
    }

    public static void writeString(String line, OutputStream outputStream) {
        CsvWriter writer = new CsvWriter(outputStream, new CsvWriterSettings());
        writer.writeRow(line);
        writer.flush();
    }

    public static String[] intersection(String[] strings1, String[] strings2) {
        return ArrayUtils.removeElements(strings1, ArrayUtils.removeElements(strings1, strings2));
    }
}
