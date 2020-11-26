/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.univocity.parsers.annotations.Parsed;

import java.util.Objects;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

@JsonIgnoreProperties({ "ratea", "rateb", "ratec" })

public class PsseNonTransformerBranch35 extends PsseNonTransformerBranch {

    @Parsed(defaultNullRead = " ")
    private String name;

    @Parsed
    private double rate1 = 0;

    @Parsed
    private double rate2 = 0;

    @Parsed
    private double rate3 = 0;

    @Parsed
    private double rate4 = 0;

    @Parsed
    private double rate5 = 0;

    @Parsed
    private double rate6 = 0;

    @Parsed
    private double rate7 = 0;

    @Parsed
    private double rate8 = 0;

    @Parsed
    private double rate9 = 0;

    @Parsed
    private double rate10 = 0;

    @Parsed
    private double rate11 = 0;

    @Parsed
    private double rate12 = 0;

    @Override
    public double getRatea() {
        throw new PsseException("Ratea not available in version 35");
    }

    @Override
    public void setRatea(double ratea) {
        throw new PsseException("Ratea not available in version 35");
    }

    @Override
    public double getRateb() {
        throw new PsseException("Rateb not available in version 35");
    }

    @Override
    public void setRateb(double rateb) {
        throw new PsseException("Rateb not available in version 35");
    }

    @Override
    public double getRatec() {
        throw new PsseException("Ratec not available in version 35");
    }

    @Override
    public void setRatec(double ratec) {
        throw new PsseException("Ratec not available in version 35");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public double getRate1() {
        return rate1;
    }

    public void setRate1(double rate1) {
        this.rate1 = rate1;
    }

    public double getRate2() {
        return rate2;
    }

    public void setRate2(double rate2) {
        this.rate2 = rate2;
    }

    public double getRate3() {
        return rate3;
    }

    public void setRate3(double rate3) {
        this.rate3 = rate3;
    }

    public double getRate4() {
        return rate4;
    }

    public void setRate4(double rate4) {
        this.rate4 = rate4;
    }

    public double getRate5() {
        return rate5;
    }

    public void setRate5(double rate5) {
        this.rate5 = rate5;
    }

    public double getRate6() {
        return rate6;
    }

    public void setRate6(double rate6) {
        this.rate6 = rate6;
    }

    public double getRate7() {
        return rate7;
    }

    public void setRate7(double rate7) {
        this.rate7 = rate7;
    }

    public double getRate8() {
        return rate8;
    }

    public void setRate8(double rate8) {
        this.rate8 = rate8;
    }

    public double getRate9() {
        return rate9;
    }

    public void setRate9(double rate9) {
        this.rate9 = rate9;
    }

    public double getRate10() {
        return rate10;
    }

    public void setRate10(double rate10) {
        this.rate10 = rate10;
    }

    public double getRate11() {
        return rate11;
    }

    public void setRate11(double rate11) {
        this.rate11 = rate11;
    }

    public double getRate12() {
        return rate12;
    }

    public void setRate12(double rate12) {
        this.rate12 = rate12;
    }
}
