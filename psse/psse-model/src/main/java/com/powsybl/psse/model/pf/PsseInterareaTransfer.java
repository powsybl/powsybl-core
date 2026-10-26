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
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultStringFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ARFROM;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ARTO;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_PTRAN;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_TRID;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseInterareaTransfer {

    private static final Map<String, PsseFieldDefinition<PsseInterareaTransfer, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES = {STR_ARFROM, STR_ARTO, STR_TRID, STR_PTRAN};

    private int arfrom;
    private int arto;
    private String trid = defaultStringFor(STR_TRID, FIELDS);
    private double ptran = defaultDoubleFor(STR_PTRAN, FIELDS);

    public static String[] getFieldNames() {
        return FIELD_NAMES;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseInterareaTransfer fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseInterareaTransfer::new);
    }

    public static String[] toRecord(PsseInterareaTransfer psseInterareaTransfer, String[] headers) {
        return Util.toRecord(psseInterareaTransfer, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseInterareaTransfer, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseInterareaTransfer, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_ARFROM, Integer.class, PsseInterareaTransfer::getArfrom, PsseInterareaTransfer::setArfrom));
        addField(fields, createNewField(STR_ARTO, Integer.class, PsseInterareaTransfer::getArto, PsseInterareaTransfer::setArto));
        addField(fields, createNewField(STR_TRID, String.class, PsseInterareaTransfer::getTrid, PsseInterareaTransfer::setTrid, "1"));
        addField(fields, createNewField(STR_PTRAN, Double.class, PsseInterareaTransfer::getPtran, PsseInterareaTransfer::setPtran, 0.0));

        return fields;
    }

    public int getArfrom() {
        return arfrom;
    }

    public void setArfrom(int arfrom) {
        this.arfrom = arfrom;
    }

    public int getArto() {
        return arto;
    }

    public void setArto(int arto) {
        this.arto = arto;
    }

    public String getTrid() {
        return trid;
    }

    public void setTrid(String trid) {
        this.trid = trid;
    }

    public double getPtran() {
        return ptran;
    }

    public void setPtran(double ptran) {
        this.ptran = ptran;
    }
}
