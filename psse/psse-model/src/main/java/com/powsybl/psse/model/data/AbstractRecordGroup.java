/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
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

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private final PsseRecordGroup recordGroup;
    private final String[] fieldNames;
    private final Map<PsseVersion.Major, String[]> fieldNamesByVersionMajor = new HashMap<>();
    private String[] quotedFields;

    AbstractRecordGroup(PsseRecordGroup recordGroup, String... fieldNames) {
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

    public List<T> read(BufferedReader reader, Context context) throws IOException {
        // XXX(Luma) RAW format is implicit here, maybe it should be explicit ?

        // Record groups in RAW format have a fixed order for fields
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

    static void readAndIgnore(PsseRecordGroup recordGroup, BufferedReader reader) throws IOException {
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

    public void write(List<T> psseObjects, Context context, OutputStream outputStream) {
        String[] headers = context.getFieldNames(recordGroup);
        String[] quoteFieldsInside = Util.intersection(quotedFields(), headers);
        writeBegin(outputStream);
        writeBlock(psseTypeClass(), psseObjects, headers, quoteFieldsInside, context.getDelimiter().charAt(0), outputStream);
        writeEnd(outputStream);
    }

    public List<T> readx(BufferedReader reader, Context context) throws IOException {
        // Use Jackson streaming API to skip contents until wanted node is found
        JsonFactory jsonFactory = new JsonFactory();
        try (JsonParser parser = jsonFactory.createParser(reader)) {
            JsonNode node = Util.readx(parser, getRecordGroup().getRawxNodeName());
            String[] actualFieldNames = Util.readFieldNames(node);
            List<String> records = Util.readRecords(node);
            context.setFieldNames(getRecordGroup(), actualFieldNames);
            return parseRecords(records, actualFieldNames, context);
        }
    }

    public List<T> read(JsonNode networkNode, Context context) {
        // XXX(Luma) RAWX format is implicit here, is it ok just because we read from a JsonNode ?

        // Records in RAWX format have arbitrary order for fields.
        // Fields present in the record group are defined explicitly in a header.
        // Order and number of field names is relevant for parsing,
        // the field names must be taken from the explicit header defined in the file.
        // We store the "actual" field names in the context for potential later use.

        JsonNode jsonNode = networkNode.get(recordGroup.getRawxNodeName());
        if (jsonNode == null) {
            return new ArrayList<>();
        }
        String[] actualFieldNames = Util.readFieldNames(jsonNode);
        List<String> records = Util.readRecords(jsonNode);
        context.setFieldNames(recordGroup, actualFieldNames);
        return parseRecords(records, actualFieldNames, context);
    }

    void writex(List<T> psseObjects, Context context, JsonGenerator generator) {
        String[] headers = context.getFieldNames(recordGroup);
        String[] quotedFieldsInside = Util.intersection(quotedFields(), headers);

        List<String> records = writexBlock(psseTypeClass(), psseObjects, headers, quotedFieldsInside, context.getDelimiter().charAt(0));
        writex(headers, records, generator);
    }

    PsseRecordGroup getRecordGroup() {
        return recordGroup;
    }

    T parseSingleRecord(String record, String[] headers, Context context) {
        return parseRecords(Collections.singletonList(record), headers, context).get(0);
    }

    List<T> parseRecords(List<String> records, String[] headers, Context context) {
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

    static <T> void writeBlock(Class<T> aClass, List<T> modelRecords, String[] headers, String[] quoteFields,
        char delimiter, OutputStream outputStream) {

        CsvWriterSettings settings = writeBlockSettings(aClass, headers, quoteFields, delimiter, '\'');
        CsvWriter writer = new CsvWriter(outputStream, settings);
        writer.processRecords(modelRecords);
        writer.flush();
    }

    static <T> List<String> writeBlock(Class<T> aClass, List<T> modelRecords, String[] headers, String[] quoteFields,
        char delimiter) {

        CsvWriterSettings settings = writeBlockSettings(aClass, headers, quoteFields, delimiter, '\'');
        CsvWriter writer = new CsvWriter(settings);
        return writer.processRecordsToString(modelRecords);
    }

    static <T> List<String> writexBlock(Class<T> aClass, List<T> modelRecords, String[] headers, String[] quoteFields,
        char delimiter) {

        CsvWriterSettings settings = writeBlockSettings(aClass, headers, quoteFields, delimiter, '"');
        CsvWriter writer = new CsvWriter(settings);
        return writer.processRecordsToString(modelRecords);
    }

    private static <T> CsvWriterSettings writeBlockSettings(Class<T> aClass, String[] headers,
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

    protected static void writeEmpty(PsseRecordGroup recordGroup, OutputStream outputStream) {
        write(", ", outputStream);
        writeBegin(recordGroup.getRawName(), outputStream);
        writeEnd(recordGroup.getRawName(), outputStream);
    }

    protected void writeBegin(OutputStream outputStream) {
        write(", ", outputStream);
        writeBegin(recordGroup.getRawName(), outputStream);
    }

    protected void writeEnd(OutputStream outputStream) {
        writeEnd(recordGroup.getRawName(), outputStream);
    }

    protected static void writeBegin(String rawName, OutputStream outputStream) {
        write(String.format("BEGIN %s DATA%n", rawName), outputStream);
    }

    protected static void writeEnd(String rawName, OutputStream outputStream) {
        write(String.format("0 / END OF %s DATA", rawName), outputStream);
    }

    protected static void writeQ(OutputStream outputStream) {
        write(String.format("%nQ%n"), outputStream);
    }

    protected static void write(String s, OutputStream outputStream) {
        try {
            outputStream.write(s.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writex(String[] fields, List<String> data, JsonGenerator g) {
        if (fields == null || data == null || fields.length == 0 || data.isEmpty()) {
            return;
        }
        // If we have a default pretty printer we will adjust it to write differently
        // RAWX Parameter Set and Data Tables
        DefaultPrettyPrinter dpp = null;
        PrettyPrinter pp = g.getPrettyPrinter();
        if (pp != null && pp instanceof DefaultPrettyPrinter) {
            dpp = (DefaultPrettyPrinter) pp;
        }
        try {
            g.writeFieldName(recordGroup.rawxNodeName);

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
            // TableData sections write every record in a separate line
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
            throw new PsseException("Writing PSSE in RAWX format ", e);
        }
    }

    public enum PsseRecordGroup {
        CASE_IDENTIFICATION("caseid"),
        SYSTEM_WIDE("?"),
        BUS("bus"),
        LOAD("load"),
        FIXED_BUS_SHUNT("fixshunt", "FIXED SHUNT"),
        GENERATOR("generator"),
        NON_TRANSFORMER_BRANCH("acline", "BRANCH"),
        SYSTEM_SWITCHING_DEVICE("sysswd", "SYSTEM SWITCHING DEVICE"),
        TRANSFORMER("transformer"),
        // Transformer record group includes two and three winding transformers
        TRANSFORMER_2("transformer2"),
        TRANSFORMER_3("transformer3"),
        AREA_INTERCHANGE("area", "AREA"),
        TWO_TERMINAL_DC_TRANSMISSION_LINE("twotermdc", "TWO-TERMINAL DC"),
        VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE("vscdc", "VOLTAGE SOURCE CONVERTER"),
        TRANSFORMER_IMPEDANCE_CORRECTION_TABLES("impcor", "IMPEDANCE CORRECTION"),
        MULTI_TERMINAL_DC_TRANSMISSION_LINE("ntermdc", "MULTI-TERMINAL DC"),
        MULTI_SECTION_LINE_GROUPING("msline", "MULTI-SECTION LINE"),
        ZONE("zone"),
        INTERAREA_TRANSFER("iatransfer", "INTER-AREA TRANSFER"),
        OWNER("owner"),
        FACTS_CONTROL_DEVICE("facts", "FACTS CONTROL DEVICE"),
        SWITCHED_SHUNT("swshunt", "SWITCHED SHUNT"),
        GNE_DEVICE("gne", "GNE DEVICE"),
        INDUCTION_MACHINE("indmach", "INDUCTION MACHINE"),
        SUBSTATION("sub");

        private final String rawxNodeName;
        private final String rawName;

        private PsseRecordGroup(String rawxNodeName) {
            this.rawxNodeName = rawxNodeName;
            this.rawName = name();
        }

        private PsseRecordGroup(String rawxNodeName, String rawName) {
            this.rawxNodeName = rawxNodeName;
            this.rawName = rawName;
        }

        public String getRawxNodeName() {
            return rawxNodeName;
        }

        public String getRawName() {
            return rawName;
        }

        // A RAWX files consist of two types of data objects: RAWX Parameter Sets and RAWX Data Tables.
        // "caseid" is a RAWX Parameter Set
        // "bus" is an RAWX Data Table (which has multiple data rows, one for each bus record)

        public boolean isDataTable() {
            return !isParameterSet();
        }

        public boolean isParameterSet() {
            return rawxNodeName.equals("caseid");
        }
    }
}
