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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;

import static com.powsybl.psse.model.io.FileFormat.LEGACY_TEXT;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class RecordGroupIOLegacyText<T> implements RecordGroupIO<T> {
    private static final Logger LOG = LoggerFactory.getLogger(RecordGroupIOLegacyText.class);

    protected final AbstractRecordGroup<T> recordGroup;

    public RecordGroupIOLegacyText(AbstractRecordGroup<T> recordGroup) {
        Objects.requireNonNull(recordGroup);
        this.recordGroup = recordGroup;
    }

    @Override
    public List<T> read(BufferedReader reader, Context context) throws IOException {
        // Record groups in legacy text format have a fixed order for fields
        // Optional fields may not be present at the end of each record.
        // We obtain the maximum number of fields read in each record of the record group.
        // This will be the number of "actual fields" recorded for the record group.
        // We store the "actual" field names in the context for potential later use.
        // For parsing records we use all the field names defined for the record group.

        List<String> records = readRecords(reader);
        return recordGroup.readFromStrings(records, context);
    }

    @Override
    public void write(List<T> psseObjects, Context context, OutputStream outputStream) {
        String[] headers = context.getFieldNames(recordGroup.identification);
        String[] actualQuotedFields = Util.retainAll(recordGroup.quotedFields(), headers);
        writeBegin(outputStream);
        write(psseObjects, headers, actualQuotedFields, context, outputStream);
        writeEnd(outputStream);
    }

    @Override
    public T readHead(BufferedReader reader, Context context) throws IOException {
        throw new PsseException("Generic record group can not be read as head record");
    }

    @Override
    public void writeHead(T psseObject, Context context, OutputStream outputStream) {
        throw new PsseException("Generic record group can not be written as head record");
    }

    protected void write(List<T> objects, String[] headers, String[] quotedFields, Context context, OutputStream outputStream) {
        CsvWriterSettings settings = recordGroup.settingsForCsvWriter(headers, quotedFields, context);
        CsvWriter writer = new CsvWriter(outputStream, settings);
        writer.processRecords(objects);
        writer.flush();
    }

    public static void skip(RecordGroupIdentification recordGroup, BufferedReader reader) throws IOException {
        LOG.debug("read and ignore record group {}", recordGroup);
        int number = -1;
        do {
            String line = reader.readLine();
            if (line == null) {
                throw new PsseException("Unexpected end of file");
            }
            try (Scanner scanner = new Scanner(line)) {
                if (scanner.hasNextInt()) {
                    number = scanner.nextInt();
                }
            }
        }
        while (number != 0);
    }

    public static void writeEmpty(RecordGroupIdentification recordGroup, OutputStream outputStream) {
        write(", ", outputStream);
        writeBegin(recordGroup.getLegacyTextName(), outputStream);
        writeEnd(recordGroup.getLegacyTextName(), outputStream);
    }

    protected void writeBegin(OutputStream outputStream) {
        write(", ", outputStream);
        writeBegin(recordGroup.getIdentification().getLegacyTextName(), outputStream);
    }

    protected void writeEnd(OutputStream outputStream) {
        writeEnd(recordGroup.getIdentification().getLegacyTextName(), outputStream);
    }

    public static void writeBegin(String legacyTextName, OutputStream outputStream) {
        write(String.format("BEGIN %s DATA%n", legacyTextName), outputStream);
    }

    public static void writeEnd(String legacyTextName, OutputStream outputStream) {
        write(String.format("0 / END OF %s DATA", legacyTextName), outputStream);
    }

    public static void writeQ(OutputStream outputStream) {
        write(String.format("%nQ%n"), outputStream);
    }

    public static void write(List<String> ss, OutputStream outputStream) {
        ss.forEach(s -> write(String.format("%s%n", s), outputStream));
    }

    public static void write(String s, OutputStream outputStream) {
        try {
            outputStream.write(s.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected static List<String> readRecords(BufferedReader reader) throws IOException {
        List<String> records = new ArrayList<>();
        String line = readRecordLine(reader);
        while (!line.trim().equals("0")) {
            records.add(line);
            line = readRecordLine(reader);
        }
        return records;
    }

    // Read a line that contains a record
    // Removes comments and normalizes spaces in unquoted areas
    protected static String readRecordLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new PsseException("PSSE. Unexpected end of file");
        }
        StringBuffer newLine = new StringBuffer();
        Matcher m = FileFormat.LEGACY_TEXT_QUOTED_OR_WHITESPACE.matcher(removeComment(line));
        while (m.find()) {
            // If current group is quoted, keep it as it is
            if (m.group().indexOf(LEGACY_TEXT.getQuote()) >= 0) {
                m.appendReplacement(newLine, m.group());
            } else {
                // current group is whitespace, keep a single whitespace
                m.appendReplacement(newLine, " ");
            }
        }
        m.appendTail(newLine);
        return newLine.toString().trim();
    }

    private static String removeComment(String line) {
        int slashIndex = line.indexOf('/');
        if (slashIndex == -1) {
            return line;
        }
        return line.substring(0, slashIndex);
    }
}
