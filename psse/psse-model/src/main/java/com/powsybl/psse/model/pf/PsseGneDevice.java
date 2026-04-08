/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseGneDevice {

    @Parsed
    private String name;

    @Parsed
    private String model;

    @Parsed
    private int nterm = 1;

    @Parsed
    private int bus1;

    @Parsed
    private int bus2;

    @Parsed
    private int nreal = 0;

    @Parsed
    private int nintg = 0;

    @Parsed
    private int nchar = 0;

    @Parsed(field = { "status", "stat" })
    private int status = 1;

    @Parsed
    private int owner;

    @Parsed
    private int nmet;

    @Parsed
    private double real1 = 0.0;

    @Parsed
    private double real2 = 0.0;

    @Parsed
    private double real3 = 0.0;

    @Parsed
    private double real4 = 0.0;

    @Parsed
    private double real5 = 0.0;

    @Parsed
    private double real6 = 0.0;

    @Parsed
    private double real7 = 0.0;

    @Parsed
    private double real8 = 0.0;

    @Parsed
    private double real9 = 0.0;

    @Parsed
    private double real10 = 0.0;

    @Parsed
    private int intg1;

    @Parsed
    private int intg2;

    @Parsed
    private int intg3;

    @Parsed
    private int intg4;

    @Parsed
    private int intg5;

    @Parsed
    private int intg6;

    @Parsed
    private int intg7;

    @Parsed
    private int intg8;

    @Parsed
    private int intg9;

    @Parsed
    private int intg10;

    @Parsed(defaultNullRead = "1")
    private String char1;

    @Parsed(defaultNullRead = "1")
    private String char2;

    @Parsed(defaultNullRead = "1")
    private String char3;

    @Parsed(defaultNullRead = "1")
    private String char4;

    @Parsed(defaultNullRead = "1")
    private String char5;

    @Parsed(defaultNullRead = "1")
    private String char6;

    @Parsed(defaultNullRead = "1")
    private String char7;

    @Parsed(defaultNullRead = "1")
    private String char8;

    @Parsed(defaultNullRead = "1")
    private String char9;

    @Parsed(defaultNullRead = "1")
    private String char10;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getNterm() {
        return nterm;
    }

    public void setNterm(int nterm) {
        this.nterm = nterm;
    }

    public int getBus1() {
        return bus1;
    }

    public void setBus1(int bus1) {
        this.bus1 = bus1;
    }

    public int getBus2() {
        return bus2;
    }

    public void setBus2(int bus2) {
        this.bus2 = bus2;
    }

    public int getNreal() {
        return nreal;
    }

    public void setNreal(int nreal) {
        this.nreal = nreal;
    }

    public int getNintg() {
        return nintg;
    }

    public void setNintg(int nintg) {
        this.nintg = nintg;
    }

    public int getNchar() {
        return nchar;
    }

    public void setNchar(int nchar) {
        this.nchar = nchar;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getNmet() {
        return nmet;
    }

    public void setNmet(int nmet) {
        this.nmet = nmet;
    }

    public double getReal1() {
        return real1;
    }

    public void setReal1(double real1) {
        this.real1 = real1;
    }

    public double getReal2() {
        return real2;
    }

    public void setReal2(double real2) {
        this.real2 = real2;
    }

    public double getReal3() {
        return real3;
    }

    public void setReal3(double real3) {
        this.real3 = real3;
    }

    public double getReal4() {
        return real4;
    }

    public void setReal4(double real4) {
        this.real4 = real4;
    }

    public double getReal5() {
        return real5;
    }

    public void setReal5(double real5) {
        this.real5 = real5;
    }

    public double getReal6() {
        return real6;
    }

    public void setReal6(double real6) {
        this.real6 = real6;
    }

    public double getReal7() {
        return real7;
    }

    public void setReal7(double real7) {
        this.real7 = real7;
    }

    public double getReal8() {
        return real8;
    }

    public void setReal8(double real8) {
        this.real8 = real8;
    }

    public double getReal9() {
        return real9;
    }

    public void setReal9(double real9) {
        this.real9 = real9;
    }

    public double getReal10() {
        return real10;
    }

    public void setReal10(double real10) {
        this.real10 = real10;
    }

    public int getIntg1() {
        return intg1;
    }

    public void setIntg1(int intg1) {
        this.intg1 = intg1;
    }

    public int getIntg2() {
        return intg2;
    }

    public void setIntg2(int intg2) {
        this.intg2 = intg2;
    }

    public int getIntg3() {
        return intg3;
    }

    public void setIntg3(int intg3) {
        this.intg3 = intg3;
    }

    public int getIntg4() {
        return intg4;
    }

    public void setIntg4(int intg4) {
        this.intg4 = intg4;
    }

    public int getIntg5() {
        return intg5;
    }

    public void setIntg5(int intg5) {
        this.intg5 = intg5;
    }

    public int getIntg6() {
        return intg6;
    }

    public void setIntg6(int intg6) {
        this.intg6 = intg6;
    }

    public int getIntg7() {
        return intg7;
    }

    public void setIntg7(int intg7) {
        this.intg7 = intg7;
    }

    public int getIntg8() {
        return intg8;
    }

    public void setIntg8(int intg8) {
        this.intg8 = intg8;
    }

    public int getIntg9() {
        return intg9;
    }

    public void setIntg9(int intg9) {
        this.intg9 = intg9;
    }

    public int getIntg10() {
        return intg10;
    }

    public void setIntg10(int intg10) {
        this.intg10 = intg10;
    }

    public String getChar1() {
        return char1;
    }

    public void setChar1(String char1) {
        this.char1 = char1;
    }

    public String getChar2() {
        return char2;
    }

    public void setChar2(String char2) {
        this.char2 = char2;
    }

    public String getChar3() {
        return char3;
    }

    public void setChar3(String char3) {
        this.char3 = char3;
    }

    public String getChar4() {
        return char4;
    }

    public void setChar4(String char4) {
        this.char4 = char4;
    }

    public String getChar5() {
        return char5;
    }

    public void setChar5(String char5) {
        this.char5 = char5;
    }

    public String getChar6() {
        return char6;
    }

    public void setChar6(String char6) {
        this.char6 = char6;
    }

    public String getChar7() {
        return char7;
    }

    public void setChar7(String char7) {
        this.char7 = char7;
    }

    public String getChar8() {
        return char8;
    }

    public void setChar8(String char8) {
        this.char8 = char8;
    }

    public String getChar9() {
        return char9;
    }

    public void setChar9(String char9) {
        this.char9 = char9;
    }

    public String getChar10() {
        return char10;
    }

    public void setChar10(String char10) {
        this.char10 = char10;
    }
}
