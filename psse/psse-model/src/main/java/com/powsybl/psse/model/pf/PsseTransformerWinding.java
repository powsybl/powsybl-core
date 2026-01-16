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
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.concatStringArrays;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseTransformerWinding extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseTransformerWinding, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_T2W = {STR_WINDV, STR_NOMV};
    private static final String[] FIELD_NAMES_PART_1 = {STR_WINDV, STR_NOMV, STR_ANG};
    private static final String[] FIELD_NAMES_PART_2 = {STR_COD, STR_CONT, STR_NODE, STR_RMA, STR_RMI, STR_VMA, STR_VMI, STR_NTP, STR_TAB, STR_CR, STR_CX, STR_CNXA};
    private static final String[] FIELD_NAMES = concatStringArrays(FIELD_NAMES_PART_1, FIELD_NAMES_PART_2);

    private double windv = defaultDoubleFor(STR_WINDV, FIELDS);
    private double nomv = defaultDoubleFor(STR_NOMV, FIELDS);
    private double ang = defaultDoubleFor(STR_ANG, FIELDS);
    private int cod = defaultIntegerFor(STR_COD, FIELDS);
    private int cont = defaultIntegerFor(STR_CONT, FIELDS);

    @Revision(since = 35)
    private int node = defaultIntegerFor(STR_NODE, FIELDS);

    private double rma = defaultDoubleFor(STR_RMA, FIELDS);
    private double rmi = defaultDoubleFor(STR_RMI, FIELDS);
    private double vma = defaultDoubleFor(STR_VMA, FIELDS);
    private double vmi = defaultDoubleFor(STR_VMI, FIELDS);
    private int ntp = defaultIntegerFor(STR_NTP, FIELDS);
    private int tab = defaultIntegerFor(STR_TAB, FIELDS);
    private double cr = defaultDoubleFor(STR_CR, FIELDS);
    private double cx = defaultDoubleFor(STR_CX, FIELDS);
    private double cnxa = defaultDoubleFor(STR_CNXA, FIELDS);

    public static String[] getFieldNamesT2W() {
        return FIELD_NAMES_T2W;
    }

    public static String[] getFieldNamesPart1() {
        return FIELD_NAMES_PART_1;
    }

    public static String[] getFieldNamesPart2() {
        return FIELD_NAMES_PART_2;
    }

    public static String[] getFieldNames() {
        return FIELD_NAMES;
    }

    public static PsseTransformerWinding fromRecord(CsvRecord rec, String[] headers) {
        return fromRecord(rec, headers, "");
    }

    public static PsseTransformerWinding fromRecord(CsvRecord rec, String[] headers, String headerSuffix) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseTransformerWinding::new, headerSuffix);
    }

    public static void toRecord(PsseTransformerWinding psseTransformerWinding, String[] headers, String[] row,
                                Set<String> unexpectedHeaders, String headerSuffix) {
        Util.toRecord(psseTransformerWinding, headers, FIELDS, row, unexpectedHeaders, headerSuffix);
    }

    public static void toRecord(PsseTransformerWinding psseTransformerWinding, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        toRecord(psseTransformerWinding, headers, row, unexpectedHeaders, "");
    }

    public static String[] toRecord(PsseTransformerWinding psseTransformerWinding, String[] headers) {
        return Util.toRecord(psseTransformerWinding, headers, FIELDS);
    }

    public static String[] toRecord(PsseTransformerWinding psseTransformerWinding, String[] headers, Set<String> unexpectedHeaders) {
        return Util.toRecord(psseTransformerWinding, headers, FIELDS, unexpectedHeaders);
    }

    private static Map<String, PsseFieldDefinition<PsseTransformerWinding, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseTransformerWinding, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_WINDV, Double.class, PsseTransformerWinding::getWindv, PsseTransformerWinding::setWindv, Double.NaN));
        addField(fields, createNewField(STR_NOMV, Double.class, PsseTransformerWinding::getNomv, PsseTransformerWinding::setNomv, 0d));
        addField(fields, createNewField(STR_ANG, Double.class, PsseTransformerWinding::getAng, PsseTransformerWinding::setAng, 0d));
        addField(fields, createNewField(STR_COD, Integer.class, PsseTransformerWinding::getCod, PsseTransformerWinding::setCod, 0));
        addField(fields, createNewField(STR_CONT, Integer.class, PsseTransformerWinding::getCont, PsseTransformerWinding::setCont, 0));
        addField(fields, createNewField(STR_NODE, Integer.class, PsseTransformerWinding::getNode, PsseTransformerWinding::setNode, 0));
        addField(fields, createNewField(STR_RMA, Double.class, PsseTransformerWinding::getRma, PsseTransformerWinding::setRma, Double.NaN));
        addField(fields, createNewField(STR_RMI, Double.class, PsseTransformerWinding::getRmi, PsseTransformerWinding::setRmi, Double.NaN));
        addField(fields, createNewField(STR_VMA, Double.class, PsseTransformerWinding::getVma, PsseTransformerWinding::setVma, Double.NaN));
        addField(fields, createNewField(STR_VMI, Double.class, PsseTransformerWinding::getVmi, PsseTransformerWinding::setVmi, Double.NaN));
        addField(fields, createNewField(STR_NTP, Integer.class, PsseTransformerWinding::getNtp, PsseTransformerWinding::setNtp, 33));
        addField(fields, createNewField(STR_TAB, Integer.class, PsseTransformerWinding::getTab, PsseTransformerWinding::setTab, 0));
        addField(fields, createNewField(STR_CR, Double.class, PsseTransformerWinding::getCr, PsseTransformerWinding::setCr, 0d));
        addField(fields, createNewField(STR_CX, Double.class, PsseTransformerWinding::getCx, PsseTransformerWinding::setCx, 0d));
        addField(fields, createNewField(STR_CNXA, Double.class, PsseTransformerWinding::getCnxa, PsseTransformerWinding::setCnxa, 0d));

        return fields;
    }

    public double getWindv() {
        return windv;
    }

    public double getNomv() {
        return nomv;
    }

    public double getAng() {
        return ang;
    }

    public int getCod() {
        return cod;
    }

    public int getCont() {
        return cont;
    }

    public int getNode() {
        checkVersion("node");
        return node;
    }

    public double getRma() {
        return rma;
    }

    public double getRmi() {
        return rmi;
    }

    public double getVma() {
        return vma;
    }

    public double getVmi() {
        return vmi;
    }

    public int getNtp() {
        return ntp;
    }

    public int getTab() {
        return tab;
    }

    public double getCr() {
        return cr;
    }

    public double getCx() {
        return cx;
    }

    public double getCnxa() {
        return cnxa;
    }

    public void setWindv(double windv) {
        this.windv = windv;
    }

    public void setNomv(double nomv) {
        this.nomv = nomv;
    }

    public void setAng(double ang) {
        this.ang = ang;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public void setCont(int cont) {
        this.cont = cont;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public void setRma(double rma) {
        this.rma = rma;
    }

    public void setRmi(double rmi) {
        this.rmi = rmi;
    }

    public void setVma(double vma) {
        this.vma = vma;
    }

    public void setVmi(double vmi) {
        this.vmi = vmi;
    }

    public void setNtp(int ntp) {
        this.ntp = ntp;
    }

    public void setTab(int tab) {
        this.tab = tab;
    }

    public void setCr(double cr) {
        this.cr = cr;
    }

    public void setCx(double cx) {
        this.cx = cx;
    }

    public void setCnxa(double cnxa) {
        this.cnxa = cnxa;
    }

    public PsseTransformerWinding copy() {
        PsseTransformerWinding copy = new PsseTransformerWinding();
        copy.windv = this.windv;
        copy.nomv = this.nomv;
        copy.ang = this.ang;
        copy.cod = this.cod;
        copy.cont = this.cont;
        copy.node = this.node;
        copy.rma = this.rma;
        copy.rmi = this.rmi;
        copy.vma = this.vma;
        copy.vmi = this.vmi;
        copy.ntp = this.ntp;
        copy.tab = this.tab;
        copy.cr = this.cr;
        copy.cx = this.cx;
        copy.cnxa = this.cnxa;
        return copy;
    }
}
