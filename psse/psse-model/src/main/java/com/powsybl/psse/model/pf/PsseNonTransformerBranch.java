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
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseNonTransformerBranch extends PsseVersioned {

    @Override
    public void setModel(PssePowerFlowModel model) {
        super.setModel(model);
        ownership.setModel(model);
        rates.setModel(model);
    }

    @Parsed(field = {"i", "ibus"})
    private int i;

    @Parsed(field = {"j", "jbus"})
    private int j;

    @Parsed(defaultNullRead = "1")
    private String ckt;

    @Parsed(field = {"r", "rpu"})
    private double r = 0.0;

    @Parsed(field = {"x", "xpu"})
    private double x;

    @Parsed(field = {"b", "bpu"})
    private double b = 0;

    @Nested
    private PsseRates rates = new PsseRates();

    @Parsed
    private double gi = 0;

    @Parsed
    private double bi = 0;

    @Parsed
    private double gj = 0;

    @Parsed
    private double bj = 0;

    @Parsed(field = {"st", "stat"})
    private int st = 1;

    @Parsed
    private int met = 1;

    @Parsed
    private double len = 0;

    @Nested
    private PsseOwnership ownership = new PsseOwnership();

    @Parsed(defaultNullRead = " ")
    @Revision(since = 35)
    private String name = " ";

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

    public String getCkt() {
        return ckt;
    }

    public void setCkt(String ckt) {
        this.ckt = ckt;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getGi() {
        return gi;
    }

    public void setGi(double gi) {
        this.gi = gi;
    }

    public double getBi() {
        return bi;
    }

    public void setBi(double bi) {
        this.bi = bi;
    }

    public double getGj() {
        return gj;
    }

    public void setGj(double gj) {
        this.gj = gj;
    }

    public double getBj() {
        return bj;
    }

    public void setBj(double bj) {
        this.bj = bj;
    }

    public int getSt() {
        return st;
    }

    public void setSt(int st) {
        this.st = st;
    }

    public int getMet() {
        return met;
    }

    public void setMet(int met) {
        this.met = met;
    }

    public double getLen() {
        return len;
    }

    public void setLen(double len) {
        this.len = len;
    }

    public String getName() {
        checkVersion("name");
        return name;
    }

    public void setName(String name) {
        checkVersion("name");
        this.name = Objects.requireNonNull(name);
    }

    public PsseOwnership getOwnership() {
        return ownership;
    }

    public void setOwnership(PsseOwnership ownership) {
        this.ownership = ownership;
    }

    public PsseRates getRates() {
        return rates;
    }

    public void setRates(PsseRates rates) {
        this.rates = rates;
    }

    public PsseNonTransformerBranch copy() {
        PsseNonTransformerBranch copy = new PsseNonTransformerBranch();
        copy.i = this.i;
        copy.j = this.j;
        copy.ckt = this.ckt;
        copy.r = this.r;
        copy.x = this.x;
        copy.b = this.b;
        copy.rates = this.rates.copy();
        copy.gi = this.gi;
        copy.bi = this.bi;
        copy.gj = this.gj;
        copy.bj = this.bj;
        copy.st = this.st;
        copy.met = this.met;
        copy.len = this.len;
        copy.ownership = this.ownership.copy();
        copy.name = this.name;
        return copy;
    }
}
