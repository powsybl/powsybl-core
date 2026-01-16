/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.FileFormat;
import com.powsybl.psse.model.io.LegacyTextReader;
import com.powsybl.psse.model.io.RecordGroupIOLegacyText;
import com.powsybl.psse.model.pf.PsseGneDevice;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.GNE_DEVICE;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class GneDeviceData extends AbstractRecordGroup<PsseGneDevice> {

    GneDeviceData() {
        super(GNE_DEVICE);
        withQuotedFields(PsseGneDevice.getFieldNamesString());
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
    }

    @Override
    protected Class<PsseGneDevice> psseTypeClass() {
        return PsseGneDevice.class;
    }

    private static class IOLegacyText extends RecordGroupIOLegacyText<PsseGneDevice> {

        private static final String[][] FIELD_NAMES = {
            {STR_NAME, STR_MODEL, STR_NTERM, STR_BUS1, STR_BUS2, STR_NREAL, STR_NINTG, STR_NCHAR, STR_STATUS, STR_OWNER, STR_NMET},
            {STR_REAL1, STR_REAL2, STR_REAL3, STR_REAL4, STR_REAL5, STR_REAL6, STR_REAL7, STR_REAL8, STR_REAL9, STR_REAL10},
            {STR_INTG1, STR_INTG2, STR_INTG3, STR_INTG4, STR_INTG5, STR_INTG6, STR_INTG7, STR_INTG8, STR_INTG9, STR_INTG10},
            {STR_CHAR1, STR_CHAR2, STR_CHAR3, STR_CHAR4, STR_CHAR5, STR_CHAR6, STR_CHAR7, STR_CHAR8, STR_CHAR9, STR_CHAR10}};

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
}
