/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.psse.model.io.FileFormat.JSON;
import static com.powsybl.psse.model.io.FileFormat.LEGACY_TEXT;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public abstract class AbstractRecordGroup<T> {

    protected final RecordGroupIdentification identification;
    private final String[] fieldNames;
    private final Map<PsseVersion.Major, String[]> fieldNamesByVersionMajor = new EnumMap<>(PsseVersion.Major.class);
    private String[] quotedFields;
    private final Table<FileFormat, PsseVersion.Major, RecordGroupIO<T>> ioFormatVersion = HashBasedTable.create();
    private final Map<FileFormat, RecordGroupIO<T>> ioFormat = new EnumMap<>(FileFormat.class);

    protected AbstractRecordGroup(RecordGroupIdentification identification, String... fieldNames) {
        this.identification = identification;
        this.fieldNames = fieldNames.length > 0 ? fieldNames : null;
        ioFormat.put(LEGACY_TEXT, new RecordGroupIOLegacyText<>(this));
        ioFormat.put(JSON, new RecordGroupIOJson<>(this));
    }

    protected void withFieldNames(PsseVersion.Major version, String... fieldNames) {
        fieldNamesByVersionMajor.put(version, fieldNames);
    }

    protected void withIO(FileFormat fileFormat, RecordGroupIO<T> rw) {
        Objects.requireNonNull(fileFormat);
        Objects.requireNonNull(rw);
        ioFormat.put(fileFormat, rw);
    }

    protected void withIO(FileFormat fileFormat, PsseVersion.Major version, RecordGroupIO<T> io) {
        Objects.requireNonNull(fileFormat);
        Objects.requireNonNull(io);
        this.ioFormatVersion.put(fileFormat, version, io);
    }

    protected void withQuotedFields(String... quotedFields) {
        this.quotedFields = quotedFields;
    }

    public RecordGroupIdentification getIdentification() {
        return this.identification;
    }

    /**
     * Some record groups have a fine level of detail on which field names should be saved in the context depending on each record
     * This function will be overridden for Transformer data:
     * We want to save different field names for transformers with 2 / 3 windings
     *
     * @param object the object to be evaluated for a specific record group identification
     * @return the specific record group identification for the object
     */
    RecordGroupIdentification getIdentificationFor(T object) {
        return this.identification;
    }

    public String[] fieldNames(PsseVersion version) {
        if (fieldNames != null) {
            return fieldNames;
        }
        String[] fieldNamesVersion = fieldNamesByVersionMajor.get(version.major());
        if (fieldNamesVersion == null) {
            throw new PsseException("Missing fieldNames for version " + version.getMajorNumber() + " in record group " + identification);
        }
        return fieldNamesVersion;
    }

    // Record groups with multiline records
    String[][] getFieldNamesByLine(PsseVersion version, String line0) {
        throw new PsseException("Multiline records no supported at this level");
    }

    public String[] quotedFields() {
        return quotedFields;
    }

    protected abstract Class<T> psseTypeClass();

    RecordGroupIO<T> ioFor(FileFormat fileFormat, PsseVersion.Major version) {
        if (ioFormatVersion.contains(fileFormat, version)) {
            return ioFormatVersion.get(fileFormat, version);
        }
        return ioFor(fileFormat);
    }

    RecordGroupIO<T> ioFor(FileFormat fileFormat) {
        RecordGroupIO<T> r = ioFormat.get(fileFormat);
        if (r == null) {
            throw new PsseException("No reader/writer for file format " + fileFormat);
        }
        return r;
    }

    RecordGroupIO<T> ioFor(Context context) {
        if (context.getVersion() == null) {
            return ioFor(context.getFileFormat());
        }
        return ioFor(context.getFileFormat(), context.getVersion().major());
    }

    public List<T> read(LegacyTextReader reader, Context context) throws IOException {
        return ioFor(context).read(reader, context);
    }

    public void write(List<T> psseObjects, Context context, OutputStream outputStream) {
        ioFor(context).write(psseObjects, context, outputStream);
    }

    public T readHead(LegacyTextReader reader, Context context) throws IOException {
        return ioFor(context).readHead(reader, context);
    }

    public void writeHead(T psseObject, Context context, OutputStream outputStream) {
        ioFor(context).writeHead(psseObject, context, outputStream);
    }

    public List<T> readFromStrings(List<String> records, Context context) {
        // fieldNames should not be updated when recordsList is empty
        if (records.isEmpty()) {
            return new ArrayList<>();
        }
        String[] allFieldNames = fieldNames(context.getVersion());
        List<T> psseObjects = parseRecords(records, allFieldNames, context);
        String[] actualFieldNames = ArrayUtils.subarray(allFieldNames, 0, context.getCurrentRecordGroupMaxNumFields());
        context.setFieldNames(identification, actualFieldNames);
        return psseObjects;
    }

    public T parseSingleRecord(String record, String[] headers, Context context) {
        return parseRecords(Collections.singletonList(record), headers, context).get(0);
    }

    List<T> parseRecords(List<String> records, String[] headers, Context context) {
        var processor = new BeanListProcessor<T>(resolveGenericClass(), headers);
        var parser = new LineParser();
        context.resetCurrentRecordGroup();

        for (String record : records) {
            String[] fields = parser.parseLine(record);
            processor.processLine(fields);
        }
        List<T> beans = processor.getBeans();
        return beans;
    }

    private Class<T> resolveGenericClass() {
        java.lang.reflect.Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof java.lang.reflect.ParameterizedType) {
            return (Class<T>) ((java.lang.reflect.ParameterizedType) superclass).getActualTypeArguments()[0];
        }
        throw new IllegalStateException("Generic type T not specified.");
    }

    public String buildRecord(T object, String[] headers, String[] quoteFields, Context context) {
        return unquoteNullString(new CsvWriter(settingsForCsvWriter(headers, quoteFields, context)).processRecordToString(object));
    }

    public List<String> buildRecords(List<T> objects, String[] headers, String[] quoteFields, Context context) {
        return unquoteNullStrings(new CsvWriter(settingsForCsvWriter(headers, quoteFields, context)).processRecordsToString(objects));
    }

    // In rawx it is possible to define null as the value of the field
    // If the field is String when it is exported the result is quoted ("null") as the field is included in the
    // quoted fields. The final strings are processed to eliminate quotes in the null string fields.
    private static List<String> unquoteNullStrings(List<String> stringList) {
        return stringList.stream().map(AbstractRecordGroup::unquoteNullString).collect(Collectors.toList());
    }

    private static String unquoteNullString(String string) {
        return string.replace("\"null\"", "null");
    }

    CsvWriterSettings settingsForCsvWriter(String[] headers, String[] quotedFields, Context context) {
        BeanWriterProcessor<T> processor = new BeanWriterProcessor<>(psseTypeClass());
        CsvWriterSettings settings = new CsvWriterSettings();
        settings.quoteFields(quotedFields);
        settings.setHeaders(headers);
        settings.getFormat().setQuote(context.getFileFormat().getQuote());
        settings.getFormat().setDelimiter(context.getDelimiter());
        settings.setIgnoreLeadingWhitespaces(false);
        settings.setIgnoreTrailingWhitespaces(false);
        settings.setRowWriterProcessor(processor);
        return settings;
    }
}
