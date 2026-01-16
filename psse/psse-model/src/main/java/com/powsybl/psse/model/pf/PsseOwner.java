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
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseOwner {

    private static final Map<String, PsseFieldDefinition<PsseOwner, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_32_33 = {STR_I, STR_OWNAME};
    private static final String[] FIELD_NAMES_35 = {STR_IOWNER, STR_OWNAME};

    private int i;
    private String owname = defaultStringFor(STR_OWNAME, FIELDS);

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseOwner fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseOwner::new);
    }

    public static String[] toRecord(PsseOwner psseOwner, String[] headers) {
        return Util.toRecord(psseOwner, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseOwner, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseOwner, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseOwner::getI, PsseOwner::setI));
        addField(fields, createNewField(STR_IOWNER, Integer.class, PsseOwner::getI, PsseOwner::setI));
        addField(fields, createNewField(STR_OWNAME, String.class, PsseOwner::getOwname, PsseOwner::setOwname, "            "));
        addField(fields, createNewField(STR_OWNER, String.class, PsseOwner::getOwname, PsseOwner::setOwname, "            "));

        return fields;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getOwname() {
        return owname;
    }

    public void setOwname(String owname) {
        this.owname = owname;
    }
}
