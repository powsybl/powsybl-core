/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.io;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.pf.io.PowerFlowRecordGroup;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
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
import java.util.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public abstract class AbstractRecordGroup<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRecordGroup.class);

    protected final RecordGroupIdentification recordGroup;
    private final String[] fieldNames;
    private final Map<PsseVersion.Major, String[]> fieldNamesByVersionMajor = new HashMap<>();
    private String[] quotedFields;

    public AbstractRecordGroup(PowerFlowRecordGroup recordGroup, String... fieldNames) {
        this.recordGroup = recordGroup;
        this.fieldNames = fieldNames.length > 0 ? fieldNames : null;
    }

    public void withFieldNames(PsseVersion.Major version, String... fieldNames) {
        fieldNamesByVersionMajor.put(version, fieldNames);
    }

    public String[] fieldNames(PsseVersion version) {
        if (fieldNames != null) {
            return fieldNames;
        }
        String[] fieldNames = fieldNamesByVersionMajor.get(version.major());
        if (fieldNames == null) {
            throw new PsseException("Missing fieldNames for version " + version.getMajorNumber() + " in record group " + recordGroup);
        }
        return fieldNames;
    }

    public void withQuotedFields(String... quotedFields) {
        this.quotedFields = quotedFields.length > 0 ? quotedFields : null;
    }

    public String[] quotedFields() {
        return quotedFields;
    }

    public abstract Class<T> psseTypeClass();

    public List<T> readLegacyText(BufferedReader reader, Context context) throws IOException {
        // Record groups in legacy text format have a fixed order for fields
        // Optional fields may not be present at the end of each record.
        // We obtain the maximum number of fields read in each record of the record group.
        // This will be the number of "actual fields" recorded for the record group.
        // We store the "actual" field names in the context for potential later use.
        // For parsing records we use all the field names defined for the record group.

        String[] allFieldNames = fieldNames(context.getVersion());
        List<String> records = Util.readRecords(reader);
        List<T> psseObjects = parseRecords(records, allFieldNames, context);
        String[] actualFieldNames = ArrayUtils.subarray(allFieldNames, 0, context.getCurrentRecordGroupMaxNumFields());
        context.setFieldNames(recordGroup, actualFieldNames);
        return psseObjects;
    }

    public static void readLegacyTextAndIgnore(PowerFlowRecordGroup recordGroup, BufferedReader reader) throws IOException {
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
        while (!(number == 0));
    }

    public void writeLegacyText(List<T> psseObjects, Context context, OutputStream outputStream) {
        String[] headers = context.getFieldNames(recordGroup);
        String[] quoteFieldsInside = Util.intersection(quotedFields(), headers);
        writeBegin(outputStream);
        writeRecords(psseTypeClass(), psseObjects, headers, quoteFieldsInside, context.getDelimiter().charAt(0), outputStream);
        writeEnd(outputStream);
    }

    protected List<T> readJson(BufferedReader reader, Context context) throws IOException {
        // Use Jackson streaming API to skip contents until wanted node is found
        JsonFactory jsonFactory = new JsonFactory();
        try (JsonParser parser = jsonFactory.createParser(reader)) {
            JsonNode node = Util.readJsonNode(parser, recordGroup.getJsonNodeName());
            String[] actualFieldNames = Util.readFieldNames(node);
            List<String> records = Util.readRecords(node);
            context.setFieldNames(recordGroup, actualFieldNames);
            return parseRecords(records, actualFieldNames, context);
        }
    }

    public List<T> readJson(JsonNode networkNode, Context context) {
        // Records in Json file format have arbitrary order for fields.
        // Fields present in the record group are defined explicitly in a header.
        // Order and number of field names is relevant for parsing,
        // the field names must be taken from the explicit header defined in the file.
        // We store the "actual" field names in the context for potential later use.

        JsonNode jsonNode = networkNode.get(recordGroup.getJsonNodeName());
        if (jsonNode == null) {
            return new ArrayList<>();
        }
        String[] actualFieldNames = Util.readFieldNames(jsonNode);
        List<String> records = Util.readRecords(jsonNode);
        context.setFieldNames(recordGroup, actualFieldNames);
        return parseRecords(records, actualFieldNames, context);
    }

    public void writeJson(List<T> psseObjects, Context context, JsonGenerator generator) {
        String[] headers = context.getFieldNames(recordGroup);
        String[] quotedFields = Util.intersection(quotedFields(), headers);

        List<String> records = writeRecordsForJson(psseTypeClass(), psseObjects, headers, quotedFields, context.getDelimiter().charAt(0));
        writeJson(headers, records, generator);
    }

    protected T parseSingleRecord(String record, String[] headers, Context context) {
        return parseRecords(Collections.singletonList(record), headers, context).get(0);
    }

    protected List<T> parseRecords(List<String> records, String[] headers, Context context) {
        int expectedCount = records.size();
        BeanListProcessor<? extends T> processor = new BeanListProcessor<>(psseTypeClass(), expectedCount);
        CsvParserSettings settings = context.getCsvParserSettings();
        settings.setHeaders(headers);
        settings.setProcessor(processor);
        CsvParser parser = new CsvParser(settings);
        context.resetCurrentRecordGroup();
        for (String record : records) {
            String[] fields = parser.parseLine(record);
            context.setCurrentRecordNumFields(fields.length);
        }
        List<? extends T> beans = processor.getBeans();
        if (beans.size() != expectedCount) {
            throw new PsseException("Parsing error");
        }
        return (List<T>) beans;
    }

    protected static <T> void writeRecords(Class<T> aClass, List<T> modelRecords, String[] headers, String[] quoteFields,
                                           char delimiter, OutputStream outputStream) {

        CsvWriterSettings settings = writeRecordsSettings(aClass, headers, quoteFields, delimiter, '\'');
        CsvWriter writer = new CsvWriter(outputStream, settings);
        writer.processRecords(modelRecords);
        writer.flush();
    }

    protected static <T> List<String> writeRecords(Class<T> aClass, List<T> modelRecords, String[] headers, String[] quoteFields,
                                                   char delimiter) {

        CsvWriterSettings settings = writeRecordsSettings(aClass, headers, quoteFields, delimiter, '\'');
        CsvWriter writer = new CsvWriter(settings);
        return writer.processRecordsToString(modelRecords);
    }

    protected static <T> List<String> writeRecordsForJson(Class<T> aClass, List<T> modelRecords, String[] headers, String[] quoteFields,
                                                          char delimiter) {

        CsvWriterSettings settings = writeRecordsSettings(aClass, headers, quoteFields, delimiter, '"');
        CsvWriter writer = new CsvWriter(settings);
        return writer.processRecordsToString(modelRecords);
    }

    private static <T> CsvWriterSettings writeRecordsSettings(Class<T> aClass, String[] headers,
                                                              String[] quoteFields, char delimiter, char quote) {

        BeanWriterProcessor<T> processor = new BeanWriterProcessor<>(aClass);

        CsvWriterSettings settings = new CsvWriterSettings();
        settings.quoteFields(quoteFields);
        settings.setHeaders(headers);
        settings.getFormat().setQuote(quote);
        settings.getFormat().setDelimiter(delimiter);
        settings.setIgnoreLeadingWhitespaces(false);
        settings.setIgnoreTrailingWhitespaces(false);
        settings.setRowWriterProcessor(processor);

        return settings;
    }

    public static void writeEmpty(RecordGroupIdentification recordGroup, OutputStream outputStream) {
        writeLegacyText(", ", outputStream);
        writeBegin(recordGroup.getLegacyTextName(), outputStream);
        writeEnd(recordGroup.getLegacyTextName(), outputStream);
    }

    protected void writeBegin(OutputStream outputStream) {
        writeLegacyText(", ", outputStream);
        writeBegin(recordGroup.getLegacyTextName(), outputStream);
    }

    protected void writeEnd(OutputStream outputStream) {
        writeEnd(recordGroup.getLegacyTextName(), outputStream);
    }

    public static void writeBegin(String legacyTextName, OutputStream outputStream) {
        writeLegacyText(String.format("BEGIN %s DATA%n", legacyTextName), outputStream);
    }

    public static void writeEnd(String legacyTextName, OutputStream outputStream) {
        writeLegacyText(String.format("0 / END OF %s DATA", legacyTextName), outputStream);
    }

    public static void writeQ(OutputStream outputStream) {
        writeLegacyText(String.format("%nQ%n"), outputStream);
    }

    public static void writeLegacyText(String s, OutputStream outputStream) {
        try {
            outputStream.write(s.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeJson(String[] fields, List<String> data, JsonGenerator g) {
        if (fields == null || data == null || fields.length == 0 || data.isEmpty()) {
            return;
        }
        // If we have a default pretty printer we will adjust it to write differently
        // for Parameter Sets and Data Tables
        DefaultPrettyPrinter dpp = null;
        PrettyPrinter pp = g.getPrettyPrinter();
        if (pp != null && pp instanceof DefaultPrettyPrinter) {
            dpp = (DefaultPrettyPrinter) pp;
        }
        try {
            g.writeFieldName(recordGroup.getJsonNodeName());

            // Fields
            g.writeStartObject();
            // List of fields is pretty printed in a single line
            if (dpp != null) {
                dpp.indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance);
            }
            g.writeArrayFieldStart("fields");
            for (int k = 0; k < fields.length; k++) {
                g.writeString(fields[k]);
            }
            g.writeEndArray();

            // Data is pretty printed depending on type of record group
            // Table Data objects write every record in a separate line
            if (dpp != null) {
                dpp.indentArraysWith(recordGroup.isParameterSet()
                    ? DefaultPrettyPrinter.FixedSpaceIndenter.instance
                    : DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            }
            g.writeArrayFieldStart("data");
            for (String s : data) {
                if (recordGroup.isDataTable()) {
                    g.writeStartArray();
                }
                g.writeRaw(" ");
                g.writeRaw(s);
                if (recordGroup.isDataTable()) {
                    g.writeEndArray();
                }
            }
            g.writeEndArray();

            g.writeEndObject();
            g.flush();
        } catch (IOException e) {
            throw new PsseException("Writing PSSE", e);
        }
    }
}
