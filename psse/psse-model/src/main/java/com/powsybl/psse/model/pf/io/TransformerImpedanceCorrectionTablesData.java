/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.io.*;
import com.powsybl.psse.model.pf.PsseTransformerImpedanceCorrection;
import com.powsybl.psse.model.pf.PsseTransformerImpedanceCorrectionPoint;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.powsybl.psse.model.PsseVersion.Major.V32;
import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.TRANSFORMER_IMPEDANCE_CORRECTION_TABLES;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TransformerImpedanceCorrectionTablesData extends AbstractRecordGroup<PsseTransformerImpedanceCorrection> {

    TransformerImpedanceCorrectionTablesData() {
        super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES);
        withIO(FileFormat.LEGACY_TEXT, V32, new IOLegacyText33(this));
        withIO(FileFormat.LEGACY_TEXT, V33, new IOLegacyText33(this));
        withIO(FileFormat.LEGACY_TEXT, V35, new IOLegacyText35(this));
        withIO(FileFormat.JSON, new IOJson(this));
    }

    @Override
    protected Class<PsseTransformerImpedanceCorrection> psseTypeClass() {
        return PsseTransformerImpedanceCorrection.class;
    }

    /**
     * For version 33, RAW format has a single-line record format with these fields:
     * I, T1, F1, T2, F2, T3, F3, ... T11, F11
     * A static inner class with exactly these fields is used as intermediate step for reading/writing the PSSE model
     */
    private static class IOLegacyText33 extends RecordGroupIOLegacyText<PsseTransformerImpedanceCorrection> {
        IOLegacyText33(AbstractRecordGroup<PsseTransformerImpedanceCorrection> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTransformerImpedanceCorrection> read(LegacyTextReader reader, Context context) throws IOException {
            List<ZCorr33> list33 = new ZCorr33Data().read(reader, context);
            return convertToImpedanceCorrectionList(list33);
        }

        @Override
        public void write(List<PsseTransformerImpedanceCorrection> impedanceCorrectionList, Context context, OutputStream outputStream) {
            writeBegin(outputStream);

            ZCorr33Data recordData = new ZCorr33Data();
            String[] headers = recordData.fieldNames(context.getVersion());
            String[] quotedFields = recordData.quotedFields();

            impedanceCorrectionList.forEach(impedanceCorrection -> {
                ZCorr33 parser33 = convertToTable(impedanceCorrection);
                // write only the read points. Each table can have different number of points
                String[] writeHeaders = ArrayUtils.subarray(headers, 0, 1 + 2 * impedanceCorrection.getPoints().size());
                String record = recordData.buildRecord(parser33, writeHeaders, quotedFields, context);
                write(String.format("%s%n", record), outputStream);
            });
            writeEnd(outputStream);
        }

        private static List<PsseTransformerImpedanceCorrection> convertToImpedanceCorrectionList(List<ZCorr33> recordList) {
            List<PsseTransformerImpedanceCorrection> impedanceCorrectionList = new ArrayList<>();
            recordList.forEach(record -> impedanceCorrectionList.add(convertToList(record)));
            return impedanceCorrectionList;
        }

        private static PsseTransformerImpedanceCorrection convertToList(ZCorr33 record) {

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

        private static ZCorr33 convertToTable(PsseTransformerImpedanceCorrection impedanceCorrectionTable) {

            ZCorr33 record = new ZCorr33();
            record.setI(impedanceCorrectionTable.getI());

            for (int i = 0; i < impedanceCorrectionTable.getPoints().size(); i++) {
                record.setTF(i + 1, impedanceCorrectionTable.getPoints().get(i).getT(), impedanceCorrectionTable.getPoints().get(i).getF());
            }

            return record;
        }

        private static class ZCorr33Data extends AbstractRecordGroup<ZCorr33> {
            ZCorr33Data() {
                super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, "i", "t1", "f1", "t2", "f2", "t3", "f3", "t4", "f4", "t5", "f5", "t6", "f6", "t7", "f7", "t8", "f8", "t9", "f9", "t10", "f10", "t11", "f11");
                withQuotedFields();
            }

            @Override
            protected Class<ZCorr33> psseTypeClass() {
                return ZCorr33.class;
            }
        }

        public static class ZCorr33 {

            @Parsed
            private int i;

            @Parsed
            private double t1 = 0.0;

            @Parsed
            private double f1 = 0.0;

            @Parsed
            private double t2 = 0.0;

            @Parsed
            private double f2 = 0.0;

            @Parsed
            private double t3 = 0.0;

            @Parsed
            private double f3 = 0.0;

            @Parsed
            private double t4 = 0.0;

            @Parsed
            private double f4 = 0.0;

            @Parsed
            private double t5 = 0.0;

            @Parsed
            private double f5 = 0.0;

            @Parsed
            private double t6 = 0.0;

            @Parsed
            private double f6 = 0.0;

            @Parsed
            private double t7 = 0.0;

            @Parsed
            private double f7 = 0.0;

            @Parsed
            private double t8 = 0.0;

            @Parsed
            private double f8 = 0.0;

            @Parsed
            private double t9 = 0.0;

            @Parsed
            private double f9 = 0.0;

            @Parsed
            private double t10 = 0.0;

            @Parsed
            private double f10 = 0.0;

            @Parsed
            private double t11 = 0.0;

            @Parsed
            private double f11 = 0.0;

            public int getI() {
                return i;
            }

            public void setI(int i) {
                this.i = i;
            }

            public double getT1() {
                return t1;
            }

            public double getF1() {
                return f1;
            }

            public double getT2() {
                return t2;
            }

            public double getF2() {
                return f2;
            }

            public double getT3() {
                return t3;
            }

            public double getF3() {
                return f3;
            }

            public double getT4() {
                return t4;
            }

            public double getF4() {
                return f4;
            }

            public double getT5() {
                return t5;
            }

            public double getF5() {
                return f5;
            }

            public double getT6() {
                return t6;
            }

            public double getF6() {
                return f6;
            }

            public double getT7() {
                return t7;
            }

            public double getF7() {
                return f7;
            }

            public double getT8() {
                return t8;
            }

            public double getF8() {
                return f8;
            }

            public double getT9() {
                return t9;
            }

            public double getF9() {
                return f9;
            }

            public double getT10() {
                return t10;
            }

            public double getF10() {
                return f10;
            }

            public double getT11() {
                return t11;
            }

            public double getF11() {
                return f11;
            }

            public void setTF(int point, double t, double f) {
                switch (point) {
                    case 1:
                        this.t1 = t;
                        this.f1 = f;
                        break;
                    case 2:
                        this.t2 = t;
                        this.f2 = f;
                        break;
                    case 3:
                        this.t3 = t;
                        this.f3 = f;
                        break;
                    case 4:
                        this.t4 = t;
                        this.f4 = f;
                        break;
                    case 5:
                        this.t5 = t;
                        this.f5 = f;
                        break;
                    case 6:
                        this.t6 = t;
                        this.f6 = f;
                        break;
                    case 7:
                        this.t7 = t;
                        this.f7 = f;
                        break;
                    case 8:
                        this.t8 = t;
                        this.f8 = f;
                        break;
                    case 9:
                        this.t9 = t;
                        this.f9 = f;
                        break;
                    case 10:
                        this.t10 = t;
                        this.f10 = f;
                        break;
                    case 11:
                        this.t11 = t;
                        this.f11 = f;
                        break;
                    default:
                        throw new PsseException("Unexpected point " + point);
                }
            }
        }
    }

    /**
     * The RAW record format for Transformer Impedance Correction Tables:
     * I, T1, Re(F1), Im(F1), T2, Re(F2), Im(F2), ... T6,  Re(F6),  Im(F6)
     *    T7, Re(F7), Im(F7), T8, Re(F8), Im(F8), ... T12, Re(F12), Im(F12)
     *    .
     *    .
     *    Tn, Re(Fn), Im(Fn), 0.0, 0.0, 0.0
     */
    private static class IOLegacyText35 extends RecordGroupIOLegacyText<PsseTransformerImpedanceCorrection> {

        private static final String[][] FIELD_NAMES = {
            {"i", "t1", "ref1", "imf1", "t2", "ref2", "imf2", "t3", "ref3", "imf3", "t4", "ref4", "imf4", "t5", "ref5", "imf5", "t6", "ref6", "imf6"},
            {"t1", "ref1", "imf1", "t2", "ref2", "imf2", "t3", "ref3", "imf3", "t4", "ref4", "imf4", "t5", "ref5", "imf5", "t6", "ref6", "imf6"},
        };
        private static final String[] QUOTED_FIELDS = {};

        IOLegacyText35(AbstractRecordGroup<PsseTransformerImpedanceCorrection> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTransformerImpedanceCorrection> read(LegacyTextReader reader, Context context) throws IOException {
            List<String> records = reader.readRecords();

            ZCorr35FirstData record1Data = new ZCorr35FirstData();
            ZCorr35PointsData record2Data = new ZCorr35PointsData();

            List<PsseTransformerImpedanceCorrection> impedanceCorrectionList = new ArrayList<>();

            int i = 0;
            while (i < records.size()) {
                ZCorr35First r1 = record1Data.parseSingleRecord(records.get(i++), FIELD_NAMES[0], context);

                PsseTransformerImpedanceCorrection impedanceCorrection = new PsseTransformerImpedanceCorrection(r1.getI());
                boolean endPoints = addImpedanceCorrectionPoints(impedanceCorrection, r1.getPoints());

                while (i < records.size() && !endPoints) {
                    ZCorr35Points r2 = record2Data.parseSingleRecord(records.get(i++), FIELD_NAMES[1], context);
                    endPoints = addImpedanceCorrectionPoints(impedanceCorrection, r2);
                }
                if (!impedanceCorrection.getPoints().isEmpty()) {
                    impedanceCorrectionList.add(impedanceCorrection);
                }
            }

            return impedanceCorrectionList;
        }

        private static boolean addImpedanceCorrectionPoints(PsseTransformerImpedanceCorrection impedanceCorrection,
            ZCorr35Points record2) {
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

            ZCorr35FirstData record1Data = new ZCorr35FirstData();
            ZCorr35PointsData record2Data = new ZCorr35PointsData();
            writeBegin(outputStream);

            impedanceCorrectionList.forEach(impedanceCorrection -> {

                int indexPoints = 0;
                ZCorr35First r1 = convertToRecord1(impedanceCorrection, indexPoints);
                String[] writeHeaders = ArrayUtils.subarray(FIELD_NAMES[0], 0, 1 + 3 * pointsInsideRecord(indexPoints, impedanceCorrection.getPoints().size()));
                String record = record1Data.buildRecord(r1, writeHeaders, QUOTED_FIELDS, context);
                write(String.format("%s%n", record), outputStream);

                indexPoints = indexPoints + 6;
                // A (0.0, 0.0, 0.0) point must be added at the end so <=
                while (indexPoints <= impedanceCorrection.getPoints().size()) {
                    ZCorr35Points r2 = convertToRecord2(impedanceCorrection, indexPoints);
                    String[] writeHeadersPoints = ArrayUtils.subarray(FIELD_NAMES[1], 0, 3 * pointsInsideRecord(indexPoints, impedanceCorrection.getPoints().size()));
                    String recordPoints = record2Data.buildRecord(r2, writeHeadersPoints, QUOTED_FIELDS, context);
                    write(String.format("%s%n", recordPoints), outputStream);

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

        private static ZCorr35First convertToRecord1(PsseTransformerImpedanceCorrection impedanceCorrection, int indexPoints) {
            ZCorr35First record1 = new ZCorr35First();
            record1.setI(impedanceCorrection.getI());
            record1.setPoints(convertToRecord2(impedanceCorrection, indexPoints));
            return record1;
        }

        private static ZCorr35Points convertToRecord2(PsseTransformerImpedanceCorrection impedanceCorrection, int indexPoints) {
            ZCorr35Points record2 = new ZCorr35Points();
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

        private static class ZCorr35FirstData extends AbstractRecordGroup<ZCorr35First> {
            ZCorr35FirstData() {
                super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES);
            }

            @Override
            protected Class<ZCorr35First> psseTypeClass() {
                return ZCorr35First.class;
            }
        }

        private static class ZCorr35PointsData extends AbstractRecordGroup<ZCorr35Points> {
            ZCorr35PointsData() {
                super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES);
            }

            @Override
            protected Class<ZCorr35Points> psseTypeClass() {
                return ZCorr35Points.class;
            }
        }

        public static class ZCorr35First {

            @Parsed
            private int i;

            @Nested
            private ZCorr35Points points;

            public int getI() {
                return i;
            }

            public void setI(int i) {
                this.i = i;
            }

            public ZCorr35Points getPoints() {
                return points;
            }

            public void setPoints(ZCorr35Points points) {
                this.points = points;
            }
        }

        public static class ZCorr35Points {

            @Parsed
            private double t1 = 0.0;

            @Parsed
            private double ref1 = 0.0;

            @Parsed
            private double imf1 = 0.0;

            @Parsed
            private double t2 = 0.0;

            @Parsed
            private double ref2 = 0.0;

            @Parsed
            private double imf2 = 0.0;

            @Parsed
            private double t3 = 0.0;

            @Parsed
            private double ref3 = 0.0;

            @Parsed
            private double imf3 = 0.0;

            @Parsed
            private double t4 = 0.0;

            @Parsed
            private double ref4 = 0.0;

            @Parsed
            private double imf4 = 0.0;

            @Parsed
            private double t5 = 0.0;

            @Parsed
            private double ref5 = 0.0;

            @Parsed
            private double imf5 = 0.0;

            @Parsed
            private double t6 = 0.0;

            @Parsed
            private double ref6 = 0.0;

            @Parsed
            private double imf6 = 0.0;

            public double getT1() {
                return t1;
            }

            public double getRef1() {
                return ref1;
            }

            public double getImf1() {
                return imf1;
            }

            public double getT2() {
                return t2;
            }

            public double getRef2() {
                return ref2;
            }

            public double getImf2() {
                return imf2;
            }

            public double getT3() {
                return t3;
            }

            public double getRef3() {
                return ref3;
            }

            public double getImf3() {
                return imf3;
            }

            public double getT4() {
                return t4;
            }

            public double getRef4() {
                return ref4;
            }

            public double getImf4() {
                return imf4;
            }

            public double getT5() {
                return t5;
            }

            public double getRef5() {
                return ref5;
            }

            public double getImf5() {
                return imf5;
            }

            public double getT6() {
                return t6;
            }

            public double getRef6() {
                return ref6;
            }

            public double getImf6() {
                return imf6;
            }

            public void setTF(int point, double t, double ref, double imf) {
                switch (point) {
                    case 1:
                        this.t1 = t;
                        this.ref1 = ref;
                        this.imf1 = imf;
                        break;
                    case 2:
                        this.t2 = t;
                        this.ref2 = ref;
                        this.imf2 = imf;
                        break;
                    case 3:
                        this.t3 = t;
                        this.ref3 = ref;
                        this.imf3 = imf;
                        break;
                    case 4:
                        this.t4 = t;
                        this.ref4 = ref;
                        this.imf4 = imf;
                        break;
                    case 5:
                        this.t5 = t;
                        this.ref5 = ref;
                        this.imf5 = imf;
                        break;
                    case 6:
                        this.t6 = t;
                        this.ref6 = ref;
                        this.imf6 = imf;
                        break;
                    default:
                        throw new PsseException("Unexpected point " + point);
                }
            }
        }

        public static class ZCorr35X {

            public ZCorr35X() {
            }

            public ZCorr35X(int itable, double tap, double refact, double imfact) {
                this.itable = itable;
                this.tap = tap;
                this.refact = refact;
                this.imfact = imfact;
            }

            @Parsed
            private int itable;

            @Parsed
            private double tap;

            @Parsed
            private double refact;

            @Parsed
            private double imfact;

            public int getItable() {
                return itable;
            }

            public double getTap() {
                return tap;
            }

            public double getRefact() {
                return refact;
            }

            public double getImfact() {
                return imfact;
            }
        }
    }

    private static class IOJson extends RecordGroupIOJson<PsseTransformerImpedanceCorrection> {
        IOJson(AbstractRecordGroup<PsseTransformerImpedanceCorrection> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTransformerImpedanceCorrection> read(LegacyTextReader reader, Context context) throws IOException {
            if (reader != null) {
                throw new PsseException("Unexpected reader. Should be null");
            }
            List<IOLegacyText35.ZCorr35X> parserRecords = new PsseTransformerImpedanceCorrection35xParserRecordData().read(null, context);
            List<PsseTransformerImpedanceCorrection> records = new ArrayList<>();
            parserRecords.forEach(parserRecord -> convertToImpedanceCorrection(records, parserRecord));
            return records;
        }

        private static void convertToImpedanceCorrection(List<PsseTransformerImpedanceCorrection> impedanceCorrectionList, IOLegacyText35.ZCorr35X parserRecord) {
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
            List<IOLegacyText35.ZCorr35X> parserList = convertToParserList(impedanceCorrectionList);
            new PsseTransformerImpedanceCorrection35xParserRecordData().write(parserList, context, null);
        }

        private static List<IOLegacyText35.ZCorr35X> convertToParserList(List<PsseTransformerImpedanceCorrection> impedanceCorrectionList) {
            List<IOLegacyText35.ZCorr35X> parserList = new ArrayList<>();

            impedanceCorrectionList.forEach(impedanceCorrection -> impedanceCorrection.getPoints().forEach(point -> {
                IOLegacyText35.ZCorr35X parserRecord = new IOLegacyText35.ZCorr35X(
                    impedanceCorrection.getI(), point.getT(), point.getRef(), point.getImf());
                parserList.add(parserRecord);
            }));
            return parserList;
        }

        private static class PsseTransformerImpedanceCorrection35xParserRecordData extends AbstractRecordGroup<IOLegacyText35.ZCorr35X> {
            PsseTransformerImpedanceCorrection35xParserRecordData() {
                super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES);
                withQuotedFields();
            }

            @Override
            protected Class<IOLegacyText35.ZCorr35X> psseTypeClass() {
                return IOLegacyText35.ZCorr35X.class;
            }
        }
    }

}
