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
import java.util.regex.Pattern;

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

    // '' Is allowed as a comment
    public static String readLineAndRemoveComment(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new PsseException("PSSE. Unexpected end of file");
        }
        StringBuffer newLine = new StringBuffer();
        Pattern p = Pattern.compile("('[^']*')|( )+");
        Matcher m = p.matcher(removeComment(line));
        while (m.find()) {
            if (m.group().contains("'")) {
                m.appendReplacement(newLine, m.group());
            } else {
                m.appendReplacement(newLine, " ");
            }
        }
        m.appendTail(newLine);
        return newLine.toString().trim();
    }

    // quote character assumed to be always '
    public static int numFieldsLegacyTextFileFormat(String data, String delimiter) {
        int fields = 0;
        Matcher m = Pattern.compile("([^\']+)|(\'([^\']*)\')").matcher(data);

        while (m.find()) {
            if (m.group().contains("'")) {
                fields++;
            } else {
                for (String field : m.group().split(delimiter)) {
                    if (!field.equals("")) {
                        fields++;
                    }
                }
            }
        }
        return fields;
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
        // XXX(Luma) improve this or consider if it is really required
        String[] intersection = new String[] {};
        for (int i = 0; i < strings1.length; i++) {
            if (ArrayUtils.contains(strings2, strings1[i])) {
                intersection = ArrayUtils.add(intersection, strings1[i]);
            }
        }
        return intersection;
    }

    public static String[] excludeFields(String[] initialFields, String[] excludedFields) {
        String[] fields = new String[] {};
        for (int i = 0; i < initialFields.length; i++) {
            if (!ArrayUtils.contains(excludedFields, initialFields[i])) {
                fields = ArrayUtils.add(fields, initialFields[i]);
            }
        }
        return fields;
    }
}
