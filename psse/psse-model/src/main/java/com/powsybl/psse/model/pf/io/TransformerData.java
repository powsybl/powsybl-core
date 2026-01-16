/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.FileFormat;
import com.powsybl.psse.model.io.LegacyTextReader;
import com.powsybl.psse.model.io.RecordGroupIOLegacyText;
import com.powsybl.psse.model.pf.PsseRates;
import com.powsybl.psse.model.pf.PsseTransformer;
import com.powsybl.psse.model.pf.PsseTransformerWinding;
import com.powsybl.psse.model.pf.internal.TransformerImpedances;
import com.powsybl.psse.model.pf.internal.TransformerWindingRecord;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.powsybl.psse.model.PsseVersion.Major.V32;
import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.io.FileFormat.VALID_DELIMITERS;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.INTERNAL_TRANSFORMER_IMPEDANCES;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.INTERNAL_TRANSFORMER_WINDING;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.TRANSFORMER;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TransformerData extends AbstractRecordGroup<PsseTransformer> {

    TransformerData() {
        super(TRANSFORMER);
        withFieldNames(V32, PsseTransformer.getFieldNames32());
        withFieldNames(V33, PsseTransformer.getFieldNames33());
        withFieldNames(V35, PsseTransformer.getFieldNames35());
        withQuotedFields(PsseTransformer.getFieldNamesString());
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
                transformer.setWinding1(windingList.get(indexWinding).getWinding(), windingList.get(indexWinding).getWindingRates());
                indexWinding = indexWinding + 1;
                transformer.setWinding2(windingList.get(indexWinding).getWinding(), windingList.get(indexWinding).getWindingRates());
                indexWinding = indexWinding + 1;
                if (transformer.getK() != 0) {
                    transformer.setWinding3(windingList.get(indexWinding).getWinding(), windingList.get(indexWinding).getWindingRates());
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
            List<String> impedanceRecordT2wList = impedanceRecordData.buildRecords(impedanceT2wList, TransformerImpedances.getFieldNamesT2W(), quotedFields, context);

            PsseTransformerWindingRecordData windingRecordData = new PsseTransformerWindingRecordData();
            String[] windingHeaders = context.getFieldNames(INTERNAL_TRANSFORMER_WINDING);
            List<String> windingRecordList = windingRecordData.buildRecords(windingList, windingHeaders, quotedFields, context);
            List<String> windingRecordT2wList = windingRecordData.buildRecords(windingT2wList, PsseTransformerWinding.getFieldNamesT2W(), quotedFields, context);

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
                super(INTERNAL_TRANSFORMER_IMPEDANCES, TransformerImpedances.getFieldNames35());
            }

            @Override
            public Class<TransformerImpedances> psseTypeClass() {
                return TransformerImpedances.class;
            }
        }

        private static class PsseTransformerWindingRecordData extends AbstractRecordGroup<TransformerWindingRecord> {
            PsseTransformerWindingRecordData() {
                super(INTERNAL_TRANSFORMER_WINDING);
                withFieldNames(V32, TransformerWindingRecord.getFieldNames3233());
                withFieldNames(V33, TransformerWindingRecord.getFieldNames3233());
                withFieldNames(V35, TransformerWindingRecord.getFieldNames35());
            }

            @Override
            public Class<TransformerWindingRecord> psseTypeClass() {
                return TransformerWindingRecord.class;
            }
        }

        private TransformerWindingRecord getWindingRecord(PsseTransformerWinding winding, PsseRates windingRates) {
            TransformerWindingRecord windingRecord = new TransformerWindingRecord();
            windingRecord.setWinding(winding);
            windingRecord.setWindingRates(windingRates);
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
}
