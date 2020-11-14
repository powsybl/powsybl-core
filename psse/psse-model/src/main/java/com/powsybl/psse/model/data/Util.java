/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseException;
import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.RetryableErrorHandler;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
final class Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    private Util() {
    }

    static String detectDelimiter(String record) {
        CsvParserSettings settings = createCsvParserSettings();
        CsvParser parser = new CsvParser(settings);
        parser.parseLine(record);
        return parser.getDetectedFormat().getDelimiterString();
    }

    static final char[] DELIMITERS = {',', ' '};

    static CsvParserSettings createCsvParserSettings() {
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

    static void readDiscardedRecordGroup(BufferedReader reader) throws IOException {
        String firstToken;
        do {
            String line = reader.readLine();
            if (line == null) {
                throw new PsseException("Unexpected end of file");
            }
            String[] tokens = line.split("[, ]");
            if (tokens.length < 1) {
                throw new PsseException("Malformed line: " + line);
            }
            firstToken = tokens[0];
        }
        while (!firstToken.equals("0"));
    }

    static List<String> readRecords(BufferedReader reader) throws IOException {
        List<String> records = new ArrayList<>();
        String line = readLineAndRemoveComment(reader);
        while (!line.trim().equals("0")) {
            records.add(line);
            line = readLineAndRemoveComment(reader);
        }
        return records;
    }

    static String removeComment(String line) {
        int slashIndex = line.indexOf('/');
        if (slashIndex == -1) {
            return line;
        }
        return line.substring(0, slashIndex);
    }

    // '' Is allowed as a comment
    static String readLineAndRemoveComment(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new PsseException("PSSE. Unexpected end of file");
        }
        StringBuffer newLine = new StringBuffer();
        Pattern p = Pattern.compile("('[^']*')|( )+");
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

    // "" Is not allowed as a field
    static String cleanRawxFieldString(String data) {
        StringBuffer newData = new StringBuffer();
        Pattern p = Pattern.compile("(\"[^\"]+\")|( )+");
        Matcher m = p.matcher(data);
        while (m.find()) {
            if (m.group().contains("\"")) {
                m.appendReplacement(newData, m.group().replace("\"", ""));
            } else {
                m.appendReplacement(newData, "");
            }
        }
        m.appendTail(newData);
        return newData.toString();
    }

    // "" Is allowed as data
    static String cleanRawxDataString(String data) {
        StringBuffer newData = new StringBuffer();
        Pattern p = Pattern.compile("(\"[^\"]*\")|( )+");
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

    static String[] nodeFieldNames(JsonNode jsonNode) {
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
}
