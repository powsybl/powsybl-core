/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseRawReader {

    private static final Logger LOGGGER = LoggerFactory.getLogger(PsseRawReader.class);

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
        return removeComment(line);
    }

    private static <T> List<T> parseRecords(List<String> records, Class<T> aClass) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(false);
        settings.setQuoteDetectionEnabled(true);
        settings.setProcessorErrorHandler(new RetryableErrorHandler<ParsingContext>() {
            @Override
            public void handleError(DataProcessingException error, Object[] inputRow, ParsingContext context) {
                LOGGGER.error(error.getMessage());
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

    private static List<String> readRecordBlock(BufferedReader reader) throws IOException {
        String line;
        List<String> records = new ArrayList<>();
        while ((line = readLineAndRemoveComment(reader)) != null) {
            if (line.startsWith("0")) {
                break;
            }
            records.add(line);
        }
        return records;
    }

    private PsseCaseIdentification readCaseIdentification(BufferedReader reader) throws IOException {
        String line = readLineAndRemoveComment(reader);
        PsseCaseIdentification caseIdentification = parseRecords(Collections.singletonList(line), PsseCaseIdentification.class).get(0);
        caseIdentification.setTitle1(reader.readLine());
        caseIdentification.setTitle2(reader.readLine());
        return caseIdentification;
    }

    public PsseRawModel read(BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader);

        // case identification
        PsseCaseIdentification caseIdentification = readCaseIdentification(reader);

        PsseRawModel model = new PsseRawModel(caseIdentification);

        // bus data
        model.getBuses().addAll(parseRecords(readRecordBlock(reader), PsseBus.class));

        // load data
        model.getLoads().addAll(parseRecords(readRecordBlock(reader), PsseLoad.class));

        // fixed shunt data
        model.getFixedShunts().addAll(parseRecords(readRecordBlock(reader), PsseFixedShunt.class));

        // generator data
        model.getGenerators().addAll(parseRecords(readRecordBlock(reader), PsseGenerator.class));

        // non transformer data
        model.getNonTransformerBranches().addAll(parseRecords(readRecordBlock(reader), PsseNonTransformerBranch.class));

        return model;
    }
}
