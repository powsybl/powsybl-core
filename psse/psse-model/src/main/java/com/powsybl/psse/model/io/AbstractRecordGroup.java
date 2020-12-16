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
import com.powsybl.commons.PowsyblException;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.powsybl.psse.model.io.RecordGroupIdentification.JsonObjectType.DATA_TABLE;
import static com.powsybl.psse.model.io.RecordGroupIdentification.JsonObjectType.PARAMETER_SET;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public abstract class AbstractRecordGroup<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRecordGroup.class);

    protected final RecordGroupIdentification recordGroup;
    private final String[] fieldNames;
    private final Map<PsseVersion.Major, String[]> fieldNamesByVersionMajor = new EnumMap<>(PsseVersion.Major.class);
    private String[] quotedFields;

    protected AbstractRecordGroup(PowerFlowRecordGroup recordGroup, String... fieldNames) {
        this.recordGroup = recordGroup;
        this.fieldNames = fieldNames.length > 0 ? fieldNames : null;
    }

    protected void withFieldNames(PsseVersion.Major version, String... fieldNames) {
        fieldNamesByVersionMajor.put(version, fieldNames);
    }

    public String[] fieldNames(PsseVersion version) {
        if (fieldNames != null) {
            return fieldNames;
        }
        String[] fieldNamesVersion = fieldNamesByVersionMajor.get(version.major());
        if (fieldNamesVersion == null) {
            throw new PsseException("Missing fieldNames for version " + version.getMajorNumber() + " in record group " + recordGroup);
        }
        return fieldNamesVersion;
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
        while (number != 0);
    }

    public void writeLegacyText(List<T> psseObjects, Context context, OutputStream outputStream) {
        String[] headers = context.getFieldNames(recordGroup);
        String[] quotedFields = Util.intersection(quotedFields(), headers);
        writeBegin(outputStream);
        writeLegacyText(psseTypeClass(), psseObjects, headers, quotedFields, context, outputStream);
        writeEnd(outputStream);
    }

    protected List<T> readJson(BufferedReader reader, Context context) throws IOException {
        // Use Jackson streaming API to skip contents until wanted node is found
        JsonFactory jsonFactory = new JsonFactory();
        try (JsonParser parser = jsonFactory.createParser(reader)) {
            JsonNode node = readJsonNode(parser);
            String[] actualFieldNames = readFieldNames(node);
            List<String> records = readRecords(node);
            context.setFieldNames(recordGroup, actualFieldNames);
            return parseRecords(records, actualFieldNames, context);
        }
    }

    private JsonNode readJsonNode(JsonParser parser) throws IOException {
        Objects.requireNonNull(parser);
        String nodeName = recordGroup.getJsonNodeName();
        Objects.requireNonNull(nodeName);
        while (parser.hasCurrentToken()) {
            if (nodeName.equals(parser.getCurrentName())) {
                return parser.readValueAsTree();
            }
        }
        throw new PsseException("Json node not found: " + nodeName);
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
        String[] actualFieldNames = readFieldNames(jsonNode);
        List<String> records = readRecords(jsonNode);
        context.setFieldNames(recordGroup, actualFieldNames);
        return parseRecords(records, actualFieldNames, context);
    }

    static String[] readFieldNames(JsonNode n) {
        JsonNode fieldsNode = n.get("fields");
        if (!fieldsNode.isArray()) {
            throw new PowsyblException("Expecting array reading fields");
        }
        List<String> fields = new ArrayList<>();
        for (JsonNode f : fieldsNode) {
            fields.add(f.asText());
        }
        return fields.toArray(new String[fields.size()]);
    }

    private List<String> readRecords(JsonNode n) {
        JsonNode dataNode = n.get("data");
        if (!dataNode.isArray()) {
            throw new PowsyblException("Expecting array reading data");
        }
        List<String> records = new ArrayList<>();
        switch (recordGroup.getJsonObjectType()) {
            case PARAMETER_SET:
                records.add(StringUtils.substringBetween(dataNode.toString(), "[", "]"));
                break;
            case DATA_TABLE:
                for (JsonNode r : dataNode) {
                    records.add(StringUtils.substringBetween(r.toString(), "[", "]"));
                }
                break;
            default:
                throw new PsseException("Unsupported Json object type " + recordGroup.getJsonObjectType());
        }
        return records;
    }

    public void writeJson(List<T> psseObjects, Context context, JsonGenerator generator) {
        String[] headers = context.getFieldNames(recordGroup);
        String[] actualQuotedFields = Util.intersection(quotedFields(), headers);
        List<String> records = buildRecords(psseTypeClass(), psseObjects, headers, actualQuotedFields, context);
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

    protected static <T> void writeLegacyText(Class<T> aClass, List<T> objects, String[] headers, String[] quotedFields, Context context, OutputStream outputStream) {
        CsvWriterSettings settings = settingsForCsvWriter(aClass, headers, quotedFields, context);
        CsvWriter writer = new CsvWriter(outputStream, settings);
        writer.processRecords(objects);
        writer.flush();
    }

    protected static <T> List<String> buildRecords(Class<T> aClass, List<T> objects, String[] headers, String[] quoteFields, Context context) {
        return new CsvWriter(settingsForCsvWriter(aClass, headers, quoteFields, context)).processRecordsToString(objects);
    }

    private static <T> CsvWriterSettings settingsForCsvWriter(Class<T> aClass, String[] headers, String[] quotedFields, Context context) {
        BeanWriterProcessor<T> processor = new BeanWriterProcessor<>(aClass);
        CsvWriterSettings settings = new CsvWriterSettings();
        settings.quoteFields(quotedFields);
        settings.setHeaders(headers);
        settings.getFormat().setQuote(FileFormat.getQuote(context.getFileFormat()));
        settings.getFormat().setDelimiter(context.getDelimiter());
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
        if (pp instanceof DefaultPrettyPrinter) {
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
                dpp.indentArraysWith(recordGroup.getJsonObjectType() == PARAMETER_SET
                    ? DefaultPrettyPrinter.FixedSpaceIndenter.instance
                    : DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            }
            g.writeArrayFieldStart("data");
            for (String s : data) {
                if (recordGroup.getJsonObjectType() == DATA_TABLE) {
                    g.writeStartArray();
                }
                g.writeRaw(" ");
                g.writeRaw(s);
                if (recordGroup.getJsonObjectType() == DATA_TABLE) {
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
