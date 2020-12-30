/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.FileFormat;
import com.powsybl.psse.model.io.RecordGroupIOJson;
import com.powsybl.psse.model.io.RecordGroupIOLegacyText;
import com.powsybl.psse.model.pf.PsseLineGrouping;
import com.powsybl.psse.model.pf.PsseLineGrouping.PsseLineGroupingParserX;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.MULTI_SECTION_LINE_GROUPING;
/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class MultiSectionLineGroupingData extends AbstractRecordGroup<PsseLineGrouping> {

    private static final String EMPTY_DUMX = "null";

    MultiSectionLineGroupingData() {
        super(MULTI_SECTION_LINE_GROUPING);
        withFieldNames(V33, "i", "j", "id", "met", "dum1", "dum2", "dum3", "dum4", "dum5", "dum6", "dum7", "dum8", "dum9");
        withFieldNames(V35, "i", "j", "id", "met", "dum1", "dum2", "dum3", "dum4", "dum5", "dum6", "dum7", "dum8", "dum9");
        withQuotedFields("id", "mslid");
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
        withIO(FileFormat.JSON, new IOJson(this));
    }

    @Override
    public Class<PsseLineGrouping> psseTypeClass() {
        return PsseLineGrouping.class;
    }

    private static class IOLegacyText extends RecordGroupIOLegacyText<PsseLineGrouping> {
        IOLegacyText(AbstractRecordGroup<PsseLineGrouping> recordGroup) {
            super(recordGroup);
        }

        @Override
        public void write(List<PsseLineGrouping> lineGroupingList, Context context, OutputStream outputStream) {
            writeBegin(outputStream);

            String[] headers = this.recordGroup.fieldNames(context.getVersion());
            String[] quotedFields = this.recordGroup.quotedFields();

            lineGroupingList.forEach(lineGrouping -> {
                // write only the non-null read points
                String[] writeHeaders = ArrayUtils.subarray(headers, 0, validRecordFields(lineGrouping));
                String record = this.recordGroup.buildRecord(lineGrouping, writeHeaders, quotedFields, context);
                write(String.format("%s%n", record), outputStream);
            });
            writeEnd(outputStream);
        }

        private static int validRecordFields(PsseLineGrouping record) {
            // I, J, Id, Met always valid
            int valid = 4;
            List<Integer> list = Arrays.asList(record.getDum1(), record.getDum2(), record.getDum3(), record.getDum4(),
                record.getDum5(), record.getDum6(), record.getDum7(), record.getDum8(), record.getDum9());

            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == null) {
                    return valid;
                }
                valid++;
            }
            return valid;
        }
    }

    private static class IOJson extends RecordGroupIOJson<PsseLineGrouping> {
        IOJson(AbstractRecordGroup<PsseLineGrouping> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseLineGrouping> read(BufferedReader reader, Context context) throws IOException {
            if (reader != null) {
                throw new PsseException("Unexpected reader. Should be null");
            }
            List<PsseLineGroupingParserX> parserRecords = new PsseLineGroupingParserXdata().read(null, context);
            List<PsseLineGrouping> records = new ArrayList<>();
            parserRecords.forEach(parserRecord -> convertToLineGrouping(records, parserRecord));
            return records;
        }

        @Override
        public void write(List<PsseLineGrouping> lineGroupingList, Context context, OutputStream outputStream) {
            if (outputStream != null) {
                throw new PsseException("Unexpected outputStream. Should be null");
            }
            List<PsseLineGroupingParserX> parserList = convertToParserList(lineGroupingList);
            new PsseLineGroupingParserXdata().write(parserList, context, null);
        }

        private static void convertToLineGrouping(List<PsseLineGrouping> lineGroupingList, PsseLineGroupingParserX parserRecord) {
            List<String> list = Arrays.asList(parserRecord.getDum1(), parserRecord.getDum2(), parserRecord.getDum3(), parserRecord.getDum4(),
                parserRecord.getDum5(), parserRecord.getDum6(), parserRecord.getDum7(), parserRecord.getDum8(), parserRecord.getDum9());

            PsseLineGrouping record = new PsseLineGrouping(parserRecord.getIbus(), parserRecord.getJbus(), parserRecord.getMslid(), parserRecord.getMet());

            int pointNumber = 0;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).contains(EMPTY_DUMX)) {
                    lineGroupingList.add(record);
                    return;
                } else {
                    pointNumber++;
                    record.setDum(pointNumber, Integer.parseInt(list.get(i)));
                }
            }
            lineGroupingList.add(record);
        }

        private static List<PsseLineGroupingParserX> convertToParserList(List<PsseLineGrouping> recordList) {
            List<PsseLineGroupingParserX> recordListx = new ArrayList<>();

            recordList.forEach(record -> {
                PsseLineGroupingParserX recordx = new PsseLineGroupingParserX();
                recordx.setIbus(record.getI());
                recordx.setJbus(record.getJ());
                recordx.setMslid(record.getId());
                recordx.setMet(record.getMet());
                recordx.setDum1(getDum(record.getDum1()));
                recordx.setDum2(getDum(record.getDum2()));
                recordx.setDum3(getDum(record.getDum3()));
                recordx.setDum4(getDum(record.getDum4()));
                recordx.setDum5(getDum(record.getDum5()));
                recordx.setDum6(getDum(record.getDum6()));
                recordx.setDum7(getDum(record.getDum7()));
                recordx.setDum8(getDum(record.getDum8()));
                recordx.setDum9(getDum(record.getDum9()));
                recordListx.add(recordx);
            });
            return recordListx;
        }

        private static String getDum(Integer dum) {
            if (dum == null) {
                return EMPTY_DUMX;
            } else {
                return String.valueOf(dum);
            }
        }

        private static class PsseLineGroupingParserXdata extends AbstractRecordGroup<PsseLineGroupingParserX> {
            PsseLineGroupingParserXdata() {
                super(MULTI_SECTION_LINE_GROUPING);
                withQuotedFields("id", "mslid");
            }

            @Override
            public Class<PsseLineGroupingParserX> psseTypeClass() {
                return PsseLineGroupingParserX.class;
            }
        }
    }
}
