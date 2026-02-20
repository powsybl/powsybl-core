/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.internal;

import com.powsybl.psse.model.PsseException;
import de.siegmar.fastcsv.reader.CsvRecord;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseMultiTerminalDcConverter {

    private int ib;
    private int n;
    private double angmx;
    private double angmn;
    private double rc;
    private double xc;
    private double ebas;
    private double tr = 1.0;
    private double tap = 1.0;
    private double tpmx = 1.5;
    private double tpmn = 0.51;
    private double tstp = 0.00625;
    private double setvl;
    private double dcpf = 1.0;
    private double marg = 0.0;
    private int cnvcod = 1;

    public static PsseMultiTerminalDcConverter fromRecord(CsvRecord rec, String[] headers) {
        PsseMultiTerminalDcConverter psseMultiTerminalDcConverter = new PsseMultiTerminalDcConverter();
        psseMultiTerminalDcConverter.setIb(parseIntFromRecord(rec, headers, "ib"));
        psseMultiTerminalDcConverter.setN(parseIntFromRecord(rec, headers, "n"));
        psseMultiTerminalDcConverter.setAngmx(parseDoubleFromRecord(rec, headers, "angmx"));
        psseMultiTerminalDcConverter.setAngmn(parseDoubleFromRecord(rec, headers, "angmn"));
        psseMultiTerminalDcConverter.setRc(parseDoubleFromRecord(rec, headers, "rc"));
        psseMultiTerminalDcConverter.setXc(parseDoubleFromRecord(rec, headers, "xc"));
        psseMultiTerminalDcConverter.setEbas(parseDoubleFromRecord(rec, headers, "ebas"));
        psseMultiTerminalDcConverter.setTr(parseDoubleFromRecord(rec, 1.0, headers, "tr"));
        psseMultiTerminalDcConverter.setTap(parseDoubleFromRecord(rec, 1.0, headers, "tap"));
        psseMultiTerminalDcConverter.setTpmx(parseDoubleFromRecord(rec, 1.5, headers, "tpmx"));
        psseMultiTerminalDcConverter.setTpmn(parseDoubleFromRecord(rec, 0.51, headers, "tpmn"));
        psseMultiTerminalDcConverter.setTstp(parseDoubleFromRecord(rec, 0.00625, headers, "tstp"));
        psseMultiTerminalDcConverter.setSetvl(parseDoubleFromRecord(rec, headers, "setvl"));
        psseMultiTerminalDcConverter.setDcpf(parseDoubleFromRecord(rec, 1.0, headers, "dcpf"));
        psseMultiTerminalDcConverter.setMarg(parseDoubleFromRecord(rec, 0.0, headers, "marg"));
        psseMultiTerminalDcConverter.setCnvcod(parseIntFromRecord(rec, 1, headers, "cnvcod"));
        return psseMultiTerminalDcConverter;
    }

    public static String[] toRecord(PsseMultiTerminalDcConverter psseMultiTerminalDcConverter, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "ib" -> String.valueOf(psseMultiTerminalDcConverter.getIb());
                case "n" -> String.valueOf(psseMultiTerminalDcConverter.getN());
                case "angmx" -> String.valueOf(psseMultiTerminalDcConverter.getAngmx());
                case "angmn" -> String.valueOf(psseMultiTerminalDcConverter.getAngmn());
                case "rc" -> String.valueOf(psseMultiTerminalDcConverter.getRc());
                case "xc" -> String.valueOf(psseMultiTerminalDcConverter.getXc());
                case "ebas" -> String.valueOf(psseMultiTerminalDcConverter.getEbas());
                case "tr" -> String.valueOf(psseMultiTerminalDcConverter.getTr());
                case "tap" -> String.valueOf(psseMultiTerminalDcConverter.getTap());
                case "tpmx" -> String.valueOf(psseMultiTerminalDcConverter.getTpmx());
                case "tpmn" -> String.valueOf(psseMultiTerminalDcConverter.getTpmn());
                case "tstp" -> String.valueOf(psseMultiTerminalDcConverter.getTstp());
                case "setvl" -> String.valueOf(psseMultiTerminalDcConverter.getSetvl());
                case "dcpf" -> String.valueOf(psseMultiTerminalDcConverter.getDcpf());
                case "marg" -> String.valueOf(psseMultiTerminalDcConverter.getMarg());
                case "cnvcod" -> String.valueOf(psseMultiTerminalDcConverter.getCnvcod());
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
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
