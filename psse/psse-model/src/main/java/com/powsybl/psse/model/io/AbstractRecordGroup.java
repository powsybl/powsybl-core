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
import com.powsybl.psse.model.pf.*;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.QuoteStrategy;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.powsybl.psse.model.io.FileFormat.JSON;
import static com.powsybl.psse.model.io.FileFormat.LEGACY_TEXT;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public abstract class AbstractRecordGroup<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRecordGroup.class);
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

    private static final Map<Class<?>, MapperFrom<?>> MAPPERS_FROM_RECORD = getMappersFromRecord();

    private static final Map<Class<?>, MapperTo<?>> MAPPERS_TO_RECORD = getMappersToRecord();

    private static Map<Class<?>, MapperFrom<?>> getMappersFromRecord() {
        Map<Class<?>, MapperFrom<?>> mappers = new HashMap<>();
        mappers.put(PsseArea.class, (MapperFrom<PsseArea>) PsseArea::fromRecord);
        mappers.put(PsseBus.class, (MapperFrom<PsseBus>) PsseBus::fromRecord);
        mappers.put(PsseCaseIdentification.class, (MapperFrom<PsseCaseIdentification>) PsseCaseIdentification::fromRecord);
        mappers.put(PsseFacts.class, (MapperFrom<PsseFacts>) PsseFacts::fromRecord);
        mappers.put(PsseFixedShunt.class, (MapperFrom<PsseFixedShunt>) PsseFixedShunt::fromRecord);
        mappers.put(PsseGenerator.class, (MapperFrom<PsseGenerator>) PsseGenerator::fromRecord);
        mappers.put(PsseOwnership.class, (MapperFrom<PsseOwnership>) PsseOwnership::fromRecord);
        mappers.put(PsseGneDevice.class, (MapperFrom<PsseGneDevice>) PsseGneDevice::fromRecord);
        mappers.put(PsseInductionMachine.class, (MapperFrom<PsseInductionMachine>) PsseInductionMachine::fromRecord);
        mappers.put(PsseInterareaTransfer.class, (MapperFrom<PsseInterareaTransfer>) PsseInterareaTransfer::fromRecord);
        mappers.put(PsseLineGrouping.class, (MapperFrom<PsseLineGrouping>) PsseLineGrouping::fromRecord);
        mappers.put(PsseLoad.class, (MapperFrom<PsseLoad>) PsseLoad::fromRecord);
        mappers.put(PsseNonTransformerBranch.class, (MapperFrom<PsseNonTransformerBranch>) PsseNonTransformerBranch::fromRecord);
        mappers.put(PsseOwner.class, (MapperFrom<PsseOwner>) PsseOwner::fromRecord);
        mappers.put(PsseRates.class, (MapperFrom<PsseRates>) PsseRates::fromRecord);
        mappers.put(PsseTransformer.class, (MapperFrom<PsseTransformer>) PsseTransformer::fromRecord);
//        mappers.put(PsseTransformerImpedanceCorrectionPoint.class, (MapperFrom<PsseTransformerImpedanceCorrectionPoint>) PsseTransformerImpedanceCorrectionPoint::fromRecord);
        mappers.put(PsseTransformerWinding.class, (MapperFrom<PsseTransformerWinding>) PsseTransformerWinding::fromRecord);
        mappers.put(PsseTwoTerminalDcConverter.class, (MapperFrom<PsseTwoTerminalDcConverter>) PsseTwoTerminalDcConverter::fromRecord);
        mappers.put(PsseTwoTerminalDcTransmissionLine.class, (MapperFrom<PsseTwoTerminalDcTransmissionLine>) PsseTwoTerminalDcTransmissionLine::fromRecord);
        mappers.put(PsseVoltageSourceConverter.class, (MapperFrom<PsseVoltageSourceConverter>) PsseVoltageSourceConverter::fromRecord);
        mappers.put(PsseVoltageSourceConverterDcTransmissionLine.class, (MapperFrom<PsseVoltageSourceConverterDcTransmissionLine>) PsseVoltageSourceConverterDcTransmissionLine::fromRecord);
        mappers.put(PsseZone.class, (MapperFrom<PsseZone>) PsseZone::fromRecord);
        return mappers;
    }

    private static Map<Class<?>, MapperTo<?>> getMappersToRecord() {
        Map<Class<?>, MapperTo<?>> mappers = new HashMap<>();
        mappers.put(PsseArea.class, (MapperTo<PsseArea>) PsseArea::toRecord);
        mappers.put(PsseBus.class, (MapperTo<PsseBus>) PsseBus::toRecord);
        mappers.put(PsseCaseIdentification.class, (MapperTo<PsseCaseIdentification>) PsseCaseIdentification::toRecord);
        mappers.put(PsseFacts.class, (MapperTo<PsseFacts>) PsseFacts::toRecord);
        mappers.put(PsseFixedShunt.class, (MapperTo<PsseFixedShunt>) PsseFixedShunt::toRecord);
        mappers.put(PsseGenerator.class, (MapperTo<PsseGenerator>) PsseGenerator::toRecord);
        mappers.put(PsseOwnership.class, (MapperTo<PsseOwnership>) PsseOwnership::toRecord);
        mappers.put(PsseGneDevice.class, (MapperTo<PsseGneDevice>) PsseGneDevice::toRecord);
        mappers.put(PsseInductionMachine.class, (MapperTo<PsseInductionMachine>) PsseInductionMachine::toRecord);
        mappers.put(PsseInterareaTransfer.class, (MapperTo<PsseInterareaTransfer>) PsseInterareaTransfer::toRecord);
        mappers.put(PsseLineGrouping.class, (MapperTo<PsseLineGrouping>) PsseLineGrouping::toRecord);
        mappers.put(PsseLoad.class, (MapperTo<PsseLoad>) PsseLoad::toRecord);
        mappers.put(PsseNonTransformerBranch.class, (MapperTo<PsseNonTransformerBranch>) PsseNonTransformerBranch::toRecord);
        mappers.put(PsseOwner.class, (MapperTo<PsseOwner>) PsseOwner::toRecord);
        mappers.put(PsseRates.class, (MapperTo<PsseRates>) PsseRates::toRecord);
        mappers.put(PsseTransformer.class, (MapperTo<PsseTransformer>) PsseTransformer::toRecord);
//        mappers.put(PsseTransformerImpedanceCorrectionPoint.class, (MapperTo<PsseTransformerImpedanceCorrectionPoint>) PsseTransformerImpedanceCorrectionPoint::toRecord);
        mappers.put(PsseTransformerWinding.class, (MapperTo<PsseTransformerWinding>) PsseTransformerWinding::toRecord);
        mappers.put(PsseTwoTerminalDcConverter.class, (MapperTo<PsseTwoTerminalDcConverter>) PsseTwoTerminalDcConverter::toRecord);
        mappers.put(PsseTwoTerminalDcTransmissionLine.class, (MapperTo<PsseTwoTerminalDcTransmissionLine>) PsseTwoTerminalDcTransmissionLine::toRecord);
        mappers.put(PsseVoltageSourceConverter.class, (MapperTo<PsseVoltageSourceConverter>) PsseVoltageSourceConverter::toRecord);
        mappers.put(PsseVoltageSourceConverterDcTransmissionLine.class, (MapperTo<PsseVoltageSourceConverterDcTransmissionLine>) PsseVoltageSourceConverterDcTransmissionLine::toRecord);
        mappers.put(PsseZone.class, (MapperTo<PsseZone>) PsseZone::toRecord);
        return mappers;
    }

    private interface MapperFrom<T> {
        T apply(NamedCsvRecord rec, PsseVersion version);
    }

    private interface MapperTo<T> {
        String[] apply(T t, String[] headers);
    }

    // Casting is safe here
    @SuppressWarnings("unchecked")
    List<T> parseRecords(List<String> records, String[] headers, Context context) {
        int expectedCount = records.size();
        if (expectedCount == 0) {
            return List.of();
        }

        // Reconstruct a string block
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(String.valueOf(context.getDelimiter()), headers)).append(System.lineSeparator());
        for (String record : records) {
            sb.append(record).append(System.lineSeparator());
        }

        // Initialize the output
        List<T> beans = new ArrayList<>(expectedCount);
        context.resetCurrentRecordGroup();

        // Parse
        try (CsvReader<NamedCsvRecord> reader = context.createCsvReaderBuilder()
            .ofNamedCsvRecord(new StringReader(sb.toString()))) {
            // Get the corresponding mapper
            MapperFrom<T> mapper = (MapperFrom<T>) MAPPERS_FROM_RECORD.get(psseTypeClass());
            if (mapper == null) {
                throw new IllegalArgumentException("Unsupported class: " + psseTypeClass());
            }

            // Parse the records
            reader.stream().forEach(rec -> {
                try {
                    beans.add(mapper.apply(rec, context.getVersion()));
                    context.setCurrentRecordNumFields(rec.getFieldCount());
                } catch (Exception e) {
                    throw new PsseException("Parsing error", e);
                }
            });
        } catch (IOException e) {
            throw new PsseException("Parsing error");
        }
        if (beans.size() != expectedCount) {
            throw new PsseException("Parsing error");
        }
        return beans;
    }

    // Casting is safe here
    @SuppressWarnings("unchecked")
    public String buildRecord(T object, String[] headers, String[] quoteFields, Context context) {
        try (StringWriter sw = new StringWriter();
             CsvWriter writer = getCsvWriterBuilder(headers, quoteFields, context).build(sw)) {
            // Get the corresponding mapper
            MapperTo<T> mapper = (MapperTo<T>) MAPPERS_TO_RECORD.get(psseTypeClass());
            if (mapper == null) {
                throw new IllegalArgumentException("Unsupported class: " + psseTypeClass());
            }
            writer.writeRecord(mapper.apply(object, headers));
            return unquoteNullString(sw.toString());
        } catch (IOException e) {
            throw new PsseException("Failed to write PSSE file", e);
        }
    }

    public List<String> buildRecords(List<T> objects, String[] headers, String[] quoteFields, Context context) {
        return objects.stream().map(object -> buildRecord(object, headers, quoteFields, context)).toList();
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

    CsvWriter.CsvWriterBuilder getCsvWriterBuilder(String[] headers, String[] quotedFields, Context context) {
        return CsvWriter.builder()
            .fieldSeparator(context.getDelimiter())
            .quoteCharacter(context.getQuote())
            .quoteStrategy(new QuoteStrategy() {
                @Override
                public boolean quoteValue(int lineNo, int fieldIdx, String value) {
                    return quotedFields != null && Arrays.asList(quotedFields).contains(headers[fieldIdx]);
                }
            });
    }
}
