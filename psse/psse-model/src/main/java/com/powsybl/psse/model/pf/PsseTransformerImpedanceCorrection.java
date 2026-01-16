/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.addSuffixToHeaders;
import static com.powsybl.psse.model.io.Util.checkForUnexpectedHeader;
import static com.powsybl.psse.model.io.Util.concatStringArrays;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseTransformerImpedanceCorrection extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseTransformerImpedanceCorrection, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_I = {STR_I};
    private static final String[] FIELD_NAMES_32_33 = concatStringArrays(FIELD_NAMES_I,
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames3233(), "1"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames3233(), "2"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames3233(), "3"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames3233(), "4"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames3233(), "5"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames3233(), "6"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames3233(), "7"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames3233(), "8"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames3233(), "9"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames3233(), "10"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames3233(), "11"));
    private static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_I,
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames35(), "1"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames35(), "2"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames35(), "3"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames35(), "4"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames35(), "5"),
        addSuffixToHeaders(PsseTransformerImpedanceCorrectionPoint.getFieldNames35(), "6"));

    private int i;
    private final List<PsseTransformerImpedanceCorrectionPoint> points = new ArrayList<>();

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseTransformerImpedanceCorrection fromRecord(CsvRecord rec, String[] headers) {
        PsseTransformerImpedanceCorrection impedanceCorrection = Util.fromRecord(rec.getFields(), headers, FIELDS, PsseTransformerImpedanceCorrection::new);
        int expectedPoints = headers[headers.length - 1].contains(STR_IMF) ? (headers.length - 1) / 3 : (headers.length - 1) / 2;
        for (int j = 0; j < expectedPoints; j++) {
            PsseTransformerImpedanceCorrectionPoint point = PsseTransformerImpedanceCorrectionPoint.fromRecord(rec, headers, String.valueOf(j + 1));
            if (point.isNotDefault()) {
                impedanceCorrection.getPoints().add(point);
            }
        }
        return impedanceCorrection;
    }

    public static String[] toRecord(PsseTransformerImpedanceCorrection impedanceCorrection, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = Util.toRecord(impedanceCorrection, headers, FIELDS, unexpectedHeaders);
        for (int i = 0; i < impedanceCorrection.getPoints().size(); i++) {
            PsseTransformerImpedanceCorrectionPoint.toRecord(impedanceCorrection.getPoints().get(i), headers, recordValues, unexpectedHeaders, String.valueOf(i + 1));
        }
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    private static Map<String, PsseFieldDefinition<PsseTransformerImpedanceCorrection, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseTransformerImpedanceCorrection, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseTransformerImpedanceCorrection::getI, PsseTransformerImpedanceCorrection::setI));

        return fields;
    }

    public PsseTransformerImpedanceCorrection() {
    }

    public PsseTransformerImpedanceCorrection(int i) {
        this.i = i;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public List<PsseTransformerImpedanceCorrectionPoint> getPoints() {
        return points;
    }

}
