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
import static com.powsybl.psse.model.io.Util.concatStringArrays;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_IS;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ISUB;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_LATI;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_LONG;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_NAME;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_SPACES_40;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_SRG;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseSubstationRecord {

    private static final Map<String, PsseFieldDefinition<PsseSubstationRecord, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_COMMON = {STR_NAME, STR_LATI, STR_LONG, STR_SRG};
    private static final String[] FIELD_NAMES_START_RAW = {STR_IS};
    private static final String[] FIELD_NAMES_START_RAWX = {STR_ISUB};
    private static final String[] FIELD_NAMES_RAW = concatStringArrays(FIELD_NAMES_START_RAW, FIELD_NAMES_COMMON);
    private static final String[] FIELD_NAMES_RAWX = concatStringArrays(FIELD_NAMES_START_RAWX, FIELD_NAMES_COMMON);

    private int is;
    private String name;
    private double lati = defaultDoubleFor(STR_LATI, FIELDS);
    private double longi = defaultDoubleFor(STR_LONG, FIELDS);
    private double srg = defaultDoubleFor(STR_SRG, FIELDS);

    public static String[] getFieldNamesRaw() {
        return FIELD_NAMES_RAW;
    }

    public static String[] getFieldNamesRawx() {
        return FIELD_NAMES_RAWX;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseSubstationRecord fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseSubstationRecord::new);
    }

    public static void toRecord(PsseSubstationRecord multiTerminalDcLink, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        Util.toRecord(multiTerminalDcLink, headers, FIELDS, row, unexpectedHeaders);
    }

    public static String[] toRecord(PsseSubstationRecord multiTerminalDcLink, String[] headers) {
        return Util.toRecord(multiTerminalDcLink, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseSubstationRecord, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseSubstationRecord, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_IS, Integer.class, PsseSubstationRecord::getIs, PsseSubstationRecord::setIs));
        addField(fields, createNewField(STR_ISUB, Integer.class, PsseSubstationRecord::getIs, PsseSubstationRecord::setIs));
        addField(fields, createNewField(STR_NAME, String.class, PsseSubstationRecord::getName, PsseSubstationRecord::setName, STR_SPACES_40));
        addField(fields, createNewField(STR_LATI, Double.class, PsseSubstationRecord::getLati, PsseSubstationRecord::setLati, 0d));
        addField(fields, createNewField(STR_LONG, Double.class, PsseSubstationRecord::getLong, PsseSubstationRecord::setLong, 0d));
        addField(fields, createNewField(STR_SRG, Double.class, PsseSubstationRecord::getSrg, PsseSubstationRecord::setSrg, 0d));

        return fields;
    }

    public int getIs() {
        return is;
    }

    public void setIs(int is) {
        this.is = is;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLati() {
        return lati;
    }

    public void setLati(double lati) {
        this.lati = lati;
    }

    public double getLong() {
        return longi;
    }

    public void setLong(double longi) {
        this.longi = longi;
    }

    public double getSrg() {
        return srg;
    }

    public void setSrg(double srg) {
        this.srg = srg;
    }

    public PsseSubstationRecord copy() {
        PsseSubstationRecord copy = new PsseSubstationRecord();
        copy.is = this.is;
        copy.name = this.name;
        copy.lati = this.lati;
        copy.longi = this.longi;
        copy.srg = this.srg;
        return copy;
    }
}
