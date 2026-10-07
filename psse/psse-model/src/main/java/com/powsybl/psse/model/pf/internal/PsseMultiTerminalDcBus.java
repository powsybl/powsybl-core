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
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_AREA;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_DCNAME;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_IB;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_IDC;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_IDC2;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_OWNER;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_RGRND;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ZONE;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseMultiTerminalDcBus {

    private static final Map<String, PsseFieldDefinition<PsseMultiTerminalDcBus, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES = {STR_IDC, STR_IB, STR_AREA, STR_ZONE, STR_DCNAME, STR_IDC2, STR_RGRND, STR_OWNER};

    private int idc;
    private int ib = defaultIntegerFor(STR_IB, FIELDS);
    private int area = defaultIntegerFor(STR_AREA, FIELDS);
    private int zone = defaultIntegerFor(STR_ZONE, FIELDS);
    private String dcname;
    private int idc2 = defaultIntegerFor(STR_IDC2, FIELDS);
    private double rgrnd = defaultDoubleFor(STR_RGRND, FIELDS);
    private int owner = defaultIntegerFor(STR_OWNER, FIELDS);

    public static String[] getFieldNames() {
        return FIELD_NAMES;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseMultiTerminalDcBus fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseMultiTerminalDcBus::new);
    }

    public static void toRecord(PsseMultiTerminalDcBus multiTerminalDcBus, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        Util.toRecord(multiTerminalDcBus, headers, FIELDS, row, unexpectedHeaders);
    }

    public static String[] toRecord(PsseMultiTerminalDcBus multiTerminalDcBus, String[] headers) {
        return Util.toRecord(multiTerminalDcBus, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseMultiTerminalDcBus, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseMultiTerminalDcBus, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_IDC, Integer.class, PsseMultiTerminalDcBus::getIdc, PsseMultiTerminalDcBus::setIdc));
        addField(fields, createNewField(STR_IB, Integer.class, PsseMultiTerminalDcBus::getIb, PsseMultiTerminalDcBus::setIb, 0));
        addField(fields, createNewField(STR_AREA, Integer.class, PsseMultiTerminalDcBus::getArea, PsseMultiTerminalDcBus::setArea, 1));
        addField(fields, createNewField(STR_ZONE, Integer.class, PsseMultiTerminalDcBus::getZone, PsseMultiTerminalDcBus::setZone, 1));
        addField(fields, createNewField(STR_DCNAME, String.class, PsseMultiTerminalDcBus::getDcname, PsseMultiTerminalDcBus::setDcname));
        addField(fields, createNewField(STR_IDC2, Integer.class, PsseMultiTerminalDcBus::getIdc2, PsseMultiTerminalDcBus::setIdc2, 0));
        addField(fields, createNewField(STR_RGRND, Double.class, PsseMultiTerminalDcBus::getRgrnd, PsseMultiTerminalDcBus::setRgrnd, 0.0));
        addField(fields, createNewField(STR_OWNER, Integer.class, PsseMultiTerminalDcBus::getOwner, PsseMultiTerminalDcBus::setOwner, 1));

        return fields;
    }

    public int getIdc() {
        return idc;
    }

    public void setIdc(int idc) {
        this.idc = idc;
    }

    public int getIb() {
        return ib;
    }

    public void setIb(int ib) {
        this.ib = ib;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public String getDcname() {
        return dcname;
    }

    public void setDcname(String dcname) {
        this.dcname = dcname;
    }

    public int getIdc2() {
        return idc2;
    }

    public void setIdc2(int idc2) {
        this.idc2 = idc2;
    }

    public double getRgrnd() {
        return rgrnd;
    }

    public void setRgrnd(double rgrnd) {
        this.rgrnd = rgrnd;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }
}
