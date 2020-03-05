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
public class PsseReader {

    private static final Logger LOGGGER = LoggerFactory.getLogger(PsseReader.class);

    private static String removeComment(String line) {
        int slashIndex = line.lastIndexOf('/');
        if (slashIndex == -1) {
            return line;
        }
        return line.substring(0, slashIndex);
    }

    private static String readLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        return removeComment(line);
    }

    private static <T> List<T> parseLines(List<String> lines, Class<T> aClass) {
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
        for (String line : lines) {
            parser.parseLine(line);
        }
        List<T> beans = processor.getBeans();
        if (beans.size() != lines.size()) {
            throw new PsseException("Parsing error");
        }
        return beans;
    }

    private static List<String> readRecords(BufferedReader reader) throws IOException {
        String line;
        List<String> lines = new ArrayList<>();
        while ((line = readLine(reader)) != null) {
            if (line.startsWith("0")) {
                break;
            }
            lines.add(line);
        }
        return lines;
    }

    private PsseCaseIdentification readCaseIdentification(BufferedReader reader, String line) throws IOException {
        PsseCaseIdentification caseIdentification = parseLines(Collections.singletonList(line), PsseCaseIdentification.class).get(0);
        caseIdentification.setTitle1(reader.readLine());
        caseIdentification.setTitle2(reader.readLine());
        return caseIdentification;
    }

    public PsseRawData read(BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader);

        String line = readLine(reader);

        // case identification
        PsseCaseIdentification caseIdentification = readCaseIdentification(reader, line);

        PsseRawData rawData = new PsseRawData(caseIdentification);

        // bus data
        rawData.getBuses().addAll(parseLines(readRecords(reader), PsseBus.class));

        // load data
        rawData.getLoads().addAll(parseLines(readRecords(reader), PsseLoad.class));

        // fixed shunt data
        rawData.getFixedShunts().addAll(parseLines(readRecords(reader), PsseFixedShunt.class));

        // generator data
        rawData.getGenerators().addAll(parseLines(readRecords(reader), PsseGenerator.class));

        // non transformer data
        rawData.getNonTransformerBranches().addAll(parseLines(readRecords(reader), PsseNonTransformerBranch.class));

        return rawData;
    }
}
