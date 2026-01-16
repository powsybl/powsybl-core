/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashMap;
import java.util.Map;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.concatStringArrays;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_BL;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_GL;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_I;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_IBUS;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ID;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_SHNTID;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_STAT;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_STATUS;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseFixedShunt {

    private static final Map<String, PsseFieldDefinition<PsseFixedShunt, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_COMMON = {STR_GL, STR_BL};
    private static final String[] FIELD_NAMES_SPECIFIC_32_33 = {STR_I, STR_ID, STR_STATUS};
    private static final String[] FIELD_NAMES_SPECIFIC_35 = {STR_IBUS, STR_SHNTID, STR_STAT};
    private static final String[] FIELD_NAMES_32_33 = concatStringArrays(FIELD_NAMES_SPECIFIC_32_33, FIELD_NAMES_COMMON);
    private static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_SPECIFIC_35, FIELD_NAMES_COMMON);

    private int i;
    private String id;
    private int status = defaultIntegerFor(STR_STATUS, FIELDS);
    private double gl = defaultDoubleFor(STR_GL, FIELDS);
    private double bl = defaultDoubleFor(STR_BL, FIELDS);

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseFixedShunt fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseFixedShunt::new);
    }

    public static String[] toRecord(PsseFixedShunt psseFixedShunt, String[] headers) {
        return Util.toRecord(psseFixedShunt, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseFixedShunt, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseFixedShunt, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseFixedShunt::getI, PsseFixedShunt::setI));
        addField(fields, createNewField(STR_IBUS, Integer.class, PsseFixedShunt::getI, PsseFixedShunt::setI));
        addField(fields, createNewField(STR_ID, String.class, PsseFixedShunt::getId, PsseFixedShunt::setId, "1"));
        addField(fields, createNewField(STR_SHNTID, String.class, PsseFixedShunt::getId, PsseFixedShunt::setId, "1"));
        addField(fields, createNewField(STR_STATUS, Integer.class, PsseFixedShunt::getStatus, PsseFixedShunt::setStatus, 1));
        addField(fields, createNewField(STR_STAT, Integer.class, PsseFixedShunt::getStatus, PsseFixedShunt::setStatus, 1));
        addField(fields, createNewField(STR_GL, Double.class, PsseFixedShunt::getGl, PsseFixedShunt::setGl, 0d));
        addField(fields, createNewField(STR_BL, Double.class, PsseFixedShunt::getBl, PsseFixedShunt::setBl, 0d));

        return fields;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getGl() {
        return gl;
    }

    public void setGl(double gl) {
        this.gl = gl;
    }

    public double getBl() {
        return bl;
    }

    public void setBl(double bl) {
        this.bl = bl;
    }

    public PsseFixedShunt copy() {
        PsseFixedShunt copy = new PsseFixedShunt();
        copy.i = this.i;
        copy.id = this.id;
        copy.status = this.status;
        copy.gl = this.gl;
        copy.bl = this.bl;
        return copy;
    }
}
