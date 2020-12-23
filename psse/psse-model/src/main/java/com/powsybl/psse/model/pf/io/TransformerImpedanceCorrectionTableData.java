/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import com.powsybl.psse.model.io.*;
import com.powsybl.psse.model.pf.PsseTransformerImpedanceCorrectionTable;
import com.univocity.parsers.annotations.Parsed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.INTERNAL_TRANSFORMER_IMPEDANCE_CORRECTION_TABLE;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.TRANSFORMER_IMPEDANCE_CORRECTION_TABLES;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TransformerImpedanceCorrectionTableData extends AbstractRecordGroup<PsseTransformerImpedanceCorrectionTable> {

    TransformerImpedanceCorrectionTableData() {
        super(TRANSFORMER_IMPEDANCE_CORRECTION_TABLES);
        withIO(FileFormat.LEGACY_TEXT, V33, new IOLegacyText33(this));
        withIO(FileFormat.LEGACY_TEXT, V35, new IOLegacyText35(this));
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

    private static class IOLegacyText33 extends RecordGroupIOLegacyText<PsseTransformerImpedanceCorrectionTable> {

        IOLegacyText33(AbstractRecordGroup<PsseTransformerImpedanceCorrectionTable> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTransformerImpedanceCorrectionTable> read(BufferedReader reader, Context context) throws IOException {
            return new ZCorrData()
                .read(reader, context)
                .stream()
                .map(zcorr -> new PsseTransformerImpedanceCorrectionTable(zcorr.getI(), zcorr.mapImpedanceCorrectionTableEntries()))
                .collect(Collectors.toList());
        }

        @Override
        public void write(List<PsseTransformerImpedanceCorrectionTable> transformers, Context context, OutputStream outputStream) {
            writeBegin(outputStream);
            writeEnd(outputStream);
            // XXX(Luma) pending implementation
            throw new PsseException("Not implemented");
        }
    }

    private static class IOLegacyText35 extends AbstractRecordGroupIOLegacyTextMultiLine<PsseTransformerImpedanceCorrectionTable> {

        IOLegacyText35(AbstractRecordGroup<PsseTransformerImpedanceCorrectionTable> recordGroup) {
            super(recordGroup);
        }

        private static final String[][] FIELD_NAMES = {
            {"i", "t1", "ref1", "imf1", "t2", "ref2", "imf2", "t3", "ref3", "imf3"},
            {"t1", "ref1", "imf1", "t2", "ref2", "imf2", "t3", "ref3", "imf3"},
        };

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

        @Override
        protected MultiLineRecord readMultiLineRecord(List<String> recordsLines, int currentLine, Context context) {
            List<String> lines = new ArrayList<>();
            for (int k = currentLine; k < recordsLines.size(); k++) {
                String line = recordsLines.get(k);
                lines.add(line);
                if (isLastLine(line, context)) {
                    break;
                }
            }
            String[][] fieldNamesByLine = new String[lines.size()][];
            fieldNamesByLine[0] = FIELD_NAMES[0];
            for (int k = 1; k < fieldNamesByLine.length; k++) {
                fieldNamesByLine[k] = FIELD_NAMES[1];
            }
            return new MultiLineRecord(fieldNamesByLine, lines.toArray(new String[0]));
        }

        @Override
        protected PsseTransformerImpedanceCorrectionTable parseMultiLineRecord(MultiLineRecord mlrecord, Context context) {
            ZCorrData zCorrData = new ZCorrData();
            List<ZCorr> zcorrs = new ArrayList<>();
            for (int k = 0; k < mlrecord.getLines().length; k++) {
                zcorrs.add(zCorrData.parseSingleRecord(mlrecord.getLines()[k], mlrecord.getFieldNamesByLine()[k], context));
            }

            int i = zcorrs.get(0).getI();
            List<PsseTransformerImpedanceCorrectionTable.Entry> entries = zcorrs.stream()
                .map(l -> l.mapImpedanceCorrectionTableEntries())
                .flatMap(List::stream)
                .collect(Collectors.toList());
            PsseTransformerImpedanceCorrectionTable t = new PsseTransformerImpedanceCorrectionTable(i, entries);
            return t;
        }

        @Override
        public void write(List<PsseTransformerImpedanceCorrectionTable> transformers, Context context, OutputStream outputStream) {
            writeBegin(outputStream);
            writeEnd(outputStream);
            // XXX(Luma) pending implementation
            throw new PsseException("Not implemented");
        }
    }

    static class ZCorrData extends AbstractRecordGroup<ZCorr> {
        ZCorrData() {
            super(INTERNAL_TRANSFORMER_IMPEDANCE_CORRECTION_TABLE);
            // XXX(Luma) should complete to t11
            withFieldNames(V33, "i", "t1", "f1", "t2", "f2", "t3", "f3");
        }

        @Override
        public Class<ZCorr> psseTypeClass() {
            return ZCorr.class;
        }
    }

    @JsonPropertyOrder(alphabetic = false)
    public static class ZCorr extends PsseVersioned {

        @Parsed(field = {"i", "itable"})
        private int i;

        // XXX(Luma) First attempt to read impedance correction tables, we define as fields a subset of taps and factors (only 3 sets)
        // XXX(Luma) For RAWX 35 there is a DataTable of fields: (itable, tap, refact, imfat)

        @Parsed(field = {"t1", "tap1"})
        private double t1 = 0;

        @Parsed(field = {"f1", "ref1", "refact1"})
        private double ref1 = 0;

        @Parsed(field = {"imf1", "imfact1"})
        @Revision(since = 35)
        private double imf1 = 0;

        @Parsed(field = {"t2", "tap2"})
        private double t2 = 0;

        @Parsed(field = {"f2", "ref2"})
        private double ref2 = 0;

        @Parsed(field = {"imf2", "imfact2"})
        @Revision(since = 35)
        private double imf2 = 0;

        @Parsed(field = {"t3", "tap3"})
        private double t3 = 0;

        @Parsed(field = {"f3", "ref3"})
        private double ref3 = 0;

        @Parsed(field = {"imf3", "imfact3"})
        @Revision(since = 35)
        private double imf3 = 0;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public double getT1() {
            return t1;
        }

        public void setT1(double t1) {
            this.t1 = t1;
        }

        public double getT2() {
            return t2;
        }

        public void setT2(double t2) {
            this.t2 = t2;
        }

        public double getT3() {
            return t3;
        }

        public void setT3(double t3) {
            this.t3 = t3;
        }

        public double getRef1() {
            return ref1;
        }

        public void setRef1(double ref1) {
            this.ref1 = ref1;
        }

        public double getImf1() {
            return imf1;
        }

        public void setImf1(double imf1) {
            this.imf1 = imf1;
        }

        public double getRef2() {
            return ref2;
        }

        public void setRef2(double ref2) {
            this.ref2 = ref2;
        }

        public double getImf2() {
            return imf2;
        }

        public void setImf2(double imf2) {
            this.imf2 = imf2;
        }

        public double getRef3() {
            return ref3;
        }

        public void setRef3(double ref3) {
            this.ref3 = ref3;
        }

        public double getImf3() {
            return imf3;
        }

        public void setImf3(double imf3) {
            this.imf3 = imf3;
        }

        public List<PsseTransformerImpedanceCorrectionTable.Entry> mapImpedanceCorrectionTableEntries() {
            List<PsseTransformerImpedanceCorrectionTable.Entry> entries = new ArrayList<>(11);
            addIfNotEmpty(t1, ref1, imf1, entries);
            addIfNotEmpty(t2, ref2, imf2, entries);
            addIfNotEmpty(t3, ref3, imf3, entries);
            return entries;
        }

        private static void addIfNotEmpty(double tap, double ref, double imf, List<PsseTransformerImpedanceCorrectionTable.Entry> entries) {
            if (tap != 0.0 || ref != 0.0 || imf != 0.0) {
                entries.add(new PsseTransformerImpedanceCorrectionTable.Entry(tap, ref, imf));
            }
        }
    }
}
