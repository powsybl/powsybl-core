/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import com.powsybl.psse.model.pf.internal.TransformerImpedances;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.addPrefixToHeaders;
import static com.powsybl.psse.model.io.Util.addSuffixToHeaders;
import static com.powsybl.psse.model.io.Util.checkForUnexpectedHeader;
import static com.powsybl.psse.model.io.Util.concatStringArrays;
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
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(value = { "impedances" })
public class PsseTransformer extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseTransformer, ?>> FIELDS = createFields();
    static final String[] FIELD_NAMES_32_33_START = {STR_I, STR_J, STR_K};
    static final String[] FIELD_NAMES_35_START = {STR_IBUS, STR_JBUS, STR_KBUS};
    static final String[] FIELD_NAMES_COMMON_1 = {STR_CKT, STR_CW, STR_CZ, STR_CM, STR_MAG1, STR_MAG2};
    static final String[] FIELD_NAMES_32_33_MIDDLE = {STR_NMETR};
    static final String[] FIELD_NAMES_35_MIDDLE = {STR_NMET};
    static final String[] FIELD_NAMES_COMMON_2 = {STR_NAME, STR_STAT, STR_O1, STR_F1, STR_O2, STR_F2, STR_O3, STR_F3, STR_O4, STR_F4};
    static final String[] FIELD_NAMES_33_35_END = {STR_VECGRP};
    static final String[] FIELD_NAMES_35_END = {STR_ZCOD};
    static final String[] FIELD_NAMES_32 = concatStringArrays(FIELD_NAMES_32_33_START, FIELD_NAMES_COMMON_1, FIELD_NAMES_32_33_MIDDLE, FIELD_NAMES_COMMON_2);
    static final String[] FIELD_NAMES_33 = concatStringArrays(FIELD_NAMES_32_33_START, FIELD_NAMES_COMMON_1, FIELD_NAMES_32_33_MIDDLE, FIELD_NAMES_COMMON_2, FIELD_NAMES_33_35_END);
    static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_35_START, FIELD_NAMES_COMMON_1, FIELD_NAMES_35_MIDDLE, FIELD_NAMES_COMMON_2, FIELD_NAMES_33_35_END, FIELD_NAMES_35_END);
    static final String[] FIELD_NAMES_35_RAWX = concatStringArrays(FIELD_NAMES_35,
        TransformerImpedances.getFieldNames35RawX(),
        computeWindingsAndRatesHeader("1"),
        computeWindingsAndRatesHeader("2"),
        computeWindingsAndRatesHeader("3"));

    private int i;
    private int j;
    private int k = defaultIntegerFor(STR_K, FIELDS);
    private String ckt;
    private int cw = defaultIntegerFor(STR_CW, FIELDS);
    private int cz = defaultIntegerFor(STR_CZ, FIELDS);
    private int cm = defaultIntegerFor(STR_CM, FIELDS);
    private double mag1 = defaultDoubleFor(STR_MAG1, FIELDS);
    private double mag2 = defaultDoubleFor(STR_MAG2, FIELDS);
    private int nmetr = defaultIntegerFor(STR_NMETR, FIELDS);
    private String name;
    private int stat = defaultIntegerFor(STR_STAT, FIELDS);
    private PsseOwnership ownership;

    @Revision(since = 33)
    private String vecgrp = defaultStringFor(STR_VECGRP, FIELDS);

    @Revision(since = 35)
    private int zcod = defaultIntegerFor(STR_ZCOD, FIELDS);

    private TransformerImpedances impedances = new TransformerImpedances();
    private PsseTransformerWinding winding1 = new PsseTransformerWinding();
    private PsseRates winding1Rates = new PsseRates();
    private PsseTransformerWinding winding2 = new PsseTransformerWinding();
    private PsseRates winding2Rates = new PsseRates();
    private PsseTransformerWinding winding3 = new PsseTransformerWinding();
    private PsseRates winding3Rates = new PsseRates();

    public static String[] getFieldNames32() {
        return FIELD_NAMES_32;
    }

    public static String[] getFieldNames33() {
        return FIELD_NAMES_33;
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

    public static PsseTransformer fromRecord(CsvRecord rec, String[] headers) {
        PsseTransformer psseTransformer = Util.fromRecord(rec.getFields(), headers, FIELDS, PsseTransformer::new);
        psseTransformer.setOwnership(PsseOwnership.fromRecord(rec, headers));
        psseTransformer.setImpedances(TransformerImpedances.fromRecord(rec, headers));
        psseTransformer.setWinding1(PsseTransformerWinding.fromRecord(rec, headers, "1"), PsseRates.fromRecord(rec, headers, "1"));
        psseTransformer.setWinding2(PsseTransformerWinding.fromRecord(rec, headers, "2"), PsseRates.fromRecord(rec, headers, "2"));
        psseTransformer.setWinding3(PsseTransformerWinding.fromRecord(rec, headers, "3"), PsseRates.fromRecord(rec, headers, "3"));
        return psseTransformer;
    }

    public static String[] toRecord(PsseTransformer psseTransformer, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = Util.toRecord(psseTransformer, headers, FIELDS, unexpectedHeaders);
        PsseOwnership.toRecord(psseTransformer.getOwnership(), headers, recordValues, unexpectedHeaders);
        TransformerImpedances.toRecord(psseTransformer.getImpedances(), headers, recordValues, unexpectedHeaders);
        PsseTransformerWinding.toRecord(psseTransformer.getWinding1(), headers, recordValues, unexpectedHeaders, "1");
        PsseRates.toRecord(psseTransformer.getWinding1Rates(), headers, recordValues, unexpectedHeaders, "1");
        PsseTransformerWinding.toRecord(psseTransformer.getWinding2(), headers, recordValues, unexpectedHeaders, "2");
        PsseRates.toRecord(psseTransformer.getWinding2Rates(), headers, recordValues, unexpectedHeaders, "2");
        PsseTransformerWinding.toRecord(psseTransformer.getWinding3(), headers, recordValues, unexpectedHeaders, "3");
        PsseRates.toRecord(psseTransformer.getWinding3Rates(), headers, recordValues, unexpectedHeaders, "3");
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    private static Map<String, PsseFieldDefinition<PsseTransformer, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseTransformer, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseTransformer::getI, PsseTransformer::setI));
        addField(fields, createNewField(STR_IBUS, Integer.class, PsseTransformer::getI, PsseTransformer::setI));
        addField(fields, createNewField(STR_J, Integer.class, PsseTransformer::getJ, PsseTransformer::setJ));
        addField(fields, createNewField(STR_JBUS, Integer.class, PsseTransformer::getJ, PsseTransformer::setJ));
        addField(fields, createNewField(STR_K, Integer.class, PsseTransformer::getK, PsseTransformer::setK, 0));
        addField(fields, createNewField(STR_KBUS, Integer.class, PsseTransformer::getK, PsseTransformer::setK, 0));
        addField(fields, createNewField(STR_CKT, String.class, PsseTransformer::getCkt, PsseTransformer::setCkt, "1"));
        addField(fields, createNewField(STR_CW, Integer.class, PsseTransformer::getCw, PsseTransformer::setCw, 1));
        addField(fields, createNewField(STR_CZ, Integer.class, PsseTransformer::getCz, PsseTransformer::setCz, 1));
        addField(fields, createNewField(STR_CM, Integer.class, PsseTransformer::getCm, PsseTransformer::setCm, 1));
        addField(fields, createNewField(STR_MAG1, Double.class, PsseTransformer::getMag1, PsseTransformer::setMag1, 0d));
        addField(fields, createNewField(STR_MAG2, Double.class, PsseTransformer::getMag2, PsseTransformer::setMag2, 0d));
        addField(fields, createNewField(STR_NMET, Integer.class, PsseTransformer::getNmetr, PsseTransformer::setNmetr, 2));
        addField(fields, createNewField(STR_NMETR, Integer.class, PsseTransformer::getNmetr, PsseTransformer::setNmetr, 2));
        addField(fields, createNewField(STR_NAME, String.class, PsseTransformer::getName, PsseTransformer::setName, STR_SPACES_12));
        addField(fields, createNewField(STR_STAT, Integer.class, PsseTransformer::getStat, PsseTransformer::setStat, 1));
        addField(fields, createNewField(STR_VECGRP, String.class, PsseTransformer::getVecgrp, PsseTransformer::setVecgrp, STR_SPACES_12));
        addField(fields, createNewField(STR_ZCOD, Integer.class, PsseTransformer::getZcod, PsseTransformer::setZcod, 0));

        return fields;
    }

    private static String[] computeWindingsAndRatesHeader(String identifier) {
        return concatStringArrays(addSuffixToHeaders(PsseTransformerWinding.getFieldNamesPart1(), identifier),
            addPrefixToHeaders(PsseRates.getFieldNames35(), STR_WDG + identifier),
            addSuffixToHeaders(PsseTransformerWinding.getFieldNamesPart2(), identifier));
    }

    @Override
    public void setModel(PssePowerFlowModel model) {
        super.setModel(model);
        ownership.setModel(model);
        winding1.setModel(model);
        winding2.setModel(model);
        winding3.setModel(model);
        winding1Rates.setModel(model);
        winding2Rates.setModel(model);
        winding3Rates.setModel(model);
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

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public String getCkt() {
        return ckt;
    }

    public void setCkt(String ckt) {
        this.ckt = ckt;
    }

    public int getCw() {
        return cw;
    }

    public void setCw(int cw) {
        this.cw = cw;
    }

    public int getCz() {
        return cz;
    }

    public void setCz(int cz) {
        this.cz = cz;
    }

    public int getCm() {
        return cm;
    }

    public void setCm(int cm) {
        this.cm = cm;
    }

    public double getMag1() {
        return mag1;
    }

    public void setMag1(double mag1) {
        this.mag1 = mag1;
    }

    public double getMag2() {
        return mag2;
    }

    public void setMag2(double mag2) {
        this.mag2 = mag2;
    }

    public int getNmetr() {
        return nmetr;
    }

    public void setNmetr(int nmetr) {
        this.nmetr = nmetr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public String getVecgrp() {
        checkVersion(STR_VECGRP);
        return vecgrp;
    }

    public void setVecgrp(String vecgrp) {
        checkVersion(STR_VECGRP);
        this.vecgrp = vecgrp;
    }

    public int getZcod() {
        checkVersion("zcod");
        return zcod;
    }

    public void setZcod(int zcod) {
        this.zcod = zcod;
    }

    public double getR12() {
        return impedances.getR12();
    }

    public void setR12(double r12) {
        this.impedances.setR12(r12);
    }

    public double getX12() {
        return impedances.getX12();
    }

    public void setX12(double x12) {
        this.impedances.setX12(x12);
    }

    public double getSbase12() {
        return impedances.getSbase12();
    }

    public void setSbase12(double sbase12) {
        this.impedances.setSbase12(sbase12);
    }

    public double getR23() {
        return impedances.getR23();
    }

    public void setR23(double r23) {
        this.impedances.setR23(r23);
    }

    public double getX23() {
        return impedances.getX23();
    }

    public void setX23(double x23) {
        this.impedances.setX23(x23);
    }

    public double getSbase23() {
        return impedances.getSbase23();
    }

    public void setSbase23(double sbase23) {
        this.impedances.setSbase23(sbase23);
    }

    public double getR31() {
        return impedances.getR31();
    }

    public void setR31(double r31) {
        this.impedances.setR31(r31);
    }

    public double getX31() {
        return impedances.getX31();
    }

    public void setX31(double x31) {
        this.impedances.setX31(x31);
    }

    public double getSbase31() {
        return impedances.getSbase31();
    }

    public void setSbase31(double sbase31) {
        this.impedances.setSbase31(sbase31);
    }

    public double getVmstar() {
        return impedances.getVmstar();
    }

    public void setVmstar(double vmstar) {
        this.impedances.setVmstar(vmstar);
    }

    public double getAnstar() {
        return impedances.getAnstar();
    }

    public void setAnstar(double anstar) {
        this.impedances.setAnstar(anstar);
    }

    public PsseTransformerWinding getWinding1() {
        return winding1;
    }

    public PsseTransformerWinding getWinding2() {
        return winding2;
    }

    public PsseTransformerWinding getWinding3() {
        return winding3;
    }

    public PsseRates getWinding1Rates() {
        return winding1Rates;
    }

    public PsseRates getWinding2Rates() {
        return winding2Rates;
    }

    public PsseRates getWinding3Rates() {
        return winding3Rates;
    }

    public PsseOwnership getOwnership() {
        return ownership;
    }

    public void setWinding1(PsseTransformerWinding winding1, PsseRates winding1Rates) {
        this.winding1 = winding1;
        this.winding1Rates = winding1Rates;
    }

    public void setWinding2(PsseTransformerWinding winding2, PsseRates winding2Rates) {
        this.winding2 = winding2;
        this.winding2Rates = winding2Rates;
    }

    public void setWinding3(PsseTransformerWinding winding3, PsseRates winding3Rates) {
        this.winding3 = winding3;
        this.winding3Rates = winding3Rates;
    }

    public void setOwnership(PsseOwnership ownership) {
        this.ownership = ownership;
    }

    public TransformerImpedances getImpedances() {
        return impedances;
    }

    public void setImpedances(TransformerImpedances impedances) {
        this.impedances = impedances;
    }

    public PsseTransformer copy() {
        PsseTransformer copy = new PsseTransformer();
        copy.i = this.i;
        copy.j = this.j;
        copy.k = this.k;
        copy.ckt = this.ckt;
        copy.cw = this.cw;
        copy.cz = this.cz;
        copy.cm = this.cm;
        copy.mag1 = this.mag1;
        copy.mag2 = this.mag2;
        copy.nmetr = this.nmetr;
        copy.name = this.name;
        copy.stat = this.stat;
        copy.ownership = this.ownership.copy();
        copy.vecgrp = this.vecgrp;
        copy.zcod = this.zcod;
        copy.impedances = this.impedances.copy();
        copy.winding1 = this.winding1.copy();
        copy.winding1Rates = this.winding1Rates.copy();
        copy.winding2 = this.winding2.copy();
        copy.winding2Rates = this.winding2Rates.copy();
        copy.winding3 = this.winding3.copy();
        copy.winding3Rates = this.winding3Rates.copy();
        return copy;
    }
}
