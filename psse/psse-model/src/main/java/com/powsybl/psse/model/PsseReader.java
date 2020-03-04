/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseReader {

    public PsseRawData read(BufferedReader reader) throws IOException {
        String line = reader.readLine();

//        PsseCaseIdentification caseIdentification = parseLines(Collections.singletonList(line), PsseCaseIdentification.class).get(0);
//        PsseRawData rawData = new PsseRawData(caseIdentification);

//        PsseDataType dataType = null;
        List<String> lines = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
//            if (line.startsWith("BUS DATA FOLLOWS")) {
//                dataType = PsseDataType.BUS;
//            } else if (line.startsWith("BRANCH DATA FOLLOWS")) {
//                dataType = PsseDataType.BRANCH;
//            } else if (line.startsWith("LOSS ZONES FOLLOWS")) {
//                dataType = PsseDataType.LOSS_ZONES;
//            } else if (line.startsWith("INTERCHANGE DATA FOLLOWS")) {
//                dataType = PsseDataType.INTERCHANGE_DATA;
//            } else if (line.startsWith("TIE LINES FOLLOWS ")) {
//                dataType = PsseDataType.TIE_LINES;
//            } else if (line.startsWith("-9")) {
//                if (dataType != null) {
//                    parseLines(lines, model, dataType);
//                    lines.clear();
//                    dataType = null;
//                }
//            } else {
//                if (dataType != null) {
//                    lines.add(line);
//                }
//            }
        }

        return null;
    }

    private static <T> List<T> parseLines(List<String> lines, Class<T> aClass) {
        CsvParserSettings settings = new CsvParserSettings();
        BeanListProcessor<T> processor = new BeanListProcessor<>(aClass);
        settings.setProcessor(processor);
        CsvParser parser = new CsvParser(settings);
        for (String line : lines) {
            System.out.println(line);
            parser.parseLine(line);
        }
        return processor.getBeans();
    }

    public static void main(String[] args) {
        String a = " 0,    100.00, 33, 0, 0, 60.00";
        CsvParserSettings settings = new CsvParserSettings();
        BeanListProcessor<PsseCaseIdentification> processor = new BeanListProcessor<>(PsseCaseIdentification.class);
        settings.setProcessor(processor);
        CsvParser parser = new CsvParser(settings);
        String[] strings = parser.parseLine(a);
    }
//    private void parseLines(List<String> lines, PsseRawData rawData, PsseDataType dataType) {
//        switch (dataType) {
//            case BUS:
//                rawData.getBuses().addAll(parseLines(lines, PsseBus.class));
//                break;
//            case LOAD:
//                rawData.getLoads().addAll(parseLines(lines, PsseLoad.class));
//                break;
//            case FIXED_SHUNT:
//                rawData.getFixedShunts().addAll(parseLines(lines, PsseFixedShunt.class));
//                break;
//            case GENERATOR:
//                rawData.getGenerators().addAll(parseLines(lines, PsseGenerator.class));
//                break;
//            case NON_TRANSFORMER_BRANCH:
//                rawData.getNonTransformerBranches().addAll(parseLines(lines, PsseNonTransformerBranch.class));
//                break;
//            case TRANSFORMER:
//                rawData.getTransformers().addAll(parseLines(lines, PsseTransformer.class));
//                break;
//            default:
//                throw new IllegalStateException("Data type unknown: " + dataType);
//        }
//    }
}
