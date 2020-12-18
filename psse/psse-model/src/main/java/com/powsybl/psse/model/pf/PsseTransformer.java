/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import com.powsybl.psse.model.pf.io.WindingHeaderTransformer;
import com.powsybl.psse.model.pf.io.WindingRatesHeaderTransformer;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

@JsonPropertyOrder(alphabetic = true)
public class PsseTransformer extends PsseVersioned {

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

    @Parsed(field = {"i", "ibus"})
    private int i;

    @Parsed(field = {"j", "jbus"})
    private int j;

    @Parsed(field = {"k", "kbus"})
    private int k = 0;

    @Parsed(defaultNullRead = "1")
    private String ckt;

    @Parsed
    private int cw = 1;

    @Parsed
    private int cz = 1;

    @Parsed
    private int cm = 1;

    @Parsed
    private double mag1 = 0;

    @Parsed
    private double mag2 = 0;

    @Parsed(field = {"nmetr", "nmet"})
    private int nmetr = 2;

    @Parsed(defaultNullRead = "            ")
    private String name;

    @Parsed
    private int stat = 1;

    @Nested
    private PsseOwnership ownership;

    @Parsed(defaultNullRead = "            ")
    // If the issue 432 in Univocity is fixed,
    // the previous annotation will be correctly processed
    // and there would be no need to initialize vecgrp with default value
    // (https://github.com/uniVocity/univocity-parsers/issues/432)
    private String vecgrp = "            ";

    @Parsed
    @Revision(since = 35)
    private int zcod = 0;

    @Parsed(field = {"r12", "r1_2"})
    private double r12 = 0;

    @Parsed(field = {"x12", "x1_2"})
    private double x12;

    @Parsed(field = {"sbase12", "sbase1_2"})
    private double sbase12 = Double.NaN;

    @Parsed(field = {"r23", "r2_3"})
    private double r23 = 0;

    @Parsed(field = {"x23", "x2_3"})
    private double x23 = Double.NaN;

    @Parsed(field = {"sbase23", "sbase2_3"})
    private double sbase23 = Double.NaN;

    @Parsed(field = {"r31", "r3_1"})
    private double r31 = 0;

    @Parsed(field = {"x31", "x3_1"})
    private double x31 = Double.NaN;

    @Parsed(field = {"sbase31", "sbase3_1"})
    private double sbase31 = Double.NaN;

    @Parsed
    private double vmstar = 1;

    @Parsed
    private double anstar = 0;

    @Nested(headerTransformer = WindingHeaderTransformer.class, args = "1")
    private PsseTransformerWinding winding1;

    @Nested(headerTransformer = WindingRatesHeaderTransformer.class, args = "1")
    private PsseRates winding1Rates;

    @Nested(headerTransformer = WindingHeaderTransformer.class, args = "2")
    private PsseTransformerWinding winding2;

    @Nested(headerTransformer = WindingRatesHeaderTransformer.class, args = "2")
    private PsseRates winding2Rates;

    @Nested(headerTransformer = WindingHeaderTransformer.class, args = "3")
    private PsseTransformerWinding winding3;

    @Nested(headerTransformer = WindingRatesHeaderTransformer.class, args = "3")
    private PsseRates winding3Rates;

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
        return vecgrp;
    }

    public void setVecgrp(String vecgrp) {
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
        return r12;
    }

    public void setR12(double r12) {
        this.r12 = r12;
    }

    public double getX12() {
        return x12;
    }

    public void setX12(double x12) {
        this.x12 = x12;
    }

    public double getSbase12() {
        return sbase12;
    }

    public void setSbase12(double sbase12) {
        this.sbase12 = sbase12;
    }

    public double getR23() {
        return r23;
    }

    public void setR23(double r23) {
        this.r23 = r23;
    }

    public double getX23() {
        return x23;
    }

    public void setX23(double x23) {
        this.x23 = x23;
    }

    public double getSbase23() {
        return sbase23;
    }

    public void setSbase23(double sbase23) {
        this.sbase23 = sbase23;
    }

    public double getR31() {
        return r31;
    }

    public void setR31(double r31) {
        this.r31 = r31;
    }

    public double getX31() {
        return x31;
    }

    public void setX31(double x31) {
        this.x31 = x31;
    }

    public double getSbase31() {
        return sbase31;
    }

    public void setSbase31(double sbase31) {
        this.sbase31 = sbase31;
    }

    public double getVmstar() {
        return vmstar;
    }

    public void setVmstar(double vmstar) {
        this.vmstar = vmstar;
    }

    public double getAnstar() {
        return anstar;
    }

    public void setAnstar(double anstar) {
        this.anstar = anstar;
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
}
