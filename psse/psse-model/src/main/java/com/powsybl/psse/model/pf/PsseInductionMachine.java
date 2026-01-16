/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

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
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseInductionMachine {

    private static final Map<String, PsseFieldDefinition<PsseInductionMachine, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_COMMON_1 = {STR_STAT, STR_SCODE, STR_DCODE, STR_AREA, STR_ZONE, STR_OWNER, STR_TCODE, STR_BCODE, STR_MBASE, STR_RATEKV, STR_PCODE, STR_PSET};
    private static final String[] FIELD_NAMES_COMMON_2 = {STR_RA, STR_XA, STR_XM, STR_R1, STR_X1, STR_R2, STR_X2, STR_X3, STR_E1, STR_SE1, STR_E2, STR_SE2, STR_IA1, STR_IA2, STR_XAMULT};
    private static final String[] FIELD_NAMES_33_START = {STR_I, STR_ID};
    private static final String[] FIELD_NAMES_33_MIDDLE = {STR_H, STR_A, STR_B, STR_D, STR_E};
    private static final String[] FIELD_NAMES_33 = concatStringArrays(FIELD_NAMES_33_START, FIELD_NAMES_COMMON_1, FIELD_NAMES_33_MIDDLE, FIELD_NAMES_COMMON_2);
    private static final String[] FIELD_NAMES_35_START = {STR_IBUS, STR_IMID};
    private static final String[] FIELD_NAMES_35_MIDDLE = {STR_HCONST, STR_ACONST, STR_BCONST, STR_DCONST, STR_ECONST};
    private static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_35_START, FIELD_NAMES_COMMON_1, FIELD_NAMES_35_MIDDLE, FIELD_NAMES_COMMON_2);

    // This dataBlock is valid since version 33
    private int i;
    private String id;
    private int stat = defaultIntegerFor(STR_STAT, FIELDS);
    private int scode = defaultIntegerFor(STR_SCODE, FIELDS);
    private int dcode = defaultIntegerFor(STR_DCODE, FIELDS);
    private int area = defaultIntegerFor(STR_AREA, FIELDS);
    private int zone = defaultIntegerFor(STR_ZONE, FIELDS);
    private int owner = defaultIntegerFor(STR_OWNER, FIELDS);
    private int tcode = defaultIntegerFor(STR_TCODE, FIELDS);
    private int bcode = defaultIntegerFor(STR_BCODE, FIELDS);
    private double mbase = defaultDoubleFor(STR_MBASE, FIELDS);
    private double ratekv = defaultDoubleFor(STR_RATEKV, FIELDS);
    private int pcode = defaultIntegerFor(STR_PCODE, FIELDS);
    private Double pset;
    private double h = defaultDoubleFor(STR_H, FIELDS);
    private double a = defaultDoubleFor(STR_A, FIELDS);
    private double b = defaultDoubleFor(STR_B, FIELDS);
    private double d = defaultDoubleFor(STR_D, FIELDS);
    private double e = defaultDoubleFor(STR_E, FIELDS);
    private double ra = defaultDoubleFor(STR_RA, FIELDS);
    private double xa = defaultDoubleFor(STR_XA, FIELDS);
    private double xm = defaultDoubleFor(STR_XM, FIELDS);
    private double r1 = defaultDoubleFor(STR_R1, FIELDS);
    private double x1 = defaultDoubleFor(STR_X1, FIELDS);
    private double r2 = defaultDoubleFor(STR_R2, FIELDS);
    private double x2 = defaultDoubleFor(STR_X2, FIELDS);
    private double x3 = defaultDoubleFor(STR_X3, FIELDS);
    private double e1 = defaultDoubleFor(STR_E1, FIELDS);
    private double se1 = defaultDoubleFor(STR_SE1, FIELDS);
    private double e2 = defaultDoubleFor(STR_E2, FIELDS);
    private double se2 = defaultDoubleFor(STR_SE2, FIELDS);
    private double ia1 = defaultDoubleFor(STR_IA1, FIELDS);
    private double ia2 = defaultDoubleFor(STR_IA2, FIELDS);
    private double xamult = defaultDoubleFor(STR_XAMULT, FIELDS);

    public static String[] getFieldNames33() {
        return FIELD_NAMES_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseInductionMachine fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseInductionMachine::new);
    }

    public static String[] toRecord(PsseInductionMachine psseInductionMachine, String[] headers) {
        return Util.toRecord(psseInductionMachine, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseInductionMachine, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseInductionMachine, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseInductionMachine::getI, PsseInductionMachine::setI));
        addField(fields, createNewField(STR_IBUS, Integer.class, PsseInductionMachine::getI, PsseInductionMachine::setI));
        addField(fields, createNewField(STR_ID, String.class, PsseInductionMachine::getId, PsseInductionMachine::setId, "1"));
        addField(fields, createNewField(STR_IMID, String.class, PsseInductionMachine::getId, PsseInductionMachine::setId, "1"));
        addField(fields, createNewField(STR_STAT, Integer.class, PsseInductionMachine::getStat, PsseInductionMachine::setStat, 1));
        addField(fields, createNewField(STR_SCODE, Integer.class, PsseInductionMachine::getScode, PsseInductionMachine::setScode, 1));
        addField(fields, createNewField(STR_DCODE, Integer.class, PsseInductionMachine::getDcode, PsseInductionMachine::setDcode, 2));
        addField(fields, createNewField(STR_AREA, Integer.class, PsseInductionMachine::getArea, PsseInductionMachine::setArea, -1));
        addField(fields, createNewField(STR_ZONE, Integer.class, PsseInductionMachine::getZone, PsseInductionMachine::setZone, -1));
        addField(fields, createNewField(STR_OWNER, Integer.class, PsseInductionMachine::getOwner, PsseInductionMachine::setOwner, -1));
        addField(fields, createNewField(STR_TCODE, Integer.class, PsseInductionMachine::getTcode, PsseInductionMachine::setTcode, 1));
        addField(fields, createNewField(STR_BCODE, Integer.class, PsseInductionMachine::getBcode, PsseInductionMachine::setBcode, 1));
        addField(fields, createNewField(STR_MBASE, Double.class, PsseInductionMachine::getMbase, PsseInductionMachine::setMbase, -1.0));
        addField(fields, createNewField(STR_RATEKV, Double.class, PsseInductionMachine::getRatekv, PsseInductionMachine::setRatekv, 0.0));
        addField(fields, createNewField(STR_PCODE, Integer.class, PsseInductionMachine::getPcode, PsseInductionMachine::setPcode, 1));
        addField(fields, createNewField(STR_PSET, Double.class, PsseInductionMachine::getPset, PsseInductionMachine::setPset, null));
        addField(fields, createNewField(STR_H, Double.class, PsseInductionMachine::getH, PsseInductionMachine::setH, 1.0));
        addField(fields, createNewField(STR_HCONST, Double.class, PsseInductionMachine::getH, PsseInductionMachine::setH, 1.0));
        addField(fields, createNewField(STR_A, Double.class, PsseInductionMachine::getA, PsseInductionMachine::setA, 1.0));
        addField(fields, createNewField(STR_ACONST, Double.class, PsseInductionMachine::getA, PsseInductionMachine::setA, 1.0));
        addField(fields, createNewField(STR_B, Double.class, PsseInductionMachine::getB, PsseInductionMachine::setB, 1.0));
        addField(fields, createNewField(STR_BCONST, Double.class, PsseInductionMachine::getB, PsseInductionMachine::setB, 1.0));
        addField(fields, createNewField(STR_D, Double.class, PsseInductionMachine::getD, PsseInductionMachine::setD, 1.0));
        addField(fields, createNewField(STR_DCONST, Double.class, PsseInductionMachine::getD, PsseInductionMachine::setD, 1.0));
        addField(fields, createNewField(STR_E, Double.class, PsseInductionMachine::getE, PsseInductionMachine::setE, 1.0));
        addField(fields, createNewField(STR_ECONST, Double.class, PsseInductionMachine::getE, PsseInductionMachine::setE, 1.0));
        addField(fields, createNewField(STR_RA, Double.class, PsseInductionMachine::getRa, PsseInductionMachine::setRa, 0.0));
        addField(fields, createNewField(STR_XA, Double.class, PsseInductionMachine::getXa, PsseInductionMachine::setXa, 0.0));
        addField(fields, createNewField(STR_XM, Double.class, PsseInductionMachine::getXm, PsseInductionMachine::setXm, 2.5));
        addField(fields, createNewField(STR_R1, Double.class, PsseInductionMachine::getR1, PsseInductionMachine::setR1, 999.0));
        addField(fields, createNewField(STR_X1, Double.class, PsseInductionMachine::getX1, PsseInductionMachine::setX1, 999.0));
        addField(fields, createNewField(STR_R2, Double.class, PsseInductionMachine::getR2, PsseInductionMachine::setR2, 999.0));
        addField(fields, createNewField(STR_X2, Double.class, PsseInductionMachine::getX2, PsseInductionMachine::setX2, 999.0));
        addField(fields, createNewField(STR_X3, Double.class, PsseInductionMachine::getX3, PsseInductionMachine::setX3, 0.0));
        addField(fields, createNewField(STR_E1, Double.class, PsseInductionMachine::getE1, PsseInductionMachine::setE1, 1.0));
        addField(fields, createNewField(STR_SE1, Double.class, PsseInductionMachine::getSe1, PsseInductionMachine::setSe1, 0.0));
        addField(fields, createNewField(STR_E2, Double.class, PsseInductionMachine::getE2, PsseInductionMachine::setE2, 1.2));
        addField(fields, createNewField(STR_SE2, Double.class, PsseInductionMachine::getSe2, PsseInductionMachine::setSe2, 0.0));
        addField(fields, createNewField(STR_IA1, Double.class, PsseInductionMachine::getIa1, PsseInductionMachine::setIa1, 0.0));
        addField(fields, createNewField(STR_IA2, Double.class, PsseInductionMachine::getIa2, PsseInductionMachine::setIa2, 0.0));
        addField(fields, createNewField(STR_XAMULT, Double.class, PsseInductionMachine::getXamult, PsseInductionMachine::setXamult, 1.0));

        return fields;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public int getScode() {
        return scode;
    }

    public void setScode(int scode) {
        this.scode = scode;
    }

    public int getDcode() {
        return dcode;
    }

    public void setDcode(int dcode) {
        this.dcode = dcode;
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

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getTcode() {
        return tcode;
    }

    public void setTcode(int tcode) {
        this.tcode = tcode;
    }

    public int getBcode() {
        return bcode;
    }

    public void setBcode(int bcode) {
        this.bcode = bcode;
    }

    public double getMbase() {
        return mbase;
    }

    public void setMbase(double mbase) {
        this.mbase = mbase;
    }

    public double getRatekv() {
        return ratekv;
    }

    public void setRatekv(double ratekv) {
        this.ratekv = ratekv;
    }

    public int getPcode() {
        return pcode;
    }

    public void setPcode(int pcode) {
        this.pcode = pcode;
    }

    public Double getPset() {
        return pset;
    }

    public void setPset(Double pset) {
        this.pset = pset;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

    public double getE() {
        return e;
    }

    public void setE(double e) {
        this.e = e;
    }

    public double getRa() {
        return ra;
    }

    public void setRa(double ra) {
        this.ra = ra;
    }

    public double getXa() {
        return xa;
    }

    public void setXa(double xa) {
        this.xa = xa;
    }

    public double getXm() {
        return xm;
    }

    public void setXm(double xm) {
        this.xm = xm;
    }

    public double getR1() {
        return r1;
    }

    public void setR1(double r1) {
        this.r1 = r1;
    }

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getR2() {
        return r2;
    }

    public void setR2(double r2) {
        this.r2 = r2;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getX3() {
        return x3;
    }

    public void setX3(double x3) {
        this.x3 = x3;
    }

    public double getE1() {
        return e1;
    }

    public void setE1(double e1) {
        this.e1 = e1;
    }

    public double getSe1() {
        return se1;
    }

    public void setSe1(double se1) {
        this.se1 = se1;
    }

    public double getE2() {
        return e2;
    }

    public void setE2(double e2) {
        this.e2 = e2;
    }

    public double getSe2() {
        return se2;
    }

    public void setSe2(double se2) {
        this.se2 = se2;
    }

    public double getIa1() {
        return ia1;
    }

    public void setIa1(double ia1) {
        this.ia1 = ia1;
    }

    public double getIa2() {
        return ia2;
    }

    public void setIa2(double ia2) {
        this.ia2 = ia2;
    }

    public double getXamult() {
        return xamult;
    }

    public void setXamult(double xamult) {
        this.xamult = xamult;
    }
}
