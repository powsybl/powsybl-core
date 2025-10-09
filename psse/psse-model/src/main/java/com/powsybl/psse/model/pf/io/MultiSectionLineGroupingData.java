/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.FileFormat;
import com.powsybl.psse.model.io.RecordGroupIOLegacyText;
import com.powsybl.psse.model.pf.PsseLineGrouping;

import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.MULTI_SECTION_LINE_GROUPING;
/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class MultiSectionLineGroupingData extends AbstractRecordGroup<PsseLineGrouping> {

    MultiSectionLineGroupingData() {
        super(MULTI_SECTION_LINE_GROUPING, "i", "j", "id", "met", "dum1", "dum2", "dum3", "dum4", "dum5", "dum6", "dum7", "dum8", "dum9");
        withQuotedFields("id", "mslid");
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
    }

    @Override
    protected Class<PsseLineGrouping> psseTypeClass() {
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
}
