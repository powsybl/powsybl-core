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
import de.siegmar.fastcsv.reader.NamedCsvRecord;

import static com.powsybl.psse.model.io.Util.defaultIfEmpty;
import static com.powsybl.psse.model.io.Util.parseIntOrDefault;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseGneDevice {

    private String name;
    private String model;
    private int nterm = 1;
    private int bus1;
    private int bus2;
    private int nreal = 0;
    private int nintg = 0;
    private int nchar = 0;
    private int status = 1;
    private int owner;
    private int nmet;
    private double real1 = 0.0;
    private double real2 = 0.0;
    private double real3 = 0.0;
    private double real4 = 0.0;
    private double real5 = 0.0;
    private double real6 = 0.0;
    private double real7 = 0.0;
    private double real8 = 0.0;
    private double real9 = 0.0;
    private double real10 = 0.0;
    private int intg1;
    private int intg2;
    private int intg3;
    private int intg4;
    private int intg5;
    private int intg6;
    private int intg7;
    private int intg8;
    private int intg9;
    private int intg10;
    private String char1;
    private String char2;
    private String char3;
    private String char4;
    private String char5;
    private String char6;
    private String char7;
    private String char8;
    private String char9;
    private String char10;

    public static PsseGneDevice fromRecord(NamedCsvRecord rec, PsseVersion version) {
        PsseGneDevice psseGneDevice = new PsseGneDevice();
        psseGneDevice.setName(rec.getField("name"));
        psseGneDevice.setModel(rec.getField("model"));
        psseGneDevice.setNterm(parseIntOrDefault(rec.getField("nterm"), 1));
        psseGneDevice.setBus1(Integer.parseInt(rec.getField("bus1")));
        psseGneDevice.setBus2(Integer.parseInt(rec.getField("bus2")));
        psseGneDevice.setNreal(Integer.parseInt(rec.getField("nreal")));
        psseGneDevice.setNintg(Integer.parseInt(rec.getField("nintg")));
        psseGneDevice.setNchar(Integer.parseInt(rec.getField("nchar")));
        psseGneDevice.setStatus(parseIntOrDefault(rec.findField("status").isPresent() ?
            rec.getField("status") :
            rec.getField("stat"), 1));
        psseGneDevice.setOwner(Integer.parseInt(rec.getField("owner")));
        psseGneDevice.setNmet(Integer.parseInt(rec.getField("nmet")));
        psseGneDevice.setReal1(Double.parseDouble(rec.getField("real1")));
        psseGneDevice.setReal2(Double.parseDouble(rec.getField("real2")));
        psseGneDevice.setReal3(Double.parseDouble(rec.getField("real3")));
        psseGneDevice.setReal4(Double.parseDouble(rec.getField("real4")));
        psseGneDevice.setReal5(Double.parseDouble(rec.getField("real5")));
        psseGneDevice.setReal6(Double.parseDouble(rec.getField("real6")));
        psseGneDevice.setReal7(Double.parseDouble(rec.getField("real7")));
        psseGneDevice.setReal8(Double.parseDouble(rec.getField("real8")));
        psseGneDevice.setReal9(Double.parseDouble(rec.getField("real9")));
        psseGneDevice.setReal10(Double.parseDouble(rec.getField("real10")));
        psseGneDevice.setIntg1(Integer.parseInt(rec.getField("intg1")));
        psseGneDevice.setIntg2(Integer.parseInt(rec.getField("intg2")));
        psseGneDevice.setIntg3(Integer.parseInt(rec.getField("intg3")));
        psseGneDevice.setIntg4(Integer.parseInt(rec.getField("intg4")));
        psseGneDevice.setIntg5(Integer.parseInt(rec.getField("intg5")));
        psseGneDevice.setIntg6(Integer.parseInt(rec.getField("intg6")));
        psseGneDevice.setIntg7(Integer.parseInt(rec.getField("intg7")));
        psseGneDevice.setIntg8(Integer.parseInt(rec.getField("intg8")));
        psseGneDevice.setIntg9(Integer.parseInt(rec.getField("intg9")));
        psseGneDevice.setIntg10(Integer.parseInt(rec.getField("intg10")));
        psseGneDevice.setChar1(defaultIfEmpty(rec.getField("char1"), "1"));
        psseGneDevice.setChar2(defaultIfEmpty(rec.getField("char2"), "1"));
        psseGneDevice.setChar3(defaultIfEmpty(rec.getField("char3"), "1"));
        psseGneDevice.setChar4(defaultIfEmpty(rec.getField("char4"), "1"));
        psseGneDevice.setChar5(defaultIfEmpty(rec.getField("char5"), "1"));
        psseGneDevice.setChar6(defaultIfEmpty(rec.getField("char6"), "1"));
        psseGneDevice.setChar7(defaultIfEmpty(rec.getField("char7"), "1"));
        psseGneDevice.setChar8(defaultIfEmpty(rec.getField("char8"), "1"));
        psseGneDevice.setChar9(defaultIfEmpty(rec.getField("char9"), "1"));
        psseGneDevice.setChar10(defaultIfEmpty(rec.getField("char10"), "1"));
        return psseGneDevice;
    }

    public static String[] toRecord(PsseGneDevice psseGneDevice, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i];
            row[i] = switch (h) {
                case "name" -> psseGneDevice.getName();
                case "model" -> psseGneDevice.getModel();
                case "nterm" -> String.valueOf(psseGneDevice.getNterm());
                case "bus1" -> String.valueOf(psseGneDevice.getBus1());
                case "bus2" -> String.valueOf(psseGneDevice.getBus2());
                case "nreal" -> String.valueOf(psseGneDevice.getNreal());
                case "nintg" -> String.valueOf(psseGneDevice.getNintg());
                case "nchar" -> String.valueOf(psseGneDevice.getNchar());
                case "status", "stat" -> String.valueOf(psseGneDevice.getStatus());
                case "owner" -> String.valueOf(psseGneDevice.getOwner());
                case "nmet" -> String.valueOf(psseGneDevice.getNmet());
                case "real1" -> String.valueOf(psseGneDevice.getReal1());
                case "real2" -> String.valueOf(psseGneDevice.getReal2());
                case "real3" -> String.valueOf(psseGneDevice.getReal3());
                case "real4" -> String.valueOf(psseGneDevice.getReal4());
                case "real5" -> String.valueOf(psseGneDevice.getReal5());
                case "real6" -> String.valueOf(psseGneDevice.getReal6());
                case "real7" -> String.valueOf(psseGneDevice.getReal7());
                case "real8" -> String.valueOf(psseGneDevice.getReal8());
                case "real9" -> String.valueOf(psseGneDevice.getReal9());
                case "real10" -> String.valueOf(psseGneDevice.getReal10());
                case "intg1" -> String.valueOf(psseGneDevice.getIntg1());
                case "intg2" -> String.valueOf(psseGneDevice.getIntg2());
                case "intg3" -> String.valueOf(psseGneDevice.getIntg3());
                case "intg4" -> String.valueOf(psseGneDevice.getIntg4());
                case "intg5" -> String.valueOf(psseGneDevice.getIntg5());
                case "intg6" -> String.valueOf(psseGneDevice.getIntg6());
                case "intg7" -> String.valueOf(psseGneDevice.getIntg7());
                case "intg8" -> String.valueOf(psseGneDevice.getIntg8());
                case "intg9" -> String.valueOf(psseGneDevice.getIntg9());
                case "intg10" -> String.valueOf(psseGneDevice.getIntg10());
                case "char1" -> psseGneDevice.getChar1();
                case "char2" -> psseGneDevice.getChar2();
                case "char3" -> psseGneDevice.getChar3();
                case "char4" -> psseGneDevice.getChar4();
                case "char5" -> psseGneDevice.getChar5();
                case "char6" -> psseGneDevice.getChar6();
                case "char7" -> psseGneDevice.getChar7();
                case "char8" -> psseGneDevice.getChar8();
                case "char9" -> psseGneDevice.getChar9();
                case "char10" -> psseGneDevice.getChar10();
                default -> throw new PsseException("Unsupported header: " + h);
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
