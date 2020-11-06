/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.RetryableErrorHandler;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseRawReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PsseRawReader.class);

    public void checkCaseIdentification(BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader);

        // just check the first record if this file is in PSS/E format
        PsseCaseIdentification caseIdentification;
        try {
            caseIdentification = readCaseIdentificationData(reader);
        } catch (PsseException e) {
            throw new PsseException("Invalid PSS/E content");
        }

        int ic = caseIdentification.getIc();
        double sbase = caseIdentification.getSbase();
        int rev = caseIdentification.getRev();
        double basfrq = caseIdentification.getBasfrq();

        if (ic == 1) {
            throw new PsseException("Incremental load of PSS/E data option (IC = 1) not supported");
        }
        if (rev > PsseConstants.SUPPORTED_VERSION) {
            throw new PsseException("PSS/E version higher than " + PsseConstants.SUPPORTED_VERSION + " not supported");
        }
        if (sbase <= 0.) {
            throw new PsseException("PSS/E Unexpected System MVA base " + sbase);
        }
        if (basfrq <= 0.) {
            throw new PsseException("PSS/E Unexpected System base frequency " + basfrq);
        }
    }

    public PsseRawModel read(BufferedReader reader) throws IOException {
        PsseContext context = new PsseContext();
        return read(reader, context);
    }

    public PsseRawModel read(BufferedReader reader, PsseContext context) throws IOException {
        Objects.requireNonNull(reader);
        Objects.requireNonNull(context);

        PsseCaseIdentification caseIdentification = readCaseIdentificationData(reader, context);
        PsseRawModel model = new PsseRawModel(caseIdentification);

        model.getBuses().addAll(readBusData(reader, context));
        model.getLoads().addAll(readLoadData(reader, context));
        model.getFixedShunts().addAll(readFixedBusShuntData(reader, context));
        model.getGenerators().addAll(readGeneratorData(reader, context));
        model.getNonTransformerBranches().addAll(readNonTransformerBranchData(reader, context));
        model.getTransformers().addAll(readTransformerData(reader, context));
        model.getAreas().addAll(readAreaInterchangeData(reader, context));

        // 2-terminal DC data
        readRecordBlock(reader); // TODO

        // voltage source converter data
        readRecordBlock(reader); // TODO

        // impedance correction data
        readRecordBlock(reader); // TODO

        // multi-terminal DC data
        readRecordBlock(reader); // TODO

        // multi-section line data
        readRecordBlock(reader); // TODO

        model.getZones().addAll(readZoneData(reader, context));

        // inter-area transfer data
        readRecordBlock(reader); // TODO

        model.getOwners().addAll(readOwnerData(reader, context));

        // facts control device data
        readRecordBlock(reader); // TODO

        model.getSwitchedShunts().addAll(readSwitchedShuntData(reader, context));

        // gne device data
        readRecordBlock(reader); // TODO

        // q record (nothing to do)
        readLineAndRemoveComment(reader);

        return model;
    }

    // Read blocks

    private static PsseCaseIdentification readCaseIdentificationData(BufferedReader reader, PsseContext context) throws IOException {
        String line = readLineAndRemoveComment(reader);

        context.setDelimiter(detectDelimiter(line));

        String[] headers = caseIdentificationDataHeaders(line.split(context.getDelimiter()).length);
        PsseCaseIdentification caseIdentification = parseRecordHeader(line, PsseCaseIdentification.class, headers);
        caseIdentification.setTitle1(reader.readLine());
        caseIdentification.setTitle2(reader.readLine());

        context.setCaseIdentificationDataReadFields(headers);
        return caseIdentification;
    }

    private static PsseCaseIdentification readCaseIdentificationData(BufferedReader reader) throws IOException {
        String line = readLineAndRemoveComment(reader);

        String[] headers = caseIdentificationDataHeaders();
        PsseCaseIdentification caseIdentification = parseRecordHeader(line, PsseCaseIdentification.class, headers);
        caseIdentification.setTitle1(reader.readLine());
        caseIdentification.setTitle2(reader.readLine());

        return caseIdentification;
    }

    private static List<PsseBus> readBusData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = busDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setBusDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseBus.class, headers);
    }

    private static List<PsseLoad> readLoadData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = loadDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setLoadDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseLoad.class, headers);
    }

    private static List<PsseFixedShunt> readFixedBusShuntData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = fixedBusShuntDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setFixedBusShuntDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseFixedShunt.class, headers);
    }

    private static List<PsseGenerator> readGeneratorData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = generatorDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setGeneratorDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseGenerator.class, headers);
    }

    private static List<PsseNonTransformerBranch> readNonTransformerBranchData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = nonTransformerBranchDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setNonTransformerBranchDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseNonTransformerBranch.class, headers);
    }

    private static List<PsseTransformer> readTransformerData(BufferedReader reader, PsseContext context) throws IOException {

        String[] windingHeaders = transformerWindingDataHeaders();
        List<PsseTransformer> transformers = new ArrayList<>();

        List<String> records = readRecordBlock(reader);
        int i = 0;
        while (i < records.size()) {
            String record1 = records.get(i++);
            String record2 = records.get(i++);
            String record3 = records.get(i++);
            String record4 = records.get(i++);
            String twtRecord = String.join(context.getDelimiter(), record1, record2);

            String[] headers = transformerDataHeaders(record1.split(context.getDelimiter()).length);
            PsseTransformer transformer = parseRecordHeader(twtRecord, PsseTransformer.class, headers);

            transformer.setWindingRecord1(parseRecordHeader(record3, PsseTransformer.WindingRecord.class, windingHeaders));
            transformer.setWindingRecord2(parseRecordHeader(record4, PsseTransformer.WindingRecord.class, windingHeaders));

            if (transformer.getK() != 0) {
                String record5 = records.get(i++);
                transformer
                    .setWindingRecord3(parseRecordHeader(record5, PsseTransformer.WindingRecord.class, windingHeaders));

                if (context.is3wTransformerDataReadFieldsEmpty()) {
                    context.set3wTransformerDataReadFields(readFields(twtRecord, headers, context.getDelimiter()),
                        readFields(record3, windingHeaders, context.getDelimiter()),
                        readFields(record4, windingHeaders, context.getDelimiter()),
                        readFields(record5, windingHeaders, context.getDelimiter()));
                }
            } else {
                if (context.is2wTransformerDataReadFieldsEmpty()) {
                    context.set2wTransformerDataReadFields(readFields(twtRecord, headers, context.getDelimiter()),
                        readFields(record3, windingHeaders, context.getDelimiter()),
                        readFields(record4, windingHeaders, context.getDelimiter()));
                }
            }
            transformers.add(transformer);
        }

        return transformers;
    }

    private static List<PsseArea> readAreaInterchangeData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = areaInterchangeDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setAreaInterchangeDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseArea.class, headers);
    }

    private static List<PsseZone> readZoneData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = zoneDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setZoneDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseZone.class, headers);
    }

    private static List<PsseOwner> readOwnerData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = ownerDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setOwnerDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseOwner.class, headers);
    }

    private static List<PsseSwitchedShunt> readSwitchedShuntData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = switchedShuntDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setSwitchedShuntDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseSwitchedShunt.class, headers);
    }

    private static <T> T parseRecordHeader(String record, Class<T> aClass, String[] headers) {
        List<T> beans = parseRecordsHeader(Collections.singletonList(record), aClass, headers);
        return beans.get(0);
    }

    private static <T> List<T> parseRecordsHeader(List<String> records, Class<T> aClass, String[] headers) {
        CsvParserSettings settings = createCsvParserSettings();
        settings.setHeaders(headers);
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

    private static String detectDelimiter(String record) {
        CsvParserSettings settings = createCsvParserSettings();
        CsvParser parser = new CsvParser(settings);
        parser.parseLine(record);
        return parser.getDetectedFormat().getDelimiterString();
    }

    private static CsvParserSettings createCsvParserSettings() {
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

        return settings;
    }

    // Read

    private static List<String> readRecordBlock(BufferedReader reader) throws IOException {
        List<String> records = new ArrayList<>();

        String line = readLineAndRemoveComment(reader);
        while (!line.trim().equals("0")) {
            records.add(line);
            line = readLineAndRemoveComment(reader);
        }

        return records;
    }

    private static String removeComment(String line) {
        int slashIndex = line.indexOf('/');
        if (slashIndex == -1) {
            return line;
        }
        return line.substring(0, slashIndex);
    }

    private static String readLineAndRemoveComment(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new PsseException("PSSE. Unexpected end of file");
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

    // Read fields

    private static String[] readFields(List<String> records, String[] headers, String delimiter) {
        if (records.isEmpty()) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        String record = records.get(0);
        return ArrayUtils.subarray(headers, 0, record.split(delimiter).length);
    }

    private static String[] readFields(String record, String[] headers, String delimiter) {
        return ArrayUtils.subarray(headers, 0, record.split(delimiter).length);
    }

    // Block fields

    private static String[] caseIdentificationDataHeaders(int firstRecordFields) {
        String[] first = new String[] {"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq"};
        return ArrayUtils.addAll(ArrayUtils.subarray(first, 0, firstRecordFields), "title1", "title2");
    }

    private static String[] caseIdentificationDataHeaders() {
        return new String[] {"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};
    }

    private static String[] busDataHeaders() {
        return new String[] {"i", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va", "nvhi", "nvlo", "evhi", "evlo"};
    }

    private static String[] loadDataHeaders() {
        return new String[] {"i", "id", "status", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale", "intrpt"};
    }

    private static String[] fixedBusShuntDataHeaders() {
        return new String[] {"i", "id", "status", "gl", "bl"};
    }

    private static String[] generatorDataHeaders() {
        return new String[] {"i", "id", "pg", "qg", "qt", "qb", "vs", "ireg", "mbase", "zr", "zx", "rt",
            "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};
    }

    private static String[] nonTransformerBranchDataHeaders() {
        return new String[] {"i", "j", "ckt", "r", "x", "b", "ratea", "rateb", "ratec", "gi", "bi", "gj", "bj",
            "st", "met", "len", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};
    }

    private static String[] transformerDataHeaders(int firstRecordFields) {
        String[] first = new String[] {"i", "j", "k", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmetr", "name", "stat",
            "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "vecgrp"};
        String[] second = new String[] {"r12", "x12", "sbase12", "r23", "x23", "sbase23", "r31", "x31", "sbase31",
            "vmstar", "anstar"};

        return ArrayUtils.addAll(ArrayUtils.subarray(first, 0, firstRecordFields), second);
    }

    private static String[] transformerWindingDataHeaders() {
        return new String[] {"windv", "nomv", "ang", "rata", "ratb", "ratc", "cod", "cont", "rma", "rmi", "vma", "vmi",
            "ntp", "tab", "cr", "cx", "cnxa"};
    }

    private static String[] areaInterchangeDataHeaders() {
        return new String[] {"i", "isw", "pdes", "ptol", "arname"};
    }

    private static String[] zoneDataHeaders() {
        return new String[] {"i", "zoname"};
    }

    private static String[] ownerDataHeaders() {
        return new String[] {"i", "owname"};
    }

    private static String[] switchedShuntDataHeaders() {
        return new String[] {"i", "modsw", "adjm", "stat", "vswhi", "vswlo", "swrem", "rmpct", "rmidnt", "binit",
            "n1", "b1", "n2", "b2", "n3", "b3", "n4", "b4", "n5", "b5", "n6", "b6", "n7", "b7", "n8", "b8"};
    }
}
