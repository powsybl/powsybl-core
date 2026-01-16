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
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ARNAME;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_I;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_IAREA;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ISW;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_PDES;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_PTOL;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseArea {

    private static final Map<String, PsseFieldDefinition<PsseArea, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_32_33 = {STR_I, STR_ISW, STR_PDES, STR_PTOL, STR_ARNAME};
    private static final String[] FIELD_NAMES_35 = {STR_IAREA, STR_ISW, STR_PDES, STR_PTOL, STR_ARNAME};

    private int i;
    private int isw = defaultIntegerFor(STR_ISW, FIELDS);
    private double pdes = defaultDoubleFor(STR_PDES, FIELDS);
    private double ptol = defaultDoubleFor(STR_PTOL, FIELDS);
    private String arname;

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseArea fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseArea::new);
    }

    public static String[] toRecord(PsseArea psseArea, String[] headers) {
        return Util.toRecord(psseArea, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseArea, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseArea, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseArea::getI, PsseArea::setI));
        addField(fields, createNewField(STR_IAREA, Integer.class, PsseArea::getI, PsseArea::setI));
        addField(fields, createNewField(STR_ISW, Integer.class, PsseArea::getIsw, PsseArea::setIsw, 0));
        addField(fields, createNewField(STR_PDES, Double.class, PsseArea::getPdes, PsseArea::setPdes, 0d));
        addField(fields, createNewField(STR_PTOL, Double.class, PsseArea::getPtol, PsseArea::setPtol, 10d));
        addField(fields, createNewField(STR_ARNAME, String.class, PsseArea::getArname, PsseArea::setArname));

        return fields;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getIsw() {
        return isw;
    }

    public void setIsw(int isw) {
        this.isw = isw;
    }

    public double getPdes() {
        return pdes;
    }

    public void setPdes(double pdes) {
        this.pdes = pdes;
    }

    public double getPtol() {
        return ptol;
    }

    public void setPtol(double ptol) {
        this.ptol = ptol;
    }

    public String getArname() {
        return arname;
    }

    public void setArname(String arname) {
        this.arname = arname;
    }
}
