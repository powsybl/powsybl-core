/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.data.AbstractRecordGroup.PsseRecordGroup;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
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

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private Util() {
    }

    static void readDiscardedRecordGroup(PsseRecordGroup recordGroup, BufferedReader reader) throws IOException {
        LOG.info("read discarded record group {}", recordGroup);
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
    static String cleanRawxFieldNames(String data) {
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
    static String cleanRawxRecord(String data) {
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

    // num fields raw format, quote is always '
    static int numFieldsRawFileFormat(String data, String delimiter) {
        int fields = 0;
        Matcher m = Pattern.compile("([^\']+)|(\'([^\']*)\')").matcher(data);

        while (m.find()) {
            if (m.group().contains("'")) {
                fields++;
            } else {
                for (String field : m.group().split(delimiter)) {
                    if (!field.equals("")) {
                        fields++;
                    }
                }
            }
        }
        return fields;
    }

    static String[] readFieldNames(JsonNode jsonNode) {
        String fieldsNode = jsonNode.get("fields").toString();
        String fieldsNodeClean = cleanRawxFieldNames(fieldsNode.substring(1, fieldsNode.length() - 1));
        return fieldsNodeClean.split(",");
    }

    static List<String> readRecords(JsonNode jsonNode) {
        String dataNode = jsonNode.get("data").toString();
        String dataNodeClean = cleanRawxRecord(dataNode.substring(1, dataNode.length() - 1));

        String[] dataNodeArray = StringUtils.substringsBetween(dataNodeClean, "[", "]");
        List<String> records = new ArrayList<>();
        if (dataNodeArray == null) {
            records.add(dataNodeClean);
        } else {
            records.addAll(Arrays.asList(dataNodeArray));
        }
        return records;
    }

    static void writeEndOfBlock(OutputStream outputStream) {
        writeString("0", outputStream);
    }

    static void writeEndOfBlockAndComment(String comment, OutputStream outputStream) {
        CsvWriter writer = new CsvWriter(outputStream, new CsvWriterSettings());
        writer.writeRow("0" + " / " + comment);
        writer.flush();
    }

    static void writeQrecord(OutputStream outputStream) {
        writeString("Q", outputStream);
    }

    static void writeListString(List<String> records, OutputStream outputStream) {
        CsvWriter writer = new CsvWriter(outputStream, new CsvWriterSettings());
        records.forEach(writer::writeRow);
        writer.flush();
    }

    static void writeString(String line, OutputStream outputStream) {
        CsvWriter writer = new CsvWriter(outputStream, new CsvWriterSettings());
        writer.writeRow(line);
        writer.flush();
    }

    static String writeJsonModel(JsonModel jsonModel) throws IOException {
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        return objectMapper.writer(prettyPrinter).writeValueAsString(jsonModel);
    }

    static String[] insideHeaders(String[] quoteFields, String[] headers) {
        String[] quoteFieldsInside = new String[] {};
        for (int i = 0; i < quoteFields.length; i++) {
            if (ArrayUtils.contains(headers, quoteFields[i])) {
                quoteFieldsInside = ArrayUtils.add(quoteFieldsInside, quoteFields[i]);
            }
        }
        return quoteFieldsInside;
    }

    static String[] excludeFields(String[] initialFields, String[] excludedFields) {
        String[] fields = new String[] {};
        for (int i = 0; i < initialFields.length; i++) {
            if (!ArrayUtils.contains(excludedFields, initialFields[i])) {
                fields = ArrayUtils.add(fields, initialFields[i]);
            }
        }
        return fields;
    }
}
