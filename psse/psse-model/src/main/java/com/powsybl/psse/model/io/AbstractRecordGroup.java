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
import com.powsybl.psse.model.pf.internal.*;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRecord;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.LineDelimiter;
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
        mappers.put(PsseGneDevice.class, (MapperFrom<PsseGneDevice>) PsseGneDevice::fromRecord);
        mappers.put(PsseInductionMachine.class, (MapperFrom<PsseInductionMachine>) PsseInductionMachine::fromRecord);
        mappers.put(PsseInterareaTransfer.class, (MapperFrom<PsseInterareaTransfer>) PsseInterareaTransfer::fromRecord);
        mappers.put(PsseLineGrouping.class, (MapperFrom<PsseLineGrouping>) PsseLineGrouping::fromRecord);
        mappers.put(PsseLoad.class, (MapperFrom<PsseLoad>) PsseLoad::fromRecord);
        mappers.put(PsseMultiTerminalDcBus.class, (MapperFrom<PsseMultiTerminalDcBus>) PsseMultiTerminalDcBus::fromRecord);
        mappers.put(PsseMultiTerminalDcBusx.class, (MapperFrom<PsseMultiTerminalDcBusx>) PsseMultiTerminalDcBusx::fromRecord);
        mappers.put(PsseMultiTerminalDcConverter.class, (MapperFrom<PsseMultiTerminalDcConverter>) PsseMultiTerminalDcConverter::fromRecord);
        mappers.put(PsseMultiTerminalDcConverterx.class, (MapperFrom<PsseMultiTerminalDcConverterx>) PsseMultiTerminalDcConverterx::fromRecord);
        mappers.put(PsseMultiTerminalDcLink.class, (MapperFrom<PsseMultiTerminalDcLink>) PsseMultiTerminalDcLink::fromRecord);
        mappers.put(PsseMultiTerminalDcLinkx.class, (MapperFrom<PsseMultiTerminalDcLinkx>) PsseMultiTerminalDcLinkx::fromRecord);
        mappers.put(PsseMultiTerminalDcMain.class, (MapperFrom<PsseMultiTerminalDcMain>) PsseMultiTerminalDcMain::fromRecord);
        mappers.put(PsseNonTransformerBranch.class, (MapperFrom<PsseNonTransformerBranch>) PsseNonTransformerBranch::fromRecord);
        mappers.put(PsseOwner.class, (MapperFrom<PsseOwner>) PsseOwner::fromRecord);
        mappers.put(PsseOwnership.class, (MapperFrom<PsseOwnership>) PsseOwnership::fromRecord);
        mappers.put(PsseRates.class, (MapperFrom<PsseRates>) PsseRates::fromRecord);
        mappers.put(PsseSubstationEquipmentTerminal.class, (MapperFrom<PsseSubstationEquipmentTerminal>) PsseSubstationEquipmentTerminal::fromRecord);
        mappers.put(PsseSubstationEquipmentTerminalCommonStart.class, (MapperFrom<PsseSubstationEquipmentTerminalCommonStart>) PsseSubstationEquipmentTerminalCommonStart::fromRecord);
        mappers.put(PsseSubstationEquipmentTerminalx.class, (MapperFrom<PsseSubstationEquipmentTerminalx>) PsseSubstationEquipmentTerminalx::fromRecord);
        mappers.put(PsseSubstationNode.class, (MapperFrom<PsseSubstationNode>) PsseSubstationNode::fromRecord);
        mappers.put(PsseSubstationNodex.class, (MapperFrom<PsseSubstationNodex>) PsseSubstationNodex::fromRecord);
        mappers.put(PsseSubstationRecord.class, (MapperFrom<PsseSubstationRecord>) PsseSubstationRecord::fromRecord);
        mappers.put(PsseSubstationSwitchingDevice.class, (MapperFrom<PsseSubstationSwitchingDevice>) PsseSubstationSwitchingDevice::fromRecord);
        mappers.put(PsseSubstationSwitchingDevicex.class, (MapperFrom<PsseSubstationSwitchingDevicex>) PsseSubstationSwitchingDevicex::fromRecord);
        mappers.put(PsseSwitchedShunt.class, (MapperFrom<PsseSwitchedShunt>) PsseSwitchedShunt::fromRecord);
        mappers.put(PsseTransformer.class, (MapperFrom<PsseTransformer>) PsseTransformer::fromRecord);
        mappers.put(PsseTransformerImpedanceCorrection.class, (MapperFrom<PsseTransformerImpedanceCorrection>) PsseTransformerImpedanceCorrection::fromRecord);
        mappers.put(PsseTransformerWinding.class, (MapperFrom<PsseTransformerWinding>) PsseTransformerWinding::fromRecord);
        mappers.put(PsseTwoTerminalDcConverter.class, (MapperFrom<PsseTwoTerminalDcConverter>) PsseTwoTerminalDcConverter::fromRecord);
        mappers.put(PsseTwoTerminalDcTransmissionLine.class, (MapperFrom<PsseTwoTerminalDcTransmissionLine>) PsseTwoTerminalDcTransmissionLine::fromRecord);
        mappers.put(PsseVoltageSourceConverter.class, (MapperFrom<PsseVoltageSourceConverter>) PsseVoltageSourceConverter::fromRecord);
        mappers.put(PsseVoltageSourceConverterDcTransmissionLine.class, (MapperFrom<PsseVoltageSourceConverterDcTransmissionLine>) PsseVoltageSourceConverterDcTransmissionLine::fromRecord);
        mappers.put(PsseZone.class, (MapperFrom<PsseZone>) PsseZone::fromRecord);
        mappers.put(TransformerImpedances.class, (MapperFrom<TransformerImpedances>) TransformerImpedances::fromRecord);
        mappers.put(TransformerWindingRecord.class, (MapperFrom<TransformerWindingRecord>) TransformerWindingRecord::fromRecord);
        mappers.put(ZCorr33.class, (MapperFrom<ZCorr33>) ZCorr33::fromRecord);
        mappers.put(ZCorr35First.class, (MapperFrom<ZCorr35First>) ZCorr35First::fromRecord);
        mappers.put(ZCorr35Points.class, (MapperFrom<ZCorr35Points>) ZCorr35Points::fromRecord);
        mappers.put(ZCorr35X.class, (MapperFrom<ZCorr35X>) ZCorr35X::fromRecord);
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
        mappers.put(PsseGneDevice.class, (MapperTo<PsseGneDevice>) PsseGneDevice::toRecord);
        mappers.put(PsseInductionMachine.class, (MapperTo<PsseInductionMachine>) PsseInductionMachine::toRecord);
        mappers.put(PsseInterareaTransfer.class, (MapperTo<PsseInterareaTransfer>) PsseInterareaTransfer::toRecord);
        mappers.put(PsseLineGrouping.class, (MapperTo<PsseLineGrouping>) PsseLineGrouping::toRecord);
        mappers.put(PsseLoad.class, (MapperTo<PsseLoad>) PsseLoad::toRecord);
        mappers.put(PsseMultiTerminalDcBus.class, (MapperTo<PsseMultiTerminalDcBus>) PsseMultiTerminalDcBus::toRecord);
        mappers.put(PsseMultiTerminalDcBusx.class, (MapperTo<PsseMultiTerminalDcBusx>) PsseMultiTerminalDcBusx::toRecord);
        mappers.put(PsseMultiTerminalDcConverter.class, (MapperTo<PsseMultiTerminalDcConverter>) PsseMultiTerminalDcConverter::toRecord);
        mappers.put(PsseMultiTerminalDcConverterx.class, (MapperTo<PsseMultiTerminalDcConverterx>) PsseMultiTerminalDcConverterx::toRecord);
        mappers.put(PsseMultiTerminalDcLink.class, (MapperTo<PsseMultiTerminalDcLink>) PsseMultiTerminalDcLink::toRecord);
        mappers.put(PsseMultiTerminalDcLinkx.class, (MapperTo<PsseMultiTerminalDcLinkx>) PsseMultiTerminalDcLinkx::toRecord);
        mappers.put(PsseMultiTerminalDcMain.class, (MapperTo<PsseMultiTerminalDcMain>) PsseMultiTerminalDcMain::toRecord);
        mappers.put(PsseNonTransformerBranch.class, (MapperTo<PsseNonTransformerBranch>) PsseNonTransformerBranch::toRecord);
        mappers.put(PsseOwner.class, (MapperTo<PsseOwner>) PsseOwner::toRecord);
        mappers.put(PsseOwnership.class, (MapperTo<PsseOwnership>) PsseOwnership::toRecord);
        mappers.put(PsseRates.class, (MapperTo<PsseRates>) PsseRates::toRecord);
        mappers.put(PsseSubstationEquipmentTerminal.class, (MapperTo<PsseSubstationEquipmentTerminal>) PsseSubstationEquipmentTerminal::toRecord);
        mappers.put(PsseSubstationEquipmentTerminalCommonStart.class, (MapperTo<PsseSubstationEquipmentTerminalCommonStart>) PsseSubstationEquipmentTerminalCommonStart::toRecord);
        mappers.put(PsseSubstationEquipmentTerminalx.class, (MapperTo<PsseSubstationEquipmentTerminalx>) PsseSubstationEquipmentTerminalx::toRecord);
        mappers.put(PsseSubstationNode.class, (MapperTo<PsseSubstationNode>) PsseSubstationNode::toRecord);
        mappers.put(PsseSubstationNodex.class, (MapperTo<PsseSubstationNodex>) PsseSubstationNodex::toRecord);
        mappers.put(PsseSubstationRecord.class, (MapperTo<PsseSubstationRecord>) PsseSubstationRecord::toRecord);
        mappers.put(PsseSubstationSwitchingDevice.class, (MapperTo<PsseSubstationSwitchingDevice>) PsseSubstationSwitchingDevice::toRecord);
        mappers.put(PsseSubstationSwitchingDevicex.class, (MapperTo<PsseSubstationSwitchingDevicex>) PsseSubstationSwitchingDevicex::toRecord);
        mappers.put(PsseSwitchedShunt.class, (MapperTo<PsseSwitchedShunt>) PsseSwitchedShunt::toRecord);
        mappers.put(PsseTransformer.class, (MapperTo<PsseTransformer>) PsseTransformer::toRecord);
        mappers.put(PsseTransformerImpedanceCorrection.class, (MapperTo<PsseTransformerImpedanceCorrection>) PsseTransformerImpedanceCorrection::toRecord);
        mappers.put(PsseTransformerWinding.class, (MapperTo<PsseTransformerWinding>) PsseTransformerWinding::toRecord);
        mappers.put(PsseTwoTerminalDcConverter.class, (MapperTo<PsseTwoTerminalDcConverter>) PsseTwoTerminalDcConverter::toRecord);
        mappers.put(PsseTwoTerminalDcTransmissionLine.class, (MapperTo<PsseTwoTerminalDcTransmissionLine>) PsseTwoTerminalDcTransmissionLine::toRecord);
        mappers.put(PsseVoltageSourceConverter.class, (MapperTo<PsseVoltageSourceConverter>) PsseVoltageSourceConverter::toRecord);
        mappers.put(PsseVoltageSourceConverterDcTransmissionLine.class, (MapperTo<PsseVoltageSourceConverterDcTransmissionLine>) PsseVoltageSourceConverterDcTransmissionLine::toRecord);
        mappers.put(PsseZone.class, (MapperTo<PsseZone>) PsseZone::toRecord);
        mappers.put(TransformerImpedances.class, (MapperTo<TransformerImpedances>) TransformerImpedances::toRecord);
        mappers.put(TransformerWindingRecord.class, (MapperTo<TransformerWindingRecord>) TransformerWindingRecord::toRecord);
        mappers.put(ZCorr33.class, (MapperTo<ZCorr33>) ZCorr33::toRecord);
        mappers.put(ZCorr35First.class, (MapperTo<ZCorr35First>) ZCorr35First::toRecord);
        mappers.put(ZCorr35Points.class, (MapperTo<ZCorr35Points>) ZCorr35Points::toRecord);
        mappers.put(ZCorr35X.class, (MapperTo<ZCorr35X>) ZCorr35X::toRecord);
        return mappers;
    }

    private interface MapperFrom<T> {
        T apply(CsvRecord rec, String[] headers);
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

        // Initialize the output
        List<T> beans = new ArrayList<>(expectedCount);
        context.resetCurrentRecordGroup();

        // Reconstruct a string block
        StringBuilder sb = new StringBuilder();
        String[] headersToUse = makeRecordsConsistents(sb, records, headers, context);

        // Parse
        try (CsvReader<CsvRecord> reader = context.createCsvReaderBuilder()
            .ofCsvRecord(new StringReader(sb.toString()))) {
            // Get the corresponding mapper
            MapperFrom<T> mapper = (MapperFrom<T>) MAPPERS_FROM_RECORD.get(psseTypeClass());
            if (mapper == null) {
                throw new IllegalArgumentException("Unsupported class: " + psseTypeClass());
            }

            // Parse the records
            reader.stream().forEach(rec -> {
                try {
                    beans.add(mapper.apply(rec, headersToUse));
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

    private String[] makeRecordsConsistents(StringBuilder sb, List<String> records, String[] headers, Context context) {
        if (records.stream().anyMatch(Objects::isNull)) {
            throw new PsseException("Parsing error");
        }
        char delimiter = context.getDelimiter();
        char quoteChar = context.getQuote(); // Add a getter in Context if needed, or hardcode '\''

        int expectedNumFields = records.stream()
            .mapToInt(rec -> countFields(rec, delimiter, quoteChar))
            .max().orElse(0);
        for (String rec : records) {
            int numFields = countFields(rec, delimiter, quoteChar);
            sb.append(rec);
            if (numFields < expectedNumFields) {
                sb.append(String.valueOf(context.getDelimiter()).repeat(Math.max(0, expectedNumFields - numFields)));
                LOGGER.info("Added {} empty fields to record {}", expectedNumFields - numFields, rec);
            }
            sb.append(System.lineSeparator());
        }
        return Arrays.copyOfRange(headers, 0, expectedNumFields);
    }

    /**
     * Count the number of fields, ignoring delimiters inside quoted text.
     */
    private int countFields(String line, char delimiter, char quoteChar) {
        boolean inQuotes = false;
        int count = 1; // At least one field
        for (char c : line.toCharArray()) {
            if (c == quoteChar) {
                inQuotes = !inQuotes;
            } else if (c == delimiter && !inQuotes) {
                count++;
            }
        }
        return count;
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
            writer.flush();
            return unquoteNullString(sw.toString());
        } catch (IOException e) {
            throw new PsseException("Failed to write PSSE file", e);
        }
    }

    public List<String> buildRecords(List<T> objects, String[] headers, String[] quoteFields, Context context) {
        return objects.stream().map(object -> buildRecord(object, headers, quoteFields, context)).collect(Collectors.toList());
    }

    private static String unquoteNullString(String string) {
        return string.replace("\"null\"", "null");
    }

    CsvWriter.CsvWriterBuilder getCsvWriterBuilder(String[] headers, String[] quotedFields, Context context) {
        return CsvWriter.builder()
            .fieldSeparator(context.getDelimiter())
            .quoteCharacter(context.getQuote())
            .lineDelimiter(LineDelimiter.PLATFORM)
            .quoteStrategy(new QuoteStrategy() {
                @Override
                public boolean quoteValue(int lineNo, int fieldIdx, String value) {
                    return quotedFields != null && Arrays.asList(quotedFields).contains(headers[fieldIdx]);
                }

                @Override
                public boolean quoteEmpty(final int lineNo, final int fieldIdx) {
                    return true;
                }
            });
    }
}
