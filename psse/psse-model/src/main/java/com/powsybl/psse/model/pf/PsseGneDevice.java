/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseException;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.Map;
import java.util.function.Function;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseGneDevice {

    private static final Map<String, Function<PsseGneDevice, String>> GETTERS = Map.ofEntries(
        Map.entry("name", PsseGneDevice::getName),
        Map.entry("model", PsseGneDevice::getModel),
        Map.entry("nterm", device -> String.valueOf(device.getNterm())),
        Map.entry("bus1", device -> String.valueOf(device.getBus1())),
        Map.entry("bus2", device -> String.valueOf(device.getBus2())),
        Map.entry("nreal", device -> String.valueOf(device.getNreal())),
        Map.entry("nintg", device -> String.valueOf(device.getNintg())),
        Map.entry("nchar", device -> String.valueOf(device.getNchar())),
        Map.entry("status", device -> String.valueOf(device.getStatus())),
        Map.entry("stat", device -> String.valueOf(device.getStatus())),
        Map.entry("owner", device -> String.valueOf(device.getOwner())),
        Map.entry("nmet", device -> String.valueOf(device.getNmet())),
        Map.entry("real1", device -> String.valueOf(device.getReal1())),
        Map.entry("real2", device -> String.valueOf(device.getReal2())),
        Map.entry("real3", device -> String.valueOf(device.getReal3())),
        Map.entry("real4", device -> String.valueOf(device.getReal4())),
        Map.entry("real5", device -> String.valueOf(device.getReal5())),
        Map.entry("real6", device -> String.valueOf(device.getReal6())),
        Map.entry("real7", device -> String.valueOf(device.getReal7())),
        Map.entry("real8", device -> String.valueOf(device.getReal8())),
        Map.entry("real9", device -> String.valueOf(device.getReal9())),
        Map.entry("real10", device -> String.valueOf(device.getReal10())),
        Map.entry("intg1", device -> String.valueOf(device.getIntg1())),
        Map.entry("intg2", device -> String.valueOf(device.getIntg2())),
        Map.entry("intg3", device -> String.valueOf(device.getIntg3())),
        Map.entry("intg4", device -> String.valueOf(device.getIntg4())),
        Map.entry("intg5", device -> String.valueOf(device.getIntg5())),
        Map.entry("intg6", device -> String.valueOf(device.getIntg6())),
        Map.entry("intg7", device -> String.valueOf(device.getIntg7())),
        Map.entry("intg8", device -> String.valueOf(device.getIntg8())),
        Map.entry("intg9", device -> String.valueOf(device.getIntg9())),
        Map.entry("intg10", device -> String.valueOf(device.getIntg10())),
        Map.entry("char1", device -> String.valueOf(device.getChar1())),
        Map.entry("char2", device -> String.valueOf(device.getChar2())),
        Map.entry("char3", device -> String.valueOf(device.getChar3())),
        Map.entry("char4", device -> String.valueOf(device.getChar4())),
        Map.entry("char5", device -> String.valueOf(device.getChar5())),
        Map.entry("char6", device -> String.valueOf(device.getChar6())),
        Map.entry("char7", device -> String.valueOf(device.getChar7())),
        Map.entry("char8", device -> String.valueOf(device.getChar8())),
        Map.entry("char9", device -> String.valueOf(device.getChar9())),
        Map.entry("char10", device -> String.valueOf(device.getChar10()))
    );

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

    public static PsseGneDevice fromRecord(CsvRecord rec, String[] headers) {
        PsseGneDevice psseGneDevice = new PsseGneDevice();
        psseGneDevice.setName(parseStringFromRecord(rec, headers, "name"));
        psseGneDevice.setModel(parseStringFromRecord(rec, headers, "model"));
        psseGneDevice.setNterm(parseIntFromRecord(rec, 1, headers, "nterm"));
        psseGneDevice.setBus1(parseIntFromRecord(rec, headers, "bus1"));
        psseGneDevice.setBus2(parseIntFromRecord(rec, headers, "bus2"));
        psseGneDevice.setNreal(parseIntFromRecord(rec, 0, headers, "nreal"));
        psseGneDevice.setNintg(parseIntFromRecord(rec, 0, headers, "nintg"));
        psseGneDevice.setNchar(parseIntFromRecord(rec, 0, headers, "nchar"));
        psseGneDevice.setStatus(parseIntFromRecord(rec, 1, headers, "status", "stat"));
        psseGneDevice.setOwner(parseIntFromRecord(rec, headers, "owner"));
        psseGneDevice.setNmet(parseIntFromRecord(rec, headers, "nmet"));
        psseGneDevice.setReal1(parseDoubleFromRecord(rec, 0d, headers, "real1"));
        psseGneDevice.setReal2(parseDoubleFromRecord(rec, 0d, headers, "real2"));
        psseGneDevice.setReal3(parseDoubleFromRecord(rec, 0d, headers, "real3"));
        psseGneDevice.setReal4(parseDoubleFromRecord(rec, 0d, headers, "real4"));
        psseGneDevice.setReal5(parseDoubleFromRecord(rec, 0d, headers, "real5"));
        psseGneDevice.setReal6(parseDoubleFromRecord(rec, 0d, headers, "real6"));
        psseGneDevice.setReal7(parseDoubleFromRecord(rec, 0d, headers, "real7"));
        psseGneDevice.setReal8(parseDoubleFromRecord(rec, 0d, headers, "real8"));
        psseGneDevice.setReal9(parseDoubleFromRecord(rec, 0d, headers, "real9"));
        psseGneDevice.setReal10(parseDoubleFromRecord(rec, 0d, headers, "real10"));
        psseGneDevice.setIntg1(parseIntFromRecord(rec, 0, headers, "intg1"));
        psseGneDevice.setIntg2(parseIntFromRecord(rec, 0, headers, "intg2"));
        psseGneDevice.setIntg3(parseIntFromRecord(rec, 0, headers, "intg3"));
        psseGneDevice.setIntg4(parseIntFromRecord(rec, 0, headers, "intg4"));
        psseGneDevice.setIntg5(parseIntFromRecord(rec, 0, headers, "intg5"));
        psseGneDevice.setIntg6(parseIntFromRecord(rec, 0, headers, "intg6"));
        psseGneDevice.setIntg7(parseIntFromRecord(rec, 0, headers, "intg7"));
        psseGneDevice.setIntg8(parseIntFromRecord(rec, 0, headers, "intg8"));
        psseGneDevice.setIntg9(parseIntFromRecord(rec, 0, headers, "intg9"));
        psseGneDevice.setIntg10(parseIntFromRecord(rec, 0, headers, "intg10"));
        psseGneDevice.setChar1(parseStringFromRecord(rec, "1", headers, "char1"));
        psseGneDevice.setChar2(parseStringFromRecord(rec, "1", headers, "char2"));
        psseGneDevice.setChar3(parseStringFromRecord(rec, "1", headers, "char3"));
        psseGneDevice.setChar4(parseStringFromRecord(rec, "1", headers, "char4"));
        psseGneDevice.setChar5(parseStringFromRecord(rec, "1", headers, "char5"));
        psseGneDevice.setChar6(parseStringFromRecord(rec, "1", headers, "char6"));
        psseGneDevice.setChar7(parseStringFromRecord(rec, "1", headers, "char7"));
        psseGneDevice.setChar8(parseStringFromRecord(rec, "1", headers, "char8"));
        psseGneDevice.setChar9(parseStringFromRecord(rec, "1", headers, "char9"));
        psseGneDevice.setChar10(parseStringFromRecord(rec, "1", headers, "char10"));
        return psseGneDevice;
    }

    public static String[] toRecord(PsseGneDevice psseGneDevice, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            Function<PsseGneDevice, String> getter = GETTERS.get(headers[i]);
            if (getter == null) {
                throw new PsseException("Unsupported header: " + headers[i]);
            }
            row[i] = getter.apply(psseGneDevice);
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
