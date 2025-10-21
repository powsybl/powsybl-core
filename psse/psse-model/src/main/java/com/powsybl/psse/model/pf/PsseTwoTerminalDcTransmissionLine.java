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
public class PsseTwoTerminalDcTransmissionLine extends PsseVersioned {

    @Override
    public void setModel(PssePowerFlowModel model) {
        super.setModel(model);
        rectifier.setModel(model);
        inverter.setModel(model);
    }

    private String name;
    private int mdc = 0;
    private double rdc;
    private double setvl;
    private double vschd;
    private double vcmod = 0.0;
    private double rcomp = 0.0;
    private double delti = 0.0;
    private String meter = "I";
    private double dcvmin = 0.0;
    private int cccitmx = 20;
    private double cccacc = 1.0;
    private PsseTwoTerminalDcConverter rectifier;
    private PsseTwoTerminalDcConverter inverter;

    public static PsseTwoTerminalDcTransmissionLine fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseTwoTerminalDcTransmissionLine psseTwoTerminalDcTransmissionLine = new PsseTwoTerminalDcTransmissionLine();
        psseTwoTerminalDcTransmissionLine.setName(parseStringFromRecord(rec, headers, "name"));
        psseTwoTerminalDcTransmissionLine.setMdc(parseIntFromRecord(rec, 0, headers, "mdc"));
        psseTwoTerminalDcTransmissionLine.setRdc(parseDoubleFromRecord(rec, headers, "rdc"));
        psseTwoTerminalDcTransmissionLine.setSetvl(parseDoubleFromRecord(rec, headers, "setvl"));
        psseTwoTerminalDcTransmissionLine.setVschd(parseDoubleFromRecord(rec, headers, "vschd"));
        psseTwoTerminalDcTransmissionLine.setVcmod(parseDoubleFromRecord(rec, 0.0, headers, "vcmod"));
        psseTwoTerminalDcTransmissionLine.setRcomp(parseDoubleFromRecord(rec, 0.0, headers, "rcomp"));
        psseTwoTerminalDcTransmissionLine.setDelti(parseDoubleFromRecord(rec, 0.0, headers, "delti"));
        psseTwoTerminalDcTransmissionLine.setMeter(parseStringFromRecord(rec, "I", headers, "meter", "met"));
        psseTwoTerminalDcTransmissionLine.setDcvmin(parseDoubleFromRecord(rec, 0.0, headers, "dcvmin"));
        psseTwoTerminalDcTransmissionLine.setCccitmx(parseIntFromRecord(rec, 20, headers, "cccitmx"));
        psseTwoTerminalDcTransmissionLine.setCccacc(parseDoubleFromRecord(rec, 1.0, headers, "cccacc"));
        psseTwoTerminalDcTransmissionLine.setRectifier(PsseTwoTerminalDcConverter.fromRecord(rec, version, headers, "r"));
        psseTwoTerminalDcTransmissionLine.setInverter(PsseTwoTerminalDcConverter.fromRecord(rec, version, headers, "i"));
        return psseTwoTerminalDcTransmissionLine;
    }

    public static String[] toRecord(PsseTwoTerminalDcTransmissionLine psseTwoTerminalDcTransmissionLine, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "name" -> psseTwoTerminalDcTransmissionLine.getName();
                case "mdc" -> String.valueOf(psseTwoTerminalDcTransmissionLine.getMdc());
                case "rdc" -> String.valueOf(psseTwoTerminalDcTransmissionLine.getRdc());
                case "setvl" -> String.valueOf(psseTwoTerminalDcTransmissionLine.getSetvl());
                case "vschd" -> String.valueOf(psseTwoTerminalDcTransmissionLine.getVschd());
                case "vcmod" -> String.valueOf(psseTwoTerminalDcTransmissionLine.getVcmod());
                case "rcomp" -> String.valueOf(psseTwoTerminalDcTransmissionLine.getRcomp());
                case "delti" -> String.valueOf(psseTwoTerminalDcTransmissionLine.getDelti());
                case "meter", "met" -> String.valueOf(psseTwoTerminalDcTransmissionLine.getMeter());
                case "dcvmin" -> String.valueOf(psseTwoTerminalDcTransmissionLine.getDcvmin());
                case "cccitmx" -> String.valueOf(psseTwoTerminalDcTransmissionLine.getCccitmx());
                case "cccacc" -> String.valueOf(psseTwoTerminalDcTransmissionLine.getCccacc());
                default -> {
                    Optional<String> optionalValue = psseTwoTerminalDcTransmissionLine.getRectifier().headerToString(headers[i].substring(0, headers[i].length() - 1));
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    optionalValue = psseTwoTerminalDcTransmissionLine.getInverter().headerToString(headers[i].substring(0, headers[i].length() - 1));
                    if (optionalValue.isPresent()) {
                        yield optionalValue.get();
                    }
                    throw new PsseException("Unsupported header: " + headers[i]);
                }
            };
        }
        return row;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMdc() {
        return mdc;
    }

    public void setMdc(int mdc) {
        this.mdc = mdc;
    }

    public double getRdc() {
        return rdc;
    }

    public void setRdc(double rdc) {
        this.rdc = rdc;
    }

    public double getSetvl() {
        return setvl;
    }

    public void setSetvl(double setvl) {
        this.setvl = setvl;
    }

    public double getVschd() {
        return vschd;
    }

    public void setVschd(double vschd) {
        this.vschd = vschd;
    }

    public double getVcmod() {
        return vcmod;
    }

    public void setVcmod(double vcmod) {
        this.vcmod = vcmod;
    }

    public double getRcomp() {
        return rcomp;
    }

    public void setRcomp(double rcomp) {
        this.rcomp = rcomp;
    }

    public double getDelti() {
        return delti;
    }

    public void setDelti(double delti) {
        this.delti = delti;
    }

    public String getMeter() {
        return meter;
    }

    public void setMeter(String meter) {
        this.meter = meter;
    }

    public double getDcvmin() {
        return dcvmin;
    }

    public void setDcvmin(double dcvmin) {
        this.dcvmin = dcvmin;
    }

    public int getCccitmx() {
        return cccitmx;
    }

    public void setCccitmx(int cccitmx) {
        this.cccitmx = cccitmx;
    }

    public double getCccacc() {
        return cccacc;
    }

    public void setCccacc(double cccacc) {
        this.cccacc = cccacc;
    }

    public void setRectifier(PsseTwoTerminalDcConverter rectifier) {
        this.rectifier = rectifier;
    }

    public PsseTwoTerminalDcConverter getRectifier() {
        return rectifier;
    }

    public void setInverter(PsseTwoTerminalDcConverter inverter) {
        this.inverter = inverter;
    }

    public PsseTwoTerminalDcConverter getInverter() {
        return inverter;
    }

    public PsseTwoTerminalDcTransmissionLine copy() {
        PsseTwoTerminalDcTransmissionLine copy = new PsseTwoTerminalDcTransmissionLine();
        copy.name = this.name;
        copy.mdc = this.mdc;
        copy.rdc = this.rdc;
        copy.setvl = this.setvl;
        copy.vschd = this.vschd;
        copy.vcmod = this.vcmod;
        copy.rcomp = this.rcomp;
        copy.delti = this.delti;
        copy.meter = this.meter;
        copy.dcvmin = this.dcvmin;
        copy.cccitmx = this.cccitmx;
        copy.cccacc = this.cccacc;
        copy.rectifier = this.rectifier.copy();
        copy.inverter = this.inverter.copy();
        return copy;
    }
}
