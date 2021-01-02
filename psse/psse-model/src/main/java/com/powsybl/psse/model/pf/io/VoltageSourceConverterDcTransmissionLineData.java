/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.io.AbstractRecordGroupIOLegacyTextMultiLine;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.FileFormat;
import com.powsybl.psse.model.pf.PsseVoltageSourceConverterDcTransmissionLine;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class VoltageSourceConverterDcTransmissionLineData extends AbstractRecordGroup<PsseVoltageSourceConverterDcTransmissionLine> {

    VoltageSourceConverterDcTransmissionLineData() {
        super(VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE);
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
        withQuotedFields("name");
    }

    @Override
    public Class<PsseVoltageSourceConverterDcTransmissionLine> psseTypeClass() {
        return PsseVoltageSourceConverterDcTransmissionLine.class;
    }

    private static class IOLegacyText extends AbstractRecordGroupIOLegacyTextMultiLine<PsseVoltageSourceConverterDcTransmissionLine> {

        IOLegacyText(AbstractRecordGroup<PsseVoltageSourceConverterDcTransmissionLine> recordGroup) {
            super(recordGroup);
        }

        @Override
        protected MultiLineRecord readMultiLineRecord(List<String> recordsLines, int currentLine, Context context) {
            int i = currentLine;
            String[][] fieldNamesByLine = getFieldNamesByLine(context.getVersion());
            String[] lines = new String[fieldNamesByLine.length];
            for (int k = 0; k < lines.length; k++) {
                lines[k] = recordsLines.get(i++);
            }
            return new MultiLineRecord(fieldNamesByLine, lines);
        }

        private String[][] getFieldNamesByLine(PsseVersion version) {
            switch (version.major()) {
                case V35:
                    return FIELD_NAMES_35;
                case V33:
                    return FIELD_NAMES_33;
                default:
                    throw new PsseException("Unsupported version " + version);
            }
        }

        @Override
        public List<PsseVoltageSourceConverterDcTransmissionLine> read(BufferedReader reader, Context context) throws IOException {
            return super.readMultiLineRecords(reader, context);
        }
    }

    private static final String[][] FIELD_NAMES_35 = {
        {"name", "mdc", "rdc", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"},
        {"ibus1", "type1", "mode1", "dcset1", "acset1", "aloss1", "bloss1", "minloss1", "smax1", "imax1", "pwf1", "maxq1", "minq1", "vsreg1", "nreg1", "rmpct1"},
        {"ibus2", "type2", "mode2", "dcset2", "acset2", "aloss2", "bloss2", "minloss2", "smax2", "imax2", "pwf2", "maxq2", "minq2", "vsreg2", "nreg2", "rmpct2"}};

    private static final String[][] FIELD_NAMES_33 = {
        {"name", "mdc", "rdc", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"},
        {"ibus1", "type1", "mode1", "dcset1", "acset1", "aloss1", "bloss1", "minloss1", "smax1", "imax1", "pwf1", "maxq1", "minq1", "remot1", "rmpct1"},
        {"ibus2", "type2", "mode2", "dcset2", "acset2", "aloss2", "bloss2", "minloss2", "smax2", "imax2", "pwf2", "maxq2", "minq2", "remot2", "rmpct2"}};
}
