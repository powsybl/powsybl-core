/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.psse.model.BlockData.PsseFileFormat;
import com.powsybl.psse.model.BlockData.PsseVersion;
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

    public boolean checkCaseIdentification(BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader);

        PsseVersion version = PsseVersion.VERSION_33;

        // just check the first record if this file is in PSS/E format
        PsseCaseIdentification caseIdentification;
        try {
            caseIdentification = new CaseIdentificationData(version).read(reader);
        } catch (PsseException e) {
            return false; // invalid PSS/E content
        }

        int ic = caseIdentification.getIc();
        double sbase = caseIdentification.getSbase();
        int rev = caseIdentification.getRev();
        double basfrq = caseIdentification.getBasfrq();

        if (ic == 0 && sbase > 0. && rev <= PsseConstants.SUPPORTED_VERSION && basfrq > 0.) {
            return true;
        }

        return false;
    }

    public PsseRawModel read(BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader);
        PsseContext context = new PsseContext();

        PsseVersion version = PsseVersion.VERSION_33;

        PsseCaseIdentification caseIdentification = new CaseIdentificationData(version).read(reader, context);
        PsseRawModel model = new PsseRawModel(caseIdentification);

        model.addBuses(new BusData(version).read(reader, context));
        model.addLoads(new LoadData(version).read(reader, context));
        model.addFixedShunts(new FixedBusShuntData(version).read(reader, context));
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
        readRecordBlock(reader);

        System.err.printf("Loads %d %n", model.getLoads().size());

        return model;
    }

    public PsseRawModel readx(String jsonFile) throws IOException {
        Objects.requireNonNull(jsonFile);
        PsseContext context = new PsseContext();

        PsseVersion version = PsseVersion.VERSION_35;
        PsseFileFormat format = PsseFileFormat.FORMAT_RAWX;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        JsonNode networkNode = rootNode.get("network");

        PsseCaseIdentification caseIdentification = new CaseIdentificationData(version, format).read(networkNode, context);
        PsseRawModel model = new PsseRawModel35(caseIdentification);

        model.addBuses(new BusData(version, format).read(networkNode, context));
        model.addLoads(new LoadData(version, format).read(networkNode, context));
        model.addFixedShunts(new FixedBusShuntData(version, format).read(networkNode, context));

        System.err.printf("Loads %d %n", model.getLoads().size());
        model.getLoads().forEach(load -> {
            PsseLoad35 load35 = (PsseLoad35) load;
            load35.print();
        });

        return model;
    }

    // Read blocks

    private static List<PsseGenerator> readGeneratorData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = PsseContext.generatorDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setGeneratorDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseGenerator.class, headers);
    }

    private static List<PsseNonTransformerBranch> readNonTransformerBranchData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = PsseContext.nonTransformerBranchDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setNonTransformerBranchDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseNonTransformerBranch.class, headers);
    }

    private static List<PsseTransformer> readTransformerData(BufferedReader reader, PsseContext context) throws IOException {

        String[] windingHeaders = PsseContext.transformerWindingDataHeaders();
        List<PsseTransformer> transformers = new ArrayList<>();

        List<String> records = readRecordBlock(reader);
        int i = 0;
        while (i < records.size()) {
            String record1 = records.get(i++);
            String record2 = records.get(i++);
            String record3 = records.get(i++);
            String record4 = records.get(i++);
            String twtRecord = String.join(context.getDelimiter(), record1, record2);

            String[] headers = PsseContext.transformerDataHeaders(record1.split(context.getDelimiter()).length);
            PsseTransformer transformer = parseRecordHeader(twtRecord, PsseTransformer.class, headers);

            transformer.setWindingRecord1(parseRecordHeader(record3, PsseTransformer.WindingRecord.class, windingHeaders));
            transformer.setWindingRecord2(parseRecordHeader(record4, PsseTransformer.WindingRecord.class, windingHeaders));

            if (transformer.getK() != 0) {
                String record5 = records.get(i++);
                transformer
                    .setWindingRecord3(parseRecordHeader(record5, PsseTransformer.WindingRecord.class, windingHeaders));

                context.set3wTransformerDataReadFields(readFields(twtRecord, headers, context.getDelimiter()),
                    readFields(record3, windingHeaders, context.getDelimiter()),
                    readFields(record4, windingHeaders, context.getDelimiter()),
                    readFields(record5, windingHeaders, context.getDelimiter()));
            } else {
                context.set2wTransformerDataReadFields(readFields(twtRecord, headers, context.getDelimiter()),
                    readFields(record3, windingHeaders, context.getDelimiter()),
                    readFields(record4, windingHeaders, context.getDelimiter()));
            }
            transformers.add(transformer);
        }

        return transformers;
    }

    private static List<PsseArea> readAreaInterchangeData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = PsseContext.areaInterchangeDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setAreaInterchangeDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseArea.class, headers);
    }

    private static List<PsseZone> readZoneData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = PsseContext.zoneDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setZoneDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseZone.class, headers);
    }

    private static List<PsseOwner> readOwnerData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = PsseContext.ownerDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setOwnerDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseOwner.class, headers);
    }

    private static List<PsseSwitchedShunt> readSwitchedShuntData(BufferedReader reader, PsseContext context) throws IOException {
        String[] headers = PsseContext.switchedShuntDataHeaders();
        List<String> records = readRecordBlock(reader);

        context.setSwitchedShuntDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseSwitchedShunt.class, headers);
    }

    // Parse

    private static <T> T parseRecordHeader(String record, Class<T> aClass, String[] headers) {
        List<T> beans = parseRecordsHeader(Collections.singletonList(record), aClass, headers);
        return beans.get(0);
    }

    private static <T> List<T> parseRecordsHeader(List<String> records, Class<T> aClass, String[] headers) {
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

    private static String detectDelimiter(String record) {
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

    private static List<String> readRecordBlock(BufferedReader reader) throws IOException {
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

    private static String readLineAndRemoveComment(BufferedReader reader) throws IOException {
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

    // Read fields

    private static String[] readFields(List<String> records, String[] headers, String delimiter) {
        if (records.isEmpty()) {
            return new String[] {};
        }
        String record = records.get(0);
        return ArrayUtils.subarray(headers, 0, record.split(delimiter).length);
    }

    private static String[] readFields(String record, String[] headers, String delimiter) {
        return ArrayUtils.subarray(headers, 0, record.split(delimiter).length);
    }
}
