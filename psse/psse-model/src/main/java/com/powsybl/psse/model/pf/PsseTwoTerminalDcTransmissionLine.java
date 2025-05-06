/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import java.lang.reflect.Field;

import com.powsybl.psse.model.PsseVersioned;
import com.univocity.parsers.annotations.HeaderTransformer;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;

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

    @Parsed
    private String name;

    @Parsed
    private int mdc = 0;

    @Parsed
    private double rdc;

    @Parsed
    private double setvl;

    @Parsed
    private double vschd;

    @Parsed
    private double vcmod = 0.0;

    @Parsed
    private double rcomp = 0.0;

    @Parsed
    private double delti = 0.0;

    @Parsed(field = {"meter", "met"})
    private String meter = "I";

    @Parsed
    private double dcvmin = 0.0;

    @Parsed
    private int cccitmx = 20;

    @Parsed
    private double cccacc = 1.0;

    @Nested(headerTransformer = ConverterHeaderTransformer.class, args = "r")
    private PsseTwoTerminalDcConverter rectifier = new PsseTwoTerminalDcConverter();

    @Nested(headerTransformer = ConverterHeaderTransformer.class, args = "i")
    private PsseTwoTerminalDcConverter inverter = new PsseTwoTerminalDcConverter();

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

    public static class ConverterHeaderTransformer extends HeaderTransformer {
        private final String converterChar;

        public ConverterHeaderTransformer(String... args) {
            converterChar = args[0];
        }

        @Override
        public String transformName(Field field, String name) {
            if (name.equals("ifx")) {
                return "if" + converterChar;
            }
            return name + converterChar;
        }
    }
}
