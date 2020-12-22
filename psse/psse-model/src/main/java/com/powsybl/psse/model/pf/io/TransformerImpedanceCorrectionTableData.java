/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.io.*;
import com.powsybl.psse.model.pf.PsseTransformerImpedanceCorrectionTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.TRANSFORMER_IMPEDANCE_CORRECTION_TABLES;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TransformerImpedanceCorrectionTableData extends AbstractRecordGroup<PsseTransformerImpedanceCorrectionTable> {

    TransformerImpedanceCorrectionTableData() {
        super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES);
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
        withIO(FileFormat.JSON, new IOJson(this));
    }

    @Override
    public String[] fieldNames(PsseVersion version) {
        throw new PsseException("Should not occur");
    }

    @Override
    public Class<PsseTransformerImpedanceCorrectionTable> psseTypeClass() {
        return PsseTransformerImpedanceCorrectionTable.class;
    }

    private static class IOJson extends RecordGroupIOJson<PsseTransformerImpedanceCorrectionTable> {
        IOJson(AbstractRecordGroup<PsseTransformerImpedanceCorrectionTable> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTransformerImpedanceCorrectionTable> readJson(JsonNode networkNode, Context context) {
            // XXX(Luma) pending implementation
            throw new PsseException("Not implemented");
        }
    }

    private static class IOLegacyText extends AbstractRecordGroupIOLegacyTextMultiLine<PsseTransformerImpedanceCorrectionTable> {
        private static final String[][] FIELD_NAMES_33 = {{"i", "t1", "f1", "t2", "f2", "t3", "f3"}};
        private static final String[][] FIELD_NAMES_35 = {{"i", "t1", "ref1", "imf1", "t2", "ref2", "imf2", "t3", "ref3", "imf3"}};

        IOLegacyText(AbstractRecordGroup<PsseTransformerImpedanceCorrectionTable> recordGroup) {
            super(recordGroup);
        }

        private boolean isLastLine(String line, Context context) {
            switch (context.getVersion().major()) {
                case V33:
                    return true;
                case V35:
                    String[] s = line.split("" + context.getDelimiter());
                    int n = s.length;
                    if (n < 3) {
                        return true;
                    }
                    return Double.valueOf(s[n - 1]) == 0.0 && Double.valueOf(s[n - 2]) == 0.0 && Double.valueOf(s[n - 3]) == 0.0;
                default:
                    throw new PsseException("Unsupported version " + context.getVersion());
            }
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
        protected MultiLineRecord readMultiLineRecord(List<String> recordsLines, int currentLine, Context context) {
            String[][] fieldNamesByLine = getFieldNamesByLine(context.getVersion());
            List<String> lines = new ArrayList<>();
            for (int k = currentLine; k < recordsLines.size(); k++) {
                String line = recordsLines.get(k);
                lines.add(line);
                if (isLastLine(line, context)) {
                    break;
                }
            }
            return new MultiLineRecord(fieldNamesByLine, lines.toArray(new String[0]));
        }

        @Override
        public List<PsseTransformerImpedanceCorrectionTable> read(BufferedReader reader, Context context) throws IOException {
            return super.readMultiLineRecords(reader, context);
        }

        @Override
        public void write(List<PsseTransformerImpedanceCorrectionTable> transformers, Context context, OutputStream outputStream) {
            writeBegin(outputStream);
            writeEnd(outputStream);
            // XXX(Luma) pending implementation
            throw new PsseException("Not implemented");
        }
    }
}
