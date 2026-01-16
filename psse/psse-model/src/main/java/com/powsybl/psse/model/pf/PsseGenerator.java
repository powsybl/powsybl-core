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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.checkForUnexpectedHeader;
import static com.powsybl.psse.model.io.Util.concatStringArrays;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseGenerator extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseGenerator, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_COMMON_1 = {STR_PG, STR_QG, STR_QT, STR_QB, STR_VS, STR_IREG};
    private static final String[] FIELD_NAMES_COMMON_2 = {STR_MBASE, STR_ZR, STR_ZX, STR_RT, STR_XT, STR_GTAP, STR_STAT, STR_RMPCT, STR_PT, STR_PB};
    private static final String[] FIELD_NAMES_COMMON_3 = {STR_O1, STR_F1, STR_O2, STR_F2, STR_O3, STR_F3, STR_O4, STR_F4, STR_WMOD, STR_WPF};
    private static final String[] FIELD_NAMES_32_33_START = {STR_I, STR_ID};
    private static final String[] FIELD_NAMES_32_33 = concatStringArrays(FIELD_NAMES_32_33_START, FIELD_NAMES_COMMON_1, FIELD_NAMES_COMMON_2, FIELD_NAMES_COMMON_3);
    private static final String[] FIELD_NAMES_35_START = {STR_IBUS, STR_MACHID};
    private static final String[] FIELD_NAMES_35_MIDDLE_1 = {STR_NREG};
    private static final String[] FIELD_NAMES_35_MIDDLE_2 = {STR_BASLOD};
    private static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_35_START, FIELD_NAMES_COMMON_1, FIELD_NAMES_35_MIDDLE_1, FIELD_NAMES_COMMON_2, FIELD_NAMES_35_MIDDLE_2, FIELD_NAMES_COMMON_3);

    private int i;
    private String id;
    private double pg = defaultDoubleFor(STR_PG, FIELDS);
    private double qg = defaultDoubleFor(STR_QG, FIELDS);
    private double qt = defaultDoubleFor(STR_QT, FIELDS);
    private double qb = defaultDoubleFor(STR_QB, FIELDS);
    private double vs = defaultDoubleFor(STR_VS, FIELDS);
    private int ireg = defaultIntegerFor(STR_IREG, FIELDS);
    private double mbase = defaultDoubleFor(STR_MBASE, FIELDS);
    private double zr = defaultDoubleFor(STR_ZR, FIELDS);
    private double zx = defaultDoubleFor(STR_ZX, FIELDS);
    private double rt = defaultDoubleFor(STR_RT, FIELDS);
    private double xt = defaultDoubleFor(STR_XT, FIELDS);
    private double gtap = defaultDoubleFor(STR_GTAP, FIELDS);
    private int stat = defaultIntegerFor(STR_STAT, FIELDS);
    private double rmpct = defaultDoubleFor(STR_RMPCT, FIELDS);
    private double pt = defaultDoubleFor(STR_PT, FIELDS);
    private double pb = defaultDoubleFor(STR_PB, FIELDS);
    private PsseOwnership ownership;
    private int wmod = defaultIntegerFor(STR_WMOD, FIELDS);
    private double wpf = defaultDoubleFor(STR_WPF, FIELDS);

    @Revision(since = 35)
    private int nreg = defaultIntegerFor(STR_NREG, FIELDS);

    @Revision(since = 35)
    private int baslod = defaultIntegerFor(STR_BASLOD, FIELDS);

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseGenerator fromRecord(CsvRecord rec, String[] headers) {
        PsseGenerator generator = Util.fromRecord(rec.getFields(), headers, FIELDS, PsseGenerator::new);
        generator.setOwnership(PsseOwnership.fromRecord(rec, headers));
        return generator;
    }

    public static String[] toRecord(PsseGenerator psseGenerator, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = Util.toRecord(psseGenerator, headers, FIELDS, unexpectedHeaders);
        PsseOwnership.toRecord(psseGenerator.getOwnership(), headers, recordValues, unexpectedHeaders);
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    private static Map<String, PsseFieldDefinition<PsseGenerator, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseGenerator, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseGenerator::getI, PsseGenerator::setI));
        addField(fields, createNewField(STR_IBUS, Integer.class, PsseGenerator::getI, PsseGenerator::setI));
        addField(fields, createNewField(STR_ID, String.class, PsseGenerator::getId, PsseGenerator::setId, "1"));
        addField(fields, createNewField(STR_MACHID, String.class, PsseGenerator::getId, PsseGenerator::setId, "1"));
        addField(fields, createNewField(STR_PG, Double.class, PsseGenerator::getPg, PsseGenerator::setPg, 0d));
        addField(fields, createNewField(STR_QG, Double.class, PsseGenerator::getQg, PsseGenerator::setQg, 0d));
        addField(fields, createNewField(STR_QT, Double.class, PsseGenerator::getQt, PsseGenerator::setQt, 9999d));
        addField(fields, createNewField(STR_QB, Double.class, PsseGenerator::getQb, PsseGenerator::setQb, -9999d));
        addField(fields, createNewField(STR_VS, Double.class, PsseGenerator::getVs, PsseGenerator::setVs, 1d));
        addField(fields, createNewField(STR_IREG, Integer.class, PsseGenerator::getIreg, PsseGenerator::setIreg, 0));
        addField(fields, createNewField(STR_MBASE, Double.class, PsseGenerator::getMbase, PsseGenerator::setMbase, Double.NaN));
        addField(fields, createNewField(STR_ZR, Double.class, PsseGenerator::getZr, PsseGenerator::setZr, 0d));
        addField(fields, createNewField(STR_ZX, Double.class, PsseGenerator::getZx, PsseGenerator::setZx, 1d));
        addField(fields, createNewField(STR_RT, Double.class, PsseGenerator::getRt, PsseGenerator::setRt, 0d));
        addField(fields, createNewField(STR_XT, Double.class, PsseGenerator::getXt, PsseGenerator::setXt, 0d));
        addField(fields, createNewField(STR_GTAP, Double.class, PsseGenerator::getGtap, PsseGenerator::setGtap, 1d));
        addField(fields, createNewField(STR_STAT, Integer.class, PsseGenerator::getStat, PsseGenerator::setStat, 1));
        addField(fields, createNewField(STR_RMPCT, Double.class, PsseGenerator::getRmpct, PsseGenerator::setRmpct, 100d));
        addField(fields, createNewField(STR_PT, Double.class, PsseGenerator::getPt, PsseGenerator::setPt, 9999d));
        addField(fields, createNewField(STR_PB, Double.class, PsseGenerator::getPb, PsseGenerator::setPb, -9999d));
        addField(fields, createNewField(STR_WMOD, Integer.class, PsseGenerator::getWmod, PsseGenerator::setWmod, 0));
        addField(fields, createNewField(STR_WPF, Double.class, PsseGenerator::getWpf, PsseGenerator::setWpf, 1d));
        addField(fields, createNewField(STR_NREG, Integer.class, PsseGenerator::getNreg, PsseGenerator::setNreg, 0));
        addField(fields, createNewField(STR_BASLOD, Integer.class, PsseGenerator::getBaslod, PsseGenerator::setBaslod, 0));

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
        checkVersion(STR_BASLOD);
        return baslod;
    }

    public void setBaslod(int baslod) {
        checkVersion(STR_BASLOD);
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
