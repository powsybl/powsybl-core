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

        String[] allFieldNames = recordGroup.fieldNames(context.getVersion());
        List<String> records = readRecords(reader);
        List<T> psseObjects = recordGroup.parseRecords(records, allFieldNames, context);
        String[] actualFieldNames = ArrayUtils.subarray(allFieldNames, 0, context.getCurrentRecordGroupMaxNumFields());
        context.setFieldNames(recordGroup.identification, actualFieldNames);
        return psseObjects;
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

    protected List<T> readMultiLineRecords(BufferedReader reader, Context context) throws IOException {
        List<T> objects = new ArrayList<>();
        // Read all records in data section
        List<String> records = readRecords(reader);
        int i = 0;
        while (i < records.size()) {
            String line0 = records.get(i++);
            String[][] fieldNamesByLine = recordGroup.getFieldNamesByLine(context.getVersion(), line0);
            String[] lines = new String[fieldNamesByLine.length];
            lines[0] = line0;
            for (int k = 1; k < lines.length; k++) {
                lines[k] = records.get(i++);
            }
            String[] actualFieldNames = actualFieldNames(fieldNamesByLine, lines, context);
            String record = String.join(Character.toString(context.getDelimiter()), lines);
            T object = recordGroup.parseSingleRecord(record, actualFieldNames, context);
            objects.add(object);

            // Some record groups have a fine level of detail on which fields should be saved depending on each record
            // (We want to save different field names for transformers with 2 / 3 windings)
            RecordGroupIdentification detailedRecordGroupForThisRecord = recordGroup.getIdentificationFor(object);
            context.setFieldNames(detailedRecordGroupForThisRecord, actualFieldNames);
        }
        return objects;
    }

    protected void writeMultiLineRecords(List<T> objects, String[][] fieldNamesByLine, String[] contextFieldNames, Context context, OutputStream outputStream) {
        int numLines = fieldNamesByLine.length;

        // Entries of the array are the lists of first, second, third lines of all records
        // A complete record k is built using recordsLines[0].get(k), recordsLines[1].get(k) ...
        List<String>[] recordsLines = new ArrayList[numLines];

        for (int l = 0; l < numLines; l++) {
            String[] headersLine = Util.retainAll(fieldNamesByLine[l], contextFieldNames);
            recordsLines[l] = recordGroup.buildRecords(objects, headersLine, Util.retainAll(recordGroup.quotedFields(), headersLine), context);
        }
        checkAllRecordsHaveAllLines(recordsLines);
        // All lines of all records have been built, now write them
        CsvWriter writer = new CsvWriter(outputStream, new CsvWriterSettings());
        int numRecords = recordsLines[0].size();
        for (int k = 0; k < numRecords; k++) {
            for (int l = 0; l < numLines; l++) {
                writer.writeRow(recordsLines[l].get(k));
            }
        }
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

    public static void write(String s, OutputStream outputStream) {
        try {
            outputStream.write(s.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<String> readRecords(BufferedReader reader) throws IOException {
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

    private static String[] actualFieldNames(String[][] fieldNamesByLine, String[] recordLines, Context context) {
        // Obtain the list of actual field names separately for each line of the record
        String[][] actualFieldNames0 = new String[recordLines.length][];
        int totalFieldNames = 0;
        String delimiter = Character.toString(context.getDelimiter());
        for (int k = 0; k < recordLines.length; k++) {
            int numFields = numFields(recordLines[k], delimiter);
            actualFieldNames0[k] = ArrayUtils.subarray(fieldNamesByLine[k], 0, numFields);
            totalFieldNames += numFields;
        }
        // Concat all actual field names in a single array
        String[] actualFieldNames = new String[totalFieldNames];
        int k = 0;
        for (String[] fieldNames : actualFieldNames0) {
            System.arraycopy(fieldNames, 0, actualFieldNames, k, fieldNames.length);
            k += fieldNames.length;
        }
        return actualFieldNames;
    }

    private static int numFields(String record, String delimiter) {
        int fields = 0;
        Matcher m = FileFormat.LEGACY_TEXT_UNQUOTED_OR_QUOTED.matcher(record);
        while (m.find()) {
            if (m.group().indexOf(LEGACY_TEXT.getQuote()) >= 0) {
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

    private static void checkAllRecordsHaveAllLines(List<String>[] recordsLines) {
        int expectedSize = recordsLines[0].size();
        for (int k = 1; k < recordsLines.length; k++) {
            int actualSize = recordsLines[k].size();
            if (expectedSize != actualSize) {
                throw new PsseException(String.format("PSSE multi-line number of records do not match. Line %d; expected number of records %d, actual %d",
                    k,
                    expectedSize,
                    actualSize));
            }
        }
    }
}
