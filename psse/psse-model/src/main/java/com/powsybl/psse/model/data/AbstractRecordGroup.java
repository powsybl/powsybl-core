/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
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
public abstract class AbstractRecordGroup<T> {

    private final PsseRecordGroup recordGroup;

    AbstractRecordGroup(PsseRecordGroup recordGroup) {
        this.recordGroup = recordGroup;
    }

    public abstract String[] fieldNames(PsseVersion version);

    public abstract Class<? extends T> psseTypeClass(PsseVersion version);

    public List<T> read(BufferedReader reader, Context context) throws IOException {
        // Record groups in RAW format have a fixed order for fields
        // Optional fields may not be present at the end of each record.
        // We obtain the maximum number of fields read in each record of the record group.
        // This will be the number of "actual fields" recorded for the record group.
        // We store the "actual" field names in the context for potential later use.
        // For parsing records we use all the field names defined for the record group.

        String[] allFieldNames = fieldNames(context.getVersion());
        List<String> records = Util.readRecords(reader);
        List<T> psseObjects = parseRecords(records, allFieldNames, context);
        String[] actualFieldNames = ArrayUtils.subarray(allFieldNames, 0, context.getCurrentRecordGroupMaxNumFields());
        context.setFieldNames(recordGroup, actualFieldNames);
        return psseObjects;
    }

    public List<T> read(JsonNode networkNode, Context context) {
        // Records in RAWX format have arbitrary order for fields.
        // Fields present in the record group are defined explicitly in a header.
        // Order and number of field names is relevant for parsing,
        // the field names must be taken from the explicit header defined in the file.
        // We store the "actual" field names in the context for potential later use.

        JsonNode jsonNode = networkNode.get(recordGroup.getRawxNodeName());
        if (jsonNode == null) {
            return new ArrayList<>();
        }
        String[] actualFieldNames = Util.readFieldNames(jsonNode);
        List<String> records = Util.readRecords(jsonNode);
        context.setFieldNames(recordGroup, actualFieldNames);
        return parseRecords(records, actualFieldNames, context);
    }

    PsseRecordGroup getRecordGroup() {
        return recordGroup;
    }

    T parseSingleRecord(String record, String[] headers, Context context) {
        return parseRecords(Collections.singletonList(record), headers, context).get(0);
    }

    List<T> parseRecords(List<String> records, String[] headers, Context context) {
        int expectedCount = records.size();
        BeanListProcessor<? extends T> processor = new BeanListProcessor<>(psseTypeClass(context.getVersion()), expectedCount);
        CsvParserSettings settings = context.getCsvParserSettings();
        settings.setHeaders(headers);
        settings.setProcessor(processor);
        CsvParser parser = new CsvParser(settings);
        context.resetCurrentRecordGroup();
        for (String record : records) {
            String[] fields = parser.parseLine(record);
            context.setCurrentRecordNumFields(fields.length);
        }
        List<? extends T> beans = processor.getBeans();
        if (beans.size() != expectedCount) {
            throw new PsseException("Parsing error");
        }
        return (List<T>) beans;
    }

    public enum PsseRecordGroup {
        CASE_IDENTIFICATION_DATA("caseid"),
        SYSTEM_WIDE_DATA("?"),
        BUS_DATA("bus"),
        LOAD_DATA("load"),
        FIXED_BUS_SHUNT_DATA("fixshunt"),
        GENERATOR_DATA("generator"),
        NON_TRANSFORMER_BRANCH_DATA("acline"),
        SYSTEM_SWITCHING_DEVICE_DATA("sysswd"),
        TRANSFORMER_DATA("transformer"),
        // Transformer record group includes two and three winding transformers
        TRANSFORMER_2_DATA("transformer2"),
        TRANSFORMER_3_DATA("transformer3"),
        AREA_INTERCHANGE_DATA("area"),
        TWO_TERMINAL_DC_TRANSMISSION_LINE_DATA("twotermdc"),
        VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_DATA("vscdc"),
        TRANSFORMER_IMPEDANCE_CORRECTION_TABLES("impcor"),
        MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA("ntermdc"),
        MULTI_SECTION_LINE_GROUPING_DATA("msline"),
        ZONE_DATA("zone"),
        INTERAREA_TRANSFER_DATA("iatransfer"),
        OWNER_DATA("owner"),
        FACTS_DEVICE_DATA("facts"),
        SWITCHED_SHUNT_DATA("swshunt"),
        GNE_DEVICE_DATA("gne"),
        INDUCTION_MACHINE_DATA("indmach"),
        SUBSTATION_DATA("sub");

        private final String rawxNodeName;

        private PsseRecordGroup(String rawxNodeName) {
            this.rawxNodeName = rawxNodeName;
        }

        public String getRawxNodeName() {
            return rawxNodeName;
        }
    }
}
