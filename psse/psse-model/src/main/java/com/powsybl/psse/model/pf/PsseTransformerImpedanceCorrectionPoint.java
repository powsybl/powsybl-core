/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseTransformerImpedanceCorrectionPoint extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseTransformerImpedanceCorrectionPoint, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_32_33 = {STR_T, STR_F};
    private static final String[] FIELD_NAMES_35 = {STR_T, STR_REF, STR_IMF};

    private double t = defaultDoubleFor(STR_T, FIELDS);

    @Revision(until = 33)
    private double f = defaultDoubleFor(STR_F, FIELDS);

    @Revision(since = 35)
    private double ref = defaultDoubleFor(STR_REF, FIELDS);

    @Revision(since = 35)
    private double imf = defaultDoubleFor(STR_IMF, FIELDS);

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static PsseTransformerImpedanceCorrectionPoint fromRecord(CsvRecord rec, String[] headers) {
        return fromRecord(rec, headers, "");
    }

    public static PsseTransformerImpedanceCorrectionPoint fromRecord(CsvRecord rec, String[] headers, String headerSuffix) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseTransformerImpedanceCorrectionPoint::new, headerSuffix);
    }

    public static void toRecord(PsseTransformerImpedanceCorrectionPoint point, String[] headers, String[] row,
                                Set<String> unexpectedHeaders, String headerSuffix) {
        Util.toRecord(point, headers, FIELDS, row, unexpectedHeaders, headerSuffix);
    }

    public static void toRecord(PsseTransformerImpedanceCorrectionPoint point, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        toRecord(point, headers, row, unexpectedHeaders, "");
    }

    public static String[] toRecord(PsseTransformerImpedanceCorrectionPoint point, String[] headers) {
        return Util.toRecord(point, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseTransformerImpedanceCorrectionPoint, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseTransformerImpedanceCorrectionPoint, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_T, Double.class, PsseTransformerImpedanceCorrectionPoint::getT, PsseTransformerImpedanceCorrectionPoint::setT, 0d));
        addField(fields, createNewField(STR_F, Double.class, PsseTransformerImpedanceCorrectionPoint::getF, PsseTransformerImpedanceCorrectionPoint::setF, 0d));
        addField(fields, createNewField(STR_REF, Double.class, PsseTransformerImpedanceCorrectionPoint::getRef, PsseTransformerImpedanceCorrectionPoint::setRef, 0d));
        addField(fields, createNewField(STR_IMF, Double.class, PsseTransformerImpedanceCorrectionPoint::getImf, PsseTransformerImpedanceCorrectionPoint::setImf, 0d));

        return fields;
    }

    public PsseTransformerImpedanceCorrectionPoint() {
    }

    public PsseTransformerImpedanceCorrectionPoint(double t, double f) {
        this.t = t;
        this.f = f;
    }

    public PsseTransformerImpedanceCorrectionPoint(double t, double ref, double imf) {
        this.t = t;
        this.ref = ref;
        this.imf = imf;
    }

    public double getT() {
        return t;
    }

    public void setT(double t) {
        this.t = t;
    }

    public double getF() {
        checkVersion("f");
        return f;
    }

    public void setF(double f) {
        checkVersion("f");
        this.f = f;
    }

    public double getRef() {
        checkVersion("ref");
        return ref;
    }

    public void setRef(double ref) {
        checkVersion("ref");
        this.ref = ref;
    }

    public double getImf() {
        checkVersion("imf");
        return imf;
    }

    public void setImf(double imf) {
        checkVersion("imf");
        this.imf = imf;
    }

    @JsonIgnore
    public boolean isNotDefault() {
        return t != 0.0 || f != 0.0 || ref != 0.0 || imf != 0.0;
    }
}
