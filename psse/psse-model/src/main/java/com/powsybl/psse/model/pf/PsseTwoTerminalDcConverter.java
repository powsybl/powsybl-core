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
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseTwoTerminalDcConverter extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseTwoTerminalDcConverter, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_COMMON_1 = {STR_IP, STR_NB, STR_ANMX, STR_ANMN, STR_RC, STR_XC, STR_EBAS, STR_TR, STR_TAP, STR_TMX, STR_TMN, STR_STP, STR_IC};
    private static final String[] FIELD_NAMES_COMMON_2 = {STR_IF, STR_IT, STR_ID, STR_XCAP};
    private static final String[] FIELD_NAMES_ND = {STR_ND};
    private static final String[] FIELD_NAMES_32_33 = concatStringArrays(FIELD_NAMES_COMMON_1, FIELD_NAMES_COMMON_2);
    private static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_COMMON_1, FIELD_NAMES_ND, FIELD_NAMES_COMMON_2);

    private int ip;
    private int nb;
    private double anmx;
    private double anmn;
    private double rc;
    private double xc;
    private double ebas;
    private double tr = defaultDoubleFor(STR_TR, FIELDS);
    private double tap = defaultDoubleFor(STR_TAP, FIELDS);
    private double tmx = defaultDoubleFor(STR_TMX, FIELDS);
    private double tmn = defaultDoubleFor(STR_TMN, FIELDS);
    private double stp = defaultDoubleFor(STR_STP, FIELDS);
    private int ic = defaultIntegerFor(STR_IC, FIELDS);

    // Originally the field name is "if", but that is not allowed
    private int ifx = defaultIntegerFor(STR_IF, FIELDS);
    private int it = defaultIntegerFor(STR_IT, FIELDS);
    private String id;
    private double xcap = defaultDoubleFor(STR_XCAP, FIELDS);

    @Revision(since = 35)
    private int nd = defaultIntegerFor(STR_ND, FIELDS);

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseTwoTerminalDcConverter fromRecord(CsvRecord rec, String[] headers) {
        return fromRecord(rec, headers, "");
    }

    public static PsseTwoTerminalDcConverter fromRecord(CsvRecord rec, String[] headers, String headerSuffix) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseTwoTerminalDcConverter::new, headerSuffix);
    }

    public static void toRecord(PsseTwoTerminalDcConverter psseTwoTerminalDcConverter, String[] headers, String[] row,
                                Set<String> unexpectedHeaders, String headerSuffix) {
        Util.toRecord(psseTwoTerminalDcConverter, headers, FIELDS, row, unexpectedHeaders, headerSuffix);
    }

    public static void toRecord(PsseTwoTerminalDcConverter psseTwoTerminalDcConverter, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        toRecord(psseTwoTerminalDcConverter, headers, row, unexpectedHeaders, "");
    }

    public static String[] toRecord(PsseTwoTerminalDcConverter psseTwoTerminalDcConverter, String[] headers) {
        return Util.toRecord(psseTwoTerminalDcConverter, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseTwoTerminalDcConverter, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseTwoTerminalDcConverter, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_IP, Integer.class, PsseTwoTerminalDcConverter::getIp, PsseTwoTerminalDcConverter::setIp));
        addField(fields, createNewField(STR_NB, Integer.class, PsseTwoTerminalDcConverter::getNb, PsseTwoTerminalDcConverter::setNb));
        addField(fields, createNewField(STR_ANMX, Double.class, PsseTwoTerminalDcConverter::getAnmx, PsseTwoTerminalDcConverter::setAnmx));
        addField(fields, createNewField(STR_ANMN, Double.class, PsseTwoTerminalDcConverter::getAnmn, PsseTwoTerminalDcConverter::setAnmn));
        addField(fields, createNewField(STR_RC, Double.class, PsseTwoTerminalDcConverter::getRc, PsseTwoTerminalDcConverter::setRc));
        addField(fields, createNewField(STR_XC, Double.class, PsseTwoTerminalDcConverter::getXc, PsseTwoTerminalDcConverter::setXc));
        addField(fields, createNewField(STR_EBAS, Double.class, PsseTwoTerminalDcConverter::getEbas, PsseTwoTerminalDcConverter::setEbas));
        addField(fields, createNewField(STR_TR, Double.class, PsseTwoTerminalDcConverter::getTr, PsseTwoTerminalDcConverter::setTr, 1d));
        addField(fields, createNewField(STR_TAP, Double.class, PsseTwoTerminalDcConverter::getTap, PsseTwoTerminalDcConverter::setTap, 1d));
        addField(fields, createNewField(STR_TMX, Double.class, PsseTwoTerminalDcConverter::getTmx, PsseTwoTerminalDcConverter::setTmx, 1.5));
        addField(fields, createNewField(STR_TMN, Double.class, PsseTwoTerminalDcConverter::getTmn, PsseTwoTerminalDcConverter::setTmn, 0.51));
        addField(fields, createNewField(STR_STP, Double.class, PsseTwoTerminalDcConverter::getStp, PsseTwoTerminalDcConverter::setStp, 0.00625));
        addField(fields, createNewField(STR_IC, Integer.class, PsseTwoTerminalDcConverter::getIc, PsseTwoTerminalDcConverter::setIc, 0));
        addField(fields, createNewField(STR_IF, Integer.class, PsseTwoTerminalDcConverter::getIf, PsseTwoTerminalDcConverter::setIf, 0));
        addField(fields, createNewField(STR_IT, Integer.class, PsseTwoTerminalDcConverter::getIt, PsseTwoTerminalDcConverter::setIt, 0));
        addField(fields, createNewField(STR_ID, String.class, PsseTwoTerminalDcConverter::getId, PsseTwoTerminalDcConverter::setId));
        addField(fields, createNewField(STR_XCAP, Double.class, PsseTwoTerminalDcConverter::getXcap, PsseTwoTerminalDcConverter::setXcap, 0d));
        addField(fields, createNewField(STR_ND, Integer.class, PsseTwoTerminalDcConverter::getNd, PsseTwoTerminalDcConverter::setNd, 0));

        return fields;
    }

    public int getIp() {
        return ip;
    }

    public void setIp(int ip) {
        this.ip = ip;
    }

    public int getNb() {
        return nb;
    }

    public void setNb(int nb) {
        this.nb = nb;
    }

    public double getAnmx() {
        return anmx;
    }

    public void setAnmx(double anmx) {
        this.anmx = anmx;
    }

    public double getAnmn() {
        return anmn;
    }

    public void setAnmn(double anmn) {
        this.anmn = anmn;
    }

    public double getRc() {
        return rc;
    }

    public void setRc(double rc) {
        this.rc = rc;
    }

    public double getXc() {
        return xc;
    }

    public void setXc(double xc) {
        this.xc = xc;
    }

    public double getEbas() {
        return ebas;
    }

    public void setEbas(double ebas) {
        this.ebas = ebas;
    }

    public double getTr() {
        return tr;
    }

    public void setTr(double tr) {
        this.tr = tr;
    }

    public double getTap() {
        return tap;
    }

    public void setTap(double tap) {
        this.tap = tap;
    }

    public double getTmx() {
        return tmx;
    }

    public void setTmx(double tmx) {
        this.tmx = tmx;
    }

    public double getTmn() {
        return tmn;
    }

    public void setTmn(double tmn) {
        this.tmn = tmn;
    }

    public double getStp() {
        return stp;
    }

    public void setStp(double stp) {
        this.stp = stp;
    }

    public int getIc() {
        return ic;
    }

    public void setIc(int ic) {
        this.ic = ic;
    }

    public int getIf() {
        return ifx;
    }

    public void setIf(int ifx) {
        this.ifx = ifx;
    }

    public int getIt() {
        return it;
    }

    public void setIt(int it) {
        this.it = it;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getXcap() {
        return xcap;
    }

    public void setXcap(double xcap) {
        this.xcap = xcap;
    }

    public int getNd() {
        checkVersion("nd");
        return nd;
    }

    public void setNd(int nd) {
        this.nd = nd;
    }

    public PsseTwoTerminalDcConverter copy() {
        PsseTwoTerminalDcConverter copy = new PsseTwoTerminalDcConverter();
        copy.ip = this.ip;
        copy.nb = this.nb;
        copy.anmx = this.anmx;
        copy.anmn = this.anmn;
        copy.rc = this.rc;
        copy.xc = this.xc;
        copy.ebas = this.ebas;
        copy.tr = this.tr;
        copy.tap = this.tap;
        copy.tmx = this.tmx;
        copy.tmn = this.tmn;
        copy.stp = this.stp;
        copy.ic = this.ic;
        copy.ifx = this.ifx;
        copy.it = this.it;
        copy.id = this.id;
        copy.xcap = this.xcap;
        copy.nd = this.nd;
        return copy;
    }
}
