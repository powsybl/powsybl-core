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
import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.io.Util;
import com.powsybl.psse.model.pf.PsseTransformer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.powsybl.psse.model.io.FileFormat.VALID_DELIMITERS;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TransformerData extends AbstractRecordGroup<PsseTransformer> {

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

    TransformerData() {
        super(PowerFlowRecordGroup.TRANSFORMER);
        withQuotedFields("ckt", "name", "vecgrp");
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

    @Override
    public String[] fieldNames(PsseVersion version) {
        throw new PsseException("Should not occur");
    }

    @Override
    protected String[][] getFieldNamesByLine(PsseVersion version, String line0) {
        return is3Winding(line0) ? fieldNames3(version) : fieldNames2(version);
    }

    public String[][] fieldNames3(PsseVersion version) {
        switch (version.major()) {
            case V35:
                return FIELD_NAMES_3_35;
            case V33:
                return FIELD_NAMES_3_33;
            default:
                throw new PsseException("Unsupported version " + version);
        }
    }

    public String[][] fieldNames2(PsseVersion version) {
        switch (version.major()) {
            case V35:
                return FIELD_NAMES_2_35;
            case V33:
                return FIELD_NAMES_2_33;
            default:
                throw new PsseException("Unsupported version " + version);
        }
    }

    @Override
    public Class<PsseTransformer> psseTypeClass() {
        return PsseTransformer.class;
    }

    @Override
    public List<PsseTransformer> readLegacyText(BufferedReader reader, Context context) throws IOException {
        return super.readLegacyTextMultiLineRecords(reader, context);
    }

    @Override
    public List<PsseTransformer> readJson(JsonNode networkNode, Context context) {
        List<PsseTransformer> transformers = super.readJson(networkNode, context);
        // Same field names for 2 and 3 winding transformers
        context.setFieldNames(PowerFlowRecordGroup.TRANSFORMER_2, context.getFieldNames(PowerFlowRecordGroup.TRANSFORMER));
        context.setFieldNames(PowerFlowRecordGroup.TRANSFORMER_3, context.getFieldNames(PowerFlowRecordGroup.TRANSFORMER));
        return transformers;
    }

    @Override
    public void writeLegacyText(List<PsseTransformer> transformers, Context context, OutputStream outputStream) {
        writeBegin(outputStream);

        List<PsseTransformer> transformerList2w = transformers.stream().filter(t -> t.getK() == 0).collect(Collectors.toList());
        if (!transformerList2w.isEmpty()) {
            String[] headers = context.getFieldNames(PowerFlowRecordGroup.TRANSFORMER_2);
            String[][] allFieldNames = fieldNames2(context.getVersion());
            this.<PsseTransformer>writeLegacyText(PsseTransformer.class, transformerList2w, allFieldNames, headers, context, outputStream, true);
        }

        List<PsseTransformer> transformerList3w = transformers.stream().filter(t -> t.getK() != 0).collect(Collectors.toList());
        if (!transformerList3w.isEmpty()) {
            String[] headers = context.getFieldNames(PowerFlowRecordGroup.TRANSFORMER_3);
            String[][] allFieldNames = fieldNames3(context.getVersion());
            this.<PsseTransformer>writeLegacyText(PsseTransformer.class, transformerList3w, allFieldNames, headers, context, outputStream, false);
        }

        writeEnd(outputStream);
    }

    private <T> void writeLegacyText(Class<T> aClass, List<T> transformerRecords, String[][] allFieldNames, String[] headers,
                                     Context context, OutputStream outputStream, boolean is2w) {

        // XXX(Luma) Before writing to the output stream
        // we are writing to a big array of strings simply to check that all corresponding sub-records
        // of all transformers have the same number of fields
        // we can get rid of that check and write directly to the outputstream

        String[] headers1 = Util.intersection(allFieldNames[0], headers);
        List<String> r1 = buildRecords(aClass, transformerRecords, headers1, Util.intersection(quotedFields(), headers1), context);

        String[] headers2 = Util.intersection(allFieldNames[1], headers);
        List<String> r2 = buildRecords(aClass, transformerRecords, headers2, Util.intersection(quotedFields(), headers2), context);

        String[] headers3 = Util.intersection(allFieldNames[2], headers);
        List<String> r3 = buildRecords(aClass, transformerRecords, headers3, Util.intersection(quotedFields(), headers3), context);

        String[] headers4 = Util.intersection(allFieldNames[3], headers);
        List<String> r4 = buildRecords(aClass, transformerRecords, headers4, Util.intersection(quotedFields(), headers4), context);

        if (is2w) {
            write2wRecords(r1, r2, r3, r4, outputStream);
        } else {
            String[] headers5 = Util.intersection(allFieldNames[4], headers);
            List<String> r5 = buildRecords(aClass, transformerRecords, headers5, Util.intersection(quotedFields(), headers5), context);

            write3wRecords(r1, r2, r3, r4, r5, outputStream);
        }
    }

    private static void write2wRecords(List<String> r1, List<String> r2, List<String> r3, List<String> r4,
        OutputStream outputStream) {
        if (r1.size() == r2.size() && r1.size() == r3.size() && r1.size() == r4.size()) {

            List<String> mixList = new ArrayList<>();
            for (int i = 0; i < r1.size(); i++) {
                mixList.add(r1.get(i));
                mixList.add(r2.get(i));
                mixList.add(r3.get(i));
                mixList.add(r4.get(i));
            }
            Util.writeListString(mixList, outputStream);
        } else {
            throw new PsseException("Psse: 2wTransformer. Transformer records do not match " +
                String.format("%d %d %d %d", r1.size(), r2.size(), r3.size(), r4.size()));
        }
    }

    private static void write3wRecords(List<String> r1, List<String> r2, List<String> r3, List<String> r4,
        List<String> r5, OutputStream outputStream) {
        if (r1.size() == r2.size() && r1.size() == r3.size() && r1.size() == r4.size() && r1.size() == r5.size()) {

            List<String> mixList = new ArrayList<>();
            for (int i = 0; i < r1.size(); i++) {
                mixList.add(r1.get(i));
                mixList.add(r2.get(i));
                mixList.add(r3.get(i));
                mixList.add(r4.get(i));
                mixList.add(r5.get(i));
            }
            Util.writeListString(mixList, outputStream);
        } else {
            throw new PsseException("Psse: 3wTransformer. Transformer records do not match " +
                String.format("%d %d %d %d %d", r1.size(), r2.size(), r3.size(), r4.size(), r5.size()));
        }
    }

}
