/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.Map;
import java.util.function.Function;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseInductionMachine {

    private static final Map<String, Function<PsseInductionMachine, String>> GETTERS = Map.ofEntries(
        Map.entry("i", m -> String.valueOf(m.getI())),
        Map.entry("ibus", m -> String.valueOf(m.getI())),
        Map.entry("id", PsseInductionMachine::getId),
        Map.entry("imid", PsseInductionMachine::getId),
        Map.entry("status", m -> String.valueOf(m.getStat())),
        Map.entry("stat", m -> String.valueOf(m.getStat())),
        Map.entry("scode", m -> String.valueOf(m.getScode())),
        Map.entry("dcode", m -> String.valueOf(m.getDcode())),
        Map.entry("area", m -> String.valueOf(m.getArea())),
        Map.entry("zone", m -> String.valueOf(m.getZone())),
        Map.entry("owner", m -> String.valueOf(m.getOwner())),
        Map.entry("tcode", m -> String.valueOf(m.getTcode())),
        Map.entry("bcode", m -> String.valueOf(m.getBcode())),
        Map.entry("mbase", m -> String.valueOf(m.getMbase())),
        Map.entry("ratekv", m -> String.valueOf(m.getRatekv())),
        Map.entry("pcode", m -> String.valueOf(m.getPcode())),
        Map.entry("pset", m -> String.valueOf(m.getPset())),
        Map.entry("h", m -> String.valueOf(m.getH())),
        Map.entry("hconst", m -> String.valueOf(m.getH())),
        Map.entry("a", m -> String.valueOf(m.getA())),
        Map.entry("aconst", m -> String.valueOf(m.getA())),
        Map.entry("b", m -> String.valueOf(m.getB())),
        Map.entry("bconst", m -> String.valueOf(m.getB())),
        Map.entry("d", m -> String.valueOf(m.getD())),
        Map.entry("dconst", m -> String.valueOf(m.getD())),
        Map.entry("e", m -> String.valueOf(m.getE())),
        Map.entry("econst", m -> String.valueOf(m.getE())),
        Map.entry("ra", m -> String.valueOf(m.getRa())),
        Map.entry("xa", m -> String.valueOf(m.getXa())),
        Map.entry("xm", m -> String.valueOf(m.getXm())),
        Map.entry("r1", m -> String.valueOf(m.getR1())),
        Map.entry("x1", m -> String.valueOf(m.getX1())),
        Map.entry("r2", m -> String.valueOf(m.getR2())),
        Map.entry("x2", m -> String.valueOf(m.getX2())),
        Map.entry("x3", m -> String.valueOf(m.getX3())),
        Map.entry("e1", m -> String.valueOf(m.getE1())),
        Map.entry("se1", m -> String.valueOf(m.getSe1())),
        Map.entry("e2", m -> String.valueOf(m.getE2())),
        Map.entry("se2", m -> String.valueOf(m.getSe2())),
        Map.entry("ia1", m -> String.valueOf(m.getIa1())),
        Map.entry("ia2", m -> String.valueOf(m.getIa2())),
        Map.entry("xamult", m -> String.valueOf(m.getXamult()))
    );

    // This dataBlock is valid since version 33
    private int i;
    private String id;
    private int stat = 1;
    private int scode = 1;
    private int dcode = 2;
    private int area = -1;
    private int zone = -1;
    private int owner = -1;
    private int tcode = 1;
    private int bcode = 1;
    private double mbase = -1.0;
    private double ratekv = 0.0;
    private int pcode = 1;
    private Double pset;
    private double h = 1.0;
    private double a = 1.0;
    private double b = 1.0;
    private double d = 1.0;
    private double e = 1.0;
    private double ra = 0.0;
    private double xa = 0.0;
    private double xm = 2.5;
    private double r1 = 999.0;
    private double x1 = 999.0;
    private double r2 = 999.0;
    private double x2 = 999.0;
    private double x3 = 0.0;
    private double e1 = 1.0;
    private double se1 = 0.0;
    private double e2 = 1.2;
    private double se2 = 0.0;
    private double ia1 = 0.0;
    private double ia2 = 0.0;
    private double xamult = 1.0;

    public static PsseInductionMachine fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseInductionMachine psseInductionMachine = new PsseInductionMachine();
        psseInductionMachine.setI(parseIntFromRecord(rec, headers, "i", "ibus"));
        psseInductionMachine.setId(parseStringFromRecord(rec, "1", headers, "id", "imid"));
        psseInductionMachine.setStat(parseIntFromRecord(rec, 1, headers, "stat"));
        psseInductionMachine.setScode(parseIntFromRecord(rec, 1, headers, "scode"));
        psseInductionMachine.setDcode(parseIntFromRecord(rec, 2, headers, "dcode"));
        psseInductionMachine.setArea(parseIntFromRecord(rec, -1, headers, "area"));
        psseInductionMachine.setZone(parseIntFromRecord(rec, -1, headers, "zone"));
        psseInductionMachine.setOwner(parseIntFromRecord(rec, -1, headers, "owner"));
        psseInductionMachine.setTcode(parseIntFromRecord(rec, 1, headers, "tcode"));
        psseInductionMachine.setBcode(parseIntFromRecord(rec, 1, headers, "bcode"));
        psseInductionMachine.setMbase(parseDoubleFromRecord(rec, -1.0, headers, "mbase"));
        psseInductionMachine.setRatekv(parseDoubleFromRecord(rec, 0.0, headers, "ratekv"));
        psseInductionMachine.setPcode(parseIntFromRecord(rec, 1, headers, "pcode"));
        psseInductionMachine.setPset(parseDoubleFromRecord(rec, null, headers, "pset"));
        psseInductionMachine.setH(parseDoubleFromRecord(rec, 1.0, headers, "h", "hconst"));
        psseInductionMachine.setA(parseDoubleFromRecord(rec, 1.0, headers, "a", "aconst"));
        psseInductionMachine.setB(parseDoubleFromRecord(rec, 1.0, headers, "b", "bconst"));
        psseInductionMachine.setD(parseDoubleFromRecord(rec, 1.0, headers, "d", "dconst"));
        psseInductionMachine.setE(parseDoubleFromRecord(rec, 1.0, headers, "e", "econst"));
        psseInductionMachine.setRa(parseDoubleFromRecord(rec, 0.0, headers, "ra"));
        psseInductionMachine.setXa(parseDoubleFromRecord(rec, 0.0, headers, "xa"));
        psseInductionMachine.setXm(parseDoubleFromRecord(rec, 2.5, headers, "xm"));
        psseInductionMachine.setR1(parseDoubleFromRecord(rec, 999.0, headers, "r1"));
        psseInductionMachine.setX1(parseDoubleFromRecord(rec, 999.0, headers, "x1"));
        psseInductionMachine.setR2(parseDoubleFromRecord(rec, 999.0, headers, "r2"));
        psseInductionMachine.setX2(parseDoubleFromRecord(rec, 999.0, headers, "x2"));
        psseInductionMachine.setX3(parseDoubleFromRecord(rec, 0.0, headers, "x3"));
        psseInductionMachine.setE1(parseDoubleFromRecord(rec, 1.0, headers, "e1"));
        psseInductionMachine.setSe1(parseDoubleFromRecord(rec, 0.0, headers, "se1"));
        psseInductionMachine.setE2(parseDoubleFromRecord(rec, 1.2, headers, "e2"));
        psseInductionMachine.setSe2(parseDoubleFromRecord(rec, 0.0, headers, "se2"));
        psseInductionMachine.setIa1(parseDoubleFromRecord(rec, 0.0, headers, "ia1"));
        psseInductionMachine.setIa2(parseDoubleFromRecord(rec, 0.0, headers, "ia2"));
        psseInductionMachine.setXamult(parseDoubleFromRecord(rec, 1.0, headers, "xamult"));
        return psseInductionMachine;
    }

    public static String[] toRecord(PsseInductionMachine psseInductionMachine, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            Function<PsseInductionMachine, String> getter = GETTERS.get(headers[i]);
            if (getter == null) {
                throw new PsseException("Unsupported header: " + headers[i]);
            }
            row[i] = getter.apply(psseInductionMachine);
        }
        return row;
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
