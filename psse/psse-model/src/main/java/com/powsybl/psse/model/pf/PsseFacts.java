/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseFacts extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseFacts, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_COMMON = {STR_MODE, STR_PDES, STR_QDES, STR_VSET, STR_SHMX, STR_TRMX,
        STR_VTMN, STR_VTMX, STR_VSMX, STR_IMX, STR_LINX, STR_RMPCT, STR_OWNER, STR_SET1, STR_SET2, STR_VSREF};
    private static final String[] FIELD_NAMES_32_33_START = {STR_NAME, STR_I, STR_J};
    private static final String[] FIELD_NAMES_32_33_END = {STR_REMOT, STR_MNAME};
    private static final String[] FIELD_NAMES_32_33 = concatStringArrays(FIELD_NAMES_32_33_START, FIELD_NAMES_COMMON, FIELD_NAMES_32_33_END);
    private static final String[] FIELD_NAMES_35_START = {STR_NAME, STR_IBUS, STR_JBUS};
    private static final String[] FIELD_NAMES_35_END = {STR_FCREG, STR_NREG, STR_MNAME};
    private static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_35_START, FIELD_NAMES_COMMON, FIELD_NAMES_35_END);

    private String name;
    private int i;
    private int j = defaultIntegerFor(STR_J, FIELDS);
    private int mode = defaultIntegerFor(STR_MODE, FIELDS);
    private double pdes = defaultDoubleFor(STR_PDES, FIELDS);
    private double qdes = defaultDoubleFor(STR_QDES, FIELDS);
    private double vset = defaultDoubleFor(STR_VSET, FIELDS);
    private double shmx = defaultDoubleFor(STR_SHMX, FIELDS);
    private double trmx = defaultDoubleFor(STR_TRMX, FIELDS);
    private double vtmn = defaultDoubleFor(STR_VTMN, FIELDS);
    private double vtmx = defaultDoubleFor(STR_VTMX, FIELDS);
    private double vsmx = defaultDoubleFor(STR_VSMX, FIELDS);
    private double imx = defaultDoubleFor(STR_IMX, FIELDS);
    private double linx = defaultDoubleFor(STR_LINX, FIELDS);
    private double rmpct = defaultDoubleFor(STR_RMPCT, FIELDS);
    private int owner = defaultIntegerFor(STR_OWNER, FIELDS);
    private double set1 = defaultDoubleFor(STR_SET1, FIELDS);
    private double set2 = defaultDoubleFor(STR_SET2, FIELDS);
    private int vsref = defaultIntegerFor(STR_VSREF, FIELDS);

    @Revision(until = 33)
    private int remot = defaultIntegerFor(STR_REMOT, FIELDS);

    private String mname;

    @Revision(since = 35)
    private int fcreg = defaultIntegerFor(STR_FCREG, FIELDS);

    @Revision(since = 35)
    private int nreg = defaultIntegerFor(STR_NREG, FIELDS);

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseFacts fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseFacts::new);
    }

    public static String[] toRecord(PsseFacts psseFacts, String[] headers) {
        return Util.toRecord(psseFacts, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseFacts, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseFacts, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_NAME, String.class, PsseFacts::getName, PsseFacts::setName));
        addField(fields, createNewField(STR_I, Integer.class, PsseFacts::getI, PsseFacts::setI));
        addField(fields, createNewField(STR_IBUS, Integer.class, PsseFacts::getI, PsseFacts::setI));
        addField(fields, createNewField(STR_J, Integer.class, PsseFacts::getJ, PsseFacts::setJ, 0));
        addField(fields, createNewField(STR_JBUS, Integer.class, PsseFacts::getJ, PsseFacts::setJ, 0));
        addField(fields, createNewField(STR_MODE, Integer.class, PsseFacts::getMode, PsseFacts::setMode, 1));
        addField(fields, createNewField(STR_PDES, Double.class, PsseFacts::getPdes, PsseFacts::setPdes, 0.0));
        addField(fields, createNewField(STR_QDES, Double.class, PsseFacts::getQdes, PsseFacts::setQdes, 0.0));
        addField(fields, createNewField(STR_VSET, Double.class, PsseFacts::getVset, PsseFacts::setVset, 1.0));
        addField(fields, createNewField(STR_SHMX, Double.class, PsseFacts::getShmx, PsseFacts::setShmx, 9999.0));
        addField(fields, createNewField(STR_TRMX, Double.class, PsseFacts::getTrmx, PsseFacts::setTrmx, 9999.0));
        addField(fields, createNewField(STR_VTMN, Double.class, PsseFacts::getVtmn, PsseFacts::setVtmn, 0.9));
        addField(fields, createNewField(STR_VTMX, Double.class, PsseFacts::getVtmx, PsseFacts::setVtmx, 1.1));
        addField(fields, createNewField(STR_VSMX, Double.class, PsseFacts::getVsmx, PsseFacts::setVsmx, 1.0));
        addField(fields, createNewField(STR_IMX, Double.class, PsseFacts::getImx, PsseFacts::setImx, 0.0));
        addField(fields, createNewField(STR_LINX, Double.class, PsseFacts::getLinx, PsseFacts::setLinx, 0.05));
        addField(fields, createNewField(STR_RMPCT, Double.class, PsseFacts::getRmpct, PsseFacts::setRmpct, 100.0));
        addField(fields, createNewField(STR_OWNER, Integer.class, PsseFacts::getOwner, PsseFacts::setOwner, 1));
        addField(fields, createNewField(STR_SET1, Double.class, PsseFacts::getSet1, PsseFacts::setSet1, 0.0));
        addField(fields, createNewField(STR_SET2, Double.class, PsseFacts::getSet2, PsseFacts::setSet2, 0.0));
        addField(fields, createNewField(STR_VSREF, Integer.class, PsseFacts::getVsref, PsseFacts::setVsref, 0));
        addField(fields, createNewField(STR_REMOT, Integer.class, PsseFacts::getRemot, PsseFacts::setRemot, 0));
        addField(fields, createNewField(STR_MNAME, String.class, PsseFacts::getMname, PsseFacts::setMname));
        addField(fields, createNewField(STR_FCREG, Integer.class, PsseFacts::getFcreg, PsseFacts::setFcreg, 0));
        addField(fields, createNewField(STR_NREG, Integer.class, PsseFacts::getNreg, PsseFacts::setNreg, 0));

        return fields;
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
        checkVersion(STR_REMOT);
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
        checkVersion(STR_FCREG);
        return fcreg;
    }

    public void setFcreg(int fcreg) {
        this.fcreg = fcreg;
    }

    public int getNreg() {
        checkVersion(STR_NREG);
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
