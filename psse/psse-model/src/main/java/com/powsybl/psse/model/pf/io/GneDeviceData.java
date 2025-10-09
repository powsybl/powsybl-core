/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.GNE_DEVICE;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.powsybl.psse.model.io.*;
import org.apache.commons.lang3.ArrayUtils;

import com.powsybl.psse.model.pf.PsseGneDevice;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class GneDeviceData extends AbstractRecordGroup<PsseGneDevice> {

    GneDeviceData() {
        super(GNE_DEVICE);
        withQuotedFields(QUOTED_FIELDS);
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
    }

    @Override
    protected Class<PsseGneDevice> psseTypeClass() {
        return PsseGneDevice.class;
    }

    private static class IOLegacyText extends RecordGroupIOLegacyText<PsseGneDevice> {

        private static final String[][] FIELD_NAMES = {
            {"name", "model", "nterm", "bus1", "bus2", "nreal", "nintg", "nchar", "status", "owner", "nmet"},
            {"real1", "real2", "real3", "real4", "real5", "real6", "real7", "real8", "real9", "real10"},
            {"intg1", "intg2", "intg3", "intg4", "intg5", "intg6", "intg7", "intg8", "intg9", "intg10"},
            {"char1", "char2", "char3", "char4", "char5", "char6", "char7", "char8", "char9", "char10"}};

        IOLegacyText(AbstractRecordGroup<PsseGneDevice> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseGneDevice> read(LegacyTextReader reader, Context context) throws IOException {
            List<String> records = reader.readRecords();

            List<PsseGneDevice> gneDeviceList = new ArrayList<>();
            int maxNumMainHeaders = 0;
            int i = 0;
            while (i < records.size()) {
                String record = records.get(i++);
                int nreal = getNreal(record, Character.toString(context.getDelimiter()));
                int nintg = getNintg(record, Character.toString(context.getDelimiter()));
                int nchar = getNchar(record, Character.toString(context.getDelimiter()));
                String[] headers = FIELD_NAMES[0];
                if (nreal > 0) {
                    record = String.join(Character.toString(context.getDelimiter()), record, records.get(i++));
                    headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(FIELD_NAMES[1], 0, nreal));
                }
                if (nintg > 0) {
                    record = String.join(Character.toString(context.getDelimiter()), record, records.get(i++));
                    headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(FIELD_NAMES[2], 0, nintg));
                }
                if (nchar > 0) {
                    record = String.join(Character.toString(context.getDelimiter()), record, records.get(i++));
                    headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(FIELD_NAMES[3], 0, nchar));
                }
                gneDeviceList.add(super.recordGroup.parseSingleRecord(record, headers, context));
                maxNumMainHeaders = setMaxNumMainHeaders(maxNumMainHeaders, context.getCurrentRecordGroupMaxNumFields() - nreal - nintg - nchar);
            }

            // Record only main headers
            String[] actualFieldNames = ArrayUtils.subarray(FIELD_NAMES[0], 0, maxNumMainHeaders);
            context.setFieldNames(super.recordGroup.getIdentification(), actualFieldNames);

            return gneDeviceList;
        }

        @Override
        public void write(List<PsseGneDevice> gneDeviceList, Context context, OutputStream outputStream) {
            writeBegin(outputStream);

            gneDeviceList.forEach(gneDevice -> {
                List<String> records = new ArrayList<>();

                String[] mainHeaders = context.getFieldNames(GNE_DEVICE);
                records.add(super.recordGroup.buildRecord(gneDevice, mainHeaders, super.recordGroup.quotedFields(), context));

                if (gneDevice.getNreal() > 0) {
                    String[] headers = ArrayUtils.subarray(FIELD_NAMES[1], 0, gneDevice.getNreal());
                    records.add(super.recordGroup.buildRecord(gneDevice, headers, super.recordGroup.quotedFields(), context));
                }
                if (gneDevice.getNintg() > 0) {
                    String[] headers = ArrayUtils.subarray(FIELD_NAMES[2], 0, gneDevice.getNintg());
                    records.add(super.recordGroup.buildRecord(gneDevice, headers, super.recordGroup.quotedFields(), context));
                }
                if (gneDevice.getNchar() > 0) {
                    String[] headers = ArrayUtils.subarray(FIELD_NAMES[3], 0, gneDevice.getNchar());
                    records.add(super.recordGroup.buildRecord(gneDevice, headers, super.recordGroup.quotedFields(), context));
                }

                write(records, outputStream);
            });
            writeEnd(outputStream);
        }

        private static int getNreal(String record, String delimiter) {
            return getN(record, delimiter, 6);
        }

        private static int getNintg(String record, String delimiter) {
            return getN(record, delimiter, 7);
        }

        private static int getNchar(String record, String delimiter) {
            return getN(record, delimiter, 8);
        }

        private static int getN(String record, String delimiter, int length) {
            String[] tokens = record.split(delimiter);
            if (tokens.length < length) {
                return 0;
            }
            return Integer.parseInt(tokens[length - 1].trim());
        }

        private static int setMaxNumMainHeaders(int maxNumMainHeaders, int numMainHeaders) {
            if (numMainHeaders > maxNumMainHeaders) {
                return numMainHeaders;
            }
            return maxNumMainHeaders;
        }
    }

    private static final String[] QUOTED_FIELDS = {"name", "model", "char1", "char2", "char3", "char4", "char5", "char6", "char7", "char8", "char9", "char10"};
}
