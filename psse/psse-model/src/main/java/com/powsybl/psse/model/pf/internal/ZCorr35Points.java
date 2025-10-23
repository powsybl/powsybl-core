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

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class ZCorr35Points {

    private double t1 = 0.0;
    private double ref1 = 0.0;
    private double imf1 = 0.0;
    private double t2 = 0.0;
    private double ref2 = 0.0;
    private double imf2 = 0.0;
    private double t3 = 0.0;
    private double ref3 = 0.0;
    private double imf3 = 0.0;
    private double t4 = 0.0;
    private double ref4 = 0.0;
    private double imf4 = 0.0;
    private double t5 = 0.0;
    private double ref5 = 0.0;
    private double imf5 = 0.0;
    private double t6 = 0.0;
    private double ref6 = 0.0;
    private double imf6 = 0.0;

    public static ZCorr35Points fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        ZCorr35Points zCorr35Points = new ZCorr35Points();
        for (int j = 1; j < 6; j++) {
            double t = parseDoubleFromRecord(rec, headers, "t" + j);
            double ref = parseDoubleFromRecord(rec, headers, "ref" + j);
            double imf = parseDoubleFromRecord(rec, headers, "imf" + j);
            zCorr35Points.setTF(j, t, ref, imf);
        }
        return zCorr35Points;
    }

    public static String[] toRecord(ZCorr35Points zCorr35Points, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "t1" -> String.valueOf(zCorr35Points.getT1());
                case "ref1" -> String.valueOf(zCorr35Points.getRef1());
                case "imf1" -> String.valueOf(zCorr35Points.getImf1());
                case "t2" -> String.valueOf(zCorr35Points.getT2());
                case "ref2" -> String.valueOf(zCorr35Points.getRef2());
                case "imf2" -> String.valueOf(zCorr35Points.getImf2());
                case "t3" -> String.valueOf(zCorr35Points.getT3());
                case "ref3" -> String.valueOf(zCorr35Points.getRef3());
                case "imf3" -> String.valueOf(zCorr35Points.getImf3());
                case "t4" -> String.valueOf(zCorr35Points.getT4());
                case "ref4" -> String.valueOf(zCorr35Points.getRef4());
                case "imf4" -> String.valueOf(zCorr35Points.getImf4());
                case "t5" -> String.valueOf(zCorr35Points.getT5());
                case "ref5" -> String.valueOf(zCorr35Points.getRef5());
                case "imf5" -> String.valueOf(zCorr35Points.getImf5());
                case "t6" -> String.valueOf(zCorr35Points.getT6());
                case "ref6" -> String.valueOf(zCorr35Points.getRef6());
                case "imf6" -> String.valueOf(zCorr35Points.getImf6());
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
    }

    public double getT1() {
        return t1;
    }

    public double getRef1() {
        return ref1;
    }

    public double getImf1() {
        return imf1;
    }

    public double getT2() {
        return t2;
    }

    public double getRef2() {
        return ref2;
    }

    public double getImf2() {
        return imf2;
    }

    public double getT3() {
        return t3;
    }

    public double getRef3() {
        return ref3;
    }

    public double getImf3() {
        return imf3;
    }

    public double getT4() {
        return t4;
    }

    public double getRef4() {
        return ref4;
    }

    public double getImf4() {
        return imf4;
    }

    public double getT5() {
        return t5;
    }

    public double getRef5() {
        return ref5;
    }

    public double getImf5() {
        return imf5;
    }

    public double getT6() {
        return t6;
    }

    public double getRef6() {
        return ref6;
    }

    public double getImf6() {
        return imf6;
    }

    public void setTF(int point, double t, double ref, double imf) {
        switch (point) {
            case 1:
                this.t1 = t;
                this.ref1 = ref;
                this.imf1 = imf;
                break;
            case 2:
                this.t2 = t;
                this.ref2 = ref;
                this.imf2 = imf;
                break;
            case 3:
                this.t3 = t;
                this.ref3 = ref;
                this.imf3 = imf;
                break;
            case 4:
                this.t4 = t;
                this.ref4 = ref;
                this.imf4 = imf;
                break;
            case 5:
                this.t5 = t;
                this.ref5 = ref;
                this.imf5 = imf;
                break;
            case 6:
                this.t6 = t;
                this.ref6 = ref;
                this.imf6 = imf;
                break;
            default:
                throw new PsseException("Unexpected point " + point);
        }
    }
}
