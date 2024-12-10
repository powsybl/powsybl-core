/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.*;
import com.powsybl.psse.model.pf.PsseVoltageSourceConverter;
import com.powsybl.psse.model.pf.PsseVoltageSourceConverterDcTransmissionLine;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.powsybl.psse.model.PsseVersion.Major.*;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.INTERNAL_VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_CONVERTER;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class VoltageSourceConverterDcTransmissionLineData extends AbstractRecordGroup<PsseVoltageSourceConverterDcTransmissionLine> {

    private static final String[] FIELD_NAMES_CONVERTER_32_33 = {"ibus", "type", "mode", "dcset", "acset", "aloss", "bloss", "minloss", "smax", "imax", "pwf", "maxq", "minq", "remot", "rmpct"};

    VoltageSourceConverterDcTransmissionLineData() {
        super(VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE, "name", "mdc", "rdc", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4");
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
        withQuotedFields("name");
    }

    @Override
    protected Class<PsseVoltageSourceConverterDcTransmissionLine> psseTypeClass() {
        return PsseVoltageSourceConverterDcTransmissionLine.class;
    }

    private static class IOLegacyText extends RecordGroupIOLegacyText<PsseVoltageSourceConverterDcTransmissionLine> {

        IOLegacyText(AbstractRecordGroup<PsseVoltageSourceConverterDcTransmissionLine> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseVoltageSourceConverterDcTransmissionLine> read(LegacyTextReader reader, Context context) throws IOException {
            List<String> mainRecords = new ArrayList<>();
            List<String> converterRecords = new ArrayList<>();
            if (!reader.isQRecordFound()) {
                String line = reader.readUntilFindingARecordLineNotEmpty();
                while (!reader.endOfBlock(line)) {
                    mainRecords.add(line);
                    converterRecords.add(reader.readRecordLine());
                    converterRecords.add(reader.readRecordLine());
                    line = reader.readUntilFindingARecordLineNotEmpty();
                }
            }

            List<PsseVoltageSourceConverterDcTransmissionLine> voltageSourceConverterDcList = super.recordGroup.readFromStrings(mainRecords, context);
            List<PsseVoltageSourceConverter> convertersList = new PsseVoltageSourceConverterRecordData().readFromStrings(converterRecords, context);

            int index = 0;
            for (PsseVoltageSourceConverterDcTransmissionLine voltageSourceConverterDc : voltageSourceConverterDcList) {
                voltageSourceConverterDc.setConverter1(convertersList.get(index));
                index = index + 1;
                voltageSourceConverterDc.setConverter2(convertersList.get(index));
                index = index + 1;
            }

            return voltageSourceConverterDcList;
        }

        @Override
        public void write(List<PsseVoltageSourceConverterDcTransmissionLine> voltageSourceConverterDcList, Context context, OutputStream outputStream) {

            PsseVoltageSourceConverterRecordData converterRecordData = new PsseVoltageSourceConverterRecordData();
            String[] mainHeaders = context.getFieldNames(VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE);
            String[] quotedFields = super.recordGroup.quotedFields();
            String[] converterHeaders = context.getFieldNames(INTERNAL_VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_CONVERTER);

            List<PsseVoltageSourceConverterDcTransmissionLine> mainList = new ArrayList<>();
            List<PsseVoltageSourceConverter> converterList = new ArrayList<>();

            voltageSourceConverterDcList.forEach(voltageSourceConverterDc -> {
                mainList.add(voltageSourceConverterDc);
                converterList.add(voltageSourceConverterDc.getConverter1());
                converterList.add(voltageSourceConverterDc.getConverter2());
            });

            List<String> mainStringList = super.recordGroup.buildRecords(mainList, mainHeaders, quotedFields, context);
            List<String> converterStringList = converterRecordData.buildRecords(converterList, converterHeaders, quotedFields, context);

            writeBegin(outputStream);
            int index = 0;
            for (String main : mainStringList) {
                String converter1 = converterStringList.get(index);
                index = index + 1;
                String converter2 = converterStringList.get(index);
                index = index + 1;
                write(Arrays.asList(main, converter1, converter2), outputStream);
            }
            writeEnd(outputStream);
        }

        private static class PsseVoltageSourceConverterRecordData extends AbstractRecordGroup<PsseVoltageSourceConverter> {
            PsseVoltageSourceConverterRecordData() {
                super(INTERNAL_VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_CONVERTER);
                withFieldNames(V32, FIELD_NAMES_CONVERTER_32_33);
                withFieldNames(V33, FIELD_NAMES_CONVERTER_32_33);
                withFieldNames(V35, "ibus", "type", "mode", "dcset", "acset", "aloss", "bloss", "minloss", "smax", "imax", "pwf", "maxq", "minq", "vsreg", "nreg", "rmpct");
                withQuotedFields();
            }

            @Override
            protected Class<PsseVoltageSourceConverter> psseTypeClass() {
                return PsseVoltageSourceConverter.class;
            }
        }
    }
}
