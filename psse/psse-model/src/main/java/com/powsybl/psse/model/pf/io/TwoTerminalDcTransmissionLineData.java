/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.*;
import com.powsybl.psse.model.pf.PsseTwoTerminalDcConverter;
import com.powsybl.psse.model.pf.PsseTwoTerminalDcTransmissionLine;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.powsybl.psse.model.PsseVersion.Major.*;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.INTERNAL_TWO_TERMINAL_DC_TRANSMISSION_LINE_CONVERTER;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.TWO_TERMINAL_DC_TRANSMISSION_LINE;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TwoTerminalDcTransmissionLineData extends AbstractRecordGroup<PsseTwoTerminalDcTransmissionLine> {

    private static final String[] FIELD_NAMES_32_33 = {"name", "mdc", "rdc", "setvl", "vschd", "vcmod", "rcomp", "delti", "meter", "dcvmin", "cccitmx", "cccacc"};
    private static final String[] FIELD_NAMES_CONVERTER_32_33 = {"ip", "nb", "anmx", "anmn", "rc", "xc", "ebas", "tr", "tap", "tmx", "tmn", "stp", "ic", "if", "it", "id", "xcap"};

    TwoTerminalDcTransmissionLineData() {
        super(TWO_TERMINAL_DC_TRANSMISSION_LINE);
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
        withFieldNames(V32, FIELD_NAMES_32_33);
        withFieldNames(V33, FIELD_NAMES_32_33);
        withFieldNames(V35, "name", "mdc", "rdc", "setvl", "vschd", "vcmod", "rcomp", "delti", "met", "dcvmin", "cccitmx", "cccacc");
        withQuotedFields("name", "meter", "idr", "idi", "met", "id");
    }

    @Override
    protected Class<PsseTwoTerminalDcTransmissionLine> psseTypeClass() {
        return PsseTwoTerminalDcTransmissionLine.class;
    }

    private static class IOLegacyText extends RecordGroupIOLegacyText<PsseTwoTerminalDcTransmissionLine> {

        IOLegacyText(AbstractRecordGroup<PsseTwoTerminalDcTransmissionLine> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTwoTerminalDcTransmissionLine> read(LegacyTextReader reader, Context context) throws IOException {
            List<String> mainRecords = new ArrayList<>();
            List<String> converterRecords = new ArrayList<>();
            if (!reader.isQRecordFound()) {
                String line = reader.readRecordLine();
                while (!reader.endOfBlock(line)) {
                    mainRecords.add(line);
                    converterRecords.add(reader.readRecordLine());
                    converterRecords.add(reader.readRecordLine());
                    line = reader.readRecordLine();
                }
            }

            List<PsseTwoTerminalDcTransmissionLine> twoTerminalDcList = super.recordGroup.readFromStrings(mainRecords, context);
            List<PsseTwoTerminalDcConverter> convertersList = new PsseTwoTerminalDcConverterRecordData().readFromStrings(converterRecords, context);

            int index = 0;
            for (PsseTwoTerminalDcTransmissionLine twoTerminalDc : twoTerminalDcList) {
                twoTerminalDc.setRectifier(convertersList.get(index));
                index = index + 1;
                twoTerminalDc.setInverter(convertersList.get(index));
                index = index + 1;
            }

            return twoTerminalDcList;
        }

        @Override
        public void write(List<PsseTwoTerminalDcTransmissionLine> twoTerminalDcList, Context context, OutputStream outputStream) {

            PsseTwoTerminalDcConverterRecordData converterRecordData = new PsseTwoTerminalDcConverterRecordData();
            String[] mainHeaders = context.getFieldNames(TWO_TERMINAL_DC_TRANSMISSION_LINE);
            String[] quotedFields = super.recordGroup.quotedFields();
            String[] converterHeaders = context.getFieldNames(INTERNAL_TWO_TERMINAL_DC_TRANSMISSION_LINE_CONVERTER);

            List<PsseTwoTerminalDcTransmissionLine> mainList = new ArrayList<>();
            List<PsseTwoTerminalDcConverter> converterList = new ArrayList<>();

            twoTerminalDcList.forEach(twoTerminalDc -> {
                mainList.add(twoTerminalDc);
                converterList.add(twoTerminalDc.getRectifier());
                converterList.add(twoTerminalDc.getInverter());
            });

            List<String> mainStringList = super.recordGroup.buildRecords(mainList, mainHeaders, quotedFields, context);
            List<String> converterStringList = converterRecordData.buildRecords(converterList, converterHeaders, quotedFields, context);

            writeBegin(outputStream);
            int index = 0;
            for (String main : mainStringList) {
                String rectifier = converterStringList.get(index);
                index = index + 1;
                String inverter = converterStringList.get(index);
                index = index + 1;
                write(Arrays.asList(main, rectifier, inverter), outputStream);
            }
            writeEnd(outputStream);
        }

        private static class PsseTwoTerminalDcConverterRecordData extends AbstractRecordGroup<PsseTwoTerminalDcConverter> {
            PsseTwoTerminalDcConverterRecordData() {
                super(INTERNAL_TWO_TERMINAL_DC_TRANSMISSION_LINE_CONVERTER);
                withFieldNames(V32, FIELD_NAMES_CONVERTER_32_33);
                withFieldNames(V33, FIELD_NAMES_CONVERTER_32_33);
                withFieldNames(V35, "ip", "nb", "anmx", "anmn", "rc", "xc", "ebas", "tr", "tap", "tmx", "tmn", "stp", "ic", "nd", "if", "it", "id", "xcap");
                withQuotedFields();
            }

            @Override
            protected Class<PsseTwoTerminalDcConverter> psseTypeClass() {
                return PsseTwoTerminalDcConverter.class;
            }
        }
    }
}
