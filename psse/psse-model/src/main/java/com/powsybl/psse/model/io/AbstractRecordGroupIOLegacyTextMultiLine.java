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
    protected AbstractRecordGroupIOLegacyTextMultiLine(AbstractRecordGroup<T> recordGroup) {
        super(recordGroup);
    }

    @Override
    public List<T> read(BufferedReader reader, Context context) throws IOException {
        return readMultiLineRecords(reader, context);
    }

    protected static class MultiLineRecord {
        private final String[][] fieldNamesByLine;
        private final String[] lines;
        private String[] actualFieldNames;

        public MultiLineRecord(String[][] fieldNamesByLine, String[] lines) {
            this.fieldNamesByLine = fieldNamesByLine;
            this.lines = lines;
        }

        public void setActualFieldNames(String[] actualFieldNames) {
            this.actualFieldNames = actualFieldNames;
        }

        public String[][] getFieldNamesByLine() {
            return fieldNamesByLine;
        }

        public String[] getActualFieldNames() {
            return actualFieldNames;
        }

        public String[] getLines() {
            return lines;
        }
    }

    protected abstract MultiLineRecord readMultiLineRecord(List<String> recordsLines, int currentLine, Context context);

    protected T parseMultiLineRecord(MultiLineRecord mlrecord, Context context) {
        // Default parsing builds a single line record adjusting headers and concatenating all the original lines
        mlrecord.actualFieldNames = actualFieldNames(mlrecord.fieldNamesByLine, mlrecord.lines, context);
        String record = String.join(Character.toString(context.getDelimiter()), mlrecord.lines);
        return recordGroup.parseSingleRecord(record, mlrecord.actualFieldNames, context);
    }

    protected List<T> readMultiLineRecords(BufferedReader reader, Context context) throws IOException {
        List<T> objects = new ArrayList<>();
        // Read all records in data section
        List<String> recordsLines = readRecords(reader);
        int i = 0;
        while (i < recordsLines.size()) {
            // Obtain lines for the current record depending on the recordGroup ...
            MultiLineRecord mlrecord = readMultiLineRecord(recordsLines, i, context);
            i += mlrecord.lines.length;

            T object = parseMultiLineRecord(mlrecord, context);
            objects.add(object);

            // Some record groups have a fine level of detail on which fields should be saved depending on each record
            // (We want to save different field names for transformers with 2 / 3 windings)
            RecordGroupIdentification detailedRecordGroupForThisRecord = recordGroup.getIdentificationFor(object);
            context.setFieldNames(detailedRecordGroupForThisRecord, mlrecord.actualFieldNames);
        }
        return objects;
    }

    protected void writeMultiLineRecords(List<T> objects, Context context, OutputStream outputStream) {
        writeBegin(outputStream);
        List<MultiLineRecord> mlrecords = buildMultiLineRecords(objects, context);
        writeMultiLineRecords0(mlrecords, outputStream);
        writeEnd(outputStream);
    }

    protected void writeMultiLineRecords0(List<MultiLineRecord> mlrecords, OutputStream outputStream) {
        CsvWriter writer = new CsvWriter(outputStream, new CsvWriterSettings());
        for (MultiLineRecord mlrecord : mlrecords) {
            for (String line : mlrecord.getLines()) {
                writer.writeRow(line);
            }
        }
        writer.flush();
    }

    protected List<MultiLineRecord> buildMultiLineRecords(List<T> objects, Context context) {
        throw new PsseException("Not implemented");
    }

    protected List<MultiLineRecord> buildMultiLineRecordsFixedLines(List<T> objects, String[][] fieldNamesByLine, String[] contextFieldNames, Context context) {
        int numLines = fieldNamesByLine.length;
        // Entries of the array are the lists of first, second, third lines of all records
        // A complete record k is built using recordsLines[0].get(k), recordsLines[1].get(k) ...
        List<String>[] recordsLines = new ArrayList[numLines];

        for (int l = 0; l < numLines; l++) {
            String[] headersLine = Util.retainAll(fieldNamesByLine[l], contextFieldNames);
            recordsLines[l] = recordGroup.buildRecords(objects, headersLine, Util.retainAll(recordGroup.quotedFields(), headersLine), context);
        }
        checkAllRecordsHaveAllLines(recordsLines);

        // XXX(Luma) build and store in mlrecords at the same time
        int numObjects = objects.size();
        List<MultiLineRecord> mlrecords = new ArrayList<>(numObjects);
        for (int k = 0; k < numObjects; k++) {
            String[] linesk = new String[numLines];
            for (int l = 0; l < numLines; l++) {
                linesk[l] = recordsLines[l].get(k);
            }
            mlrecords.add(new MultiLineRecord(fieldNamesByLine, linesk));
        }
        return mlrecords;
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
