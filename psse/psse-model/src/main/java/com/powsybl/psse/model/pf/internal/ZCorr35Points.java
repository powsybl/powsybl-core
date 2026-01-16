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
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class ZCorr35Points {

    private static final Map<String, PsseFieldDefinition<ZCorr35Points, ?>> FIELDS = createFields();

    private double t1 = defaultDoubleFor(STR_T1, FIELDS);
    private double ref1 = defaultDoubleFor(STR_REF1, FIELDS);
    private double imf1 = defaultDoubleFor(STR_IMF1, FIELDS);
    private double t2 = defaultDoubleFor(STR_T2, FIELDS);
    private double ref2 = defaultDoubleFor(STR_REF2, FIELDS);
    private double imf2 = defaultDoubleFor(STR_IMF2, FIELDS);
    private double t3 = defaultDoubleFor(STR_T3, FIELDS);
    private double ref3 = defaultDoubleFor(STR_REF3, FIELDS);
    private double imf3 = defaultDoubleFor(STR_IMF3, FIELDS);
    private double t4 = defaultDoubleFor(STR_T4, FIELDS);
    private double ref4 = defaultDoubleFor(STR_REF4, FIELDS);
    private double imf4 = defaultDoubleFor(STR_IMF4, FIELDS);
    private double t5 = defaultDoubleFor(STR_T5, FIELDS);
    private double ref5 = defaultDoubleFor(STR_REF5, FIELDS);
    private double imf5 = defaultDoubleFor(STR_IMF5, FIELDS);
    private double t6 = defaultDoubleFor(STR_T6, FIELDS);
    private double ref6 = defaultDoubleFor(STR_REF6, FIELDS);
    private double imf6 = defaultDoubleFor(STR_IMF6, FIELDS);

    private static Map<String, PsseFieldDefinition<ZCorr35Points, ?>> createFields() {
        Map<String, PsseFieldDefinition<ZCorr35Points, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_T1, Double.class, ZCorr35Points::getT1, ZCorr35Points::setT1, 0d));
        addField(fields, createNewField(STR_REF1, Double.class, ZCorr35Points::getRef1, ZCorr35Points::setRef1, 0d));
        addField(fields, createNewField(STR_IMF1, Double.class, ZCorr35Points::getImf1, ZCorr35Points::setImf1, 0d));
        addField(fields, createNewField(STR_T2, Double.class, ZCorr35Points::getT2, ZCorr35Points::setT2, 0d));
        addField(fields, createNewField(STR_REF2, Double.class, ZCorr35Points::getRef2, ZCorr35Points::setRef2, 0d));
        addField(fields, createNewField(STR_IMF2, Double.class, ZCorr35Points::getImf2, ZCorr35Points::setImf2, 0d));
        addField(fields, createNewField(STR_T3, Double.class, ZCorr35Points::getT3, ZCorr35Points::setT3, 0d));
        addField(fields, createNewField(STR_REF3, Double.class, ZCorr35Points::getRef3, ZCorr35Points::setRef3, 0d));
        addField(fields, createNewField(STR_IMF3, Double.class, ZCorr35Points::getImf3, ZCorr35Points::setImf3, 0d));
        addField(fields, createNewField(STR_T4, Double.class, ZCorr35Points::getT4, ZCorr35Points::setT4, 0d));
        addField(fields, createNewField(STR_REF4, Double.class, ZCorr35Points::getRef4, ZCorr35Points::setRef4, 0d));
        addField(fields, createNewField(STR_IMF4, Double.class, ZCorr35Points::getImf4, ZCorr35Points::setImf4, 0d));
        addField(fields, createNewField(STR_T5, Double.class, ZCorr35Points::getT5, ZCorr35Points::setT5, 0d));
        addField(fields, createNewField(STR_REF5, Double.class, ZCorr35Points::getRef5, ZCorr35Points::setRef5, 0d));
        addField(fields, createNewField(STR_IMF5, Double.class, ZCorr35Points::getImf5, ZCorr35Points::setImf5, 0d));
        addField(fields, createNewField(STR_T6, Double.class, ZCorr35Points::getT6, ZCorr35Points::setT6, 0d));
        addField(fields, createNewField(STR_REF6, Double.class, ZCorr35Points::getRef6, ZCorr35Points::setRef6, 0d));
        addField(fields, createNewField(STR_IMF6, Double.class, ZCorr35Points::getImf6, ZCorr35Points::setImf6, 0d));

        return fields;
    }

    public static ZCorr35Points fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, ZCorr35Points::new);
    }

    public static void toRecord(ZCorr35Points zCorr35Points, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        Util.toRecord(zCorr35Points, headers, FIELDS, row, unexpectedHeaders);
    }

    public static String[] toRecord(ZCorr35Points zCorr35Points, String[] headers) {
        return Util.toRecord(zCorr35Points, headers, FIELDS);
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

    public void setT1(double t1) {
        this.t1 = t1;
    }

    public void setRef1(double ref1) {
        this.ref1 = ref1;
    }

    public void setImf1(double imf1) {
        this.imf1 = imf1;
    }

    public void setT2(double t2) {
        this.t2 = t2;
    }

    public void setRef2(double ref2) {
        this.ref2 = ref2;
    }

    public void setImf2(double imf2) {
        this.imf2 = imf2;
    }

    public void setT3(double t3) {
        this.t3 = t3;
    }

    public void setRef3(double ref3) {
        this.ref3 = ref3;
    }

    public void setImf3(double imf3) {
        this.imf3 = imf3;
    }

    public void setT4(double t4) {
        this.t4 = t4;
    }

    public void setRef4(double ref4) {
        this.ref4 = ref4;
    }

    public void setImf4(double imf4) {
        this.imf4 = imf4;
    }

    public void setT5(double t5) {
        this.t5 = t5;
    }

    public void setRef5(double ref5) {
        this.ref5 = ref5;
    }

    public void setImf5(double imf5) {
        this.imf5 = imf5;
    }

    public void setT6(double t6) {
        this.t6 = t6;
    }

    public void setRef6(double ref6) {
        this.ref6 = ref6;
    }

    public void setImf6(double imf6) {
        this.imf6 = imf6;
    }
}
