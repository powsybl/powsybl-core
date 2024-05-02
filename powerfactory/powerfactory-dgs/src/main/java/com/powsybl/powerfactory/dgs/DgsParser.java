/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.dgs;

import com.powsybl.commons.PowsyblException;
import com.powsybl.powerfactory.model.DataAttributeType;
import com.powsybl.powerfactory.model.PowerFactoryException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DgsParser {

    private static final Pattern ATTR_DESCR_PATTERN = Pattern.compile("(.+)\\(([airp]+)(:\\d*)?\\)");
    private static final Pattern QUOTED_TEXT_PATTERN = Pattern.compile("(\"[^\"]*\")");
    private static final DataAttributeType DEFAULT_VECTOR_TYPE = DataAttributeType.INTEGER;
    private static final DataAttributeType DEFAULT_MATRIX_TYPE = DataAttributeType.FLOAT;
    private static final Logger LOGGER = LoggerFactory.getLogger(DgsParser.class);

    public static final String VERSION = "Version";

    public static final String ID = "ID";

    enum DgsVersion {
        V5,
        V6,
        V7
    }

    private static final class ParsingContext {

        private boolean general = false;

        private DgsVersion version;

        private List<ElementData> elementData;

        private boolean decimalSeparatorIsComma = false;

        private double parseDouble(String value) {
            return parseFloat(value);
        }

        private float parseFloat(String value) {
            if (decimalSeparatorIsComma) {
                return Float.parseFloat(value.replace(',', '.'));
            }
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException ex) {
                try {
                    float v = Float.parseFloat(value.replace(',', '.'));
                    decimalSeparatorIsComma = true;
                    return v;
                } catch (NumberFormatException ex2) {
                    throw new PowerFactoryException("Invalid real value [" + value + "]");
                }
            }
        }
    }

    private static void readObjectTableHeader(String trimmedLine, DgsHandler handler, ParsingContext context) {
        String[] fields = trimmedLine.split(";");

        String className = fields[0].substring(2);

        handler.onObjectTableHeader(className);

        context.elementData = new ArrayList<>();

        int index = 1;
        while (index < fields.length) {
            if (isMatrixAttributeHeader(fields, index, fields.length)) {
                index = readMatrixAttributeHeader(fields, index, fields.length, handler, context);
            } else if (isVectorAttributeHeader(fields, index)) {
                index = readVectorAttributeHeader(fields, index, fields.length, handler, context);
            } else {
                readSimpleAttributeHeader(fields, index, handler, context);
            }
            index++;
        }
    }

    private static void readSimpleAttributeHeader(String[] fields, int index, DgsHandler handler, ParsingContext context) {
        AttributeHeader attributeHeader = readAttributeHeader(fields, index);

        if (context.version == DgsVersion.V5 && attributeHeader.attributeName.equals(ID)) {
            context.elementData.add(new IdElementData(index - 1));
        } else {
            handler.onAttributeDescription(attributeHeader.attributeName, attributeHeader.attributeType);
            context.elementData.add(new SimpleElementData(attributeHeader.attributeName, attributeHeader.attributeType, index - 1));
        }
    }

    private static boolean isMatrixAttributeHeader(String[] fields, int index, int fieldsLength) {
        if (index + 1 >= fieldsLength) {
            return false;
        }
        return readAttributeNameHeader(fields, index).contains(":SIZEROW")
            && readAttributeNameHeader(fields, index + 1).contains(":SIZECOL");
    }

    private static int readMatrixAttributeHeader(String[] fields, int initialIndex, int fieldsLength, DgsHandler handler, ParsingContext context) {
        int index = initialIndex;
        AttributeHeader rowAttributeHeader = readAttributeHeader(fields, index);
        String[] rowMatrixFields = splitAndCheckAttributeNameHeader(rowAttributeHeader.attributeName, 2);
        String matrixNameHeader = rowMatrixFields[0];

        // skip next attribute, cols
        index++;

        // Matrix could be empty
        DataAttributeType matrixTypeHeader = DEFAULT_MATRIX_TYPE;
        int matrixRows = 0;
        int matrixCols = 0;

        boolean exitLoop = index + 1 >= fieldsLength;
        while (!exitLoop) {
            AttributeHeader nextAttributeHeader = readAttributeHeader(fields, index + 1);
            String[] nextMatrixFields = splitAttributeNameHeader(nextAttributeHeader.attributeName);

            if (isSplitOk(nextMatrixFields, matrixNameHeader, 3)) {
                matrixTypeHeader = nextAttributeHeader.attributeType;
                matrixRows = Integer.parseInt(nextMatrixFields[1]) + 1;
                matrixCols = Integer.parseInt(nextMatrixFields[2]) + 1;
                index++;
                exitLoop = index + 1 >= fieldsLength;
            } else {
                exitLoop = true;
            }
        }

        handler.onAttributeDescription(matrixNameHeader, getMatrixAttributeType(matrixTypeHeader));
        context.elementData.add(new ElementDataMatrix(matrixNameHeader, getMatrixAttributeType(matrixTypeHeader), initialIndex - 1, matrixRows, matrixCols));
        return index;
    }

    private static boolean isVectorAttributeHeader(String[] fields, int index) {
        return readAttributeNameHeader(fields, index).contains(":SIZEROW");
    }

    private static int readVectorAttributeHeader(String[] fields, int initialIndex, int fieldsLength, DgsHandler handler, ParsingContext context) {
        int index = initialIndex;
        AttributeHeader attributeHeader = readAttributeHeader(fields, index);
        String[] vectorFields = splitAndCheckAttributeNameHeader(attributeHeader.attributeName, 2);
        String vectorNameHeader = vectorFields[0];

        // Vector could be empty
        DataAttributeType vectorTypeHeader = DEFAULT_VECTOR_TYPE;
        int vectorLength = 0;

        boolean exitLoop = index + 1 >= fieldsLength;
        while (!exitLoop) {
            AttributeHeader nextAttributeHeader = readAttributeHeader(fields, index + 1);
            String[] nextVectorFields = splitAttributeNameHeader(nextAttributeHeader.attributeName);

            if (isSplitOk(nextVectorFields, vectorNameHeader, 2)) {
                vectorTypeHeader = nextAttributeHeader.attributeType;
                vectorLength = Integer.parseInt(nextVectorFields[1]) + 1;
                index++;
                exitLoop = index + 1 >= fieldsLength;
            } else {
                exitLoop = true;
            }
        }

        handler.onAttributeDescription(vectorNameHeader, getVectorAttributeType(vectorTypeHeader));
        context.elementData.add(new ElementDataVector(vectorNameHeader, getVectorAttributeType(vectorTypeHeader), initialIndex - 1, vectorLength));
        return index;
    }

    private static AttributeHeader readAttributeHeader(String[] fields, int index) {
        String field = fields[index];

        Matcher matcher = ATTR_DESCR_PATTERN.matcher(field);
        if (!matcher.matches()) {
            throw new PowerFactoryException("Invalid attribute description: '" + field + "'");
        }

        return new AttributeHeader(matcher.group(1), matcher.group(2).charAt(0));
    }

    private static String readAttributeNameHeader(String[] fields, int index) {
        return readAttributeHeader(fields, index).attributeName;
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

    private static void readObjectTableRow(String trimmedLine, DgsHandler handler, ParsingContext context) {
        Objects.requireNonNull(context.elementData);
        String[] fields = splitConsideringQuotedText(trimmedLine);
        for (ElementData elementData : context.elementData) {
            elementData.read(fields, handler, context);
        }
    }

    public void read(Reader reader, DgsHandler handler) {
        Objects.requireNonNull(reader);
        Objects.requireNonNull(handler);
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            ParsingContext context = new ParsingContext();
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
                } else if (context.general) {
                    readGeneralTableRow(trimmedLine, handler, context);
                } else {
                    readObjectTableRow(trimmedLine, handler, context);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void readGeneralTableRow(String trimmedLine, DgsHandler handler, ParsingContext context) {
        String[] fields = splitConsideringQuotedText(trimmedLine);
        if (fields.length < 3) {
            throw new PowsyblException(String.format("Not enough fields in the line: '%s'", trimmedLine));
        }
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
                throw new IllegalStateException("Unexpected vector attribute type: " + attributeType);
        }
        return type;
    }

    private static DataAttributeType getMatrixAttributeType(DataAttributeType attributeType) {
        if (attributeType == DataAttributeType.FLOAT) {
            return DataAttributeType.DOUBLE_MATRIX;
        } else {
            throw new IllegalStateException("Unexpected matrix attribute type: " + attributeType);
        }
    }

    private static final class AttributeHeader {
        private final String attributeName;
        private final DataAttributeType attributeType;

        private AttributeHeader(String attributeName, char attributeType) {
            this.attributeName = attributeName;
            this.attributeType = getDataAttributeType(attributeType);
        }

        private static DataAttributeType getDataAttributeType(char attributeType) {
            DataAttributeType type;
            switch (attributeType) {
                case 'a':
                    type = DataAttributeType.STRING;
                    break;
                case 'i':
                    type = DataAttributeType.INTEGER;
                    break;
                case 'r':
                    type = DataAttributeType.FLOAT;
                    break;
                case 'p':
                    type = DataAttributeType.OBJECT;
                    break;
                default:
                    throw new IllegalStateException("Unexpected attribute type: " + attributeType);
            }
            return type;
        }
    }

    private interface ElementData {
        void read(String[] fields, DgsHandler handler, ParsingContext context);
    }

    private abstract static class AbstractElementData implements ElementData {

        protected final String attributeName;
        protected final DataAttributeType attributeType;
        protected final int indexField;

        protected AbstractElementData(String attributeName, DataAttributeType attributeType, int indexField) {
            this.attributeName = attributeName;
            this.attributeType = attributeType;
            this.indexField = indexField;
        }

        protected static boolean isValidField(String[] fields, int index) {
            return fields.length > index && !fields[index].isEmpty();
        }
    }

    private static final class IdElementData implements ElementData {

        private final int index;

        private IdElementData(int index) {
            this.index = index;
        }

        @Override
        public void read(String[] fields, DgsHandler handler, ParsingContext context) {
            handler.onID(Long.parseLong(fields[index]));
        }
    }

    private static final class SimpleElementData extends AbstractElementData {

        private SimpleElementData(String attributeName, DataAttributeType attributeType, int indexField) {
            super(attributeName, attributeType, indexField);
        }

        public void read(String[] fields, DgsHandler handler, ParsingContext context) {
            switch (attributeType) {
                case STRING:
                    read(fields, Function.identity(), handler::onStringValue);
                    break;
                case INTEGER:
                    read(fields, Integer::parseInt, handler::onIntegerValue);
                    break;
                case FLOAT:
                    read(fields, context::parseFloat, handler::onRealValue);
                    break;
                case OBJECT:
                    read(fields, Long::parseLong, handler::onObjectValue);
                    break;
                default:
                    throw new PowerFactoryException("Unexpected attribute type:" + attributeType);
            }
        }

        private <T> void read(String[] fields, Function<String, T> parser, BiConsumer<String, T> onValue) {
            if (isValidField(fields, indexField)) {
                String value = fields[indexField];
                onValue.accept(attributeName, parser.apply(value));
            }
        }
    }

    private static final class ElementDataVector extends AbstractElementData {
        private final int length;

        private ElementDataVector(String attributeName, DataAttributeType attributeType, int indexField, int length) {
            super(attributeName, attributeType, indexField);
            this.length = length;
        }

        @Override
        public void read(String[] fields, DgsHandler handler, ParsingContext context) {
            switch (attributeType) {
                case STRING_VECTOR:
                    readVector(fields, Function.identity(), handler::onStringVectorValue);
                    break;
                case INTEGER_VECTOR:
                    readVector(fields, Integer::parseInt, handler::onIntVectorValue);
                    break;
                case DOUBLE_VECTOR:
                    readVector(fields, context::parseDouble, handler::onDoubleVectorValue);
                    break;
                case OBJECT_VECTOR:
                    // Read object numbers as long integers
                    readVector(fields, Long::parseLong, handler::onObjectVectorValue);
                    break;
                default:
                    throw new PowerFactoryException("Unexpected vector attribute type:" + attributeType);
            }
        }

        private <T> void readVector(String[] fields, Function<String, T> parserFunction, BiConsumer<String, List<T>> onValue) {
            read(fields, parserFunction).ifPresent(v -> onValue.accept(attributeName, v));
        }

        private <T> Optional<List<T>> read(String[] fields, Function<String, T> parserFunction) {
            Objects.requireNonNull(fields);
            List<T> values = new ArrayList<>();
            int actualLength = Integer.parseInt(fields[indexField]);
            if (actualLength > this.length) {
                throw new PowerFactoryException("VectorArray: Unexpected length: '" + attributeName +
                    "' length: " + actualLength + ", expected length: " + this.length);
            }
            if (actualLength == 0) {
                return Optional.empty();
            }
            for (int i = 0; i < actualLength; i++) {
                if (isValidField(fields, indexField + 1 + i)) {
                    values.add(parserFunction.apply(fields[indexField + 1 + i]));
                } else {
                    return Optional.empty();
                }
            }
            return Optional.of(values);
        }
    }

    private static final class ElementDataMatrix extends AbstractElementData {
        private final int rows;
        private final int cols;

        private ElementDataMatrix(String attributeName, DataAttributeType attributeType, int indexField, int rows, int cols) {
            super(attributeName, attributeType, indexField);
            this.rows = rows;
            this.cols = cols;
        }

        @Override
        public void read(String[] fields, DgsHandler handler, ParsingContext context) {
            read(fields, context).ifPresent(m -> handler.onDoubleMatrixValue(attributeName, m));
        }

        private Optional<RealMatrix> read(String[] fields, ParsingContext context) {
            int actualRows = Integer.parseInt(fields[indexField]);
            int actualCols = Integer.parseInt(fields[indexField + 1]);
            if (actualRows == 0 || actualCols == 0) {
                return Optional.empty();
            }
            if (actualRows != this.rows) {
                LOGGER.debug("RealMatrix: actual rows {} different than expected {}. All actual rows will be read.", actualRows, this.rows);
            }
            if (actualCols != this.cols) {
                throw new PowerFactoryException("RealMatrix: Unexpected number of cols: '"
                    + attributeName + "' rows: " + actualRows + " cols: " + actualCols + " expected cols: " + this.cols);
            }
            RealMatrix realMatrix = new BlockRealMatrix(actualRows, actualCols);
            for (int i = 0; i < actualRows; i++) {
                for (int j = 0; j < actualCols; j++) {
                    if (isValidField(fields, indexField + 2 + i * actualCols + j)) {
                        double value = context.parseDouble(fields[indexField + 2 + i * actualCols + j]);
                        realMatrix.setEntry(i, j, value);
                    } else {
                        return Optional.empty();
                    }
                }
            }
            return Optional.of(realMatrix);
        }
    }
}
