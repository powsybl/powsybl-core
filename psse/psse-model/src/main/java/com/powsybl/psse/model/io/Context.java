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
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.IntStream;

import static com.powsybl.psse.model.io.FileFormat.LEGACY_TEXT;
import static com.powsybl.psse.model.io.FileFormat.VALID_DELIMITERS;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class Context {

    private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);
    public static final String VALID_QUOTES = "'\"";

    private final Map<String, String[]> fieldNames = new HashMap<>();

    private FileFormat fileFormat = LEGACY_TEXT;
    private char delimiter = LEGACY_TEXT.getDefaultDelimiter();
    private char quote = LEGACY_TEXT.getQuote();
    private PsseVersion version;
    private int currentRecordGroupMaxNumFields;
    private JsonGenerator jsonGenerator;
    private JsonNode networkNode;

    public Context() {
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

    public char getQuote() {
        return quote;
    }

    private Context setQuote(char quote) {
        this.quote = quote;
        return this;
    }

    /**
     * Test each possible delimiter and quote to find the one that gives the most consistent field count
     * @param rec the line of text to use for the detection
     */
    public void detectDelimiter(String rec) {
        char bestDelimiter = '\0';
        char bestQuote = '\0';
        int maxFieldCount = 0;
        long maxQuoteCount = 0;

        for (char testDelimiter : VALID_DELIMITERS.toCharArray()) {
            for (char testQuote : VALID_QUOTES.toCharArray()) {
                long quoteCount = IntStream.range(0, rec.length())
                    .filter(i -> rec.charAt(i) == testQuote
                        && (i == 0 || i == rec.length() - 1 || rec.charAt(i - 1) == testDelimiter || rec.charAt(i + 1) == testDelimiter))
                    .count();
                int fieldCount = countFields(rec, testDelimiter, testQuote);

                // Save as the best delimiter and quote if both conditions are met:
                // - there is an even and higher number of quotes
                // - either the number of quotes is higher or the number of quotes is the same and the field count is higher
                if (quoteCount % 2 == 0
                    && (quoteCount > maxQuoteCount || quoteCount == maxQuoteCount && fieldCount > maxFieldCount)) {
                    maxFieldCount = fieldCount;
                    maxQuoteCount = quoteCount;
                    bestDelimiter = testDelimiter;
                    bestQuote = testQuote;
                }
            }
        }

        if (bestDelimiter == '\0' || bestQuote == '\0') {
            throw new PsseException(String.format("Unable to detect delimiter and/or quote for record, using %s", rec));
        }

        setDelimiter(bestDelimiter);
        setQuote(bestQuote);
    }

    public CsvReader.CsvReaderBuilder createCsvReaderBuilder() {
        return CsvReader.builder()
            .fieldSeparator(delimiter)
            .quoteCharacter(quote);
    }

    public Context setFieldNames(RecordGroupIdentification recordGroup, String[] fieldNames) {
        this.fieldNames.put(recordGroup.getUniqueName(), fieldNames);
        return this;
    }

    public String[] getFieldNames(RecordGroupIdentification recordGroup) {
        return fieldNames.get(recordGroup.getUniqueName());
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

    public JsonNode getNetworkNode() {
        return networkNode;
    }

    public Context setNetworkNode(JsonNode networkNode) {
        this.networkNode = networkNode;
        return this;
    }

    private int countFields(String rec, char delimiter, char quote) {
        try (CsvReader<CsvRecord> csvReader = CsvReader.builder()
            .fieldSeparator(delimiter)
            .quoteCharacter(quote)
            .ofCsvRecord(rec)) {

            Iterator<CsvRecord> rows = csvReader.iterator();
            CsvRecord row = rows.next();
            if (row != null) {
                return row.getFieldCount();
            }
        } catch (Exception e) {
            LOGGER.debug("Error parsing with delimiter '{}' and quote '{}': {}", delimiter, quote, e.getMessage());
        }
        return 0;
    }
}
