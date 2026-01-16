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
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultStringFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_I;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_IZONE;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_SPACES_12;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ZONAME;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseZone {

    private static final Map<String, PsseFieldDefinition<PsseZone, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_32_33 = {STR_I, STR_ZONAME};
    private static final String[] FIELD_NAMES_35 = {STR_IZONE, STR_ZONAME};

    private int i;
    private String zoname = defaultStringFor(STR_ZONAME, FIELDS);

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseZone fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseZone::new);
    }

    public static String[] toRecord(PsseZone psseZone, String[] headers) {
        return Util.toRecord(psseZone, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseZone, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseZone, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseZone::getI, PsseZone::setI));
        addField(fields, createNewField(STR_IZONE, Integer.class, PsseZone::getI, PsseZone::setI));
        addField(fields, createNewField(STR_ZONAME, String.class, PsseZone::getZoname, PsseZone::setZoname, STR_SPACES_12));

        return fields;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getZoname() {
        return zoname;
    }

    public void setZoname(String zoname) {
        this.zoname = zoname;
    }
}
