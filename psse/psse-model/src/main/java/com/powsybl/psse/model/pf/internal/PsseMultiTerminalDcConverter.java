/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.internal;

import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseMultiTerminalDcConverter {

    private static final Map<String, PsseFieldDefinition<PsseMultiTerminalDcConverter, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES = {STR_IB, STR_N, STR_ANGMX, STR_ANGMN, STR_RC, STR_XC, STR_EBAS, STR_TR, STR_TAP, STR_TPMX, STR_TPMN, STR_TSTP, STR_SETVL, STR_DCPF, STR_MARG, STR_CNVCOD};

    private int ib;
    private int n;
    private double angmx;
    private double angmn;
    private double rc;
    private double xc;
    private double ebas;
    private double tr = defaultDoubleFor(STR_TR, FIELDS);
    private double tap = defaultDoubleFor(STR_TAP, FIELDS);
    private double tpmx = defaultDoubleFor(STR_TPMX, FIELDS);
    private double tpmn = defaultDoubleFor(STR_TPMN, FIELDS);
    private double tstp = defaultDoubleFor(STR_TSTP, FIELDS);
    private double setvl;
    private double dcpf = defaultDoubleFor(STR_DCPF, FIELDS);
    private double marg = defaultDoubleFor(STR_MARG, FIELDS);
    private int cnvcod = defaultIntegerFor(STR_CNVCOD, FIELDS);

    public static String[] getFieldNames() {
        return FIELD_NAMES;
    }

    public static PsseMultiTerminalDcConverter fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseMultiTerminalDcConverter::new);
    }

    public static void toRecord(PsseMultiTerminalDcConverter multiTerminalDcConverter, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        Util.toRecord(multiTerminalDcConverter, headers, FIELDS, row, unexpectedHeaders);
    }

    public static String[] toRecord(PsseMultiTerminalDcConverter multiTerminalDcConverter, String[] headers) {
        return Util.toRecord(multiTerminalDcConverter, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseMultiTerminalDcConverter, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseMultiTerminalDcConverter, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_IB, Integer.class, PsseMultiTerminalDcConverter::getIb, PsseMultiTerminalDcConverter::setIb));
        addField(fields, createNewField(STR_N, Integer.class, PsseMultiTerminalDcConverter::getN, PsseMultiTerminalDcConverter::setN));
        addField(fields, createNewField(STR_ANGMX, Double.class, PsseMultiTerminalDcConverter::getAngmx, PsseMultiTerminalDcConverter::setAngmx));
        addField(fields, createNewField(STR_ANGMN, Double.class, PsseMultiTerminalDcConverter::getAngmn, PsseMultiTerminalDcConverter::setAngmn));
        addField(fields, createNewField(STR_RC, Double.class, PsseMultiTerminalDcConverter::getRc, PsseMultiTerminalDcConverter::setRc));
        addField(fields, createNewField(STR_XC, Double.class, PsseMultiTerminalDcConverter::getXc, PsseMultiTerminalDcConverter::setXc));
        addField(fields, createNewField(STR_EBAS, Double.class, PsseMultiTerminalDcConverter::getEbas, PsseMultiTerminalDcConverter::setEbas));
        addField(fields, createNewField(STR_TR, Double.class, PsseMultiTerminalDcConverter::getTr, PsseMultiTerminalDcConverter::setTr, 1.0));
        addField(fields, createNewField(STR_TAP, Double.class, PsseMultiTerminalDcConverter::getTap, PsseMultiTerminalDcConverter::setTap, 1.0));
        addField(fields, createNewField(STR_TPMX, Double.class, PsseMultiTerminalDcConverter::getTpmx, PsseMultiTerminalDcConverter::setTpmx, 1.5));
        addField(fields, createNewField(STR_TPMN, Double.class, PsseMultiTerminalDcConverter::getTpmn, PsseMultiTerminalDcConverter::setTpmn, 0.51));
        addField(fields, createNewField(STR_TSTP, Double.class, PsseMultiTerminalDcConverter::getTstp, PsseMultiTerminalDcConverter::setTstp, 0.00625));
        addField(fields, createNewField(STR_SETVL, Double.class, PsseMultiTerminalDcConverter::getSetvl, PsseMultiTerminalDcConverter::setSetvl));
        addField(fields, createNewField(STR_DCPF, Double.class, PsseMultiTerminalDcConverter::getDcpf, PsseMultiTerminalDcConverter::setDcpf, 1.0));
        addField(fields, createNewField(STR_MARG, Double.class, PsseMultiTerminalDcConverter::getMarg, PsseMultiTerminalDcConverter::setMarg, 0.0));
        addField(fields, createNewField(STR_CNVCOD, Integer.class, PsseMultiTerminalDcConverter::getCnvcod, PsseMultiTerminalDcConverter::setCnvcod, 1));

        return fields;
    }

    public int getIb() {
        return ib;
    }

    public void setIb(int ib) {
        this.ib = ib;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public double getAngmx() {
        return angmx;
    }

    public void setAngmx(double angmx) {
        this.angmx = angmx;
    }

    public double getAngmn() {
        return angmn;
    }

    public void setAngmn(double angmn) {
        this.angmn = angmn;
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

    public double getTpmx() {
        return tpmx;
    }

    public void setTpmx(double tpmx) {
        this.tpmx = tpmx;
    }

    public double getTpmn() {
        return tpmn;
    }

    public void setTpmn(double tpmn) {
        this.tpmn = tpmn;
    }

    public double getTstp() {
        return tstp;
    }

    public void setTstp(double tstp) {
        this.tstp = tstp;
    }

    public double getSetvl() {
        return setvl;
    }

    public void setSetvl(double setvl) {
        this.setvl = setvl;
    }

    public double getDcpf() {
        return dcpf;
    }

    public void setDcpf(double dcpf) {
        this.dcpf = dcpf;
    }

    public double getMarg() {
        return marg;
    }

    public void setMarg(double marg) {
        this.marg = marg;
    }

    public int getCnvcod() {
        return cnvcod;
    }

    public void setCnvcod(int cnvcod) {
        this.cnvcod = cnvcod;
    }
}
