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
public abstract class AbstractRecordGroupIOLegacyTextMultiLine<T> extends RecordGroupIOLegacyText<T> {
    public AbstractRecordGroupIOLegacyTextMultiLine(AbstractRecordGroup<T> recordGroup) {
        super(recordGroup);
    }

    protected static class MultiLineRecord {
        private final String[][] fieldNamesByLine;
        private final String[] lines;

        public MultiLineRecord(String[][] fieldNamesByLine, String[] lines) {
            this.fieldNamesByLine = fieldNamesByLine;
            this.lines = lines;
        }
    }

    protected abstract MultiLineRecord readMultiLineRecord(List<String> recordsLines, int currentLine, Context context);

    protected List<T> readMultiLineRecords(BufferedReader reader, Context context) throws IOException {
        List<T> objects = new ArrayList<>();
        // Read all records in data section
        List<String> recordsLines = readRecords(reader);
        int i = 0;
        while (i < recordsLines.size()) {
            // Obtain lines for the current record depending on the recordGroup ...
            MultiLineRecord mlrecord = readMultiLineRecord(recordsLines, i, context);
            i += mlrecord.lines.length;

            String[] actualFieldNames = actualFieldNames(mlrecord.fieldNamesByLine, mlrecord.lines, context);
            String record = String.join(Character.toString(context.getDelimiter()), mlrecord.lines);
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
