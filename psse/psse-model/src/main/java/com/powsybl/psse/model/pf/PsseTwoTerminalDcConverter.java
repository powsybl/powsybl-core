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
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.Optional;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseTwoTerminalDcConverter extends PsseVersioned {

    private int ip;
    private int nb;
    private double anmx;
    private double anmn;
    private double rc;
    private double xc;
    private double ebas;
    private double tr = 1.0;
    private double tap = 1.0;
    private double tmx = 1.5;
    private double tmn = 0.51;
    private double stp = 0.00625;
    private int ic = 0;

    // Originally the field name is "if", but that is not allowed
    private int ifx = 0;
    private int it = 0;
    private String id;
    private double xcap = 0.0;

    @Revision(since = 35)
    private int nd = 0;

    public static PsseTwoTerminalDcConverter fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        return fromRecord(rec, version, headers, "");
    }

    public static PsseTwoTerminalDcConverter fromRecord(CsvRecord rec, PsseVersion version, String[] headers, String headerSuffix) {
        PsseTwoTerminalDcConverter psseTwoTerminalDcConverter = new PsseTwoTerminalDcConverter();
        psseTwoTerminalDcConverter.setIp(parseIntFromRecord(rec, headers, "ip" + headerSuffix));
        psseTwoTerminalDcConverter.setNb(parseIntFromRecord(rec, headers, "nb" + headerSuffix));
        psseTwoTerminalDcConverter.setAnmx(parseDoubleFromRecord(rec, headers, "anmx" + headerSuffix));
        psseTwoTerminalDcConverter.setAnmn(parseDoubleFromRecord(rec, headers, "anmn" + headerSuffix));
        psseTwoTerminalDcConverter.setRc(parseDoubleFromRecord(rec, headers, "rc" + headerSuffix));
        psseTwoTerminalDcConverter.setXc(parseDoubleFromRecord(rec, headers, "xc" + headerSuffix));
        psseTwoTerminalDcConverter.setEbas(parseDoubleFromRecord(rec, headers, "ebas" + headerSuffix));
        psseTwoTerminalDcConverter.setTr(parseDoubleFromRecord(rec, 1.0, headers, "tr" + headerSuffix));
        psseTwoTerminalDcConverter.setTap(parseDoubleFromRecord(rec, 1.0, headers, "tap" + headerSuffix));
        psseTwoTerminalDcConverter.setTmx(parseDoubleFromRecord(rec, 1.5, headers, "tmx" + headerSuffix));
        psseTwoTerminalDcConverter.setTmn(parseDoubleFromRecord(rec, 0.51, headers, "tmn" + headerSuffix));
        psseTwoTerminalDcConverter.setStp(parseDoubleFromRecord(rec, 0.00625, headers, "stp" + headerSuffix));
        psseTwoTerminalDcConverter.setIc(parseIntFromRecord(rec, 0, headers, "ic" + headerSuffix));
        psseTwoTerminalDcConverter.setIf(parseIntFromRecord(rec, 0, headers, "if" + headerSuffix));
        psseTwoTerminalDcConverter.setIt(parseIntFromRecord(rec, 0, headers, "it" + headerSuffix));
        psseTwoTerminalDcConverter.setId(parseStringFromRecord(rec, "1", headers, "id" + headerSuffix));
        psseTwoTerminalDcConverter.setXcap(parseDoubleFromRecord(rec, 0.0, headers, "xcap" + headerSuffix));
        if (version.getMajorNumber() >= 35) {
            psseTwoTerminalDcConverter.setNd(parseIntFromRecord(rec, 0, headers, "nd" + headerSuffix));
        }
        return psseTwoTerminalDcConverter;
    }

    public static String[] toRecord(PsseTwoTerminalDcConverter psseTwoTerminalDcConverter, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            Optional<String> optionalValue = psseTwoTerminalDcConverter.headerToString(headers[i]);
            if (optionalValue.isEmpty()) {
                throw new PsseException("Unsupported header: " + headers[i]);
            }
            row[i] = optionalValue.get();
        }
        return row;
    }

    public Optional<String> headerToString(String header) {
        return switch (header) {
            case "ip" -> Optional.of(String.valueOf(getIp()));
            case "nb" -> Optional.of(String.valueOf(getNb()));
            case "anmx" -> Optional.of(String.valueOf(getAnmx()));
            case "anmn" -> Optional.of(String.valueOf(getAnmn()));
            case "rc" -> Optional.of(String.valueOf(getRc()));
            case "xc" -> Optional.of(String.valueOf(getXc()));
            case "ebas" -> Optional.of(String.valueOf(getEbas()));
            case "tr" -> Optional.of(String.valueOf(getTr()));
            case "tap" -> Optional.of(String.valueOf(getTap()));
            case "tmx" -> Optional.of(String.valueOf(getTmx()));
            case "tmn" -> Optional.of(String.valueOf(getTmn()));
            case "stp" -> Optional.of(String.valueOf(getStp()));
            case "ic" -> Optional.of(String.valueOf(getIc()));
            case "if" -> Optional.of(String.valueOf(getIf()));
            case "it" -> Optional.of(String.valueOf(getIt()));
            case "id" -> Optional.of(String.valueOf(getId()));
            case "xcap" -> Optional.of(String.valueOf(getXcap()));
            case "nd" -> Optional.of(String.valueOf(getNd()));
            default -> Optional.empty();
        };
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
