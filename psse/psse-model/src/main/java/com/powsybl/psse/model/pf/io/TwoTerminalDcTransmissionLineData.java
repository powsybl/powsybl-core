/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.TWO_TERMINAL_DC_TRANSMISSION_LINE;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.io.AbstractRecordGroupIOLegacyTextMultiLine;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.FileFormat;
import com.powsybl.psse.model.pf.PsseTwoTerminalDcTransmissionLine;


/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TwoTerminalDcTransmissionLineData extends AbstractRecordGroup<PsseTwoTerminalDcTransmissionLine> {

    TwoTerminalDcTransmissionLineData() {
        super(TWO_TERMINAL_DC_TRANSMISSION_LINE);
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
        withQuotedFields("name", "meter", "idr", "idi", "met");
    }

    @Override
    public Class<PsseTwoTerminalDcTransmissionLine> psseTypeClass() {
        return PsseTwoTerminalDcTransmissionLine.class;
    }

    private static class IOLegacyText extends AbstractRecordGroupIOLegacyTextMultiLine<PsseTwoTerminalDcTransmissionLine> {

        IOLegacyText(AbstractRecordGroup<PsseTwoTerminalDcTransmissionLine> recordGroup) {
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
        public List<PsseTwoTerminalDcTransmissionLine> read(BufferedReader reader, Context context) throws IOException {
            return super.readMultiLineRecords(reader, context);
        }
    }

    private static final String[][] FIELD_NAMES_35 = {
        {"name", "mdc", "rdc", "setvl", "vschd", "vcmod", "rcomp", "delti", "met", "dcvin", "cccitmx", "cccacc"},
        {"ipr", "nbr", "anmxr", "anmnr", "rcr", "xcr", "ebasr", "trr", "tapr", "tmxr", "tmnr", "stpr", "icr", "ndr", "ifr", "itr", "idr", "xcapr"},
        {"ipi", "nbi", "anmxi", "anmni", "rci", "xci", "ebasi", "tri", "tapi", "tmxi", "tmni", "stpi", "ici", "ndi", "ifi", "iti", "idi", "xcapi"}};
    private static final String[][] FIELD_NAMES_33 = {
        {"name", "mdc", "rdc", "setvl", "vschd", "vcmod", "rcomp", "delti", "meter", "dcvin", "cccitmx", "cccacc"},
        {"ipr", "nbr", "anmxr", "anmnr", "rcr", "xcr", "ebasr", "trr", "tapr", "tmxr", "tmnr", "stpr", "icr", "ifr", "itr", "idr", "xcapr"},
        {"ipi", "nbi", "anmxi", "anmni", "rci", "xci", "ebasi", "tri", "tapi", "tmxi", "tmni", "stpi", "ici", "ifi", "iti", "idi", "xcapi"}};
}
