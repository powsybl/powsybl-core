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
public class ZCorr33 {

    private int i;
    private double t1 = 0.0;
    private double f1 = 0.0;
    private double t2 = 0.0;
    private double f2 = 0.0;
    private double t3 = 0.0;
    private double f3 = 0.0;
    private double t4 = 0.0;
    private double f4 = 0.0;
    private double t5 = 0.0;
    private double f5 = 0.0;
    private double t6 = 0.0;
    private double f6 = 0.0;
    private double t7 = 0.0;
    private double f7 = 0.0;
    private double t8 = 0.0;
    private double f8 = 0.0;
    private double t9 = 0.0;
    private double f9 = 0.0;
    private double t10 = 0.0;
    private double f10 = 0.0;
    private double t11 = 0.0;
    private double f11 = 0.0;

    public static ZCorr33 fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        ZCorr33 zCorr33 = new ZCorr33();
        zCorr33.setI(parseIntFromRecord(rec, headers, "i"));
        for (int j = 1; j < 12; j++) {
            double t = parseDoubleFromRecord(rec, headers, "t" + j);
            double f = parseDoubleFromRecord(rec, headers, "f" + j);
            zCorr33.setTF(j, t, f);
        }
        return zCorr33;
    }

    public static String[] toRecord(ZCorr33 zCorr33, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "i" -> String.valueOf(zCorr33.getI());
                case "t1" -> String.valueOf(zCorr33.getT1());
                case "f1" -> String.valueOf(zCorr33.getF1());
                case "t2" -> String.valueOf(zCorr33.getT2());
                case "f2" -> String.valueOf(zCorr33.getF2());
                case "t3" -> String.valueOf(zCorr33.getT3());
                case "f3" -> String.valueOf(zCorr33.getF3());
                case "t4" -> String.valueOf(zCorr33.getT4());
                case "f4" -> String.valueOf(zCorr33.getF4());
                case "t5" -> String.valueOf(zCorr33.getT5());
                case "f5" -> String.valueOf(zCorr33.getF5());
                case "t6" -> String.valueOf(zCorr33.getT6());
                case "f6" -> String.valueOf(zCorr33.getF6());
                case "t7" -> String.valueOf(zCorr33.getT7());
                case "f7" -> String.valueOf(zCorr33.getF7());
                case "t8" -> String.valueOf(zCorr33.getT8());
                case "f8" -> String.valueOf(zCorr33.getF8());
                case "t9" -> String.valueOf(zCorr33.getT9());
                case "f9" -> String.valueOf(zCorr33.getF9());
                case "t10" -> String.valueOf(zCorr33.getT10());
                case "f10" -> String.valueOf(zCorr33.getF10());
                case "t11" -> String.valueOf(zCorr33.getT11());
                case "f11" -> String.valueOf(zCorr33.getF11());
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public double getT1() {
        return t1;
    }

    public double getF1() {
        return f1;
    }

    public double getT2() {
        return t2;
    }

    public double getF2() {
        return f2;
    }

    public double getT3() {
        return t3;
    }

    public double getF3() {
        return f3;
    }

    public double getT4() {
        return t4;
    }

    public double getF4() {
        return f4;
    }

    public double getT5() {
        return t5;
    }

    public double getF5() {
        return f5;
    }

    public double getT6() {
        return t6;
    }

    public double getF6() {
        return f6;
    }

    public double getT7() {
        return t7;
    }

    public double getF7() {
        return f7;
    }

    public double getT8() {
        return t8;
    }

    public double getF8() {
        return f8;
    }

    public double getT9() {
        return t9;
    }

    public double getF9() {
        return f9;
    }

    public double getT10() {
        return t10;
    }

    public double getF10() {
        return f10;
    }

    public double getT11() {
        return t11;
    }

    public double getF11() {
        return f11;
    }

    public void setTF(int point, double t, double f) {
        switch (point) {
            case 1:
                this.t1 = t;
                this.f1 = f;
                break;
            case 2:
                this.t2 = t;
                this.f2 = f;
                break;
            case 3:
                this.t3 = t;
                this.f3 = f;
                break;
            case 4:
                this.t4 = t;
                this.f4 = f;
                break;
            case 5:
                this.t5 = t;
                this.f5 = f;
                break;
            case 6:
                this.t6 = t;
                this.f6 = f;
                break;
            case 7:
                this.t7 = t;
                this.f7 = f;
                break;
            case 8:
                this.t8 = t;
                this.f8 = f;
                break;
            case 9:
                this.t9 = t;
                this.f9 = f;
                break;
            case 10:
                this.t10 = t;
                this.f10 = f;
                break;
            case 11:
                this.t11 = t;
                this.f11 = f;
                break;
            default:
                throw new PsseException("Unexpected point " + point);
        }
    }
}
