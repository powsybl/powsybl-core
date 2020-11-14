/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseTransformer;
import com.powsybl.psse.model.PsseTransformer35;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TransformerData extends AbstractRecordGroup<PsseTransformer> {

    TransformerData() {
        super(PsseRecordGroup.TRANSFORMER_DATA);
    }

    @Override
    public String[] fieldNames(PsseVersion version) {
        throw new PsseException("XXX(Luma) fieldNames for PsseTransformer");
    }

    @Override
    public Class<? extends PsseTransformer> psseTypeClass(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return PsseTransformer35.class;
        } else {
            return PsseTransformer.class;
        }
    }

    @Override
    public List<PsseTransformer> read(BufferedReader reader, Context context) throws IOException {
        List<PsseTransformer> transformers = new ArrayList<>();

        List<String> records = Util.readRecords(reader);
        int i = 0;
        while (i < records.size()) {
            String record1 = records.get(i++);
            String record2 = records.get(i++);
            String record3 = records.get(i++);
            String record4 = records.get(i++);

            if (is3wtransformer(record1, context.getDelimiter())) {
                String record5 = records.get(i++);
                PsseTransformer transformer = transformer3wRecords(record1, record2, record3, record4, record5, context);
                transformers.add(transformer);
            } else {
                PsseTransformer transformer = transformer2wRecords(record1, record2, record3, record4, context);
                transformers.add(transformer);
            }
        }

        return transformers;
    }

    private PsseTransformer transformer3wRecords(String record1, String record2, String record3, String record4,
        String record5, Context context) {

        String twtRecord = String.join(context.getDelimiter(), record1, record2, record3, record4, record5);
        String[] headers = transformerDataHeaders(record1.split(context.getDelimiter()).length,
            record2.split(context.getDelimiter()).length,
            record3.split(context.getDelimiter()).length,
            record4.split(context.getDelimiter()).length,
            record5.split(context.getDelimiter()).length,
            context.getVersion());

        // XXX(Luma) we can not manage properly different field names for 2 and 3 winding records
        // because they all appear in the same record group
        if (context.is3wTransformerDataReadFieldsEmpty()) {
            context.setFieldNames(PsseRecordGroup.TRANSFORMER_3_DATA, headers);
        }

        return parseSingleRecord(twtRecord, headers, context);
    }

    private PsseTransformer transformer2wRecords(String record1, String record2, String record3, String record4,
        Context context) {

        String twtRecord = String.join(context.getDelimiter(), record1, record2, record3, record4);
        String[] headers = transformerDataHeaders(record1.split(context.getDelimiter()).length,
            record2.split(context.getDelimiter()).length,
            record3.split(context.getDelimiter()).length,
            record4.split(context.getDelimiter()).length,
            context.getVersion());

        // XXX(Luma) we can not manage properly different field names for 2 and 3 winding records
        if (context.is2wTransformerDataReadFieldsEmpty()) {
            context.setFieldNames(PsseRecordGroup.TRANSFORMER_2_DATA, headers);
        }

        return parseSingleRecord(twtRecord, headers, context);
    }

    @Override
    public List<PsseTransformer> read(JsonNode networkNode, Context context) {
        List<PsseTransformer> transformers = super.read(networkNode, context);
        // XXX(Luma) The fieldNames for transformers apply for 2 and 3 winding transformers
        context.setFieldNames(PsseRecordGroup.TRANSFORMER_2_DATA, context.getFieldNames(PsseRecordGroup.TRANSFORMER_DATA));
        context.setFieldNames(PsseRecordGroup.TRANSFORMER_3_DATA, context.getFieldNames(PsseRecordGroup.TRANSFORMER_DATA));
        return transformers;
    }

    private static boolean is3wtransformer(String record, String delimiter) {
        String[] tokens = record.split(delimiter);
        if (tokens.length < 3) {
            return false;
        }
        return Integer.parseInt(tokens[2].trim()) != 0;
    }

    private static void setRawxReadFields(List<String> records, String[] headers, Context context) {
        // XXX(Luma) we can not manage properly different field names for 2 and 3 winding records
        for (String record : records) {
            if (is3wtransformer(record, context.getDelimiter())) {
                if (context.is3wTransformerDataReadFieldsEmpty()) {
                    context.setFieldNames(PsseRecordGroup.TRANSFORMER_3_DATA, headers);
                }
            } else {
                if (context.is2wTransformerDataReadFieldsEmpty()) {
                    context.setFieldNames(PsseRecordGroup.TRANSFORMER_2_DATA, headers);
                }
            }
            if (!context.is3wTransformerDataReadFieldsEmpty() && !context.is2wTransformerDataReadFieldsEmpty()) {
                return;
            }
        }
    }

    private static String[] transformerDataHeaders(int record1Fields, int record2Fields, int record3Fields, int record4Fields, int record5Fields, PsseVersion version) {

        String[] headers = new String[] {};
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(transformerRecord1DataHeaders(version), 0, record1Fields));
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(transformerRecord2DataHeaders(version), 0, record2Fields));
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(transformerRecord3DataHeaders(version), 0, record3Fields));
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(transformerRecord4DataHeaders(version), 0, record4Fields));
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(transformerRecord5DataHeaders(version), 0, record5Fields));

        return headers;
    }

    private static String[] transformerDataHeaders(int record1Fields, int record2Fields, int record3Fields, int record4Fields, PsseVersion version) {

        String[] headers = new String[] {};
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(transformerRecord1DataHeaders(version), 0, record1Fields));
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(transformerRecord2DataHeaders(version), 0, record2Fields));
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(transformerRecord3DataHeaders(version), 0, record3Fields));
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(transformerRecord4DataHeaders(version), 0, record4Fields));

        return headers;
    }

    private static String[] transformerRecord1DataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"ibus", "jbus", "kbus", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmet", "name", "stat",
                "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "vecgrp", "zcod"};
        } else {
            return new String[] {"i", "j", "k", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmetr", "name", "stat",
                "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "vecgrp"};
        }
    }

    private static String[] transformerRecord2DataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"r1_2", "x1_2", "sbase1_2", "r2_3", "x2_3", "sbase2_3", "r3_1", "x3_1", "sbase3_1",
                "vmstar", "anstar"};
        } else {
            return new String[] {"r12", "x12", "sbase12", "r23", "x23", "sbase23", "r31", "x31", "sbase31", "vmstar", "anstar"};
        }
    }

    private static String[] transformerRecord3DataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"windv1", "nomv1", "ang1", "wdg1rate1", "wdg1rate2", "wdg1rate3", "wdg1rate4",
                "wdg1rate5", "wdg1rate6", "wdg1rate7", "wdg1rate8", "wdg1rate9", "wdg1rate10", "wdg1rate11", "wdg1rate12",
                "cod1", "cont1", "node1", "rma1", "rmi1", "vma1", "vmi1", "ntp1", "tab1", "cr1", "cx1", "cnxa1"};
        } else {
            return new String[] {"windv1", "nomv1", "ang1", "rata1", "ratb1", "ratc1", "cod1", "cont1", "rma1", "rmi1",
                "vma1", "vmi1", "ntp1", "tab1", "cr1", "cx1", "cnxa1"};
        }
    }

    private static String[] transformerRecord4DataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"windv2", "nomv2", "ang2", "wdg2rate1", "wdg2rate2", "wdg2rate3", "wdg2rate4",
                "wdg2rate5", "wdg2rate6", "wdg2rate7", "wdg2rate8", "wdg2rate9", "wdg2rate10", "wdg2rate11", "wdg2rate12",
                "cod2", "cont2", "node2", "rma2", "rmi2", "vma2", "vmi2", "ntp2", "tab2", "cr2", "cx2", "cnxa2"};
        } else {
            return new String[] {"windv2", "nomv2", "ang2", "rata2", "ratb2", "ratc2", "cod2", "cont2", "rma2", "rmi2",
                "vma2", "vmi2", "ntp2", "tab2", "cr2", "cx2", "cnxa2"};
        }
    }

    private static String[] transformerRecord5DataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"windv3", "nomv3", "ang3", "wdg3rate1", "wdg3rate2", "wdg3rate3", "wdg3rate4",
                "wdg3rate5", "wdg3rate6", "wdg3rate7", "wdg3rate8", "wdg3rate9", "wdg3rate10", "wdg3rate11", "wdg3rate12",
                "cod3", "cont3", "node3", "rma3", "rmi3", "vma3", "vmi3", "ntp3", "tab3", "cr3", "cx3", "cnxa3"};
        } else {
            return new String[] {"windv3", "nomv3", "ang3", "rata3", "ratb3", "ratc3", "cod3", "cont3", "rma3", "rmi3",
                "vma3", "vmi3", "ntp3", "tab3", "cr3", "cx3", "cnxa3"};
        }
    }
}
