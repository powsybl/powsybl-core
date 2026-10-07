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
import java.util.Objects;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.checkForUnexpectedHeader;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.defaultStringFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseNonTransformerBranch extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseNonTransformerBranch, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_32_33 = {STR_I, STR_J, STR_CKT, STR_R, STR_X, STR_B, STR_RATEA, STR_RATEB,
        STR_RATEC, STR_GI, STR_BI, STR_GJ, STR_BJ, STR_STAT, STR_MET, STR_LEN, STR_O1, STR_F1, STR_O2, STR_F2, STR_O3,
        STR_F3, STR_O4, STR_F4};
    private static final String[] FIELD_NAMES_35 = {STR_I, STR_J, STR_CKT, STR_R, STR_X, STR_B, STR_NAME, STR_RATE1,
        STR_RATE2, STR_RATE3, STR_RATE4, STR_RATE5, STR_RATE6, STR_RATE7, STR_RATE8, STR_RATE9, STR_RATE10, STR_RATE11,
        STR_RATE12, STR_GI, STR_BI, STR_GJ, STR_BJ, STR_STAT, STR_MET, STR_LEN, STR_O1, STR_F1, STR_O2, STR_F2, STR_O3,
        STR_F3, STR_O4, STR_F4};
    private static final String[] FIELD_NAMES_35X = {STR_IBUS, STR_JBUS, STR_CKT, STR_RPU, STR_XPU, STR_BPU, STR_NAME,
        STR_RATE1, STR_RATE2, STR_RATE3, STR_RATE4, STR_RATE5, STR_RATE6, STR_RATE7, STR_RATE8, STR_RATE9, STR_RATE10,
        STR_RATE11, STR_RATE12, STR_GI, STR_BI, STR_GJ, STR_BJ, STR_STAT, STR_MET, STR_LEN, STR_O1, STR_F1, STR_O2,
        STR_F2, STR_O3, STR_F3, STR_O4, STR_F4};

    private int i;
    private int j;
    private String ckt;
    private double r = defaultDoubleFor(STR_R, FIELDS);
    private double x;
    private double b = defaultDoubleFor(STR_B, FIELDS);
    private PsseRates rates;
    private double gi = defaultDoubleFor(STR_GI, FIELDS);
    private double bi = defaultDoubleFor(STR_BI, FIELDS);
    private double gj = defaultDoubleFor(STR_GJ, FIELDS);
    private double bj = defaultDoubleFor(STR_BJ, FIELDS);
    private int st = defaultIntegerFor(STR_ST, FIELDS);
    private int met = defaultIntegerFor(STR_MET, FIELDS);
    private double len = defaultDoubleFor(STR_LEN, FIELDS);
    private PsseOwnership ownership;

    @Revision(since = 35)
    private String name = defaultStringFor(STR_NAME, FIELDS);

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNamesX() {
        return FIELD_NAMES_35X;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseNonTransformerBranch fromRecord(CsvRecord rec, String[] headers) {
        PsseNonTransformerBranch nonTransformerBranch = Util.fromRecord(rec.getFields(), headers, FIELDS, PsseNonTransformerBranch::new);
        nonTransformerBranch.setRates(PsseRates.fromRecord(rec, headers));
        nonTransformerBranch.setOwnership(PsseOwnership.fromRecord(rec, headers));
        return nonTransformerBranch;
    }

    public static String[] toRecord(PsseNonTransformerBranch nonTransformerBranch, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = Util.toRecord(nonTransformerBranch, headers, FIELDS, unexpectedHeaders);
        PsseRates.toRecord(nonTransformerBranch.getRates(), headers, recordValues, unexpectedHeaders);
        PsseOwnership.toRecord(nonTransformerBranch.getOwnership(), headers, recordValues, unexpectedHeaders);
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    private static Map<String, PsseFieldDefinition<PsseNonTransformerBranch, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseNonTransformerBranch, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseNonTransformerBranch::getI, PsseNonTransformerBranch::setI));
        addField(fields, createNewField(STR_IBUS, Integer.class, PsseNonTransformerBranch::getI, PsseNonTransformerBranch::setI));
        addField(fields, createNewField(STR_J, Integer.class, PsseNonTransformerBranch::getJ, PsseNonTransformerBranch::setJ));
        addField(fields, createNewField(STR_JBUS, Integer.class, PsseNonTransformerBranch::getJ, PsseNonTransformerBranch::setJ));
        addField(fields, createNewField(STR_CKT, String.class, PsseNonTransformerBranch::getCkt, PsseNonTransformerBranch::setCkt));
        addField(fields, createNewField(STR_R, Double.class, PsseNonTransformerBranch::getR, PsseNonTransformerBranch::setR, 0.0));
        addField(fields, createNewField(STR_RPU, Double.class, PsseNonTransformerBranch::getR, PsseNonTransformerBranch::setR, 0.0));
        addField(fields, createNewField(STR_X, Double.class, PsseNonTransformerBranch::getX, PsseNonTransformerBranch::setX));
        addField(fields, createNewField(STR_XPU, Double.class, PsseNonTransformerBranch::getX, PsseNonTransformerBranch::setX));
        addField(fields, createNewField(STR_B, Double.class, PsseNonTransformerBranch::getB, PsseNonTransformerBranch::setB, 0.0));
        addField(fields, createNewField(STR_BPU, Double.class, PsseNonTransformerBranch::getB, PsseNonTransformerBranch::setB, 0.0));
        addField(fields, createNewField(STR_GI, Double.class, PsseNonTransformerBranch::getGi, PsseNonTransformerBranch::setGi, 0.0));
        addField(fields, createNewField(STR_BI, Double.class, PsseNonTransformerBranch::getBi, PsseNonTransformerBranch::setBi, 0.0));
        addField(fields, createNewField(STR_GJ, Double.class, PsseNonTransformerBranch::getGj, PsseNonTransformerBranch::setGj, 0.0));
        addField(fields, createNewField(STR_BJ, Double.class, PsseNonTransformerBranch::getBj, PsseNonTransformerBranch::setBj, 0.0));
        addField(fields, createNewField(STR_ST, Integer.class, PsseNonTransformerBranch::getSt, PsseNonTransformerBranch::setSt, 1));
        addField(fields, createNewField(STR_STAT, Integer.class, PsseNonTransformerBranch::getSt, PsseNonTransformerBranch::setSt, 1));
        addField(fields, createNewField(STR_MET, Integer.class, PsseNonTransformerBranch::getMet, PsseNonTransformerBranch::setMet, 1));
        addField(fields, createNewField(STR_LEN, Double.class, PsseNonTransformerBranch::getLen, PsseNonTransformerBranch::setLen, 0.0));
        addField(fields, createNewField(STR_NAME, String.class, PsseNonTransformerBranch::getName, PsseNonTransformerBranch::setName, " "));

        return fields;
    }

    @Override
    public void setModel(PssePowerFlowModel model) {
        super.setModel(model);
        ownership.setModel(model);
        rates.setModel(model);
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

    public String getCkt() {
        return ckt;
    }

    public void setCkt(String ckt) {
        this.ckt = ckt;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getGi() {
        return gi;
    }

    public void setGi(double gi) {
        this.gi = gi;
    }

    public double getBi() {
        return bi;
    }

    public void setBi(double bi) {
        this.bi = bi;
    }

    public double getGj() {
        return gj;
    }

    public void setGj(double gj) {
        this.gj = gj;
    }

    public double getBj() {
        return bj;
    }

    public void setBj(double bj) {
        this.bj = bj;
    }

    public int getSt() {
        return st;
    }

    public void setSt(int st) {
        this.st = st;
    }

    public int getMet() {
        return met;
    }

    public void setMet(int met) {
        this.met = met;
    }

    public double getLen() {
        return len;
    }

    public void setLen(double len) {
        this.len = len;
    }

    public String getName() {
        checkVersion(STR_NAME);
        return name;
    }

    public void setName(String name) {
        checkVersion(STR_NAME);
        this.name = Objects.requireNonNull(name);
    }

    public PsseOwnership getOwnership() {
        return ownership;
    }

    public void setOwnership(PsseOwnership ownership) {
        this.ownership = ownership;
    }

    public PsseRates getRates() {
        return rates;
    }

    public void setRates(PsseRates rates) {
        this.rates = rates;
    }

    public PsseNonTransformerBranch copy() {
        PsseNonTransformerBranch copy = new PsseNonTransformerBranch();
        copy.i = this.i;
        copy.j = this.j;
        copy.ckt = this.ckt;
        copy.r = this.r;
        copy.x = this.x;
        copy.b = this.b;
        copy.rates = this.rates.copy();
        copy.gi = this.gi;
        copy.bi = this.bi;
        copy.gj = this.gj;
        copy.bj = this.bj;
        copy.st = this.st;
        copy.met = this.met;
        copy.len = this.len;
        copy.ownership = this.ownership.copy();
        copy.name = this.name;
        return copy;
    }
}
