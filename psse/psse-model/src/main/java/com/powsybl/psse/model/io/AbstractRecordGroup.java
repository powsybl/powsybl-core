/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.io;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static com.powsybl.psse.model.io.FileFormat.JSON;
import static com.powsybl.psse.model.io.FileFormat.LEGACY_TEXT;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public abstract class AbstractRecordGroup<T> {

    protected final RecordGroupIdentification identification;
    private final String[] fieldNames;
    private final Map<PsseVersion.Major, String[]> fieldNamesByVersionMajor = new EnumMap<>(PsseVersion.Major.class);
    private String[] quotedFields;
    private final Map<FileFormat, RecordGroupIO<T>> io = new EnumMap<>(FileFormat.class);

    protected AbstractRecordGroup(RecordGroupIdentification identification, String... fieldNames) {
        this.identification = identification;
        this.fieldNames = fieldNames.length > 0 ? fieldNames : null;
        io.put(LEGACY_TEXT, new RecordGroupIOLegacyText<>(this));
        io.put(JSON, new RecordGroupIOJson<>(this));
    }

    protected void withFieldNames(PsseVersion.Major version, String... fieldNames) {
        fieldNamesByVersionMajor.put(version, fieldNames);
    }

    protected void withIO(FileFormat fileFormat, RecordGroupIO<T> rw) {
        Objects.requireNonNull(fileFormat);
        Objects.requireNonNull(rw);
        io.put(fileFormat, rw);
    }

    protected void withQuotedFields(String... quotedFields) {
        this.quotedFields = quotedFields.length > 0 ? quotedFields : null;
    }

    public RecordGroupIdentification getIdentification() {
        return this.identification;
    }

    /**
     * Some record groups have a fine level of detail on which field names should be saved in the context depending on each record
     * This function will be override for Transformer data:
     * We want to save different field names for transformers with 2 / 3 windings
     *
     * @param object the object to be evaluated for a specific record group identification
     * @return the specific record group identification for the object
     */
    public RecordGroupIdentification getIdentificationFor(T object) {
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
    protected String[][] getFieldNamesByLine(PsseVersion version, String line0) {
        throw new PsseException("Multiline records no supported at this level");
    }

    public String[] quotedFields() {
        return quotedFields;
    }

    public abstract Class<T> psseTypeClass();

    public RecordGroupIO<T> ioFor(FileFormat fileFormat) {
        RecordGroupIO<T> r = io.get(fileFormat);
        if (r == null) {
            throw new PsseException("No reader/writer for file format " + fileFormat);
        }
        return r;
    }

    public List<T> read(BufferedReader reader, Context context) throws IOException {
        return ioFor(context.getFileFormat()).read(reader, context);
    }

    public void write(List<T> psseObjects, Context context, OutputStream outputStream) {
        ioFor(context.getFileFormat()).write(psseObjects, context, outputStream);
    }

    public T readHead(BufferedReader reader, Context context) throws IOException {
        return ioFor(context.getFileFormat()).readHead(reader, context);
    }

    public void writeHead(T psseObject, Context context, OutputStream outputStream) {
        ioFor(context.getFileFormat()).writeHead(psseObject, context, outputStream);
    }

    public T parseSingleRecord(String record, String[] headers, Context context) {
        return parseRecords(Collections.singletonList(record), headers, context).get(0);
    }

    public List<T> parseRecords(List<String> records, String[] headers, Context context) {
        int expectedCount = records.size();
        BeanListProcessor<T> processor = new BeanListProcessor<>(psseTypeClass(), expectedCount);
        CsvParserSettings settings = context.getCsvParserSettings();
        settings.setHeaders(headers);
        settings.setProcessor(processor);
        CsvParser parser = new CsvParser(settings);
        context.resetCurrentRecordGroup();
        for (String record : records) {
            String[] fields = parser.parseLine(record);
            context.setCurrentRecordNumFields(fields.length);
        }
        List<T> beans = processor.getBeans();
        if (beans.size() != expectedCount) {
            throw new PsseException("Parsing error");
        }
        return beans;
    }

    protected List<String> buildRecords(List<T> objects, String[] headers, String[] quoteFields, Context context) {
        return new CsvWriter(settingsForCsvWriter(headers, quoteFields, context)).processRecordsToString(objects);
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
