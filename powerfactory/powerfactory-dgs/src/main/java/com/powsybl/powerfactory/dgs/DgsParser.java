/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.dgs;

import com.powsybl.powerfactory.model.DataAttributeType;
import com.powsybl.powerfactory.model.PowerFactoryException;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DgsParser {

    private static final Pattern QUOTED_TEXT_PATTERN = Pattern.compile("(\"[^\"]*\")");
    private static final DataAttributeType DEFAULT_VECTOR_TYPE = DataAttributeType.INTEGER;
    private static final DataAttributeType DEFAULT_MATRIX_TYPE = DataAttributeType.FLOAT;

    public static final String VERSION = "Version";

    public static final String ID = "ID";

    private static void readObjectTableHeader(String trimmedLine, DgsHandler handler, DgsParsingContext context) {
        String[] fields = trimmedLine.split(";");

        String className = fields[0].substring(2);

        List<DgsAttribute> attributes = Arrays.stream(fields)
                .skip(1)
                .map(DgsAttribute::parse)
                .collect(Collectors.toList());

        handler.onObjectTableHeader(className);

        context.valueParsers = new ArrayList<>();

        int index = 1;
        while (index < fields.length) {
            if (isMatrixAttributeHeader(attributes, index, fields.length)) {
                index = readMatrixAttributeHeader(attributes, index, fields.length, handler, context);
            } else if (isVectorAttributeHeader(attributes, index)) {
                index = readVectorAttributeHeader(attributes, index, fields.length, handler, context);
            } else {
                readSimpleAttributeHeader(attributes, index, handler, context);
            }
            index++;
        }
    }

    private static void readSimpleAttributeHeader(List<DgsAttribute> attributes, int index, DgsHandler handler, DgsParsingContext context) {
        DgsAttribute attribute = attributes.get(index - 1);

        if (context.version == DgsVersion.V5 && attribute.getName().equals(ID)) {
            context.valueParsers.add(new DgsIdValueParser(index - 1));
        } else {
            handler.onAttributeDescription(attribute.getName(), attribute.getType());
            context.valueParsers.add(new DgsSimpleValueParser(attribute.getName(), attribute.getType(), index - 1));
        }
    }

    private static boolean isMatrixAttributeHeader(List<DgsAttribute> attributes, int index, int fieldsLength) {
        if (index + 1 >= fieldsLength) {
            return false;
        }
        return attributes.get(index - 1).getName().contains(":SIZEROW")
            && attributes.get(index).getName().contains(":SIZECOL");
    }

    private static int readMatrixAttributeHeader(List<DgsAttribute> attributes, int initialIndex, int fieldsLength, DgsHandler handler, DgsParsingContext context) {
        int index = initialIndex;
        DgsAttribute rowAttribute = attributes.get(index - 1);
        String[] rowMatrixFields = splitAndCheckAttributeNameHeader(rowAttribute.getName(), 2);
        String matrixNameHeader = rowMatrixFields[0];

        // skip next attribute, cols
        index++;

        // Matrix could be empty
        DataAttributeType matrixTypeHeader = DEFAULT_MATRIX_TYPE;
        int matrixRows = 0;
        int matrixCols = 0;

        boolean exitLoop = index + 1 >= fieldsLength;
        while (!exitLoop) {
            DgsAttribute nextAttribute = attributes.get(index);
            String[] nextMatrixFields = splitAttributeNameHeader(nextAttribute.getName());

            if (isSplitOk(nextMatrixFields, matrixNameHeader, 3)) {
                matrixTypeHeader = nextAttribute.getType();
                matrixRows = Integer.parseInt(nextMatrixFields[1]) + 1;
                matrixCols = Integer.parseInt(nextMatrixFields[2]) + 1;
                index++;
                exitLoop = index + 1 >= fieldsLength;
            } else {
                exitLoop = true;
            }
        }

        handler.onAttributeDescription(matrixNameHeader, getMatrixAttributeType(matrixTypeHeader));
        context.valueParsers.add(new DgsMatrixValueParser(matrixNameHeader, getMatrixAttributeType(matrixTypeHeader), initialIndex - 1, matrixRows, matrixCols));
        return index;
    }

    private static boolean isVectorAttributeHeader(List<DgsAttribute> attributes, int index) {
        return attributes.get(index - 1).getName().contains(":SIZEROW");
    }

    private static int readVectorAttributeHeader(List<DgsAttribute> attributes, int initialIndex, int fieldsLength, DgsHandler handler, DgsParsingContext context) {
        int index = initialIndex;
        DgsAttribute attribute = attributes.get(index - 1);
        String[] vectorFields = splitAndCheckAttributeNameHeader(attribute.getName(), 2);
        String vectorNameHeader = vectorFields[0];

        // Vector could be empty
        DataAttributeType vectorTypeHeader = DEFAULT_VECTOR_TYPE;
        int vectorLength = 0;

        boolean exitLoop = index + 1 >= fieldsLength;
        while (!exitLoop) {
            DgsAttribute nextAttributeHeader = attributes.get(index);
            String[] nextVectorFields = splitAttributeNameHeader(nextAttributeHeader.getName());

            if (isSplitOk(nextVectorFields, vectorNameHeader, 2)) {
                vectorTypeHeader = nextAttributeHeader.getType();
                vectorLength = Integer.parseInt(nextVectorFields[1]) + 1;
                index++;
                exitLoop = index + 1 >= fieldsLength;
            } else {
                exitLoop = true;
            }
        }

        handler.onAttributeDescription(vectorNameHeader, getVectorAttributeType(vectorTypeHeader));
        context.valueParsers.add(new DgsVectorValueParser(vectorNameHeader, getVectorAttributeType(vectorTypeHeader), initialIndex - 1, vectorLength));
        return index;
    }

    private static String[] splitAttributeNameHeader(String attributeNameHeader) {
        return attributeNameHeader.split(":");
    }

    private static String[] splitAndCheckAttributeNameHeader(String attributeNameHeader, int expectedLength) {
        String[] fields = splitAttributeNameHeader(attributeNameHeader);
        if (fields.length != expectedLength) {
            throw new PowerFactoryException("Unexpected number of fields in attributeNameHeader: '" + attributeNameHeader + "'");
        }
        return fields;
    }

    private static boolean isSplitOk(String[] fields, String nameHeader, int expectedLength) {
        if (fields.length < 1) {
            return false;
        }
        if (!fields[0].equals(nameHeader)) {
            return false;
        }
        if (fields.length != expectedLength) {
            throw new PowerFactoryException("Unexpected number of fields in attributeNameHeader: '" + nameHeader + "'");
        }
        return true;
    }

    private static void readObjectTableRow(String trimmedLine, DgsHandler handler, DgsParsingContext context) {
        Objects.requireNonNull(context.valueParsers);
        String[] fields = splitConsideringQuotedText(trimmedLine);
        for (DgsValueParser elementData : context.valueParsers) {
            elementData.parse(fields, handler, context);
        }
    }

    public void read(Reader reader, DgsHandler handler) {
        Objects.requireNonNull(reader);
        Objects.requireNonNull(handler);
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            DgsParsingContext context = new DgsParsingContext();
            while ((line = bufferedReader.readLine()) != null) {
                String trimmedLine = line.trim();

                if (trimmedLine.isEmpty() || trimmedLine.startsWith("*")) {
                    continue;
                }

                if (trimmedLine.startsWith("$$")) { // table header
                    if (trimmedLine.startsWith("$$General")) {
                        context.general = true;
                    } else {
                        context.general = false;
                        readObjectTableHeader(trimmedLine, handler, context);
                    }
                } else {
                    if (context.general) {
                        readGeneralTableRow(trimmedLine, handler, context);
                    } else {
                        readObjectTableRow(trimmedLine, handler, context);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void readGeneralTableRow(String trimmedLine, DgsHandler handler, DgsParsingContext context) {
        String[] fields = splitConsideringQuotedText(trimmedLine);
        String descr = fields[1];
        String val = fields[2];
        if (descr.equals(VERSION)) {
            if (val.equals("5.0")) {
                context.version = DgsVersion.V5;
            } else {
                throw new PowerFactoryException("Unsupported DGS ASCII version: " + val);
            }
        }
        handler.onGeneralAttribute(descr, val);
    }

    private static String[] splitConsideringQuotedText(String line) {
        Objects.requireNonNull(line);
        String[] tokens = new String[] {};
        Matcher m = QUOTED_TEXT_PATTERN.matcher(line);
        int start = 0;
        while (m.find()) {
            tokens = ArrayUtils.addAll(tokens, line.substring(start, m.start() - 1).split(";"));
            tokens = ArrayUtils.add(tokens, line.substring(m.start() + 1, m.end() - 1));
            // Skip the delimiter
            start = m.end() + 1;
        }
        if (start < line.length()) {
            tokens = ArrayUtils.addAll(tokens, line.substring(start).split(";"));
        }
        return tokens;
    }

    private static DataAttributeType getVectorAttributeType(DataAttributeType attributeType) {
        DataAttributeType type;
        switch (attributeType) {
            case STRING:
                type = DataAttributeType.STRING_VECTOR;
                break;
            case INTEGER:
                type = DataAttributeType.INTEGER_VECTOR;
                break;
            case FLOAT:
                type = DataAttributeType.DOUBLE_VECTOR;
                break;
            case OBJECT:
                type = DataAttributeType.OBJECT_VECTOR;
                break;
            default:
                throw new AssertionError("Unexpected vector attribute type: " + attributeType);
        }
        return type;
    }

    private static DataAttributeType getMatrixAttributeType(DataAttributeType attributeType) {
        if (attributeType == DataAttributeType.FLOAT) {
            return DataAttributeType.DOUBLE_MATRIX;
        } else {
            throw new AssertionError("Unexpected matrix attribute type: " + attributeType);
        }
    }
}
