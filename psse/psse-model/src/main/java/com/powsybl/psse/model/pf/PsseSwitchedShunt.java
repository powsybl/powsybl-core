/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashMap;
import java.util.Map;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.concatStringArrays;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.defaultStringFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 *
 * @author Jean-Baptiste Heyberger {@literal <Jean-Baptiste.Heyberger at rte-france.com>}
 */
public class PsseSwitchedShunt extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseSwitchedShunt, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_32_33_START = {STR_I};
    private static final String[] FIELD_NAMES_35_START = {STR_I, STR_ID};
    private static final String[] FIELD_NAMES_35_RAWX_START = {STR_IBUS, STR_SHNTID};
    private static final String[] FIELD_NAMES_COMMON_1 = {STR_MODSW, STR_ADJM, STR_STAT, STR_VSWHI, STR_VSWLO};
    private static final String[] FIELD_NAMES_32_33_MIDDLE = {STR_SWREM};
    private static final String[] FIELD_NAMES_35_MIDDLE = {STR_SWREG, STR_NREG};
    private static final String[] FIELD_NAMES_COMMON_2 = {STR_RMPCT, STR_RMIDNT, STR_BINIT};
    private static final String[] FIELD_NAMES_32_33_END = {STR_N1, STR_B1, STR_N2, STR_B2, STR_N3, STR_B3, STR_N4, STR_B4, STR_N5,
        STR_B5, STR_N6, STR_B6, STR_N7, STR_B7, STR_N8, STR_B8};
    private static final String[] FIELD_NAMES_35_END = {STR_S1, STR_N1, STR_B1, STR_S2, STR_N2, STR_B2, STR_S3, STR_N3, STR_B3, STR_S4,
        STR_N4, STR_B4, STR_S5, STR_N5, STR_B5, STR_S6, STR_N6, STR_B6, STR_S7, STR_N7, STR_B7, STR_S8, STR_N8, STR_B8};
    private static final String[] FIELD_NAMES_32_33 = concatStringArrays(FIELD_NAMES_32_33_START, FIELD_NAMES_COMMON_1, FIELD_NAMES_32_33_MIDDLE, FIELD_NAMES_COMMON_2, FIELD_NAMES_32_33_END);
    private static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_35_START, FIELD_NAMES_COMMON_1, FIELD_NAMES_35_MIDDLE, FIELD_NAMES_COMMON_2, FIELD_NAMES_35_END);
    private static final String[] FIELD_NAMES_35_RAWX = concatStringArrays(FIELD_NAMES_35_RAWX_START, FIELD_NAMES_COMMON_1, FIELD_NAMES_35_MIDDLE, FIELD_NAMES_COMMON_2, FIELD_NAMES_35_END);

    private int i;
    private int modsw = defaultIntegerFor(STR_MODSW, FIELDS);
    private int adjm = defaultIntegerFor(STR_ADJM, FIELDS);
    private int stat = defaultIntegerFor(STR_STAT, FIELDS);
    private double vswhi = defaultDoubleFor(STR_VSWHI, FIELDS);
    private double vswlo = defaultDoubleFor(STR_VSWLO, FIELDS);

    @Revision(until = 33)
    private int swrem = defaultIntegerFor(STR_SWREM, FIELDS);

    private double rmpct = defaultDoubleFor(STR_RMPCT, FIELDS);
    private String rmidnt;
    private double binit = defaultDoubleFor(STR_BINIT, FIELDS);
    private int n1 = defaultIntegerFor(STR_N1, FIELDS);
    private double b1 = defaultDoubleFor(STR_B1, FIELDS);
    private int n2 = defaultIntegerFor(STR_N2, FIELDS);
    private double b2 = defaultDoubleFor(STR_B2, FIELDS);
    private int n3 = defaultIntegerFor(STR_N3, FIELDS);
    private double b3 = defaultDoubleFor(STR_B3, FIELDS);
    private int n4 = defaultIntegerFor(STR_N4, FIELDS);
    private double b4 = defaultDoubleFor(STR_B4, FIELDS);
    private int n5 = defaultIntegerFor(STR_N5, FIELDS);
    private double b5 = defaultDoubleFor(STR_B5, FIELDS);
    private int n6 = defaultIntegerFor(STR_N6, FIELDS);
    private double b6 = defaultDoubleFor(STR_B6, FIELDS);
    private int n7 = defaultIntegerFor(STR_N7, FIELDS);
    private double b7 = defaultDoubleFor(STR_B7, FIELDS);
    private int n8 = defaultIntegerFor(STR_N8, FIELDS);
    private double b8 = defaultDoubleFor(STR_B8, FIELDS);

    @Revision(since = 35)
    private String id = defaultStringFor(STR_ID, FIELDS);

    @Revision(since = 35)
    private int swreg = defaultIntegerFor(STR_SWREG, FIELDS);

    @Revision(since = 35)
    private int nreg = defaultIntegerFor(STR_NREG, FIELDS);

    @Revision(since = 35)
    private int s1 = defaultIntegerFor(STR_S1, FIELDS);

    @Revision(since = 35)
    private int s2 = defaultIntegerFor(STR_S2, FIELDS);

    @Revision(since = 35)
    private int s3 = defaultIntegerFor(STR_S3, FIELDS);

    @Revision(since = 35)
    private int s4 = defaultIntegerFor(STR_S4, FIELDS);

    @Revision(since = 35)
    private int s5 = defaultIntegerFor(STR_S5, FIELDS);

    @Revision(since = 35)
    private int s6 = defaultIntegerFor(STR_S6, FIELDS);

    @Revision(since = 35)
    private int s7 = defaultIntegerFor(STR_S7, FIELDS);

    @Revision(since = 35)
    private int s8 = defaultIntegerFor(STR_S8, FIELDS);

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNames35RawX() {
        return FIELD_NAMES_35_RAWX;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseSwitchedShunt fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseSwitchedShunt::new);
    }

    public static String[] toRecord(PsseSwitchedShunt psseSwitchedShunt, String[] headers) {
        return Util.toRecord(psseSwitchedShunt, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseSwitchedShunt, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseSwitchedShunt, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseSwitchedShunt::getI, PsseSwitchedShunt::setI));
        addField(fields, createNewField(STR_IBUS, Integer.class, PsseSwitchedShunt::getI, PsseSwitchedShunt::setI));
        addField(fields, createNewField(STR_MODSW, Integer.class, PsseSwitchedShunt::getModsw, PsseSwitchedShunt::setModsw, 1));
        addField(fields, createNewField(STR_ADJM, Integer.class, PsseSwitchedShunt::getAdjm, PsseSwitchedShunt::setAdjm, 0));
        addField(fields, createNewField(STR_STAT, Integer.class, PsseSwitchedShunt::getStat, PsseSwitchedShunt::setStat, 1));
        addField(fields, createNewField(STR_VSWHI, Double.class, PsseSwitchedShunt::getVswhi, PsseSwitchedShunt::setVswhi, 1d));
        addField(fields, createNewField(STR_VSWLO, Double.class, PsseSwitchedShunt::getVswlo, PsseSwitchedShunt::setVswlo, 1d));
        addField(fields, createNewField(STR_SWREM, Integer.class, PsseSwitchedShunt::getSwrem, PsseSwitchedShunt::setSwrem, 0));
        addField(fields, createNewField(STR_RMPCT, Double.class, PsseSwitchedShunt::getRmpct, PsseSwitchedShunt::setRmpct, 100d));
        addField(fields, createNewField(STR_RMIDNT, String.class, PsseSwitchedShunt::getRmidnt, PsseSwitchedShunt::setRmidnt, " "));
        addField(fields, createNewField(STR_BINIT, Double.class, PsseSwitchedShunt::getBinit, PsseSwitchedShunt::setBinit, 0.0));
        addField(fields, createNewField(STR_N1, Integer.class, PsseSwitchedShunt::getN1, PsseSwitchedShunt::setN1, 0));
        addField(fields, createNewField(STR_B1, Double.class, PsseSwitchedShunt::getB1, PsseSwitchedShunt::setB1, 0d));
        addField(fields, createNewField(STR_N2, Integer.class, PsseSwitchedShunt::getN2, PsseSwitchedShunt::setN2, 0));
        addField(fields, createNewField(STR_B2, Double.class, PsseSwitchedShunt::getB2, PsseSwitchedShunt::setB2, 0d));
        addField(fields, createNewField(STR_N3, Integer.class, PsseSwitchedShunt::getN3, PsseSwitchedShunt::setN3, 0));
        addField(fields, createNewField(STR_B3, Double.class, PsseSwitchedShunt::getB3, PsseSwitchedShunt::setB3, 0d));
        addField(fields, createNewField(STR_N4, Integer.class, PsseSwitchedShunt::getN4, PsseSwitchedShunt::setN4, 0));
        addField(fields, createNewField(STR_B4, Double.class, PsseSwitchedShunt::getB4, PsseSwitchedShunt::setB4, 0d));
        addField(fields, createNewField(STR_N5, Integer.class, PsseSwitchedShunt::getN5, PsseSwitchedShunt::setN5, 0));
        addField(fields, createNewField(STR_B5, Double.class, PsseSwitchedShunt::getB5, PsseSwitchedShunt::setB5, 0d));
        addField(fields, createNewField(STR_N6, Integer.class, PsseSwitchedShunt::getN6, PsseSwitchedShunt::setN6, 0));
        addField(fields, createNewField(STR_B6, Double.class, PsseSwitchedShunt::getB6, PsseSwitchedShunt::setB6, 0d));
        addField(fields, createNewField(STR_N7, Integer.class, PsseSwitchedShunt::getN7, PsseSwitchedShunt::setN7, 0));
        addField(fields, createNewField(STR_B7, Double.class, PsseSwitchedShunt::getB7, PsseSwitchedShunt::setB7, 0d));
        addField(fields, createNewField(STR_N8, Integer.class, PsseSwitchedShunt::getN8, PsseSwitchedShunt::setN8, 0));
        addField(fields, createNewField(STR_B8, Double.class, PsseSwitchedShunt::getB8, PsseSwitchedShunt::setB8, 0d));
        addField(fields, createNewField(STR_ID, String.class, PsseSwitchedShunt::getId, PsseSwitchedShunt::setId, "1"));
        addField(fields, createNewField(STR_SHNTID, String.class, PsseSwitchedShunt::getId, PsseSwitchedShunt::setId, "1"));
        addField(fields, createNewField(STR_SWREG, Integer.class, PsseSwitchedShunt::getSwreg, PsseSwitchedShunt::setSwreg, 0));
        addField(fields, createNewField(STR_NREG, Integer.class, PsseSwitchedShunt::getNreg, PsseSwitchedShunt::setNreg, 0));
        addField(fields, createNewField(STR_S1, Integer.class, PsseSwitchedShunt::getS1, PsseSwitchedShunt::setS1, 1));
        addField(fields, createNewField(STR_S2, Integer.class, PsseSwitchedShunt::getS2, PsseSwitchedShunt::setS2, 1));
        addField(fields, createNewField(STR_S3, Integer.class, PsseSwitchedShunt::getS3, PsseSwitchedShunt::setS3, 1));
        addField(fields, createNewField(STR_S4, Integer.class, PsseSwitchedShunt::getS4, PsseSwitchedShunt::setS4, 1));
        addField(fields, createNewField(STR_S5, Integer.class, PsseSwitchedShunt::getS5, PsseSwitchedShunt::setS5, 1));
        addField(fields, createNewField(STR_S6, Integer.class, PsseSwitchedShunt::getS6, PsseSwitchedShunt::setS6, 1));
        addField(fields, createNewField(STR_S7, Integer.class, PsseSwitchedShunt::getS7, PsseSwitchedShunt::setS7, 1));
        addField(fields, createNewField(STR_S8, Integer.class, PsseSwitchedShunt::getS8, PsseSwitchedShunt::setS8, 1));

        return fields;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getModsw() {
        return modsw;
    }

    public void setModsw(int modsw) {
        this.modsw = modsw;
    }

    public int getAdjm() {
        return adjm;
    }

    public void setAdjm(int adjm) {
        this.adjm = adjm;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public double getVswhi() {
        return vswhi;
    }

    public void setVswhi(double vswhi) {
        this.vswhi = vswhi;
    }

    public double getVswlo() {
        return vswlo;
    }

    public void setVswlo(double vswlo) {
        this.vswlo = vswlo;
    }

    public int getSwrem() {
        checkVersion(STR_SWREM);
        return swrem;
    }

    public void setSwrem(int swrem) {
        this.swrem = swrem;
    }

    public double getRmpct() {
        return rmpct;
    }

    public void setRmpct(double rmpct) {
        this.rmpct = rmpct;
    }

    public String getRmidnt() {
        return rmidnt;
    }

    public void setRmidnt(String rmidnt) {
        this.rmidnt = rmidnt;
    }

    public double getBinit() {
        return binit;
    }

    public void setBinit(double binit) {
        this.binit = binit;
    }

    public int getN1() {
        return n1;
    }

    public void setN1(int n1) {
        this.n1 = n1;
    }

    public double getB1() {
        return b1;
    }

    public void setB1(double b1) {
        this.b1 = b1;
    }

    public int getN2() {
        return n2;
    }

    public void setN2(int n2) {
        this.n2 = n2;
    }

    public double getB2() {
        return b2;
    }

    public void setB2(double b2) {
        this.b2 = b2;
    }

    public int getN3() {
        return n3;
    }

    public void setN3(int n3) {
        this.n3 = n3;
    }

    public double getB3() {
        return b3;
    }

    public void setB3(double b3) {
        this.b3 = b3;
    }

    public int getN4() {
        return n4;
    }

    public void setN4(int n4) {
        this.n4 = n4;
    }

    public double getB4() {
        return b4;
    }

    public void setB4(double b4) {
        this.b4 = b4;
    }

    public int getN5() {
        return n5;
    }

    public void setN5(int n5) {
        this.n5 = n5;
    }

    public double getB5() {
        return b5;
    }

    public void setB5(double b5) {
        this.b5 = b5;
    }

    public int getN6() {
        return n6;
    }

    public void setN6(int n6) {
        this.n6 = n6;
    }

    public double getB6() {
        return b6;
    }

    public void setB6(double b6) {
        this.b6 = b6;
    }

    public int getN7() {
        return n7;
    }

    public void setN7(int n7) {
        this.n7 = n7;
    }

    public double getB7() {
        return b7;
    }

    public void setB7(double b7) {
        this.b7 = b7;
    }

    public int getN8() {
        return n8;
    }

    public void setN8(int n8) {
        this.n8 = n8;
    }

    public double getB8() {
        return b8;
    }

    public void setB8(double b8) {
        this.b8 = b8;
    }

    public String getId() {
        checkVersion("id");
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSwreg() {
        checkVersion(STR_SWREG);
        return swreg;
    }

    public void setSwreg(int swreg) {
        this.swreg = swreg;
    }

    public int getNreg() {
        checkVersion("nreg");
        return nreg;
    }

    public void setNreg(int nreg) {
        this.nreg = nreg;
    }

    public int getS1() {
        checkVersion("s1");
        return s1;
    }

    public void setS1(int s1) {
        this.s1 = s1;
    }

    public int getS2() {
        checkVersion("s2");
        return s2;
    }

    public void setS2(int s2) {
        this.s2 = s2;
    }

    public int getS3() {
        checkVersion("s3");
        return s3;
    }

    public void setS3(int s3) {
        this.s3 = s3;
    }

    public int getS4() {
        checkVersion("s4");
        return s4;
    }

    public void setS4(int s4) {
        this.s4 = s4;
    }

    public int getS5() {
        checkVersion("s5");
        return s5;
    }

    public void setS5(int s5) {
        this.s5 = s5;
    }

    public int getS6() {
        checkVersion("s6");
        return s6;
    }

    public void setS6(int s6) {
        this.s6 = s6;
    }

    public int getS7() {
        checkVersion("s7");
        return s7;
    }

    public void setS7(int s7) {
        this.s7 = s7;
    }

    public int getS8() {
        checkVersion("s8");
        return s8;
    }

    public void setS8(int s8) {
        this.s8 = s8;
    }

    public PsseSwitchedShunt copy() {
        PsseSwitchedShunt copy = new PsseSwitchedShunt();
        copy.i = this.i;
        copy.modsw = this.modsw;
        copy.adjm = this.adjm;
        copy.stat = this.stat;
        copy.vswhi = this.vswhi;
        copy.vswlo = this.vswlo;
        copy.swrem = this.swrem;
        copy.rmpct = this.rmpct;
        copy.rmidnt = this.rmidnt;
        copy.binit = this.binit;
        copy.n1 = this.n1;
        copy.b1 = this.b1;
        copy.n2 = this.n2;
        copy.b2 = this.b2;
        copy.n3 = this.n3;
        copy.b3 = this.b3;
        copy.n4 = this.n4;
        copy.b4 = this.b4;
        copy.n5 = this.n5;
        copy.b5 = this.b5;
        copy.n6 = this.n6;
        copy.b6 = this.b6;
        copy.n7 = this.n7;
        copy.b7 = this.b7;
        copy.n8 = this.n8;
        copy.b8 = this.b8;
        copy.id = this.id;
        copy.swreg = this.swreg;
        copy.nreg = this.nreg;
        copy.s1 = this.s1;
        copy.s2 = this.s2;
        copy.s3 = this.s3;
        copy.s4 = this.s4;
        copy.s5 = this.s5;
        copy.s6 = this.s6;
        copy.s7 = this.s7;
        copy.s8 = this.s8;
        return copy;
    }
}
