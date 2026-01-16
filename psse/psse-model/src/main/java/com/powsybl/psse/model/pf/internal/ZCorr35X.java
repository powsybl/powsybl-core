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

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_IMFACT;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ITABLE;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_REFACT;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_TAP;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class ZCorr35X {

    private static final Map<String, PsseFieldDefinition<ZCorr35X, ?>> FIELDS = createFields();

    private int itable;
    private double tap;
    private double refact;
    private double imfact;

    private static Map<String, PsseFieldDefinition<ZCorr35X, ?>> createFields() {
        Map<String, PsseFieldDefinition<ZCorr35X, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_ITABLE, Integer.class, ZCorr35X::getItable, ZCorr35X::setItable));
        addField(fields, createNewField(STR_TAP, Double.class, ZCorr35X::getTap, ZCorr35X::setTap));
        addField(fields, createNewField(STR_REFACT, Double.class, ZCorr35X::getRefact, ZCorr35X::setRefact));
        addField(fields, createNewField(STR_IMFACT, Double.class, ZCorr35X::getImfact, ZCorr35X::setImfact));

        return fields;
    }

    public static ZCorr35X fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, ZCorr35X::new);
    }

    public static String[] toRecord(ZCorr35X zCorr35X, String[] headers) {
        return Util.toRecord(zCorr35X, headers, FIELDS);
    }

    public ZCorr35X() {
    }

    public ZCorr35X(int itable, double tap, double refact, double imfact) {
        this.itable = itable;
        this.tap = tap;
        this.refact = refact;
        this.imfact = imfact;
    }

    public int getItable() {
        return itable;
    }

    public double getTap() {
        return tap;
    }

    public double getRefact() {
        return refact;
    }

    public double getImfact() {
        return imfact;
    }

    public void setItable(int itable) {
        this.itable = itable;
    }

    public void setTap(double tap) {
        this.tap = tap;
    }

    public void setRefact(double refact) {
        this.refact = refact;
    }

    public void setImfact(double imfact) {
        this.imfact = imfact;
    }
}
