/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.FileFormat;
import com.powsybl.psse.model.io.LegacyTextReader;
import com.powsybl.psse.model.io.RecordGroupIOJson;
import com.powsybl.psse.model.io.RecordGroupIOLegacyText;
import com.powsybl.psse.model.pf.PsseTransformerImpedanceCorrection;
import com.powsybl.psse.model.pf.PsseTransformerImpedanceCorrectionPoint;
import com.powsybl.psse.model.pf.internal.ZCorr33;
import com.powsybl.psse.model.pf.internal.ZCorr35First;
import com.powsybl.psse.model.pf.internal.ZCorr35Points;
import com.powsybl.psse.model.pf.internal.ZCorr35X;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRecord;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        withFieldNames(V32, PsseTransformerImpedanceCorrection.getFieldNames3233());
        withFieldNames(V33, PsseTransformerImpedanceCorrection.getFieldNames3233());
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
            List<String> records = reader.readRecords();
            return super.recordGroup.readFromStrings(records, context);
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
                write(record, outputStream);
            });
            writeEnd(outputStream);
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
                super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, PsseTransformerImpedanceCorrection.getFieldNames3233());
                withQuotedFields();
            }

            @Override
            protected Class<ZCorr33> psseTypeClass() {
                return ZCorr33.class;
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

        IOLegacyText35(AbstractRecordGroup<PsseTransformerImpedanceCorrection> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTransformerImpedanceCorrection> read(LegacyTextReader reader, Context context) throws IOException {
            List<String> records = reader.readRecords();

            List<PsseTransformerImpedanceCorrection> impedanceCorrectionList = new ArrayList<>();

            int i = 0;
            while (i < records.size()) {
                boolean endPoint;
                List<String> headers = new ArrayList<>();
                StringBuilder fullRecord = new StringBuilder();
                // First line always contains the field "i"
                endPoint = addLineToRecord(fullRecord, records.get(i++), headers, PsseTransformerImpedanceCorrection.getFieldNames35(), context);

                // Then we can have an undefined number of lines
                while (i < records.size() && !endPoint) {
                    // Add the line to the full record
                    endPoint = addLineToRecord(fullRecord, records.get(i++), headers, PsseTransformerImpedanceCorrectionPoint.getFieldNames35(), context);
                }

                PsseTransformerImpedanceCorrection impedanceCorrection = super.recordGroup.parseSingleRecord(fullRecord.toString(), headers.toArray(headers.toArray(new String[0])), context);
                impedanceCorrectionList.add(impedanceCorrection);
            }

            return impedanceCorrectionList;
        }

        private boolean addLineToRecord(StringBuilder sb, String line, List<String> headerList, String[] headers, Context context) {
            if (!sb.toString().isEmpty()) {
                sb.append(context.getDelimiter());
            }
            sb.append(line);
            headerList.addAll(Arrays.asList(headers));
            return checkIfLastPoint(line, context);
        }

        private static boolean checkIfLastPoint(String line, Context context) {
            try (var csv = CsvReader.builder()
                .quoteCharacter(context.getQuote())
                .fieldSeparator(context.getDelimiter())
                .ofCsvRecord(new StringReader(line))) {
                for (CsvRecord rec : csv) {
                    if (rec.getFieldCount() < 3) {
                        return true;
                    }
                    for (int i = rec.getFieldCount() - 3; i < rec.getFieldCount(); i++) {
                        if (Double.parseDouble(rec.getField(i)) != 0.0) {
                            return false;
                        }
                    }
                }
                return true;
            } catch (IOException e) {
                return true;
            }
        }

        @Override
        public void write(List<PsseTransformerImpedanceCorrection> impedanceCorrectionList, Context context, OutputStream outputStream) {

            ZCorr35FirstData record1Data = new ZCorr35FirstData();
            ZCorr35PointsData record2Data = new ZCorr35PointsData();
            writeBegin(outputStream);

            impedanceCorrectionList.forEach(impedanceCorrection -> {

                int indexPoints = 0;
                ZCorr35First r1 = convertToRecord1(impedanceCorrection, indexPoints);
                String[] writeHeaders = ArrayUtils.subarray(PsseTransformerImpedanceCorrection.getFieldNames35(), 0, 1 + 3 * pointsInsideRecord(indexPoints, impedanceCorrection.getPoints().size()));
                String record = record1Data.buildRecord(r1, writeHeaders, PsseTransformerImpedanceCorrection.getFieldNamesString(), context);
                write(record, outputStream);

                indexPoints = indexPoints + 6;
                // A (0.0, 0.0, 0.0) point must be added at the end so <=
                while (indexPoints <= impedanceCorrection.getPoints().size()) {
                    ZCorr35Points r2 = convertToRecord2(impedanceCorrection, indexPoints);
                    String[] writeHeadersPoints = ArrayUtils.subarray(PsseTransformerImpedanceCorrectionPoint.getFieldNames35(), 0, 3 * pointsInsideRecord(indexPoints, impedanceCorrection.getPoints().size()));
                    String recordPoints = record2Data.buildRecord(r2, writeHeadersPoints, PsseTransformerImpedanceCorrection.getFieldNamesString(), context);
                    write(recordPoints, outputStream);

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
            List<ZCorr35X> parserRecords = new PsseTransformerImpedanceCorrection35xParserRecordData().read(null, context);
            List<PsseTransformerImpedanceCorrection> records = new ArrayList<>();
            parserRecords.forEach(parserRecord -> convertToImpedanceCorrection(records, parserRecord));
            return records;
        }

        private static void convertToImpedanceCorrection(List<PsseTransformerImpedanceCorrection> impedanceCorrectionList, ZCorr35X parserRecord) {
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
            List<ZCorr35X> parserList = convertToParserList(impedanceCorrectionList);
            new PsseTransformerImpedanceCorrection35xParserRecordData().write(parserList, context, null);
        }

        private static List<ZCorr35X> convertToParserList(List<PsseTransformerImpedanceCorrection> impedanceCorrectionList) {
            List<ZCorr35X> parserList = new ArrayList<>();

            impedanceCorrectionList.forEach(impedanceCorrection -> impedanceCorrection.getPoints().forEach(point -> {
                ZCorr35X parserRecord = new ZCorr35X(
                    impedanceCorrection.getI(), point.getT(), point.getRef(), point.getImf());
                parserList.add(parserRecord);
            }));
            return parserList;
        }

        private static class PsseTransformerImpedanceCorrection35xParserRecordData extends AbstractRecordGroup<ZCorr35X> {
            PsseTransformerImpedanceCorrection35xParserRecordData() {
                super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES);
                withQuotedFields();
            }

            @Override
            protected Class<ZCorr35X> psseTypeClass() {
                return ZCorr35X.class;
            }
        }
    }

}
