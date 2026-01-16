/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.internal;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashMap;
import java.util.Map;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class ZCorr33 {

    private static final Map<String, PsseFieldDefinition<ZCorr33, ?>> FIELDS = createFields();

    private int i;
    private double t1 = defaultDoubleFor(STR_T1, FIELDS);
    private double f1 = defaultDoubleFor(STR_F1, FIELDS);
    private double t2 = defaultDoubleFor(STR_T2, FIELDS);
    private double f2 = defaultDoubleFor(STR_F2, FIELDS);
    private double t3 = defaultDoubleFor(STR_T3, FIELDS);
    private double f3 = defaultDoubleFor(STR_F3, FIELDS);
    private double t4 = defaultDoubleFor(STR_T4, FIELDS);
    private double f4 = defaultDoubleFor(STR_F4, FIELDS);
    private double t5 = defaultDoubleFor(STR_T5, FIELDS);
    private double f5 = defaultDoubleFor(STR_F5, FIELDS);
    private double t6 = defaultDoubleFor(STR_T6, FIELDS);
    private double f6 = defaultDoubleFor(STR_F6, FIELDS);
    private double t7 = defaultDoubleFor(STR_T7, FIELDS);
    private double f7 = defaultDoubleFor(STR_F7, FIELDS);
    private double t8 = defaultDoubleFor(STR_T8, FIELDS);
    private double f8 = defaultDoubleFor(STR_F8, FIELDS);
    private double t9 = defaultDoubleFor(STR_T9, FIELDS);
    private double f9 = defaultDoubleFor(STR_F9, FIELDS);
    private double t10 = defaultDoubleFor(STR_T10, FIELDS);
    private double f10 = defaultDoubleFor(STR_F10, FIELDS);
    private double t11 = defaultDoubleFor(STR_T11, FIELDS);
    private double f11 = defaultDoubleFor(STR_F11, FIELDS);

    private static Map<String, PsseFieldDefinition<ZCorr33, ?>> createFields() {
        Map<String, PsseFieldDefinition<ZCorr33, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, ZCorr33::getI, ZCorr33::setI));
        addField(fields, createNewField(STR_T1, Double.class, ZCorr33::getT1, ZCorr33::setT1, 0d));
        addField(fields, createNewField(STR_F1, Double.class, ZCorr33::getF1, ZCorr33::setF1, 0d));
        addField(fields, createNewField(STR_T2, Double.class, ZCorr33::getT2, ZCorr33::setT2, 0d));
        addField(fields, createNewField(STR_F2, Double.class, ZCorr33::getF2, ZCorr33::setF2, 0d));
        addField(fields, createNewField(STR_T3, Double.class, ZCorr33::getT3, ZCorr33::setT3, 0d));
        addField(fields, createNewField(STR_F3, Double.class, ZCorr33::getF3, ZCorr33::setF3, 0d));
        addField(fields, createNewField(STR_T4, Double.class, ZCorr33::getT4, ZCorr33::setT4, 0d));
        addField(fields, createNewField(STR_F4, Double.class, ZCorr33::getF4, ZCorr33::setF4, 0d));
        addField(fields, createNewField(STR_T5, Double.class, ZCorr33::getT5, ZCorr33::setT5, 0d));
        addField(fields, createNewField(STR_F5, Double.class, ZCorr33::getF5, ZCorr33::setF5, 0d));
        addField(fields, createNewField(STR_T6, Double.class, ZCorr33::getT6, ZCorr33::setT6, 0d));
        addField(fields, createNewField(STR_F6, Double.class, ZCorr33::getF6, ZCorr33::setF6, 0d));
        addField(fields, createNewField(STR_T7, Double.class, ZCorr33::getT7, ZCorr33::setT7, 0d));
        addField(fields, createNewField(STR_F7, Double.class, ZCorr33::getF7, ZCorr33::setF7, 0d));
        addField(fields, createNewField(STR_T8, Double.class, ZCorr33::getT8, ZCorr33::setT8, 0d));
        addField(fields, createNewField(STR_F8, Double.class, ZCorr33::getF8, ZCorr33::setF8, 0d));
        addField(fields, createNewField(STR_T9, Double.class, ZCorr33::getT9, ZCorr33::setT9, 0d));
        addField(fields, createNewField(STR_F9, Double.class, ZCorr33::getF9, ZCorr33::setF9, 0d));
        addField(fields, createNewField(STR_T10, Double.class, ZCorr33::getT10, ZCorr33::setT10, 0d));
        addField(fields, createNewField(STR_F10, Double.class, ZCorr33::getF10, ZCorr33::setF10, 0d));
        addField(fields, createNewField(STR_T11, Double.class, ZCorr33::getT11, ZCorr33::setT11, 0d));
        addField(fields, createNewField(STR_F11, Double.class, ZCorr33::getF11, ZCorr33::setF11, 0d));

        return fields;
    }

    public static ZCorr33 fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, ZCorr33::new);
    }

    public static String[] toRecord(ZCorr33 zCorr33, String[] headers) {
        return Util.toRecord(zCorr33, headers, FIELDS);
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

    public void setT1(double t1) {
        this.t1 = t1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public void setT2(double t2) {
        this.t2 = t2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public void setT3(double t3) {
        this.t3 = t3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public void setT4(double t4) {
        this.t4 = t4;
    }

    public void setF4(double f4) {
        this.f4 = f4;
    }

    public void setT5(double t5) {
        this.t5 = t5;
    }

    public void setF5(double f5) {
        this.f5 = f5;
    }

    public void setT6(double t6) {
        this.t6 = t6;
    }

    public void setF6(double f6) {
        this.f6 = f6;
    }

    public void setT7(double t7) {
        this.t7 = t7;
    }

    public void setF7(double f7) {
        this.f7 = f7;
    }

    public void setT8(double t8) {
        this.t8 = t8;
    }

    public void setF8(double f8) {
        this.f8 = f8;
    }

    public void setT9(double t9) {
        this.t9 = t9;
    }

    public void setF9(double f9) {
        this.f9 = f9;
    }

    public void setT10(double t10) {
        this.t10 = t10;
    }

    public void setF10(double f10) {
        this.f10 = f10;
    }

    public void setT11(double t11) {
        this.t11 = t11;
    }

    public void setF11(double f11) {
        this.f11 = f11;
    }
}
