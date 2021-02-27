/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.*;
import com.powsybl.psse.model.pf.PsseRates;
import com.powsybl.psse.model.pf.PsseTransformer;
import com.powsybl.psse.model.pf.PsseTransformerWinding;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.powsybl.psse.model.io.FileFormat.VALID_DELIMITERS;
import static com.powsybl.psse.model.PsseVersion.Major.V32;
import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TransformerData extends AbstractRecordGroup<PsseTransformer> {

    private static final String[] FIELD_NAMES_T2W_SECOND_RECORD = {"r12", "x12", "sbase12"};
    private static final String[] FIELD_NAMES_T2W_SECOND_WINDING_RECORD = {"windv", "nomv"};
    private static final String[] FIELD_NAMES_WINDING_32_33 = {"windv", "nomv", "ang", "rata", "ratb", "ratc", "cod", "cont", "rma", "rmi", "vma", "vmi", "ntp", "tab", "cr", "cx", "cnxa"};

    TransformerData() {
        super(TRANSFORMER);
        withFieldNames(V32, "i", "j", "k", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmetr", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4");
        withFieldNames(V33, "i", "j", "k", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmetr", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "vecgrp");
        withFieldNames(V35, "ibus", "jbus", "kbus", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmet", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "vecgrp", "zcod");
        withQuotedFields("ckt", "name", "vecgrp");
        withIO(FileFormat.LEGACY_TEXT, new IOLegacyText(this));
    }

    @Override
    public Class<PsseTransformer> psseTypeClass() {
        return PsseTransformer.class;
    }

    private static class IOLegacyText extends RecordGroupIOLegacyText<PsseTransformer> {

        IOLegacyText(AbstractRecordGroup<PsseTransformer> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTransformer> read(BufferedReader reader, Context context) throws IOException {
            List<String> mainRecords = new ArrayList<>();
            List<String> secondRecords = new ArrayList<>();
            List<String> windingRecords = new ArrayList<>();
            String line = readRecordLine(reader);
            while (!endOfBlock(line)) {
                mainRecords.add(line);
                secondRecords.add(readRecordLine(reader));
                windingRecords.add(readRecordLine(reader));
                windingRecords.add(readRecordLine(reader));
                if (is3Winding(line)) {
                    windingRecords.add(readRecordLine(reader));
                }
                line = readRecordLine(reader);
            }

            List<PsseTransformer> transformerList = super.recordGroup.readFromStrings(mainRecords, context);
            List<TransformerSecondRecord> secondList = new PsseTransformerSecondRecordData().readFromStrings(secondRecords, context);
            List<TransformerWindingRecord> windingList = new PsseTransformerWindingRecordData().readFromStrings(windingRecords, context);

            int indexSecond = 0;
            int indexWinding = 0;
            for (PsseTransformer transformer : transformerList) {
                setSecondRecord(transformer, secondList.get(indexSecond));
                indexSecond = indexSecond + 1;
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

            List<TransformerSecondRecord> secondList = new ArrayList<>();
            List<TransformerSecondRecord> secondT2wList = new ArrayList<>();
            List<TransformerWindingRecord> windingList = new ArrayList<>();
            List<TransformerWindingRecord> windingT2wList = new ArrayList<>();

            transformerList.forEach(transformer -> {
                if (transformer.getK() == 0) {
                    secondT2wList.add(getSecondRecord(transformer));
                    windingList.add(getWindingRecord(transformer.getWinding1(), transformer.getWinding1Rates()));
                    windingT2wList.add(getWindingRecord(transformer.getWinding2(), transformer.getWinding2Rates()));
                } else {
                    secondList.add(getSecondRecord(transformer));
                    windingList.add(getWindingRecord(transformer.getWinding1(), transformer.getWinding1Rates()));
                    windingList.add(getWindingRecord(transformer.getWinding2(), transformer.getWinding2Rates()));
                    windingList.add(getWindingRecord(transformer.getWinding3(), transformer.getWinding3Rates()));
                }
            });

            String[] mainHeaders = context.getFieldNames(TRANSFORMER);
            String[] quotedFields = super.recordGroup.quotedFields();
            List<String> firstRecordList = super.recordGroup.buildRecords(transformerList, mainHeaders, quotedFields, context);

            PsseTransformerSecondRecordData secondRecordData = new PsseTransformerSecondRecordData();
            String[] secondHeaders = context.getFieldNames(INTERNAL_TRANSFORMER_SECOND_RECORD);
            List<String> secondRecordList = secondRecordData.buildRecords(secondList, secondHeaders, quotedFields, context);
            List<String> secondRecordT2wList = secondRecordData.buildRecords(secondT2wList, FIELD_NAMES_T2W_SECOND_RECORD, quotedFields, context);

            PsseTransformerWindingRecordData windingRecordData = new PsseTransformerWindingRecordData();
            String[] windingHeaders = context.getFieldNames(INTERNAL_TRANSFORMER_WINDING);
            List<String> windingRecordList = windingRecordData.buildRecords(windingList, windingHeaders, quotedFields, context);
            List<String> windingRecordT2wList = windingRecordData.buildRecords(windingT2wList, FIELD_NAMES_T2W_SECOND_WINDING_RECORD, quotedFields, context);

            writeBegin(outputStream);
            int indexFirst = 0;
            int indexSecond = 0;
            int indexSecondT2w = 0;
            int indexWinding = 0;
            int indexWindingT2w = 0;
            for (PsseTransformer transformer : transformerList) {
                String first = firstRecordList.get(indexFirst);
                indexFirst = indexFirst + 1;
                if (transformer.getK() == 0) {
                    String second = secondRecordT2wList.get(indexSecondT2w);
                    indexSecondT2w = indexSecondT2w + 1;
                    String winding1 = windingRecordList.get(indexWinding);
                    indexWinding = indexWinding + 1;
                    String winding2 = windingRecordT2wList.get(indexWindingT2w);
                    indexWindingT2w = indexWindingT2w + 1;
                    write(Arrays.asList(first, second, winding1, winding2), outputStream);

                } else {
                    String second = secondRecordList.get(indexSecond);
                    indexSecond = indexSecond + 1;
                    String winding1 = windingRecordList.get(indexWinding);
                    indexWinding = indexWinding + 1;
                    String winding2 = windingRecordList.get(indexWinding);
                    indexWinding = indexWinding + 1;
                    String winding3 = windingRecordList.get(indexWinding);
                    indexWinding = indexWinding + 1;
                    write(Arrays.asList(first, second, winding1, winding2, winding3), outputStream);
                }
            }
            writeEnd(outputStream);
        }

        private static class PsseTransformerSecondRecordData extends AbstractRecordGroup<TransformerSecondRecord> {
            PsseTransformerSecondRecordData() {
                super(INTERNAL_TRANSFORMER_SECOND_RECORD, "r12", "x12", "sbase12", "r23", "x23", "sbase23", "r31", "x31", "sbase31", "vmstar", "anstar");
            }

            @Override
            public Class<TransformerSecondRecord> psseTypeClass() {
                return TransformerSecondRecord.class;
            }
        }

        private static class PsseTransformerWindingRecordData extends AbstractRecordGroup<TransformerWindingRecord> {
            PsseTransformerWindingRecordData() {
                super(INTERNAL_TRANSFORMER_WINDING);
                withFieldNames(V32, FIELD_NAMES_WINDING_32_33);
                withFieldNames(V33, FIELD_NAMES_WINDING_32_33);
                withFieldNames(V35, "windv", "nomv", "ang", "wdgrate1", "wdgrate2", "wdgrate3", "wdgrate4", "wdgrate5", "wdgrate6", "wdgrate7", "wdgrate8", "wdgrate9", "wdgrate10", "wdgrate11", "wdgrate12", "cod", "cont", "node", "rma", "rmi", "vma", "vmi", "ntp", "tab", "cr", "cx", "cnxa");
            }

            @Override
            public Class<TransformerWindingRecord> psseTypeClass() {
                return TransformerWindingRecord.class;
            }
        }

        private static void setSecondRecord(PsseTransformer transformer, TransformerSecondRecord secondRecord) {
            transformer.setR12(secondRecord.r12);
            transformer.setX12(secondRecord.x12);
            transformer.setSbase12(secondRecord.sbase12);
            transformer.setR23(secondRecord.r23);
            transformer.setX23(secondRecord.x23);
            transformer.setSbase23(secondRecord.sbase23);
            transformer.setR31(secondRecord.r31);
            transformer.setX31(secondRecord.x31);
            transformer.setSbase31(secondRecord.sbase31);
            transformer.setVmstar(secondRecord.vmstar);
            transformer.setAnstar(secondRecord.anstar);
        }

        private TransformerSecondRecord getSecondRecord(PsseTransformer transformer) {
            TransformerSecondRecord secondRecord = new TransformerSecondRecord();
            secondRecord.r12 = transformer.getR12();
            secondRecord.x12 = transformer.getX12();
            secondRecord.sbase12 = transformer.getSbase12();
            secondRecord.r23 = transformer.getR23();
            secondRecord.x23 = transformer.getX23();
            secondRecord.sbase23 = transformer.getSbase23();
            secondRecord.r31 = transformer.getR31();
            secondRecord.x31 = transformer.getX31();
            secondRecord.sbase31 = transformer.getSbase31();
            secondRecord.vmstar = transformer.getVmstar();
            secondRecord.anstar = transformer.getAnstar();
            return secondRecord;
        }

        private TransformerWindingRecord getWindingRecord(PsseTransformerWinding winding, PsseRates windingRates) {
            TransformerWindingRecord windingRecord = new TransformerWindingRecord();
            windingRecord.winding = winding;
            windingRecord.windingRates = windingRates;
            return windingRecord;
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
    }

    public static class TransformerWindingRecord {
        @Nested()
        private PsseTransformerWinding winding;

        @Nested()
        private PsseRates windingRates;
    }

    public static class TransformerSecondRecord {
        @Parsed(field = {"r12", "r1_2"})
        private double r12 = 0;

        @Parsed(field = {"x12", "x1_2"})
        private double x12;

        @Parsed(field = {"sbase12", "sbase1_2"})
        private double sbase12 = Double.NaN;

        @Parsed(field = {"r23", "r2_3"})
        private double r23 = 0;

        @Parsed(field = {"x23", "x2_3"})
        private double x23 = Double.NaN;

        @Parsed(field = {"sbase23", "sbase2_3"})
        private double sbase23 = Double.NaN;

        @Parsed(field = {"r31", "r3_1"})
        private double r31 = 0;

        @Parsed(field = {"x31", "x3_1"})
        private double x31 = Double.NaN;

        @Parsed(field = {"sbase31", "sbase3_1"})
        private double sbase31 = Double.NaN;

        @Parsed
        private double vmstar = 1;

        @Parsed
        private double anstar = 0;
    }
}
