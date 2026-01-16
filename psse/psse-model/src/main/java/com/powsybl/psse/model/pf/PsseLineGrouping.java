/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.defaultStringFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseLineGrouping {

    private static final Map<String, PsseFieldDefinition<PsseLineGrouping, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES = {STR_I, STR_J, STR_ID, STR_MET,
        STR_DUM1, STR_DUM2, STR_DUM3, STR_DUM4, STR_DUM5, STR_DUM6, STR_DUM7, STR_DUM8, STR_DUM9};

    private int i;
    private int j;
    private String id = defaultStringFor(STR_ID, FIELDS);
    private int met = defaultIntegerFor(STR_MET, FIELDS);
    private Integer dum1;
    private Integer dum2;
    private Integer dum3;
    private Integer dum4;
    private Integer dum5;
    private Integer dum6;
    private Integer dum7;
    private Integer dum8;
    private Integer dum9;

    public static String[] getFieldNames() {
        return FIELD_NAMES;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseLineGrouping fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseLineGrouping::new);
    }

    public static String[] toRecord(PsseLineGrouping psseLineGrouping, String[] headers) {
        return Util.toRecord(psseLineGrouping, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseLineGrouping, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseLineGrouping, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseLineGrouping::getI, PsseLineGrouping::setI));
        addField(fields, createNewField(STR_IBUS, Integer.class, PsseLineGrouping::getI, PsseLineGrouping::setI));
        addField(fields, createNewField(STR_J, Integer.class, PsseLineGrouping::getJ, PsseLineGrouping::setJ));
        addField(fields, createNewField(STR_JBUS, Integer.class, PsseLineGrouping::getJ, PsseLineGrouping::setJ));
        addField(fields, createNewField(STR_ID, String.class, PsseLineGrouping::getId, PsseLineGrouping::setId, "&1"));
        addField(fields, createNewField(STR_MSLID, String.class, PsseLineGrouping::getId, PsseLineGrouping::setId, "&1"));
        addField(fields, createNewField(STR_MET, Integer.class, PsseLineGrouping::getMet, PsseLineGrouping::setMet, 1));
        addField(fields, createNewField(STR_DUM1, Integer.class, PsseLineGrouping::getDum1, PsseLineGrouping::setDum1, null));
        addField(fields, createNewField(STR_DUM2, Integer.class, PsseLineGrouping::getDum2, PsseLineGrouping::setDum2, null));
        addField(fields, createNewField(STR_DUM3, Integer.class, PsseLineGrouping::getDum3, PsseLineGrouping::setDum3, null));
        addField(fields, createNewField(STR_DUM4, Integer.class, PsseLineGrouping::getDum4, PsseLineGrouping::setDum4, null));
        addField(fields, createNewField(STR_DUM5, Integer.class, PsseLineGrouping::getDum5, PsseLineGrouping::setDum5, null));
        addField(fields, createNewField(STR_DUM6, Integer.class, PsseLineGrouping::getDum6, PsseLineGrouping::setDum6, null));
        addField(fields, createNewField(STR_DUM7, Integer.class, PsseLineGrouping::getDum7, PsseLineGrouping::setDum7, null));
        addField(fields, createNewField(STR_DUM8, Integer.class, PsseLineGrouping::getDum8, PsseLineGrouping::setDum8, null));
        addField(fields, createNewField(STR_DUM9, Integer.class, PsseLineGrouping::getDum9, PsseLineGrouping::setDum9, null));

        return fields;
    }

    public PsseLineGrouping() {
    }

    public PsseLineGrouping(int i, int j, String id, int met) {
        this.i = i;
        this.j = j;
        this.id = id;
        this.met = met;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMet() {
        return met;
    }

    public void setMet(int met) {
        this.met = met;
    }

    public Integer getDum1() {
        return dum1;
    }

    public void setDum1(Integer dum1) {
        this.dum1 = dum1;
    }

    public Integer getDum2() {
        return dum2;
    }

    public void setDum2(Integer dum2) {
        this.dum2 = dum2;
    }

    public Integer getDum3() {
        return dum3;
    }

    public void setDum3(Integer dum3) {
        this.dum3 = dum3;
    }

    public Integer getDum4() {
        return dum4;
    }

    public void setDum4(Integer dum4) {
        this.dum4 = dum4;
    }

    public Integer getDum5() {
        return dum5;
    }

    public void setDum5(Integer dum5) {
        this.dum5 = dum5;
    }

    public Integer getDum6() {
        return dum6;
    }

    public void setDum6(Integer dum6) {
        this.dum6 = dum6;
    }

    public Integer getDum7() {
        return dum7;
    }

    public void setDum7(Integer dum7) {
        this.dum7 = dum7;
    }

    public Integer getDum8() {
        return dum8;
    }

    public void setDum8(Integer dum8) {
        this.dum8 = dum8;
    }

    public Integer getDum9() {
        return dum9;
    }

    public void setDum9(Integer dum9) {
        this.dum9 = dum9;
    }
}
