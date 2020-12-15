/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.io;

import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.pf.io.PowerFlowRecordGroup;
import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.RetryableErrorHandler;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.powsybl.psse.model.io.FileFormat.JSON;
import static com.powsybl.psse.model.io.FileFormat.LEGACY_TEXT;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class Context {

    private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);

    private final Map<String, String[]> fieldNames = new HashMap<>();
    private final CsvParserSettings csvParserSettings;
    private String delimiter;
    private PsseVersion version;
    private FileFormat fileFormat = LEGACY_TEXT;
    private int currentRecordGroupMaxNumFields;

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

    public Context setFileFormat(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
        return this;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void detectDelimiter(String record) {
        // The order of delimiters is relevant
        csvParserSettings.setDelimiterDetectionEnabled(true, ',', ' ');
        CsvParser parser = new CsvParser(csvParserSettings);
        parser.parseLine(record);
        this.delimiter = parser.getDetectedFormat().getDelimiterString();

        csvParserSettings.getFormat().setDelimiter(this.delimiter);
        if (fileFormat.equals(JSON)) {
            csvParserSettings.getFormat().setQuote('"');
        } else {
            csvParserSettings.getFormat().setQuote('\'');
        }
        csvParserSettings.setDelimiterDetectionEnabled(false);
        csvParserSettings.setQuoteDetectionEnabled(false);
    }

    public void setFieldNames(RecordGroupIdentification recordGroup, String[] fieldNames) {
        this.fieldNames.put(recordGroup.getUniqueName(), fieldNames);
    }

    public String[] getFieldNames(RecordGroupIdentification recordGroup) {
        return fieldNames.get(recordGroup.getUniqueName());
    }

    public CsvParserSettings getCsvParserSettings() {
        return csvParserSettings;
    }

    // XXX(Luma) remove these methods from here
    public boolean is3wTransformerDataReadFieldsEmpty() {
        return fieldNames.get(PowerFlowRecordGroup.TRANSFORMER_3) == null;
    }

    public boolean is2wTransformerDataReadFieldsEmpty() {
        return fieldNames.get(PowerFlowRecordGroup.TRANSFORMER_2) == null;
    }

    public void resetCurrentRecordGroup() {
        currentRecordGroupMaxNumFields = 0;
    }

    public int getCurrentRecordGroupMaxNumFields() {
        return currentRecordGroupMaxNumFields;
    }

    public void setCurrentRecordNumFields(int numFields) {
        currentRecordGroupMaxNumFields = Math.max(currentRecordGroupMaxNumFields, numFields);
    }

}
