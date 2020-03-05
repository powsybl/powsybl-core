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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseReader {

    private static final Logger LOGGGER = LoggerFactory.getLogger(PsseReader.class);

    public PsseRawData read(BufferedReader reader) throws IOException {
        String line = reader.readLine();

        PsseCaseIdentification caseIdentification = parseLines(Collections.singletonList(line), PsseCaseIdentification.class).get(0);
        PsseRawData rawData = new PsseRawData(caseIdentification);

        List<String> lines = new ArrayList<>();
        while ((line = reader.readLine()) != null) {

        }

        return rawData;
    }

    private static <T> List<T> parseLines(List<String> lines, Class<T> aClass) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(false);
        settings.getFormat().setLineSeparator("\n");
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
            Arrays.toString(parser.parseLine(line));
        }
        return processor.getBeans();
    }

    private void parseLines(List<String> lines, PsseRawData rawData, PsseDataType dataType) {
        switch (dataType) {
            case BUS:
                rawData.getBuses().addAll(parseLines(lines, PsseBus.class));
                break;
            case LOAD:
                rawData.getLoads().addAll(parseLines(lines, PsseLoad.class));
                break;
            case FIXED_SHUNT:
                rawData.getFixedShunts().addAll(parseLines(lines, PsseFixedShunt.class));
                break;
            case GENERATOR:
                rawData.getGenerators().addAll(parseLines(lines, PsseGenerator.class));
                break;
            case NON_TRANSFORMER_BRANCH:
                rawData.getNonTransformerBranches().addAll(parseLines(lines, PsseNonTransformerBranch.class));
                break;
            case TRANSFORMER:
                rawData.getTransformers().addAll(parseLines(lines, PsseTransformer.class));
                break;
            default:
                throw new IllegalStateException("Data type unknown: " + dataType);
        }
    }
}
