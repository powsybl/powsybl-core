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
import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

import java.util.Optional;

import static com.powsybl.psse.model.io.Util.defaultIfEmpty;
import static com.powsybl.psse.model.io.Util.parseDoubleOrDefault;
import static com.powsybl.psse.model.io.Util.parseIntOrDefault;
import static java.lang.Integer.parseInt;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseGenerator extends PsseVersioned {

    private static final String STRING_I = "i";
    private static final String STRING_ID = "id";
    private static final String STRING_NREG = "nreg";
    private static final String STRING_BASLOD = "baslod";

    private int i;
    private String id;
    private double pg = 0;
    private double qg = 0;
    private double qt = 9999;
    private double qb = -9999;
    private double vs = 1;
    private int ireg = 0;
    private double mbase = Double.NaN;
    private double zr = 0;
    private double zx = 1;
    private double rt = 0;
    private double xt = 0;
    private double gtap = 1;
    private int stat = 1;
    private double rmpct = 100;
    private double pt = 9999;
    private double pb = -9999;
    private PsseOwnership ownership;
    private int wmod = 0;
    private double wpf = 1;

    @Revision(since = 35)
    private int nreg = 0;

    @Revision(since = 35)
    private int baslod = 0;

    public static PsseGenerator fromRecord(NamedCsvRecord rec, PsseVersion version) {
        PsseGenerator psseGenerator = new PsseGenerator();
        psseGenerator.setI(parseInt(rec.findField(STRING_I).isPresent() ?
            rec.getField(STRING_I) :
            rec.getField("ibus")));
        psseGenerator.setId(defaultIfEmpty(rec.findField(STRING_ID).isPresent() ?
            rec.getField(STRING_ID) :
            rec.getField("machid"), "1"));
        psseGenerator.setPg(Double.parseDouble(rec.getField("pg")));
        psseGenerator.setQg(Double.parseDouble(rec.getField("qg")));
        psseGenerator.setQt(Double.parseDouble(rec.getField("qt")));
        psseGenerator.setQb(Double.parseDouble(rec.getField("qb")));
        psseGenerator.setVs(Double.parseDouble(rec.getField("vs")));
        psseGenerator.setIreg(parseInt(rec.getField("ireg")));
        psseGenerator.setMbase(Double.parseDouble(rec.getField("mbase")));
        psseGenerator.setZr(Double.parseDouble(rec.getField("zr")));
        psseGenerator.setZx(Double.parseDouble(rec.getField("zx")));
        psseGenerator.setRt(Double.parseDouble(rec.getField("rt")));
        psseGenerator.setXt(Double.parseDouble(rec.getField("xt")));
        psseGenerator.setGtap(Double.parseDouble(rec.getField("gtap")));
        psseGenerator.setStat(parseInt(rec.getField("stat")));
        psseGenerator.setRmpct(Double.parseDouble(rec.getField("rmpct")));
        psseGenerator.setPt(Double.parseDouble(rec.getField("pt")));
        psseGenerator.setPb(Double.parseDouble(rec.getField("pb")));
        psseGenerator.setOwnership(PsseOwnership.fromRecord(rec, version));
        psseGenerator.setWmod(parseIntOrDefault(rec.getField("wmod"), 0));
        psseGenerator.setWpf(parseDoubleOrDefault(rec.getField("wpf"), 1.0));
        if (version.getMajorNumber() >= 35) {
            psseGenerator.setNreg(parseInt(rec.getField(STRING_NREG)));
            psseGenerator.setBaslod(parseInt(rec.getField(STRING_BASLOD)));
        }

        return psseGenerator;
    }

    public static String[] toRecord(PsseGenerator psseGenerator, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case STRING_I, "ibus" -> String.valueOf(psseGenerator.getI());
                case STRING_ID, "machid" -> psseGenerator.getId();
                case "pg" -> String.valueOf(psseGenerator.getPg());
                case "qg" -> String.valueOf(psseGenerator.getQg());
                case "qt" -> String.valueOf(psseGenerator.getQt());
                case "qb" -> String.valueOf(psseGenerator.getQb());
                case "vs" -> String.valueOf(psseGenerator.getVs());
                case "ireg" -> String.valueOf(psseGenerator.getIreg());
                case "mbase" -> String.valueOf(psseGenerator.getMbase());
                case "zr" -> String.valueOf(psseGenerator.getZr());
                case "zx" -> String.valueOf(psseGenerator.getZx());
                case "rt" -> String.valueOf(psseGenerator.getRt());
                case "xt" -> String.valueOf(psseGenerator.getXt());
                case "gtap" -> String.valueOf(psseGenerator.getGtap());
                case "stat" -> String.valueOf(psseGenerator.getStat());
                case "rmpct" -> String.valueOf(psseGenerator.getRmpct());
                case "pt" -> String.valueOf(psseGenerator.getPt());
                case "pb" -> String.valueOf(psseGenerator.getPb());
                case "wmod" -> String.valueOf(psseGenerator.getWmod());
                case "wpf" -> String.valueOf(psseGenerator.getWpf());
                case STRING_NREG -> String.valueOf(psseGenerator.getNreg());
                case STRING_BASLOD -> String.valueOf(psseGenerator.getBaslod());
                default -> {
                    Optional<String> optionalValue = psseGenerator.getOwnership().headerToString(headers[i]);
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    throw new PsseException("Unsupported header: " + headers[i]);
                }
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPg() {
        return pg;
    }

    public void setPg(double pg) {
        this.pg = pg;
    }

    public double getQg() {
        return qg;
    }

    public void setQg(double qg) {
        this.qg = qg;
    }

    public double getQt() {
        return qt;
    }

    public void setQt(double qt) {
        this.qt = qt;
    }

    public double getQb() {
        return qb;
    }

    public void setQb(double qb) {
        this.qb = qb;
    }

    public double getVs() {
        return vs;
    }

    public void setVs(double vs) {
        this.vs = vs;
    }

    public int getIreg() {
        return ireg;
    }

    public void setIreg(int ireg) {
        this.ireg = ireg;
    }

    public double getMbase() {
        return mbase;
    }

    public void setMbase(double mbase) {
        this.mbase = mbase;
    }

    public double getZr() {
        return zr;
    }

    public void setZr(double zr) {
        this.zr = zr;
    }

    public double getZx() {
        return zx;
    }

    public void setZx(double zx) {
        this.zx = zx;
    }

    public double getRt() {
        return rt;
    }

    public void setRt(double rt) {
        this.rt = rt;
    }

    public double getXt() {
        return xt;
    }

    public void setXt(double xt) {
        this.xt = xt;
    }

    public double getGtap() {
        return gtap;
    }

    public void setGtap(double gtap) {
        this.gtap = gtap;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public double getRmpct() {
        return rmpct;
    }

    public void setRmpct(double rmpct) {
        this.rmpct = rmpct;
    }

    public double getPt() {
        return pt;
    }

    public void setPt(double pt) {
        this.pt = pt;
    }

    public double getPb() {
        return pb;
    }

    public void setPb(double pb) {
        this.pb = pb;
    }

    public int getWmod() {
        return wmod;
    }

    public void setWmod(int wmod) {
        this.wmod = wmod;
    }

    public double getWpf() {
        return wpf;
    }

    public void setWpf(double wpf) {
        this.wpf = wpf;
    }

    public int getNreg() {
        checkVersion("nreg");
        return nreg;
    }

    public void setNreg(int nreg) {
        checkVersion("nreg");
        this.nreg = nreg;
    }

    public int getBaslod() {
        checkVersion("baslod");
        return baslod;
    }

    public void setBaslod(int baslod) {
        checkVersion("baslod");
        this.baslod = baslod;
    }

    public PsseOwnership getOwnership() {
        return ownership;
    }

    public void setOwnership(PsseOwnership ownership) {
        this.ownership = ownership;
    }

    public PsseGenerator copy() {
        PsseGenerator copy = new PsseGenerator();
        copy.i = this.i;
        copy.id = this.id;
        copy.pg = this.pg;
        copy.qg = this.qg;
        copy.qt = this.qt;
        copy.qb = this.qb;
        copy.vs = this.vs;
        copy.ireg = this.ireg;
        copy.mbase = this.mbase;
        copy.zr = this.zr;
        copy.zx = this.zx;
        copy.rt = this.rt;
        copy.xt = this.xt;
        copy.gtap = this.gtap;
        copy.stat = this.stat;
        copy.rmpct = this.rmpct;
        copy.pt = this.pt;
        copy.pb = this.pb;
        copy.ownership = this.ownership.copy();
        copy.wmod = this.wmod;
        copy.wpf = this.wpf;
        copy.nreg = this.nreg;
        copy.baslod = this.baslod;
        return copy;
    }
}
