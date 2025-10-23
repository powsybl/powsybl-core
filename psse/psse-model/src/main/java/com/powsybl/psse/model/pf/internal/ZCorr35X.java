/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.internal;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import de.siegmar.fastcsv.reader.CsvRecord;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class ZCorr35X {

    public ZCorr35X() {
    }

    public ZCorr35X(int itable, double tap, double refact, double imfact) {
        this.itable = itable;
        this.tap = tap;
        this.refact = refact;
        this.imfact = imfact;
    }

    private int itable;
    private double tap;
    private double refact;
    private double imfact;

    public static ZCorr35X fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        ZCorr35X zCorr35X = new ZCorr35X();
        zCorr35X.setItable(parseIntFromRecord(rec, headers, "itable"));
        zCorr35X.setTap(parseDoubleFromRecord(rec, headers, "tap"));
        zCorr35X.setRefact(parseDoubleFromRecord(rec, headers, "refact"));
        zCorr35X.setImfact(parseDoubleFromRecord(rec, headers, "imfact"));
        return zCorr35X;
    }

    public static String[] toRecord(ZCorr35X zCorr35X, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "itable" -> String.valueOf(zCorr35X.getItable());
                case "tap" -> String.valueOf(zCorr35X.getTap());
                case "refact" -> String.valueOf(zCorr35X.getRefact());
                case "imfact" -> String.valueOf(zCorr35X.getImfact());
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
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
