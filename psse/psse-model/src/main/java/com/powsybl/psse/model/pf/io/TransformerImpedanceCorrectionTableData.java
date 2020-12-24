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
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import com.powsybl.psse.model.pf.PsseTransformerImpedanceCorrectionTable;
import com.univocity.parsers.annotations.HeaderTransformer;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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

        // For version 33, RAW as a single-line record format with these fields:
        // I, T1, F1, T2, F2, T3, F3, ... T11, F11

        IOLegacyText33(AbstractRecordGroup<PsseTransformerImpedanceCorrectionTable> recordGroup) {
            super(recordGroup);
        }

        @Override
        public List<PsseTransformerImpedanceCorrectionTable> read(BufferedReader reader, Context context) throws IOException {
            return new ZCorrData()
                .read(reader, context)
                .stream()
                .map(ZCorr::toTable)
                .collect(Collectors.toList());
        }

        @Override
        public void write(List<PsseTransformerImpedanceCorrectionTable> transformerImpedanceCorrectionTables, Context context, OutputStream outputStream) {
            List<ZCorr> zcorrs = transformerImpedanceCorrectionTables
                .stream()
                .map(t -> ZCorr.fromTable(t))
                .collect(Collectors.toList());
            new ZCorrData().write(zcorrs, context, outputStream);
        }
    }

    private static class IOLegacyText35 extends AbstractRecordGroupIOLegacyTextMultiLine<PsseTransformerImpedanceCorrectionTable> {

        private static final String[][] FIELD_NAMES = {
            {"i", "t1", "ref1", "imf1", "t2", "ref2", "imf2", "t3", "ref3", "imf3", "t4", "ref4", "imf4", "t5", "ref5", "imf5", "t6", "ref6", "imf6"},
            {"t1", "ref1", "imf1", "t2", "ref2", "imf2", "t3", "ref3", "imf3", "t4", "ref4", "imf4", "t5", "ref5", "imf5", "t6", "ref6", "imf6"},
        };

        // The RAW record format for Transformer Impedance Correction Tables:
        // I, T1, Re(F1), Im(F1), T2, Re(F2), Im(F2), ... T6,  Re(F6),  Im(F6)
        //   T7, Re(F7), Im(F7), T8, Re(F8), Im(F8), ... T12, Re(F12), Im(F12)
        //   .
        //   .
        //   Tn, Re(Fn), Im(Fn), 0.0, 0.0, 0.0

        IOLegacyText35(AbstractRecordGroup<PsseTransformerImpedanceCorrectionTable> recordGroup) {
            super(recordGroup);
        }

        private boolean isLastLine(String line, Context context) {
            // From PSSE documentation:
            // "End of data for a table is specified by specifying an additional point
            // with the three values defining the point all specified as 0.0."
            String[] s = line.split("" + context.getDelimiter());
            int n = s.length;
            if (n < 3) {
                return true;
            }
            return Double.valueOf(s[n - 1]) == 0.0 && Double.valueOf(s[n - 2]) == 0.0 && Double.valueOf(s[n - 3]) == 0.0;
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
            // First line has different field names, it contains the table number "i" and 6 factors
            fieldNamesByLine[0] = FIELD_NAMES[0];
            // Rest of lines have the same fields, 6 factors
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
                .map(ZCorr::itemsToTableEntries)
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
            withFieldNames(V33, "i", "t1", "ref1", "t2", "ref2", "t3", "ref3", "t4", "ref4", "t5", "ref5", "t6", "ref6", "t7", "ref7", "t8", "ref8", "t9", "ref9", "t10", "ref10", "t11", "ref11");
        }

        @Override
        public Class<ZCorr> psseTypeClass() {
            return ZCorr.class;
        }
    }

    // XXX(Luma) For RAWX 35 there is a DataTable of fields: (itable, tap, refact, imfat)

    // Internal class to bridge the common model for correction tables and the specific format used in RAW files

    @JsonPropertyOrder(alphabetic = false)
    public static class ZCorr extends PsseVersioned {

        private static final int NUM_ITEMS = 11;

        @Parsed
        private int i;
        @Nested(headerTransformer = ZCorrItemHeaderTransformer.class, args = "1")
        private ZCorrItem item1;
        @Nested(headerTransformer = ZCorrItemHeaderTransformer.class, args = "2")
        private ZCorrItem item2;
        @Nested(headerTransformer = ZCorrItemHeaderTransformer.class, args = "3")
        private ZCorrItem item3;
        @Nested(headerTransformer = ZCorrItemHeaderTransformer.class, args = "4")
        private ZCorrItem item4;
        @Nested(headerTransformer = ZCorrItemHeaderTransformer.class, args = "5")
        private ZCorrItem item5;
        @Nested(headerTransformer = ZCorrItemHeaderTransformer.class, args = "6")
        private ZCorrItem item6;
        @Nested(headerTransformer = ZCorrItemHeaderTransformer.class, args = "7")
        private ZCorrItem item7;
        @Nested(headerTransformer = ZCorrItemHeaderTransformer.class, args = "8")
        private ZCorrItem item8;
        @Nested(headerTransformer = ZCorrItemHeaderTransformer.class, args = "9")
        private ZCorrItem item9;
        @Nested(headerTransformer = ZCorrItemHeaderTransformer.class, args = "10")
        private ZCorrItem item10;
        @Nested(headerTransformer = ZCorrItemHeaderTransformer.class, args = "11")
        private ZCorrItem item11;

        public static ZCorr fromTable(PsseTransformerImpedanceCorrectionTable table) {
            ZCorr zcorr = new ZCorr();
            zcorr.i = table.getI();
            List<ZCorrItem> items = new ArrayList<>(NUM_ITEMS);
            int numFactors = table.getFactors().size();
            int k = 0;
            for (k = 0; k < numFactors; k++) {
                ZCorrItem item = new ZCorrItem();
                item.t = table.getFactors().get(k).getTap();
                item.ref = table.getFactors().get(k).getReFactor();
                item.imf = table.getFactors().get(k).getImFactor();
                items.add(item);
            }
            for (; k < NUM_ITEMS; k++) {
                items.add(null);
            }
            zcorr.item1 = items.get(0);
            zcorr.item2 = items.get(1);
            zcorr.item3 = items.get(2);
            zcorr.item4 = items.get(3);
            zcorr.item5 = items.get(4);
            zcorr.item6 = items.get(5);
            zcorr.item7 = items.get(6);
            zcorr.item8 = items.get(7);
            zcorr.item9 = items.get(8);
            zcorr.item10 = items.get(9);
            zcorr.item11 = items.get(10);
            return zcorr;
        }

        public PsseTransformerImpedanceCorrectionTable toTable() {
            return new PsseTransformerImpedanceCorrectionTable(i, itemsToTableEntries());
        }

        public List<PsseTransformerImpedanceCorrectionTable.Entry> itemsToTableEntries() {
            List<PsseTransformerImpedanceCorrectionTable.Entry> entries = new ArrayList<>(11);
            for (ZCorrItem item : Arrays.asList(item1, item2, item3, item4, item5, item6, item7, item8, item9, item10, item11)) {
                if (item.t == 0.0 && item.ref == 0.0 && item.imf == 0.0) {
                    break;
                }
                entries.add(new PsseTransformerImpedanceCorrectionTable.Entry(item.t, item.ref, item.imf));
            }
            return entries;
        }

        @Override
        public void setModel(PssePowerFlowModel model) {
            super.setModel(model);
            item1.setModel(model);
            item2.setModel(model);
            item3.setModel(model);
            item4.setModel(model);
            item5.setModel(model);
            item6.setModel(model);
            item7.setModel(model);
            item8.setModel(model);
            item9.setModel(model);
            item10.setModel(model);
            item11.setModel(model);
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public static class ZCorrItem extends PsseVersioned {
            @Parsed
            private double t = 0;

            @Parsed
            private double ref = 0;

            @Parsed
            @Revision(since = 35)
            private double imf = 0;

            public double getT() {
                return t;
            }

            public void setT(double t) {
                this.t = t;
            }

            public double getRef() {
                return ref;
            }

            public void setRef(double ref) {
                this.ref = ref;
            }

            public double getImf() {
                return imf;
            }

            public void setImf(double imf) {
                this.imf = imf;
            }
        }

        public static class ZCorrItemHeaderTransformer extends HeaderTransformer {
            private final String itemNumber;

            public ZCorrItemHeaderTransformer(String... args) {
                itemNumber = args[0];
            }

            @Override
            public String transformName(Field field, String name) {
                // Add "<itemNumber>" as a suffix
                return name + itemNumber;
            }
        }
    }
}
