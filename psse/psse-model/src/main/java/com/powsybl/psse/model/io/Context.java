/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseVersion;
import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.RetryableErrorHandler;
import com.univocity.parsers.csv.CsvParserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.powsybl.psse.model.io.FileFormat.LEGACY_TEXT;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class Context {

    private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);

    private final Map<String, String[]> fieldNames = new HashMap<>();
    private final CsvParserSettings csvParserSettings;

    private FileFormat fileFormat = LEGACY_TEXT;
    private char delimiter = LEGACY_TEXT.getDefaultDelimiter();
    private PsseVersion version;
    private int currentRecordGroupMaxNumFields;
    private JsonGenerator jsonGenerator;
    private JsonNode networkNode;

    public Context() {
        csvParserSettings = new CsvParserSettings();
        csvParserSettings.setHeaderExtractionEnabled(false);
        csvParserSettings.setQuoteDetectionEnabled(true);
        csvParserSettings.setProcessorErrorHandler(new RetryableErrorHandler<ParsingContext>() {
            @Override
            public void handleError(DataProcessingException error, Object[] inputRow, ParsingContext context) {
                LOGGER.error("Parsing context {}", context, error);
            }
        });
    }

    public PsseVersion getVersion() {
        return version;
    }

    public Context setVersion(PsseVersion version) {
        this.version = version;
        return this;
    }

    public FileFormat getFileFormat() {
        return fileFormat;
    }

    public Context setFileFormat(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
        return this;
    }

    public char getDelimiter() {
        return delimiter;
    }

    private Context setDelimiter(char delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public void detectDelimiter(String record) {
        // The order of delimiters is relevant
        // We pass the delimiters as an array of chars (it will be modified by the parser)

        int slashIndex = record.indexOf("/");
        String recordWithoutComment = slashIndex > 0 ? record.substring(0, +slashIndex) : record;
        char delimiter = recordWithoutComment.contains(",") ? ',' : ' ';
        setDelimiter(delimiter);
        csvParserSettings.getFormat().setQuote(getFileFormat().getQuote());
        csvParserSettings.setDelimiterDetectionEnabled(false);
        csvParserSettings.setQuoteDetectionEnabled(false);
    }

    public Context setFieldNames(RecordGroupIdentification recordGroup, String[] fieldNames) {
        this.fieldNames.put(recordGroup.getUniqueName(), fieldNames);
        return this;
    }

    public String[] getFieldNames(RecordGroupIdentification recordGroup) {
        return fieldNames.get(recordGroup.getUniqueName());
    }

    CsvParserSettings getCsvParserSettings() {
        return csvParserSettings;
    }

    void resetCurrentRecordGroup() {
        currentRecordGroupMaxNumFields = 0;
    }

    public int getCurrentRecordGroupMaxNumFields() {
        return currentRecordGroupMaxNumFields;
    }

    Context setCurrentRecordNumFields(int numFields) {
        currentRecordGroupMaxNumFields = Math.max(currentRecordGroupMaxNumFields, numFields);
        return this;
    }

    JsonGenerator getJsonGenerator() {
        return jsonGenerator;
    }

    public Context setJsonGenerator(JsonGenerator jsonGenerator) {
        this.jsonGenerator = jsonGenerator;
        return this;
    }

    JsonNode getNetworkNode() {
        return networkNode;
    }

    public Context setNetworkNode(JsonNode networkNode) {
        this.networkNode = networkNode;
        return this;
    }
}
