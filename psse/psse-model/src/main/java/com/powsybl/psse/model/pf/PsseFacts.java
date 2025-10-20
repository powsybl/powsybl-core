/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

import static com.powsybl.psse.model.io.Util.getFieldFromMultiplePotentialHeaders;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseFacts extends PsseVersioned {

    private static final String STRING_REMOT = "remot";
    private static final String STRING_FCREG = "fcreg";
    private static final String STRING_NREG = "nreg";

    private String name;

    private int i;

    private int j = 0;

    private int mode = 1;

    private double pdes = 0.0;

    private double qdes = 0.0;

    private double vset = 1.0;

    private double shmx = 9999.0;

    private double trmx = 9999.0;

    private double vtmn = 0.9;

    private double vtmx = 1.1;

    private double vsmx = 1.0;

    private double imx = 0.0;

    private double linx = 0.05;

    private double rmpct = 100.0;

    private int owner = 1;

    private double set1 = 0.0;

    private double set2 = 0.0;

    private int vsref = 0;

    @Revision(until = 33)
    private int remot = 0;

    private String mname;

    @Revision(since = 35)
    private int fcreg = 0;

    @Revision(since = 35)
    private int nreg = 0;

    public static PsseFacts fromRecord(NamedCsvRecord rec, PsseVersion version) {
        PsseFacts psseFacts = new PsseFacts();
        psseFacts.setName(rec.getField("name"));
        psseFacts.setI(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "i", "ibus")));
        psseFacts.setJ(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "j", "jbus")));
        psseFacts.setMode(Integer.parseInt(rec.getField("mode")));
        psseFacts.setPdes(Double.parseDouble(rec.getField("pdes")));
        psseFacts.setQdes(Double.parseDouble(rec.getField("qdes")));
        psseFacts.setVset(Double.parseDouble(rec.getField("vset")));
        psseFacts.setShmx(Double.parseDouble(rec.getField("shmx")));
        psseFacts.setTrmx(Double.parseDouble(rec.getField("trmx")));
        psseFacts.setVtmn(Double.parseDouble(rec.getField("vtmn")));
        psseFacts.setVtmx(Double.parseDouble(rec.getField("vtmx")));
        psseFacts.setVsmx(Double.parseDouble(rec.getField("vsmx")));
        psseFacts.setImx(Double.parseDouble(rec.getField("imx")));
        psseFacts.setLinx(Double.parseDouble(rec.getField("linx")));
        psseFacts.setRmpct(Double.parseDouble(rec.getField("rmpct")));
        psseFacts.setOwner(Integer.parseInt(rec.getField("owner")));
        psseFacts.setSet1(Double.parseDouble(rec.getField("set1")));
        psseFacts.setSet2(Double.parseDouble(rec.getField("set2")));
        psseFacts.setVsref(Integer.parseInt(rec.getField("vsref")));
        if (version.getMajorNumber() <= 33) {
            psseFacts.setRemot(Integer.parseInt(rec.getField(STRING_REMOT)));
        }
        psseFacts.setMname(rec.getField("mname"));
        if (version.getMajorNumber() >= 35) {
            psseFacts.setFcreg(Integer.parseInt(rec.getField(STRING_FCREG)));
            psseFacts.setNreg(Integer.parseInt(rec.getField(STRING_NREG)));
        }
        return psseFacts;
    }

    public static String[] toRecord(PsseFacts psseFacts, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "name" -> psseFacts.getName();
                case "i", "ibus" -> String.valueOf(psseFacts.getI());
                case "j", "jbus" -> String.valueOf(psseFacts.getJ());
                case "mode" -> String.valueOf(psseFacts.getMode());
                case "pdes" -> String.valueOf(psseFacts.getPdes());
                case "qdes" -> String.valueOf(psseFacts.getQdes());
                case "vset" -> String.valueOf(psseFacts.getVset());
                case "shmx" -> String.valueOf(psseFacts.getShmx());
                case "trmx" -> String.valueOf(psseFacts.getTrmx());
                case "vtmn" -> String.valueOf(psseFacts.getVtmn());
                case "vtmx" -> String.valueOf(psseFacts.getVtmx());
                case "vsmx" -> String.valueOf(psseFacts.getVsmx());
                case "imx" -> String.valueOf(psseFacts.getImx());
                case "linx" -> String.valueOf(psseFacts.getLinx());
                case "rmpct" -> String.valueOf(psseFacts.getRmpct());
                case "owner" -> String.valueOf(psseFacts.getOwner());
                case "set1" -> String.valueOf(psseFacts.getSet1());
                case "set2" -> String.valueOf(psseFacts.getSet2());
                case "vsref" -> String.valueOf(psseFacts.getVsref());
                case STRING_REMOT -> String.valueOf(psseFacts.getRemot());
                case "mname" -> psseFacts.getMname();
                case STRING_FCREG -> String.valueOf(psseFacts.getFcreg());
                case STRING_NREG -> String.valueOf(psseFacts.getNreg());
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public double getPdes() {
        return pdes;
    }

    public void setPdes(double pdes) {
        this.pdes = pdes;
    }

    public double getQdes() {
        return qdes;
    }

    public void setQdes(double qdes) {
        this.qdes = qdes;
    }

    public double getVset() {
        return vset;
    }

    public void setVset(double vset) {
        this.vset = vset;
    }

    public double getShmx() {
        return shmx;
    }

    public void setShmx(double shmx) {
        this.shmx = shmx;
    }

    public double getTrmx() {
        return trmx;
    }

    public void setTrmx(double trmx) {
        this.trmx = trmx;
    }

    public double getVtmn() {
        return vtmn;
    }

    public void setVtmn(double vtmn) {
        this.vtmn = vtmn;
    }

    public double getVtmx() {
        return vtmx;
    }

    public void setVtmx(double vtmx) {
        this.vtmx = vtmx;
    }

    public double getVsmx() {
        return vsmx;
    }

    public void setVsmx(double vsmx) {
        this.vsmx = vsmx;
    }

    public double getImx() {
        return imx;
    }

    public void setImx(double imx) {
        this.imx = imx;
    }

    public double getLinx() {
        return linx;
    }

    public void setLinx(double linx) {
        this.linx = linx;
    }

    public double getRmpct() {
        return rmpct;
    }

    public void setRmpct(double rmpct) {
        this.rmpct = rmpct;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public double getSet1() {
        return set1;
    }

    public void setSet1(double set1) {
        this.set1 = set1;
    }

    public double getSet2() {
        return set2;
    }

    public void setSet2(double set2) {
        this.set2 = set2;
    }

    public int getVsref() {
        return vsref;
    }

    public void setVsref(int vsref) {
        this.vsref = vsref;
    }

    public int getRemot() {
        checkVersion(STRING_REMOT);
        return remot;
    }

    public void setRemot(int remot) {
        this.remot = remot;
    }

    public String getMname() {
        return mname;
    }

    public void setMname(String mname) {
        this.mname = mname;
    }

    public int getFcreg() {
        checkVersion(STRING_FCREG);
        return fcreg;
    }

    public void setFcreg(int fcreg) {
        this.fcreg = fcreg;
    }

    public int getNreg() {
        checkVersion(STRING_NREG);
        return nreg;
    }

    public void setNreg(int nreg) {
        this.nreg = nreg;
    }

    public PsseFacts copy() {
        PsseFacts copy = new PsseFacts();
        copy.name = this.name;
        copy.i = this.i;
        copy.j = this.j;
        copy.mode = this.mode;
        copy.pdes = this.pdes;
        copy.qdes = this.qdes;
        copy.vset = this.vset;
        copy.shmx = this.shmx;
        copy.trmx = this.trmx;
        copy.vtmn = this.vtmn;
        copy.vtmx = this.vtmx;
        copy.vsmx = this.vsmx;
        copy.imx = this.imx;
        copy.linx = this.linx;
        copy.rmpct = this.rmpct;
        copy.owner = this.owner;
        copy.set1 = this.set1;
        copy.set2 = this.set2;
        copy.vsref = this.vsref;
        copy.remot = this.remot;
        copy.mname = this.mname;
        copy.fcreg = this.fcreg;
        copy.nreg = this.nreg;
        return copy;
    }
}
