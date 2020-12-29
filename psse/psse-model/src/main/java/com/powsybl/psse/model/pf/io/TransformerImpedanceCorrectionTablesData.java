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
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.FileFormat;
import com.powsybl.psse.model.io.RecordGroupIOJson;
import com.powsybl.psse.model.io.RecordGroupIOLegacyText;
import com.powsybl.psse.model.pf.PsseTransformerImpedanceCorrection;
import com.powsybl.psse.model.pf.PsseTransformerImpedanceCorrection.PsseTransformerImpedanceCorrection33ParserRecord;
import com.powsybl.psse.model.pf.PsseTransformerImpedanceCorrection.PsseTransformerImpedanceCorrection35ParserRecord1;
import com.powsybl.psse.model.pf.PsseTransformerImpedanceCorrection.PsseTransformerImpedanceCorrection35ParserRecord2;
import com.powsybl.psse.model.pf.PsseTransformerImpedanceCorrection.PsseTransformerImpedanceCorrection35xParserRecord;
import com.powsybl.psse.model.pf.PsseTransformerImpedanceCorrectionPoint;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.TRANSFORMER_IMPEDANCE_CORRECTION_TABLES;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TransformerImpedanceCorrectionTablesData extends AbstractRecordGroup<PsseTransformerImpedanceCorrection> {

    TransformerImpedanceCorrectionTablesData() {
        super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES);
        withIO(FileFormat.LEGACY_TEXT, V33, new IOLegacyText33(this));
        withIO(FileFormat.LEGACY_TEXT, V35, new IOLegacyText35(this));
        withIO(FileFormat.JSON, new IOJson(this));
    }

    @Override
    public Class<PsseTransformerImpedanceCorrection> psseTypeClass() {
        return PsseTransformerImpedanceCorrection.class;
    }

    // For version 33, RAW as a single-line record format with these fields:
    // I, T1, F1, T2, F2, T3, F3, ... T11, F11
    private static class IOLegacyText33 extends RecordGroupIOLegacyText<PsseTransformerImpedanceCorrection> {
        IOLegacyText33(AbstractRecordGroup<PsseTransformerImpedanceCorrection> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTransformerImpedanceCorrection> read(BufferedReader reader, Context context) throws IOException {
            List<PsseTransformerImpedanceCorrection33ParserRecord> list33 = new PsseTransformerImpedanceCorrection33ParserRecordData().read(reader, context);
            return convertToImpedanceCorrectionList(list33);
        }

        @Override
        public void write(List<PsseTransformerImpedanceCorrection> impedanceCorrectionList, Context context, OutputStream outputStream) {
            writeBegin(outputStream);

            PsseTransformerImpedanceCorrection33ParserRecordData recordData = new PsseTransformerImpedanceCorrection33ParserRecordData();
            String[] headers = recordData.fieldNames(context.getVersion());
            String[] quotedFields = recordData.quotedFields();

            impedanceCorrectionList.forEach(impedanceCorrection -> {
                PsseTransformerImpedanceCorrection33ParserRecord parser33 = convertToTable(impedanceCorrection);
                // write only the read points. Each table can have different number of points
                String[] writeHeaders = ArrayUtils.subarray(headers, 0, 1 + 2 * impedanceCorrection.getPoints().size());
                String record = recordData.buildRecord(parser33, writeHeaders, quotedFields, context);
                write(String.format("%s%n", record), outputStream);
            });
            writeEnd(outputStream);
        }

        private static List<PsseTransformerImpedanceCorrection> convertToImpedanceCorrectionList(List<PsseTransformerImpedanceCorrection33ParserRecord> recordList) {
            List<PsseTransformerImpedanceCorrection> impedanceCorrectionList = new ArrayList<>();
            recordList.forEach(record -> impedanceCorrectionList.add(convertToList(record)));
            return impedanceCorrectionList;
        }

        private static PsseTransformerImpedanceCorrection convertToList(PsseTransformerImpedanceCorrection33ParserRecord record) {

            PsseTransformerImpedanceCorrection impedanceCorrection = new PsseTransformerImpedanceCorrection(record.getI());
            List<Double> list = Arrays.asList(record.getT1(), record.getF1(), record.getT2(), record.getF2(), record.getT3(), record.getF3(),
                record.getT4(), record.getF4(), record.getT5(), record.getF5(), record.getT6(), record.getF6(), record.getT7(), record.getF7(),
                record.getT8(), record.getF8(), record.getT9(), record.getF9(), record.getT10(), record.getF10(), record.getT11(), record.getF11());

            for (int i = 0; i < list.size(); i = i + 2) {
                if (validPoint(list.get(i), list.get(i + 1))) {
                    impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(list.get(i), list.get(i + 1)));
                }
            }
            return impedanceCorrection;
        }

        private static boolean validPoint(double t, double f) {
            return t != 0.0 && f != 0.0;
        }

        private static PsseTransformerImpedanceCorrection33ParserRecord convertToTable(PsseTransformerImpedanceCorrection impedanceCorrectionTable) {

            PsseTransformerImpedanceCorrection33ParserRecord record = new PsseTransformerImpedanceCorrection33ParserRecord();
            record.setI(impedanceCorrectionTable.getI());

            for (int i = 0; i < impedanceCorrectionTable.getPoints().size(); i++) {
                record.setTF(i + 1, impedanceCorrectionTable.getPoints().get(i).getT(), impedanceCorrectionTable.getPoints().get(i).getF());
            }

            return record;
        }

        private static class PsseTransformerImpedanceCorrection33ParserRecordData extends AbstractRecordGroup<PsseTransformerImpedanceCorrection33ParserRecord> {
            PsseTransformerImpedanceCorrection33ParserRecordData() {
                super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES);
                withFieldNames(V33, "i", "t1", "f1", "t2", "f2", "t3", "f3", "t4", "f4", "t5", "f5", "t6", "f6", "t7", "f7", "t8", "f8", "t9", "f9", "t10", "f10", "t11", "f11");
                withQuotedFields();
            }

            @Override
            public Class<PsseTransformerImpedanceCorrection33ParserRecord> psseTypeClass() {
                return PsseTransformerImpedanceCorrection33ParserRecord.class;
            }
        }
    }

    private static class IOLegacyText35 extends RecordGroupIOLegacyText<PsseTransformerImpedanceCorrection> {

        private static final String[][] FIELD_NAMES = {
            {"i", "t1", "ref1", "imf1", "t2", "ref2", "imf2", "t3", "ref3", "imf3", "t4", "ref4", "imf4", "t5", "ref5", "imf5", "t6", "ref6", "imf6"},
            {"t1", "ref1", "imf1", "t2", "ref2", "imf2", "t3", "ref3", "imf3", "t4", "ref4", "imf4", "t5", "ref5", "imf5", "t6", "ref6", "imf6"},
        };
        private static final String[] QUOTED_FIELDS = {};

        // The RAW record format for Transformer Impedance Correction Tables:
        // I, T1, Re(F1), Im(F1), T2, Re(F2), Im(F2), ... T6,  Re(F6),  Im(F6)
        //   T7, Re(F7), Im(F7), T8, Re(F8), Im(F8), ... T12, Re(F12), Im(F12)
        //   .
        //   .
        //   Tn, Re(Fn), Im(Fn), 0.0, 0.0, 0.0

        IOLegacyText35(AbstractRecordGroup<PsseTransformerImpedanceCorrection> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTransformerImpedanceCorrection> read(BufferedReader reader, Context context) throws IOException {
            List<String> records = readBlock(reader);

            PsseTransformerImpedanceCorrection35ParserRecord1Data record1Data = new PsseTransformerImpedanceCorrection35ParserRecord1Data();
            PsseTransformerImpedanceCorrection35ParserRecord2Data record2Data = new PsseTransformerImpedanceCorrection35ParserRecord2Data();

            List<PsseTransformerImpedanceCorrection> impedanceCorrectionList = new ArrayList<>();

            int i = 0;
            while (i < records.size()) {
                PsseTransformerImpedanceCorrection35ParserRecord1 r1 = record1Data.parseSingleRecord(records.get(i++), FIELD_NAMES[0], context);

                PsseTransformerImpedanceCorrection impedanceCorrection = new PsseTransformerImpedanceCorrection(r1.getI());
                boolean endPoints = addImpedanceCorrectionPoints(impedanceCorrection, r1.getRecord2());

                while (i < records.size() && !endPoints) {
                    PsseTransformerImpedanceCorrection35ParserRecord2 r2 = record2Data.parseSingleRecord(records.get(i++), FIELD_NAMES[1], context);
                    endPoints = addImpedanceCorrectionPoints(impedanceCorrection, r2);
                }
                if (!impedanceCorrection.getPoints().isEmpty()) {
                    impedanceCorrectionList.add(impedanceCorrection);
                }
            }

            return impedanceCorrectionList;
        }

        private static boolean addImpedanceCorrectionPoints(PsseTransformerImpedanceCorrection impedanceCorrection,
            PsseTransformerImpedanceCorrection35ParserRecord2 record2) {
            Objects.requireNonNull(record2);

            List<Double> list = Arrays.asList(record2.getT1(), record2.getRef1(), record2.getImf1(), record2.getT2(), record2.getRef2(), record2.getImf2(),
                record2.getT3(), record2.getRef3(), record2.getImf3(), record2.getT4(), record2.getRef4(), record2.getImf4(),
                record2.getT5(), record2.getRef5(), record2.getImf5(), record2.getT6(), record2.getRef6(), record2.getImf6());

            for (int i = 0; i < list.size(); i = i + 3) {
                if (endPoint(list.get(i), list.get(i + 1), list.get(i + 2))) {
                    return true;
                } else {
                    impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(list.get(i), list.get(i + 1), list.get(i + 2)));
                }
            }

            return false;
        }

        private static boolean endPoint(double t, double ref, double imf) {
            return t == 0.0 && ref == 0.0 && imf == 0.0;
        }

        @Override
        public void write(List<PsseTransformerImpedanceCorrection> impedanceCorrectionList, Context context, OutputStream outputStream) {

            PsseTransformerImpedanceCorrection35ParserRecord1Data record1Data = new PsseTransformerImpedanceCorrection35ParserRecord1Data();
            PsseTransformerImpedanceCorrection35ParserRecord2Data record2Data = new PsseTransformerImpedanceCorrection35ParserRecord2Data();
            writeBegin(outputStream);

            impedanceCorrectionList.forEach(impedanceCorrection -> {

                int indexPoints = 0;
                PsseTransformerImpedanceCorrection35ParserRecord1 r1 = convertToRecord1(impedanceCorrection, indexPoints);
                String[] writeHeaders = ArrayUtils.subarray(FIELD_NAMES[0], 0, 1 + 3 * pointsInsideRecord(indexPoints, impedanceCorrection.getPoints().size()));
                String record = record1Data.buildRecord(r1, writeHeaders, QUOTED_FIELDS, context);
                write(String.format("%s%n", record), outputStream);

                indexPoints = indexPoints + 6;
                // A (0.0, 0.0, 0.0) point must be added at the end so <=
                while (indexPoints <= impedanceCorrection.getPoints().size()) {
                    PsseTransformerImpedanceCorrection35ParserRecord2 r2 = convertToRecord2(impedanceCorrection, indexPoints);
                    writeHeaders = ArrayUtils.subarray(FIELD_NAMES[1], 0, 3 * pointsInsideRecord(indexPoints, impedanceCorrection.getPoints().size()));
                    record = record2Data.buildRecord(r2, writeHeaders, QUOTED_FIELDS, context);
                    write(String.format("%s%n", record), outputStream);

                    indexPoints = indexPoints + 6;
                }
            });

            writeEnd(outputStream);
        }

        private static int pointsInsideRecord(int indexPoints, int numPoints) {
            int pendingPoints = numPoints - indexPoints;
            if (pendingPoints >= 6) {
                return 6;
            } else {
                return pendingPoints + 1;
            }
        }

        private static PsseTransformerImpedanceCorrection35ParserRecord1 convertToRecord1(PsseTransformerImpedanceCorrection impedanceCorrection, int indexPoints) {
            PsseTransformerImpedanceCorrection35ParserRecord1 record1 = new PsseTransformerImpedanceCorrection35ParserRecord1();
            record1.setI(impedanceCorrection.getI());
            record1.setRecord2(convertToRecord2(impedanceCorrection, indexPoints));
            return record1;
        }

        private static PsseTransformerImpedanceCorrection35ParserRecord2 convertToRecord2(PsseTransformerImpedanceCorrection impedanceCorrection, int indexPoints) {
            PsseTransformerImpedanceCorrection35ParserRecord2 record2 = new PsseTransformerImpedanceCorrection35ParserRecord2();
            int pointNumber = 0;
            int index = indexPoints;
            while (index < impedanceCorrection.getPoints().size() && pointNumber < 6) {
                pointNumber++;
                PsseTransformerImpedanceCorrectionPoint point = impedanceCorrection.getPoints().get(index);
                record2.setTF(pointNumber, point.getT(), point.getRef(), point.getImf());
                index++;
            }
            if (pointNumber < 6) {
                pointNumber++;
                record2.setTF(pointNumber, 0.0, 0.0, 0.0);
            }
            return record2;
        }

        private static class PsseTransformerImpedanceCorrection35ParserRecord1Data extends AbstractRecordGroup<PsseTransformerImpedanceCorrection35ParserRecord1> {
            PsseTransformerImpedanceCorrection35ParserRecord1Data() {
                super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES);
            }

            @Override
            public Class<PsseTransformerImpedanceCorrection35ParserRecord1> psseTypeClass() {
                return PsseTransformerImpedanceCorrection35ParserRecord1.class;
            }
        }

        private static class PsseTransformerImpedanceCorrection35ParserRecord2Data extends AbstractRecordGroup<PsseTransformerImpedanceCorrection35ParserRecord2> {
            PsseTransformerImpedanceCorrection35ParserRecord2Data() {
                super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES);
            }

            @Override
            public Class<PsseTransformerImpedanceCorrection35ParserRecord2> psseTypeClass() {
                return PsseTransformerImpedanceCorrection35ParserRecord2.class;
            }
        }
    }

    private static class IOJson extends RecordGroupIOJson<PsseTransformerImpedanceCorrection> {
        IOJson(AbstractRecordGroup<PsseTransformerImpedanceCorrection> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTransformerImpedanceCorrection> read(BufferedReader reader, Context context) throws IOException {
            if (reader != null) {
                throw new PsseException("Unexpected reader. Should be null");
            }
            List<PsseTransformerImpedanceCorrection35xParserRecord> parserRecords = new PsseTransformerImpedanceCorrection35xParserRecordData().read(null, context);
            List<PsseTransformerImpedanceCorrection> records = new ArrayList<>();
            parserRecords.forEach(parserRecord -> convertToImpedanceCorrection(records, parserRecord));
            return records;
        }

        private static void convertToImpedanceCorrection(List<PsseTransformerImpedanceCorrection> impedanceCorrectionList, PsseTransformerImpedanceCorrection35xParserRecord parserRecord) {
            if (impedanceCorrectionList.isEmpty()) {
                PsseTransformerImpedanceCorrection impedanceCorrection = new PsseTransformerImpedanceCorrection(parserRecord.getItable());
                impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(parserRecord.getTap(), parserRecord.getRefact(), parserRecord.getImfact()));
                impedanceCorrectionList.add(impedanceCorrection);
            } else {
                PsseTransformerImpedanceCorrection lastImpedanceCorrection = impedanceCorrectionList.get(impedanceCorrectionList.size() - 1);
                if (lastImpedanceCorrection.getI() == parserRecord.getItable()) {
                    lastImpedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(parserRecord.getTap(), parserRecord.getRefact(), parserRecord.getImfact()));
                } else {
                    PsseTransformerImpedanceCorrection impedanceCorrection = new PsseTransformerImpedanceCorrection(parserRecord.getItable());
                    impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(parserRecord.getTap(), parserRecord.getRefact(), parserRecord.getImfact()));
                    impedanceCorrectionList.add(impedanceCorrection);
                }
            }
        }

        @Override
        public void write(List<PsseTransformerImpedanceCorrection> impedanceCorrectionList, Context context, OutputStream outputStream) {
            if (outputStream != null) {
                throw new PsseException("Unexpected outputStream. Should be null");
            }
            List<PsseTransformerImpedanceCorrection35xParserRecord> parserList =  convertToParserList(impedanceCorrectionList);
            new PsseTransformerImpedanceCorrection35xParserRecordData().write(parserList, context, null);
        }

        private static List<PsseTransformerImpedanceCorrection35xParserRecord> convertToParserList(List<PsseTransformerImpedanceCorrection> impedanceCorrectionList) {
            List<PsseTransformerImpedanceCorrection35xParserRecord> parserList = new ArrayList<>();

            impedanceCorrectionList.forEach(impedanceCorrection -> impedanceCorrection.getPoints().forEach(point -> {
                PsseTransformerImpedanceCorrection35xParserRecord parserRecord = new PsseTransformerImpedanceCorrection35xParserRecord(
                    impedanceCorrection.getI(), point.getT(), point.getRef(), point.getImf());
                parserList.add(parserRecord);
            }));
            return parserList;
        }

        private static class PsseTransformerImpedanceCorrection35xParserRecordData extends AbstractRecordGroup<PsseTransformerImpedanceCorrection35xParserRecord> {
            PsseTransformerImpedanceCorrection35xParserRecordData() {
                super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES);
                withQuotedFields();
            }

            @Override
            public Class<PsseTransformerImpedanceCorrection35xParserRecord> psseTypeClass() {
                return PsseTransformerImpedanceCorrection35xParserRecord.class;
            }
        }
    }
}
