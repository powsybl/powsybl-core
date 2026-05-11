/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.internal;

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
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_DCCKT;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_IDC;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_JDC;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_LDC;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_MET;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_RDC;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseMultiTerminalDcLink {

    private static final Map<String, PsseFieldDefinition<PsseMultiTerminalDcLink, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES = {STR_IDC, STR_JDC, STR_DCCKT, STR_MET, STR_RDC, STR_LDC};

    private int idc;
    private int jdc;
    private String dcckt;
    private int met = defaultIntegerFor(STR_MET, FIELDS);
    private double rdc;
    private double ldc = defaultDoubleFor(STR_LDC, FIELDS);

    public static String[] getFieldNames() {
        return FIELD_NAMES;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseMultiTerminalDcLink, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseMultiTerminalDcLink, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_IDC, Integer.class, PsseMultiTerminalDcLink::getIdc, PsseMultiTerminalDcLink::setIdc));
        addField(fields, createNewField(STR_JDC, Integer.class, PsseMultiTerminalDcLink::getJdc, PsseMultiTerminalDcLink::setJdc));
        addField(fields, createNewField(STR_DCCKT, String.class, PsseMultiTerminalDcLink::getDcckt, PsseMultiTerminalDcLink::setDcckt));
        addField(fields, createNewField(STR_MET, Integer.class, PsseMultiTerminalDcLink::getMet, PsseMultiTerminalDcLink::setMet, 1));
        addField(fields, createNewField(STR_RDC, Double.class, PsseMultiTerminalDcLink::getRdc, PsseMultiTerminalDcLink::setRdc));
        addField(fields, createNewField(STR_LDC, Double.class, PsseMultiTerminalDcLink::getLdc, PsseMultiTerminalDcLink::setLdc, 0.0));

        return fields;
    }

    public static PsseMultiTerminalDcLink fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseMultiTerminalDcLink::new);
    }

    public static void toRecord(PsseMultiTerminalDcLink multiTerminalDcLink, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        Util.toRecord(multiTerminalDcLink, headers, FIELDS, row, unexpectedHeaders);
    }

    public static String[] toRecord(PsseMultiTerminalDcLink multiTerminalDcLink, String[] headers) {
        return Util.toRecord(multiTerminalDcLink, headers, FIELDS);
    }

    public int getIdc() {
        return idc;
    }

    public void setIdc(int idc) {
        this.idc = idc;
    }

    public int getJdc() {
        return jdc;
    }

    public void setJdc(int jdc) {
        this.jdc = jdc;
    }

    public String getDcckt() {
        return dcckt;
    }

    public void setDcckt(String dcckt) {
        this.dcckt = dcckt;
    }

    public int getMet() {
        return met;
    }

    public void setMet(int met) {
        this.met = met;
    }

    public double getRdc() {
        return rdc;
    }

    public void setRdc(double rdc) {
        this.rdc = rdc;
    }

    public double getLdc() {
        return ldc;
    }

    public void setLdc(double ldc) {
        this.ldc = ldc;
    }
}
