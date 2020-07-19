/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseConstants.PsseFileFormat;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseException;
import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.RetryableErrorHandler;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class BlockData {

    private static final String PSSE = "Psse: ";

    enum PsseBlockData {
        CaseIdentificationData,
        BusData,
        LoadData,
        FixedBusShuntData,
        GeneratorData,
        NonTransformerBranchData,
        TransformerData,
        AreaInterchangeData,
        TwoTerminalDcTransmissionLineData,
        VoltageSourceConverterDcTransmissionLineData,
        TransformerImpedanceCorrectionTables,
        MultiTerminalDcTransmissionLineData,
        MultiSectionLineGrupingData,
        ZoneData,
        InterareaTransferData,
        OwnerData,
        FactsDeviceData,
        SwitchedShuntData,
        GneDeviceData,
        InductionMachineData,
        Qrecord
    }

    private final PsseVersion psseVersion;
    private final PsseFileFormat psseFileFormat;

    BlockData(PsseVersion psseVersion) {
        this.psseVersion = psseVersion;
        this.psseFileFormat = PsseFileFormat.FORMAT_RAW;
    }

    BlockData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        this.psseVersion = psseVersion;
        this.psseFileFormat = psseFileFormat;
    }

    PsseVersion getPsseVersion() {
        return psseVersion;
    }

    PsseFileFormat getFileFormat() {
        return psseFileFormat;
    }

    // Parse

    static <T> T parseRecordHeader(String record, Class<T> aClass, String[] headers) {
        List<T> beans = parseRecordsHeader(Collections.singletonList(record), aClass, headers);
        return beans.get(0);
    }

    static <T> List<T> parseRecordsHeader(List<String> records, Class<T> aClass, String[] headers) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(false);
        settings.setQuoteDetectionEnabled(true);
        settings.setDelimiterDetectionEnabled(true, ',', ' '); // sequence order is relevant
        settings.setHeaders(headers);
        settings.setProcessorErrorHandler(new RetryableErrorHandler<ParsingContext>() {
            @Override
            public void handleError(DataProcessingException error, Object[] inputRow, ParsingContext context) {
                LOGGER.error(error.getMessage());
            }
        });
        BeanListProcessor<T> processor = new BeanListProcessor<>(aClass);
        settings.setProcessor(processor);
        CsvParser parser = new CsvParser(settings);
        for (String record : records) {
            parser.parseLine(record);
        }
        List<T> beans = processor.getBeans();
        if (beans.size() != records.size()) {
            throw new PsseException("Parsing error");
        }
        return beans;
    }

    static String detectDelimiter(String record) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(false);
        settings.setQuoteDetectionEnabled(true);
        settings.setDelimiterDetectionEnabled(true, ',', ' '); // sequence order is relevant
        settings.setProcessorErrorHandler(new RetryableErrorHandler<ParsingContext>() {
            @Override
            public void handleError(DataProcessingException error, Object[] inputRow, ParsingContext context) {
                LOGGER.error(error.getMessage());
            }
        });
        CsvParser parser = new CsvParser(settings);
        parser.parseLine(record);
        return parser.getDetectedFormat().getDelimiterString();
    }

    // Read

    public static void readDiscardedRecordBlock(BufferedReader reader) throws IOException {
        readRecordBlock(reader);
    }

    static List<String> readRecordBlock(BufferedReader reader) throws IOException {
        String line;
        List<String> records = new ArrayList<>();
        while ((line = readLineAndRemoveComment(reader)) != null) {
            if (line.trim().equals("0")) {
                break;
            }
            records.add(line);
        }
        return records;
    }

    private static String removeComment(String line) {
        int slashIndex = line.lastIndexOf('/');
        if (slashIndex == -1) {
            return line;
        }
        return line.substring(0, slashIndex);
    }

    static String readLineAndRemoveComment(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        StringBuffer newLine = new StringBuffer();
        Pattern p = Pattern.compile("('[^']+')|( )+");
        Matcher m = p.matcher(removeComment(line));
        while (m.find()) {
            if (m.group().contains("'")) {
                m.appendReplacement(newLine, m.group());
            } else {
                m.appendReplacement(newLine, " ");
            }
        }
        m.appendTail(newLine);
        return newLine.toString().trim();
    }

    private static String cleanRawxFieldString(String data) {
        StringBuffer newData = new StringBuffer();
        Pattern p = Pattern.compile("(\"[^\"]+\")|( )+");
        Matcher m = p.matcher(data);
        while (m.find()) {
            if (m.group().contains("\"")) {
                m.appendReplacement(newData, m.group().replace("\"",  ""));
            } else {
                m.appendReplacement(newData, "");
            }
        }
        m.appendTail(newData);
        return newData.toString();
    }

    private static String cleanRawxDataString(String data) {
        StringBuffer newData = new StringBuffer();
        Pattern p = Pattern.compile("(\"[^\"]+\")|( )+");
        Matcher m = p.matcher(data);
        while (m.find()) {
            if (m.group().contains("\"")) {
                m.appendReplacement(newData, m.group());
            } else {
                m.appendReplacement(newData, "");
            }
        }
        m.appendTail(newData);
        return newData.toString().trim();
    }

    // rawx management

    static String[] nodeFields(JsonNode jsonNode) {
        String fieldsNode = jsonNode.get("fields").toString();
        String fieldsNodeClean = cleanRawxFieldString(fieldsNode.substring(1, fieldsNode.length() - 1));
        return fieldsNodeClean.split(",");
    }

    static List<String> nodeRecords(JsonNode jsonNode) {
        String dataNode = jsonNode.get("data").toString();
        String dataNodeClean = cleanRawxDataString(dataNode.substring(1, dataNode.length() - 1));

        String[] dataNodeArray = StringUtils.substringsBetween(dataNodeClean, "[", "]");
        List<String> records = new ArrayList<>();
        if (dataNodeArray == null) {
            records.add(dataNodeClean);
        } else {
            records.addAll(Arrays.asList(dataNodeArray));
        }
        return records;
    }

    // Read fields

    static String[] readFields(List<String> records, String[] headers, String delimiter) {
        if (records.isEmpty()) {
            return new String[] {};
        }
        String record = records.get(0);
        return ArrayUtils.subarray(headers, 0, record.split(delimiter).length);
    }

    static String[] readFields(String record, String[] headers, String delimiter) {
        return ArrayUtils.subarray(headers, 0, record.split(delimiter).length);
    }

    // Version and file format management

    void assertExpectedVersion(PsseBlockData blockData, PsseVersion minimumVersion, PsseVersion maximumVersion) {
        if (psseVersion.ordinal() >= minimumVersion.ordinal() &&  psseVersion.ordinal() <= maximumVersion.ordinal()
            && psseFileFormat == PsseFileFormat.FORMAT_RAW) {
            return;
        }
        throw new PsseException(PSSE + blockData + ". Wrong version, expected version between (" + minimumVersion
            + ", " + maximumVersion + ") actual version " + psseVersion + ". Expected format "
            + PsseFileFormat.FORMAT_RAW + " actual format" + psseFileFormat);
    }

    void assertExpectedVersion(PsseBlockData blockData, PsseVersion minimumVersion, PsseVersion maximumVersion,
        PsseFileFormat fileFormat) {
        if (psseVersion.ordinal() >= minimumVersion.ordinal() && psseVersion.ordinal() <= maximumVersion.ordinal() &&
            psseFileFormat == fileFormat) {
            return;
        }
        throw new PsseException(PSSE + blockData + ". Wrong version, expected version between (" + minimumVersion
            + ", " + maximumVersion + ") actual version " + psseVersion + ". Expected format "
            + fileFormat + " actual format" + psseFileFormat);
    }

    void assertMinimumExpectedVersion(PsseBlockData blockData, PsseVersion version) {
        if (psseVersion.ordinal() >= version.ordinal() && psseFileFormat == PsseFileFormat.FORMAT_RAW) {
            return;
        }
        throw new PsseException(PSSE + blockData + ". Wrong version, minimum expected version" + version + " actual version "
            + psseVersion + ". Expected format " + PsseFileFormat.FORMAT_RAW + " actual format " + psseFileFormat);
    }

    void assertMinimumExpectedVersion(PsseBlockData blockData, PsseVersion version, PsseFileFormat fileFormat) {
        if (psseVersion.ordinal() >= version.ordinal() && psseFileFormat == fileFormat) {
            return;
        }
        throw new PsseException(PSSE + blockData + ". Wrong version, minimum expected version " + version + " actual version "
            + psseVersion + ". Expected format " + fileFormat + " actual format " + psseFileFormat);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockData.class);
}
