/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_F1;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_F2;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_F3;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_F4;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_O1;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_O2;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_O3;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_O4;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseOwnership extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseOwnership, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES = {STR_O1, STR_F1, STR_O2, STR_F2, STR_O3, STR_F3, STR_O4, STR_F4};

    private int o1 = defaultIntegerFor(STR_O1, FIELDS);
    private double f1 = defaultDoubleFor(STR_F1, FIELDS);
    private int o2 = defaultIntegerFor(STR_O2, FIELDS);
    private double f2 = defaultDoubleFor(STR_F2, FIELDS);
    private int o3 = defaultIntegerFor(STR_O3, FIELDS);
    private double f3 = defaultDoubleFor(STR_F3, FIELDS);
    private int o4 = defaultIntegerFor(STR_O4, FIELDS);
    private double f4 = defaultDoubleFor(STR_F4, FIELDS);

    public static String[] getFieldNames() {
        return FIELD_NAMES;
    }

    public static PsseOwnership fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseOwnership::new);
    }

    public static void toRecord(PsseOwnership psseOwnership, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        Util.toRecord(psseOwnership, headers, FIELDS, row, unexpectedHeaders);
    }

    public static String[] toRecord(PsseOwnership psseOwnership, String[] headers) {
        return Util.toRecord(psseOwnership, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseOwnership, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseOwnership, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_O1, Integer.class, PsseOwnership::getO1, PsseOwnership::setO1, -1));
        addField(fields, createNewField(STR_F1, Double.class, PsseOwnership::getF1, PsseOwnership::setF1, 1d));
        addField(fields, createNewField(STR_O2, Integer.class, PsseOwnership::getO2, PsseOwnership::setO2, 0));
        addField(fields, createNewField(STR_F2, Double.class, PsseOwnership::getF2, PsseOwnership::setF2, 1d));
        addField(fields, createNewField(STR_O3, Integer.class, PsseOwnership::getO3, PsseOwnership::setO3, 0));
        addField(fields, createNewField(STR_F3, Double.class, PsseOwnership::getF3, PsseOwnership::setF3, 1d));
        addField(fields, createNewField(STR_O4, Integer.class, PsseOwnership::getO4, PsseOwnership::setO4, 0));
        addField(fields, createNewField(STR_F4, Double.class, PsseOwnership::getF4, PsseOwnership::setF4, 1d));

        return fields;
    }

    public int getO1() {
        return o1;
    }

    public void setO1(int o1) {
        this.o1 = o1;
    }

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public int getO2() {
        return o2;
    }

    public void setO2(int o2) {
        this.o2 = o2;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public int getO3() {
        return o3;
    }

    public void setO3(int o3) {
        this.o3 = o3;
    }

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public int getO4() {
        return o4;
    }

    public void setO4(int o4) {
        this.o4 = o4;
    }

    public double getF4() {
        return f4;
    }

    public void setF4(double f4) {
        this.f4 = f4;
    }

    public PsseOwnership copy() {
        PsseOwnership copy = new PsseOwnership();
        copy.o1 = this.o1;
        copy.f1 = this.f1;
        copy.o2 = this.o2;
        copy.f2 = this.f2;
        copy.o3 = this.o3;
        copy.f3 = this.f3;
        copy.o4 = this.o4;
        copy.f4 = this.f4;
        return copy;
    }
}
