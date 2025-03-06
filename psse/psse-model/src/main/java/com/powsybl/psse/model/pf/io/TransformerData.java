/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.*;
import com.powsybl.psse.model.pf.PsseRates;
import com.powsybl.psse.model.pf.PsseTransformer;
import com.powsybl.psse.model.pf.PsseTransformer.TransformerImpedances;
import com.powsybl.psse.model.pf.PsseTransformerWinding;
import com.univocity.parsers.annotations.Nested;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static com.powsybl.psse.model.io.FileFormat.VALID_DELIMITERS;
import static com.powsybl.psse.model.PsseVersion.Major.V32;
import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.*;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TransformerData extends AbstractRecordGroup<PsseTransformer> {

    private static final String[] FIELD_NAMES_T2W_IMPEDANCES_RECORD = {"r12", "x12", "sbase12"};
    private static final String[] FIELD_NAMES_T2W_WINDING_RECORD = {STR_WINDV, "nomv"};
    private static final String[] FIELD_NAMES_WINDING_32_33 = {STR_WINDV, "nomv", "ang", "rata", "ratb", "ratc", "cod", "cont", "rma", "rmi", "vma", "vmi", "ntp", "tab", "cr", "cx", "cnxa"};
    static final String[] FIELD_NAMES_35 = {"ibus", "jbus", "kbus", STR_CKT, STR_CW, STR_CZ, STR_CM, STR_MAG1, STR_MAG2, "nmet", STR_NAME, STR_STAT, STR_O1, STR_F1, STR_O2, STR_F2, STR_O3, STR_F3, STR_O4, STR_F4, STR_VECGRP, "zcod"};
    static final String[] FIELD_NAMES_IMPEDANCES_35 = {"r12", "x12", "sbase12", "r23", "x23", "sbase23", "r31", "x31", "sbase31", "vmstar", "anstar"};
    static final String[] FIELD_NAMES_WINDING_35 = {STR_WINDV, "nomv", "ang", "wdgrate1", "wdgrate2", "wdgrate3", "wdgrate4", "wdgrate5", "wdgrate6", "wdgrate7", "wdgrate8", "wdgrate9", "wdgrate10", "wdgrate11", "wdgrate12", "cod", "cont", "node", "rma", "rmi", "vma", "vmi", "ntp", "tab", "cr", "cx", "cnxa"};
    static final String[] FIELD_NAMES_IMPEDANCES_AND_WINDINGS_35_RAWX = {"r1_2", "x1_2", "sbase1_2", "r2_3", "x2_3", "sbase2_3", "r3_1", "x3_1", "sbase3_1", "vmstar", "anstar", "windv1", "nomv1", "ang1", "wdg1rate1", "wdg1rate2", "wdg1rate3", "wdg1rate4", "wdg1rate5", "wdg1rate6", "wdg1rate7", "wdg1rate8", "wdg1rate9", "wdg1rate10", "wdg1rate11", "wdg1rate12", "cod1", "cont1", "node1", "rma1", "rmi1", "vma1", "vmi1", "ntp1", "tab1", "cr1", "cx1", "cnxa1", "windv2", "nomv2", "ang2", "wdg2rate1", "wdg2rate2", "wdg2rate3", "wdg2rate4", "wdg2rate5", "wdg2rate6", "wdg2rate7", "wdg2rate8", "wdg2rate9", "wdg2rate10", "wdg2rate11", "wdg2rate12", "cod2", "cont2", "node2", "rma2", "rmi2", "vma2", "vmi2", "ntp2", "tab2", "cr2", "cx2", "cnxa2", "windv3", "nomv3", "ang3", "wdg3rate1", "wdg3rate2", "wdg3rate3", "wdg3rate4", "wdg3rate5", "wdg3rate6", "wdg3rate7", "wdg3rate8", "wdg3rate9", "wdg3rate10", "wdg3rate11", "wdg3rate12", "cod3", "cont3", "node3", "rma3", "rmi3", "vma3", "vmi3", "ntp3", "tab3", "cr3", "cx3", "cnxa3"};
    static final String[] FIELD_NAMES_35_RAWX = Stream.concat(Arrays.stream(FIELD_NAMES_35), Arrays.stream(FIELD_NAMES_IMPEDANCES_AND_WINDINGS_35_RAWX)).toArray(String[]::new);

    TransformerData() {
        super(TRANSFORMER);
        withFieldNames(V32, "i", "j", "k", STR_CKT, STR_CW, STR_CZ, STR_CM, STR_MAG1, STR_MAG2, "nmetr", STR_NAME, STR_STAT, STR_O1, STR_F1, STR_O2, STR_F2, STR_O3, STR_F3, STR_O4, STR_F4);
        withFieldNames(V33, "i", "j", "k", STR_CKT, STR_CW, STR_CZ, STR_CM, STR_MAG1, STR_MAG2, "nmetr", STR_NAME, STR_STAT, STR_O1, STR_F1, STR_O2, STR_F2, STR_O3, STR_F3, STR_O4, STR_F4, STR_VECGRP);
        withFieldNames(V35, FIELD_NAMES_35);
        withQuotedFields(STR_CKT, STR_NAME, STR_VECGRP);
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
    }

    @Override
    protected Class<PsseTransformer> psseTypeClass() {
        return PsseTransformer.class;
    }

    private static class IOLegacyText extends RecordGroupIOLegacyText<PsseTransformer> {

        IOLegacyText(AbstractRecordGroup<PsseTransformer> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTransformer> read(LegacyTextReader reader, Context context) throws IOException {
            List<String> mainRecords = new ArrayList<>();
            List<String> impedanceRecords = new ArrayList<>();
            List<String> windingRecords = new ArrayList<>();
            if (!reader.isQRecordFound()) {
                String line = reader.readUntilFindingARecordLineNotEmpty();
                while (!reader.endOfBlock(line)) {
                    mainRecords.add(line);
                    impedanceRecords.add(reader.readRecordLine());
                    windingRecords.add(reader.readRecordLine());
                    windingRecords.add(reader.readRecordLine());
                    if (is3Winding(line)) {
                        windingRecords.add(reader.readRecordLine());
                    }
                    line = reader.readUntilFindingARecordLineNotEmpty();
                }
            }

            List<PsseTransformer> transformerList = super.recordGroup.readFromStrings(mainRecords, context);
            List<TransformerImpedances> impedanceList = new PsseTransformerImpedancesRecordData().readFromStrings(impedanceRecords, context);
            List<TransformerWindingRecord> windingList = new PsseTransformerWindingRecordData().readFromStrings(windingRecords, context);

            int indexImpedance = 0;
            int indexWinding = 0;
            for (PsseTransformer transformer : transformerList) {
                transformer.setImpedances(impedanceList.get(indexImpedance));
                indexImpedance = indexImpedance + 1;
                transformer.setWinding1(windingList.get(indexWinding).winding, windingList.get(indexWinding).windingRates);
                indexWinding = indexWinding + 1;
                transformer.setWinding2(windingList.get(indexWinding).winding, windingList.get(indexWinding).windingRates);
                indexWinding = indexWinding + 1;
                if (transformer.getK() != 0) {
                    transformer.setWinding3(windingList.get(indexWinding).winding, windingList.get(indexWinding).windingRates);
                    indexWinding = indexWinding + 1;
                }
            }

            return transformerList;
        }

        @Override
        public void write(List<PsseTransformer> transformerList, Context context, OutputStream outputStream) {

            List<TransformerImpedances> impedanceList = new ArrayList<>();
            List<TransformerImpedances> impedanceT2wList = new ArrayList<>();
            List<TransformerWindingRecord> windingList = new ArrayList<>();
            List<TransformerWindingRecord> windingT2wList = new ArrayList<>();

            transformerList.forEach(transformer -> {
                if (transformer.getK() == 0) {
                    impedanceT2wList.add(transformer.getImpedances());
                    windingList.add(getWindingRecord(transformer.getWinding1(), transformer.getWinding1Rates()));
                    windingT2wList.add(getWindingRecord(transformer.getWinding2(), transformer.getWinding2Rates()));
                } else {
                    impedanceList.add(transformer.getImpedances());
                    windingList.add(getWindingRecord(transformer.getWinding1(), transformer.getWinding1Rates()));
                    windingList.add(getWindingRecord(transformer.getWinding2(), transformer.getWinding2Rates()));
                    windingList.add(getWindingRecord(transformer.getWinding3(), transformer.getWinding3Rates()));
                }
            });

            String[] mainHeaders = context.getFieldNames(TRANSFORMER);
            String[] quotedFields = super.recordGroup.quotedFields();
            List<String> firstRecordList = super.recordGroup.buildRecords(transformerList, mainHeaders, quotedFields, context);

            PsseTransformerImpedancesRecordData impedanceRecordData = new PsseTransformerImpedancesRecordData();
            String[] impedanceHeaders = context.getFieldNames(INTERNAL_TRANSFORMER_IMPEDANCES);
            List<String> impedanceRecordList = impedanceRecordData.buildRecords(impedanceList, impedanceHeaders, quotedFields, context);
            List<String> impedanceRecordT2wList = impedanceRecordData.buildRecords(impedanceT2wList, FIELD_NAMES_T2W_IMPEDANCES_RECORD, quotedFields, context);

            PsseTransformerWindingRecordData windingRecordData = new PsseTransformerWindingRecordData();
            String[] windingHeaders = context.getFieldNames(INTERNAL_TRANSFORMER_WINDING);
            List<String> windingRecordList = windingRecordData.buildRecords(windingList, windingHeaders, quotedFields, context);
            List<String> windingRecordT2wList = windingRecordData.buildRecords(windingT2wList, FIELD_NAMES_T2W_WINDING_RECORD, quotedFields, context);

            writeBegin(outputStream);
            int indexFirst = 0;
            int indexImpedance = 0;
            int indexImpedanceT2w = 0;
            int indexWinding = 0;
            int indexWindingT2w = 0;
            for (PsseTransformer transformer : transformerList) {
                String first = firstRecordList.get(indexFirst);
                indexFirst = indexFirst + 1;
                if (transformer.getK() == 0) {
                    String impedance = impedanceRecordT2wList.get(indexImpedanceT2w);
                    indexImpedanceT2w = indexImpedanceT2w + 1;
                    String winding1 = windingRecordList.get(indexWinding);
                    indexWinding = indexWinding + 1;
                    String winding2 = windingRecordT2wList.get(indexWindingT2w);
                    indexWindingT2w = indexWindingT2w + 1;
                    write(Arrays.asList(first, impedance, winding1, winding2), outputStream);

                } else {
                    String impedance = impedanceRecordList.get(indexImpedance);
                    indexImpedance = indexImpedance + 1;
                    String winding1 = windingRecordList.get(indexWinding);
                    indexWinding = indexWinding + 1;
                    String winding2 = windingRecordList.get(indexWinding);
                    indexWinding = indexWinding + 1;
                    String winding3 = windingRecordList.get(indexWinding);
                    indexWinding = indexWinding + 1;
                    write(Arrays.asList(first, impedance, winding1, winding2, winding3), outputStream);
                }
            }
            writeEnd(outputStream);
        }

        private static class PsseTransformerImpedancesRecordData extends AbstractRecordGroup<TransformerImpedances> {
            PsseTransformerImpedancesRecordData() {
                super(INTERNAL_TRANSFORMER_IMPEDANCES, FIELD_NAMES_IMPEDANCES_35);
            }

            @Override
            public Class<TransformerImpedances> psseTypeClass() {
                return TransformerImpedances.class;
            }
        }

        private static class PsseTransformerWindingRecordData extends AbstractRecordGroup<TransformerWindingRecord> {
            PsseTransformerWindingRecordData() {
                super(INTERNAL_TRANSFORMER_WINDING);
                withFieldNames(V32, FIELD_NAMES_WINDING_32_33);
                withFieldNames(V33, FIELD_NAMES_WINDING_32_33);
                withFieldNames(V35, FIELD_NAMES_WINDING_35);
            }

            @Override
            public Class<TransformerWindingRecord> psseTypeClass() {
                return TransformerWindingRecord.class;
            }
        }

        private TransformerWindingRecord getWindingRecord(PsseTransformerWinding winding, PsseRates windingRates) {
            TransformerWindingRecord windingRecord = new TransformerWindingRecord();
            windingRecord.winding = winding;
            windingRecord.windingRates = windingRates;
            return windingRecord;
        }

        private static boolean is3Winding(String recordStr) {
            try (Scanner scanner = new Scanner(recordStr)) {
                // Valid delimiters surrounded by any number of whitespace
                scanner.useDelimiter("\\s*[" + VALID_DELIMITERS + "]\\s*");
                int i = scanner.hasNextInt() ? scanner.nextInt() : 0;
                int j = scanner.hasNextInt() ? scanner.nextInt() : 0;
                int k = scanner.hasNextInt() ? scanner.nextInt() : 0;
                return i != 0 && j != 0 && k != 0;
            }
        }
    }

    public static class TransformerWindingRecord {
        @Nested()
        private PsseTransformerWinding winding;

        @Nested()
        private PsseRates windingRates;
    }
}
