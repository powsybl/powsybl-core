/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashMap;
import java.util.Map;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.defaultStringFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseGneDevice {

    private static final Map<String, PsseFieldDefinition<PsseGneDevice, ?>> FIELDS = createFields();

    private String name;
    private String model;
    private int nterm = defaultIntegerFor(STR_NTERM, FIELDS);
    private int bus1;
    private int bus2;
    private int nreal = defaultIntegerFor(STR_NREAL, FIELDS);
    private int nintg = defaultIntegerFor(STR_NINTG, FIELDS);
    private int nchar = defaultIntegerFor(STR_NCHAR, FIELDS);
    private int status = defaultIntegerFor(STR_STATUS, FIELDS);
    private int owner;
    private int nmet;
    private double real1 = defaultDoubleFor(STR_REAL1, FIELDS);
    private double real2 = defaultDoubleFor(STR_REAL2, FIELDS);
    private double real3 = defaultDoubleFor(STR_REAL3, FIELDS);
    private double real4 = defaultDoubleFor(STR_REAL4, FIELDS);
    private double real5 = defaultDoubleFor(STR_REAL5, FIELDS);
    private double real6 = defaultDoubleFor(STR_REAL6, FIELDS);
    private double real7 = defaultDoubleFor(STR_REAL7, FIELDS);
    private double real8 = defaultDoubleFor(STR_REAL8, FIELDS);
    private double real9 = defaultDoubleFor(STR_REAL9, FIELDS);
    private double real10 = defaultDoubleFor(STR_REAL10, FIELDS);
    private int intg1 = defaultIntegerFor(STR_INTG1, FIELDS);
    private int intg2 = defaultIntegerFor(STR_INTG2, FIELDS);
    private int intg3 = defaultIntegerFor(STR_INTG3, FIELDS);
    private int intg4 = defaultIntegerFor(STR_INTG4, FIELDS);
    private int intg5 = defaultIntegerFor(STR_INTG5, FIELDS);
    private int intg6 = defaultIntegerFor(STR_INTG6, FIELDS);
    private int intg7 = defaultIntegerFor(STR_INTG7, FIELDS);
    private int intg8 = defaultIntegerFor(STR_INTG8, FIELDS);
    private int intg9 = defaultIntegerFor(STR_INTG9, FIELDS);
    private int intg10 = defaultIntegerFor(STR_INTG10, FIELDS);
    private String char1 = defaultStringFor(STR_CHAR1, FIELDS);
    private String char2 = defaultStringFor(STR_CHAR2, FIELDS);
    private String char3 = defaultStringFor(STR_CHAR3, FIELDS);
    private String char4 = defaultStringFor(STR_CHAR4, FIELDS);
    private String char5 = defaultStringFor(STR_CHAR5, FIELDS);
    private String char6 = defaultStringFor(STR_CHAR6, FIELDS);
    private String char7 = defaultStringFor(STR_CHAR7, FIELDS);
    private String char8 = defaultStringFor(STR_CHAR8, FIELDS);
    private String char9 = defaultStringFor(STR_CHAR9, FIELDS);
    private String char10 = defaultStringFor(STR_CHAR10, FIELDS);

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseGneDevice fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseGneDevice::new);
    }

    public static String[] toRecord(PsseGneDevice psseGneDevice, String[] headers) {
        return Util.toRecord(psseGneDevice, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseGneDevice, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseGneDevice, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_NAME, String.class, PsseGneDevice::getName, PsseGneDevice::setName));
        addField(fields, createNewField(STR_MODEL, String.class, PsseGneDevice::getModel, PsseGneDevice::setModel));
        addField(fields, createNewField(STR_NTERM, Integer.class, PsseGneDevice::getNterm, PsseGneDevice::setNterm, 1));
        addField(fields, createNewField(STR_BUS1, Integer.class, PsseGneDevice::getBus1, PsseGneDevice::setBus1));
        addField(fields, createNewField(STR_BUS2, Integer.class, PsseGneDevice::getBus2, PsseGneDevice::setBus2));
        addField(fields, createNewField(STR_NREAL, Integer.class, PsseGneDevice::getNreal, PsseGneDevice::setNreal, 0));
        addField(fields, createNewField(STR_NINTG, Integer.class, PsseGneDevice::getNintg, PsseGneDevice::setNintg, 0));
        addField(fields, createNewField(STR_NCHAR, Integer.class, PsseGneDevice::getNchar, PsseGneDevice::setNchar, 0));
        addField(fields, createNewField(STR_STATUS, Integer.class, PsseGneDevice::getStatus, PsseGneDevice::setStatus, 1));
        addField(fields, createNewField(STR_STAT, Integer.class, PsseGneDevice::getStatus, PsseGneDevice::setStatus, 1));
        addField(fields, createNewField(STR_OWNER, Integer.class, PsseGneDevice::getOwner, PsseGneDevice::setOwner));
        addField(fields, createNewField(STR_NMET, Integer.class, PsseGneDevice::getNmet, PsseGneDevice::setNmet));
        addField(fields, createNewField(STR_REAL1, Double.class, PsseGneDevice::getReal1, PsseGneDevice::setReal1, 0.0));
        addField(fields, createNewField(STR_REAL2, Double.class, PsseGneDevice::getReal2, PsseGneDevice::setReal2, 0.0));
        addField(fields, createNewField(STR_REAL3, Double.class, PsseGneDevice::getReal3, PsseGneDevice::setReal3, 0.0));
        addField(fields, createNewField(STR_REAL4, Double.class, PsseGneDevice::getReal4, PsseGneDevice::setReal4, 0.0));
        addField(fields, createNewField(STR_REAL5, Double.class, PsseGneDevice::getReal5, PsseGneDevice::setReal5, 0.0));
        addField(fields, createNewField(STR_REAL6, Double.class, PsseGneDevice::getReal6, PsseGneDevice::setReal6, 0.0));
        addField(fields, createNewField(STR_REAL7, Double.class, PsseGneDevice::getReal7, PsseGneDevice::setReal7, 0.0));
        addField(fields, createNewField(STR_REAL8, Double.class, PsseGneDevice::getReal8, PsseGneDevice::setReal8, 0.0));
        addField(fields, createNewField(STR_REAL9, Double.class, PsseGneDevice::getReal9, PsseGneDevice::setReal9, 0.0));
        addField(fields, createNewField(STR_REAL10, Double.class, PsseGneDevice::getReal10, PsseGneDevice::setReal10, 0.0));
        addField(fields, createNewField(STR_INTG1, Integer.class, PsseGneDevice::getIntg1, PsseGneDevice::setIntg1, 0));
        addField(fields, createNewField(STR_INTG2, Integer.class, PsseGneDevice::getIntg2, PsseGneDevice::setIntg2, 0));
        addField(fields, createNewField(STR_INTG3, Integer.class, PsseGneDevice::getIntg3, PsseGneDevice::setIntg3, 0));
        addField(fields, createNewField(STR_INTG4, Integer.class, PsseGneDevice::getIntg4, PsseGneDevice::setIntg4, 0));
        addField(fields, createNewField(STR_INTG5, Integer.class, PsseGneDevice::getIntg5, PsseGneDevice::setIntg5, 0));
        addField(fields, createNewField(STR_INTG6, Integer.class, PsseGneDevice::getIntg6, PsseGneDevice::setIntg6, 0));
        addField(fields, createNewField(STR_INTG7, Integer.class, PsseGneDevice::getIntg7, PsseGneDevice::setIntg7, 0));
        addField(fields, createNewField(STR_INTG8, Integer.class, PsseGneDevice::getIntg8, PsseGneDevice::setIntg8, 0));
        addField(fields, createNewField(STR_INTG9, Integer.class, PsseGneDevice::getIntg9, PsseGneDevice::setIntg9, 0));
        addField(fields, createNewField(STR_INTG10, Integer.class, PsseGneDevice::getIntg10, PsseGneDevice::setIntg10, 0));
        addField(fields, createNewField(STR_CHAR1, String.class, PsseGneDevice::getChar1, PsseGneDevice::setChar1, "1"));
        addField(fields, createNewField(STR_CHAR2, String.class, PsseGneDevice::getChar2, PsseGneDevice::setChar2, "1"));
        addField(fields, createNewField(STR_CHAR3, String.class, PsseGneDevice::getChar3, PsseGneDevice::setChar3, "1"));
        addField(fields, createNewField(STR_CHAR4, String.class, PsseGneDevice::getChar4, PsseGneDevice::setChar4, "1"));
        addField(fields, createNewField(STR_CHAR5, String.class, PsseGneDevice::getChar5, PsseGneDevice::setChar5, "1"));
        addField(fields, createNewField(STR_CHAR6, String.class, PsseGneDevice::getChar6, PsseGneDevice::setChar6, "1"));
        addField(fields, createNewField(STR_CHAR7, String.class, PsseGneDevice::getChar7, PsseGneDevice::setChar7, "1"));
        addField(fields, createNewField(STR_CHAR8, String.class, PsseGneDevice::getChar8, PsseGneDevice::setChar8, "1"));
        addField(fields, createNewField(STR_CHAR9, String.class, PsseGneDevice::getChar9, PsseGneDevice::setChar9, "1"));
        addField(fields, createNewField(STR_CHAR10, String.class, PsseGneDevice::getChar10, PsseGneDevice::setChar10, "1"));

        return fields;
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
