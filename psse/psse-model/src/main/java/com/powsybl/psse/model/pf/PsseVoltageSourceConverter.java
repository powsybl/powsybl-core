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

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseVoltageSourceConverter extends PsseVersioned {

    private int ibus;
    private int type;
    private int mode = 1;
    private double dcset;
    private double acset = 1.0;
    private double aloss = 0.0;
    private double bloss = 0.0;
    private double minloss = 0.0;
    private double smax = 0.0;
    private double imax = 0.0;
    private double pwf = 1.0;
    private double maxq = 9999.0;
    private double minq = -9999.0;

    @Revision(until = 33)
    private int remot = 0;

    private double rmpct = 100.0;

    @Revision(since = 35)
    private int vsreg = 0;

    @Revision(since = 35)
    private int nreg = 0;

    public static PsseVoltageSourceConverter fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        return fromRecord(rec, version, headers, "");
    }

    public static PsseVoltageSourceConverter fromRecord(CsvRecord rec, PsseVersion version, String[] headers, String headerSuffix) {
        PsseVoltageSourceConverter psseVoltageSourceConverter = new PsseVoltageSourceConverter();
        psseVoltageSourceConverter.setIbus(parseIntFromRecord(rec, headers, "ibus" + headerSuffix));
        psseVoltageSourceConverter.setType(parseIntFromRecord(rec, headers, "type" + headerSuffix));
        psseVoltageSourceConverter.setMode(parseIntFromRecord(rec, 1, headers, "mode" + headerSuffix));
        psseVoltageSourceConverter.setDcset(parseDoubleFromRecord(rec, headers, "dcset" + headerSuffix));
        psseVoltageSourceConverter.setAcset(parseDoubleFromRecord(rec, 1.0, headers, "acset" + headerSuffix));
        psseVoltageSourceConverter.setAloss(parseDoubleFromRecord(rec, 0.0, headers, "aloss" + headerSuffix));
        psseVoltageSourceConverter.setBloss(parseDoubleFromRecord(rec, 0.0, headers, "bloss" + headerSuffix));
        psseVoltageSourceConverter.setMinloss(parseDoubleFromRecord(rec, 0.0, headers, "minloss" + headerSuffix));
        psseVoltageSourceConverter.setSmax(parseDoubleFromRecord(rec, 0.0, headers, "smax" + headerSuffix));
        psseVoltageSourceConverter.setImax(parseDoubleFromRecord(rec, 0.0, headers, "imax" + headerSuffix));
        psseVoltageSourceConverter.setPwf(parseDoubleFromRecord(rec, 1.0, headers, "pwf" + headerSuffix));
        psseVoltageSourceConverter.setMaxq(parseDoubleFromRecord(rec, 9999.0, headers, "maxq" + headerSuffix));
        psseVoltageSourceConverter.setMinq(parseDoubleFromRecord(rec, -9999.0, headers, "minq" + headerSuffix));
        if (version.getMajorNumber() <= 33) {
            psseVoltageSourceConverter.setRemot(parseIntFromRecord(rec, 0, headers, "remot" + headerSuffix));
        }
        psseVoltageSourceConverter.setRmpct(parseDoubleFromRecord(rec, 100.0, headers, "rmpct" + headerSuffix));
        if (version.getMajorNumber() >= 35) {
            psseVoltageSourceConverter.setVsreg(parseIntFromRecord(rec, 0, headers, "vsreg" + headerSuffix));
            psseVoltageSourceConverter.setNreg(parseIntFromRecord(rec, 0, headers, "nreg" + headerSuffix));
        }
        return psseVoltageSourceConverter;
    }

    public static String[] toRecord(PsseVoltageSourceConverter psseVoltageSourceConverter, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            Optional<String> optionalValue = psseVoltageSourceConverter.headerToString(headers[i]);
            if (optionalValue.isEmpty()) {
                throw new PsseException("Unsupported header: " + headers[i]);
            }
            row[i] = optionalValue.get();
        }
        return row;
    }

    public Optional<String> headerToString(String header) {
        return switch (header) {
            case "ibus" -> Optional.of(String.valueOf(getIbus()));
            case "type" -> Optional.of(String.valueOf(getType()));
            case "mode" -> Optional.of(String.valueOf(getMode()));
            case "dcset" -> Optional.of(String.valueOf(getDcset()));
            case "acset" -> Optional.of(String.valueOf(getAcset()));
            case "aloss" -> Optional.of(String.valueOf(getAloss()));
            case "bloss" -> Optional.of(String.valueOf(getBloss()));
            case "minloss" -> Optional.of(String.valueOf(getMinloss()));
            case "smax" -> Optional.of(String.valueOf(getSmax()));
            case "imax" -> Optional.of(String.valueOf(getImax()));
            case "pwf" -> Optional.of(String.valueOf(getPwf()));
            case "maxq" -> Optional.of(String.valueOf(getMaxq()));
            case "minq" -> Optional.of(String.valueOf(getMinq()));
            case "remot" -> Optional.of(String.valueOf(getRemot()));
            case "rmpct" -> Optional.of(String.valueOf(getRmpct()));
            case "vsreg" -> Optional.of(String.valueOf(getVsreg()));
            case "nreg" -> Optional.of(String.valueOf(getNreg()));
            default -> Optional.empty();
        };
    }

    public int getIbus() {
        return ibus;
    }

    public void setIbus(int ibus) {
        this.ibus = ibus;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public double getDcset() {
        return dcset;
    }

    public void setDcset(double dcset) {
        this.dcset = dcset;
    }

    public double getAcset() {
        return acset;
    }

    public void setAcset(double acset) {
        this.acset = acset;
    }

    public double getAloss() {
        return aloss;
    }

    public void setAloss(double aloss) {
        this.aloss = aloss;
    }

    public double getBloss() {
        return bloss;
    }

    public void setBloss(double bloss) {
        this.bloss = bloss;
    }

    public double getMinloss() {
        return minloss;
    }

    public void setMinloss(double minloss) {
        this.minloss = minloss;
    }

    public double getSmax() {
        return smax;
    }

    public void setSmax(double smax) {
        this.smax = smax;
    }

    public double getImax() {
        return imax;
    }

    public void setImax(double imax) {
        this.imax = imax;
    }

    public double getPwf() {
        return pwf;
    }

    public void setPwf(double pwf) {
        this.pwf = pwf;
    }

    public double getMaxq() {
        return maxq;
    }

    public void setMaxq(double maxq) {
        this.maxq = maxq;
    }

    public double getMinq() {
        return minq;
    }

    public void setMinq(double minq) {
        this.minq = minq;
    }

    public int getRemot() {
        checkVersion("remot");
        return remot;
    }

    public void setRemot(int remot) {
        this.remot = remot;
    }

    public double getRmpct() {
        return rmpct;
    }

    public void setRmpct(double rmpct) {
        this.rmpct = rmpct;
    }

    public int getVsreg() {
        checkVersion("vsreg");
        return vsreg;
    }

    public void setVsreg(int vsreg) {
        this.vsreg = vsreg;
    }

    public int getNreg() {
        checkVersion("nreg");
        return nreg;
    }

    public void setNreg(int nreg) {
        this.nreg = nreg;
    }

    public PsseVoltageSourceConverter copy() {
        PsseVoltageSourceConverter copy = new PsseVoltageSourceConverter();
        copy.ibus = this.ibus;
        copy.type = this.type;
        copy.mode = this.mode;
        copy.dcset = this.dcset;
        copy.acset = this.acset;
        copy.aloss = this.aloss;
        copy.bloss = this.bloss;
        copy.minloss = this.minloss;
        copy.smax = this.smax;
        copy.imax = this.imax;
        copy.pwf = this.pwf;
        copy.maxq = this.maxq;
        copy.minq = this.minq;
        copy.remot = this.remot;
        copy.rmpct = this.rmpct;
        copy.vsreg = this.vsreg;
        copy.nreg = this.nreg;
        return copy;
    }
}
