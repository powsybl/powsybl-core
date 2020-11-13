/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseException;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public abstract class AbstractDataBlock<T> {

    private final PsseDataBlock dataBlock;

    AbstractDataBlock(PsseDataBlock dataBlock) {
        this.dataBlock = dataBlock;
    }

    public abstract String[] fieldNames(PsseVersion version);

    public abstract Class<? extends T> psseTypeClass(PsseVersion version);

    public List<T> read(BufferedReader reader, PsseContext context) throws IOException {
        // XXX(Luma) data blocks in RAW format have a fixed order for columns
        // Optional columns may appear at the end of each record
        // We obtain the maximum number of columns in each record of the data block
        // This will be the number of "actual columns" used in the data block
        // We store these columns in the context
        // For parsing records we use all the field names defined in the data block

        String[] allFieldNames = fieldNames(context.getVersion());
        List<String> records = Util.readRecordBlock(reader);
        //String[] actualFieldNames = readActualFieldNames(records, allFieldNames, context.getDelimiter());

        //context.setFieldNames(dataBlock, actualFieldNames);
        int[] maxColumns = new int[1];
        List<T> psseObjects = parseRecords(records, psseTypeClass(context.getVersion()), allFieldNames, maxColumns);
        String[] actualFieldNames = ArrayUtils.subarray(allFieldNames, 0, maxColumns[0]);
        context.setFieldNames(dataBlock, actualFieldNames);
        return psseObjects;
    }

    public List<T> readx(JsonNode networkNode, PsseContext context) {
        JsonNode jsonNode = networkNode.get(dataBlock.getRawxNodeName());
        if (jsonNode == null) {
            return new ArrayList<>();
        }

        // XXX(Luma) data blocks in RAW format have arbitrary order for columns
        // Columns present in the data block are defined explicitly in a header
        // Order and number of columns is relevant,
        // We have to use the field names obtained from the header to parse the records

        String[] actualFieldNames = Util.nodeFieldNames(jsonNode);
        List<String> records = Util.nodeRecords(jsonNode);

        context.setFieldNames(dataBlock, actualFieldNames);
        return parseRecords(records, psseTypeClass(context.getVersion()), actualFieldNames);
    }

    PsseDataBlock getDataBlock() {
        return dataBlock;
    }

    T parseRecordHeader(String record, Class<? extends T> aClass, String[] headers) {
        List<T> beans = parseRecords(Collections.singletonList(record), aClass, headers);
        return beans.get(0);
    }

    List<T> parseRecords(List<String> records, Class<? extends T> aClass, String[] headers) {
        return parseRecords(records, aClass, headers, new int[1]);
    }

    List<T> parseRecords(List<String> records, Class<? extends T> aClass, String[] headers, int[] maxColumns) {
        CsvParserSettings settings = Util.createCsvParserSettings();
        settings.setHeaders(headers);
        BeanListProcessor<? extends T> processor = new BeanListProcessor<>(aClass);
        settings.setProcessor(processor);
        CsvParser parser = new CsvParser(settings);
        for (String record : records) {
            String[] fields = parser.parseLine(record);
            maxColumns[0] = Math.max(maxColumns[0], fields.length);
        }
        List<? extends T> beans = processor.getBeans();
        if (beans.size() != records.size()) {
            throw new PsseException("Parsing error");
        }
        return (List<T>) beans;
    }

    public enum PsseDataBlock {
        CASE_IDENTIFICATION_DATA("caseid"),
        BUS_DATA("bus"),
        LOAD_DATA("load"),
        FIXED_BUS_SHUNT_DATA("fixshunt"),
        GENERATOR_DATA("generator"),
        NON_TRANSFORMER_BRANCH_DATA("acline"),
        TRANSFORMER_DATA("transformer"),
        // XXX(Luma) do we really need to split transformers in 2 and 3 winding ?
        // XXX(Luma) these are not real data blocks in the PSS/E input
        TRANSFORMER_2_DATA("transformer2"),
        TRANSFORMER_3_DATA("transformer3"),
        AREA_INTERCHANGE_DATA("area"),
        TWO_TERMINAL_DC_TRANSMISSION_LINE_DATA("XXX"),
        VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_DATA("XXX"),
        TRANSFORMER_IMPEDANCE_CORRECTION_TABLES("XXX"),
        MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA("XXX"),
        MULTI_SECTION_LINE_GROUPING_DATA("XXX"),
        ZONE_DATA("zone"),
        INTERAREA_TRANSFER_DATA("XXX"),
        OWNER_DATA("owner"),
        FACTS_DEVICE_DATA("XXX"),
        SWITCHED_SHUNT_DATA("swshunt"),
        GNE_DEVICE_DATA("XXX"),
        INDUCTION_MACHINE_DATA("XXX"),
        Q_RECORD("XXX");

        private final String rawxNodeName;

        private PsseDataBlock(String rawxNodeName) {
            this.rawxNodeName = rawxNodeName;
        }

        public String getRawxNodeName() {
            return rawxNodeName;
        }
    }
}
