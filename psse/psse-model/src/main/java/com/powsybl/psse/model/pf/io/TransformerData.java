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
import com.powsybl.psse.model.pf.PsseTransformer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.powsybl.psse.model.io.FileFormat.VALID_DELIMITERS;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TransformerData extends AbstractRecordGroup<PsseTransformer> {

    TransformerData() {
        super(TRANSFORMER);
        withQuotedFields("ckt", "name", "vecgrp");
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
        withIO(FileFormat.JSON, new IOJson(this));
    }

    @Override
    public String[] fieldNames(PsseVersion version) {
        throw new PsseException("Should not occur");
    }

    @Override
    protected Class<PsseTransformer> psseTypeClass() {
        return PsseTransformer.class;
    }

    @Override
    protected RecordGroupIdentification getIdentificationFor(PsseTransformer transformer) {
        return transformer.getK() == 0 ? PowerFlowRecordGroup.TRANSFORMER_2 : PowerFlowRecordGroup.TRANSFORMER_3;
    }

    private static class IOJson extends RecordGroupIOJson<PsseTransformer> {
        IOJson(AbstractRecordGroup<PsseTransformer> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTransformer> readJson(JsonNode networkNode, Context context) {
            List<PsseTransformer> transformers = super.readJson(networkNode, context);
            // Same field names for 2 and 3 winding transformers
            context.setFieldNames(TRANSFORMER_2, context.getFieldNames(TRANSFORMER));
            context.setFieldNames(TRANSFORMER_3, context.getFieldNames(TRANSFORMER));
            return transformers;
        }
    }

    private static class IOLegacyText extends AbstractRecordGroupIOLegacyTextMultiLine<PsseTransformer> {

        IOLegacyText(AbstractRecordGroup<PsseTransformer> recordGroup) {
            super(recordGroup);
        }

        @Override
        protected MultiLineRecord readMultiLineRecord(List<String> recordsLines, int currentLine, Context context) {
            int i = currentLine;
            String line0 = recordsLines.get(i++);
            String[][] fieldNamesByLine = getFieldNamesByLine(context.getVersion(), line0);
            String[] lines = new String[fieldNamesByLine.length];
            lines[0] = line0;
            for (int k = 1; k < lines.length; k++) {
                lines[k] = recordsLines.get(i++);
            }
            return new MultiLineRecord(fieldNamesByLine, lines);
        }

        private String[][] getFieldNamesByLine(PsseVersion version, String line0) {
            return is3Winding(line0) ? fieldNames3Winding(version) : fieldNames2Winding(version);
        }

        @Override
        public List<PsseTransformer> read(BufferedReader reader, Context context) throws IOException {
            return super.readMultiLineRecords(reader, context);
        }

        @Override
        public void write(List<PsseTransformer> transformers, Context context, OutputStream outputStream) {
            writeBegin(outputStream);

            // Process all transformers with 2 windings together
            List<PsseTransformer> transformers2Windings = transformers.stream().filter(t -> t.getK() == 0).collect(Collectors.toList());
            if (!transformers2Windings.isEmpty()) {
                String[] contextFieldNames = context.getFieldNames(TRANSFORMER_2);
                String[][] fieldNamesByLine = fieldNames2Winding(context.getVersion());
                writeMultiLineRecords0(
                    buildMultiLineRecordsFixedLines(transformers2Windings, fieldNamesByLine, contextFieldNames, context),
                    outputStream);
            }
            // Process all transformers with 3 windings together
            List<PsseTransformer> transformers3Windings = transformers.stream().filter(t -> t.getK() != 0).collect(Collectors.toList());
            if (!transformers3Windings.isEmpty()) {
                String[] contextFieldNames = context.getFieldNames(TRANSFORMER_3);
                String[][] fieldNamesByLine = fieldNames3Winding(context.getVersion());
                writeMultiLineRecords0(
                    buildMultiLineRecordsFixedLines(transformers3Windings, fieldNamesByLine, contextFieldNames, context),
                    outputStream);
            }

            writeEnd(outputStream);
        }

        private static boolean is3Winding(String record) {
            try (Scanner scanner = new Scanner(record)) {
                // Valid delimiters surrounded by any number of whitespace
                scanner.useDelimiter("\\s*[" + VALID_DELIMITERS + "]\\s*");
                int i = scanner.hasNextInt() ? scanner.nextInt() : 0;
                int j = scanner.hasNextInt() ? scanner.nextInt() : 0;
                int k = scanner.hasNextInt() ? scanner.nextInt() : 0;
                return i != 0 && j != 0 && k != 0;
            }
        }

        private static String[][] fieldNames3Winding(PsseVersion version) {
            switch (version.major()) {
                case V35:
                    return FIELD_NAMES_3_35;
                case V33:
                    return FIELD_NAMES_3_33;
                case V32:
                    return FIELD_NAMES_3_32;
                default:
                    throw new PsseException("Unsupported version " + version);
            }
        }

        private static String[][] fieldNames2Winding(PsseVersion version) {
            switch (version.major()) {
                case V35:
                    return FIELD_NAMES_2_35;
                case V33:
                    return FIELD_NAMES_2_33;
                case V32:
                    return FIELD_NAMES_2_32;
                default:
                    throw new PsseException("Unsupported version " + version);
            }
        }
    }

    private static final String[][] FIELD_NAMES_3_35 = {
        {"ibus", "jbus", "kbus", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmet", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "vecgrp", "zcod"},
        {"r1_2", "x1_2", "sbase1_2", "r2_3", "x2_3", "sbase2_3", "r3_1", "x3_1", "sbase3_1", "vmstar", "anstar"},
        {"windv1", "nomv1", "ang1", "wdg1rate1", "wdg1rate2", "wdg1rate3", "wdg1rate4", "wdg1rate5", "wdg1rate6", "wdg1rate7", "wdg1rate8", "wdg1rate9", "wdg1rate10", "wdg1rate11", "wdg1rate12", "cod1", "cont1", "node1", "rma1", "rmi1", "vma1", "vmi1", "ntp1", "tab1", "cr1", "cx1", "cnxa1"},
        {"windv2", "nomv2", "ang2", "wdg2rate1", "wdg2rate2", "wdg2rate3", "wdg2rate4", "wdg2rate5", "wdg2rate6", "wdg2rate7", "wdg2rate8", "wdg2rate9", "wdg2rate10", "wdg2rate11", "wdg2rate12", "cod2", "cont2", "node2", "rma2", "rmi2", "vma2", "vmi2", "ntp2", "tab2", "cr2", "cx2", "cnxa2"},
        {"windv3", "nomv3", "ang3", "wdg3rate1", "wdg3rate2", "wdg3rate3", "wdg3rate4", "wdg3rate5", "wdg3rate6", "wdg3rate7", "wdg3rate8", "wdg3rate9", "wdg3rate10", "wdg3rate11", "wdg3rate12", "cod3", "cont3", "node3", "rma3", "rmi3", "vma3", "vmi3", "ntp3", "tab3", "cr3", "cx3", "cnxa3"}
    };
    private static final String[][] FIELD_NAMES_3_33 = {
        {"i", "j", "k", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmetr", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "vecgrp"},
        {"r12", "x12", "sbase12", "r23", "x23", "sbase23", "r31", "x31", "sbase31", "vmstar", "anstar"},
        {"windv1", "nomv1", "ang1", "rata1", "ratb1", "ratc1", "cod1", "cont1", "rma1", "rmi1", "vma1", "vmi1", "ntp1", "tab1", "cr1", "cx1", "cnxa1"},
        {"windv2", "nomv2", "ang2", "rata2", "ratb2", "ratc2", "cod2", "cont2", "rma2", "rmi2", "vma2", "vmi2", "ntp2", "tab2", "cr2", "cx2", "cnxa2"},
        {"windv3", "nomv3", "ang3", "rata3", "ratb3", "ratc3", "cod3", "cont3", "rma3", "rmi3", "vma3", "vmi3", "ntp3", "tab3", "cr3", "cx3", "cnxa3"}
    };
    private static final String[][] FIELD_NAMES_3_32 = {
        {"i", "j", "k", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmetr", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"},
        {"r12", "x12", "sbase12", "r23", "x23", "sbase23", "r31", "x31", "sbase31", "vmstar", "anstar"},
        {"windv1", "nomv1", "ang1", "rata1", "ratb1", "ratc1", "cod1", "cont1", "rma1", "rmi1", "vma1", "vmi1", "ntp1", "tab1", "cr1", "cx1", "cnxa1"},
        {"windv2", "nomv2", "ang2", "rata2", "ratb2", "ratc2", "cod2", "cont2", "rma2", "rmi2", "vma2", "vmi2", "ntp2", "tab2", "cr2", "cx2", "cnxa2"},
        {"windv3", "nomv3", "ang3", "rata3", "ratb3", "ratc3", "cod3", "cont3", "rma3", "rmi3", "vma3", "vmi3", "ntp3", "tab3", "cr3", "cx3", "cnxa3"}
    };

    private static final String[][] FIELD_NAMES_2_35 = {
        {"ibus", "jbus", "kbus", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmet", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "vecgrp"},
        {"r1_2", "x1_2", "sbase1_2"},
        {"windv1", "nomv1", "ang1", "wdg1rate1", "wdg1rate2", "wdg1rate3", "wdg1rate4", "wdg1rate5", "wdg1rate6", "wdg1rate7", "wdg1rate8", "wdg1rate9", "wdg1rate10", "wdg1rate11", "wdg1rate12", "cod1", "cont1", "node1", "rma1", "rmi1", "vma1", "vmi1", "ntp1", "tab1", "cr1", "cx1", "cnxa1"},
        {"windv2", "nomv2"}
    };
    private static final String[][] FIELD_NAMES_2_33 = {
        {"i", "j", "k", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmetr", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "vecgrp"},
        {"r12", "x12", "sbase12"},
        {"windv1", "nomv1", "ang1", "rata1", "ratb1", "ratc1", "cod1", "cont1", "rma1", "rmi1", "vma1", "vmi1", "ntp1", "tab1", "cr1", "cx1", "cnxa1"},
        {"windv2", "nomv2"}
    };
    private static final String[][] FIELD_NAMES_2_32 = {
        {"i", "j", "k", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmetr", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"},
        {"r12", "x12", "sbase12"},
        {"windv1", "nomv1", "ang1", "rata1", "ratb1", "ratc1", "cod1", "cont1", "rma1", "rmi1", "vma1", "vmi1", "ntp1", "tab1", "cr1", "cx1", "cnxa1"},
        {"windv2", "nomv2"}
    };
}
