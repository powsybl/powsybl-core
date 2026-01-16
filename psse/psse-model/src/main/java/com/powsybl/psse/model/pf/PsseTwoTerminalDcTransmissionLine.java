/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.addSuffixToHeaders;
import static com.powsybl.psse.model.io.Util.checkForUnexpectedHeader;
import static com.powsybl.psse.model.io.Util.concatStringArrays;
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
public class PsseTwoTerminalDcTransmissionLine extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseTwoTerminalDcTransmissionLine, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_COMMON_1 = {STR_NAME, STR_MDC, STR_RDC, STR_SETVL, STR_VSCHD, STR_VCMOD, STR_RCOMP, STR_DELTI};
    private static final String[] FIELD_NAMES_METER_32_33 = {STR_METER};
    private static final String[] FIELD_NAMES_METER_35 = {STR_MET};
    private static final String[] FIELD_NAMES_COMMON_2 = {STR_DCVMIN, STR_CCCITMX, STR_CCCACC};
    private static final String[] FIELD_NAMES_32_33 = concatStringArrays(FIELD_NAMES_COMMON_1, FIELD_NAMES_METER_32_33, FIELD_NAMES_COMMON_2);
    private static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_COMMON_1, FIELD_NAMES_METER_35, FIELD_NAMES_COMMON_2);
    private static final String[] FIELD_NAMES_35_RAWX = concatStringArrays(FIELD_NAMES_35,
        addSuffixToHeaders(PsseTwoTerminalDcConverter.getFieldNames35(), STR_R),
        addSuffixToHeaders(PsseTwoTerminalDcConverter.getFieldNames35(), STR_I));
    private static final String[] FIELD_NAMES_STRING = concatStringArrays(stringHeaders(FIELDS),
        addSuffixToHeaders(PsseTwoTerminalDcConverter.getFieldNamesString(), STR_R),
        addSuffixToHeaders(PsseTwoTerminalDcConverter.getFieldNamesString(), STR_I));

    private String name;
    private int mdc = defaultIntegerFor(STR_MDC, FIELDS);
    private double rdc;
    private double setvl;
    private double vschd;
    private double vcmod = defaultDoubleFor(STR_VCMOD, FIELDS);
    private double rcomp = defaultDoubleFor(STR_RCOMP, FIELDS);
    private double delti = defaultDoubleFor(STR_DELTI, FIELDS);
    private String meter = defaultStringFor(STR_METER, FIELDS);
    private double dcvmin = defaultDoubleFor(STR_DCVMIN, FIELDS);
    private int cccitmx = defaultIntegerFor(STR_CCCITMX, FIELDS);
    private double cccacc = defaultDoubleFor(STR_CCCACC, FIELDS);
    private PsseTwoTerminalDcConverter rectifier = new PsseTwoTerminalDcConverter();
    private PsseTwoTerminalDcConverter inverter = new PsseTwoTerminalDcConverter();

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNames35RawX() {
        return FIELD_NAMES_35_RAWX;
    }

    public static String[] getFieldNamesString() {
        return FIELD_NAMES_STRING;
    }

    public static PsseTwoTerminalDcTransmissionLine fromRecord(CsvRecord rec, String[] headers) {
        PsseTwoTerminalDcTransmissionLine dcTransmissionLine = Util.fromRecord(rec.getFields(), headers, FIELDS, PsseTwoTerminalDcTransmissionLine::new);
        dcTransmissionLine.setRectifier(PsseTwoTerminalDcConverter.fromRecord(rec, headers, STR_R));
        dcTransmissionLine.setInverter(PsseTwoTerminalDcConverter.fromRecord(rec, headers, STR_I));
        return dcTransmissionLine;
    }

    public static String[] toRecord(PsseTwoTerminalDcTransmissionLine dcTransmissionLine, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = Util.toRecord(dcTransmissionLine, headers, FIELDS, unexpectedHeaders);
        PsseTwoTerminalDcConverter.toRecord(dcTransmissionLine.getRectifier(), headers, recordValues, unexpectedHeaders, STR_R);
        PsseTwoTerminalDcConverter.toRecord(dcTransmissionLine.getInverter(), headers, recordValues, unexpectedHeaders, STR_I);
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    private static Map<String, PsseFieldDefinition<PsseTwoTerminalDcTransmissionLine, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseTwoTerminalDcTransmissionLine, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_NAME, String.class, PsseTwoTerminalDcTransmissionLine::getName, PsseTwoTerminalDcTransmissionLine::setName));
        addField(fields, createNewField(STR_MDC, Integer.class, PsseTwoTerminalDcTransmissionLine::getMdc, PsseTwoTerminalDcTransmissionLine::setMdc, 0));
        addField(fields, createNewField(STR_RDC, Double.class, PsseTwoTerminalDcTransmissionLine::getRdc, PsseTwoTerminalDcTransmissionLine::setRdc));
        addField(fields, createNewField(STR_SETVL, Double.class, PsseTwoTerminalDcTransmissionLine::getSetvl, PsseTwoTerminalDcTransmissionLine::setSetvl));
        addField(fields, createNewField(STR_VSCHD, Double.class, PsseTwoTerminalDcTransmissionLine::getVschd, PsseTwoTerminalDcTransmissionLine::setVschd));
        addField(fields, createNewField(STR_VCMOD, Double.class, PsseTwoTerminalDcTransmissionLine::getVcmod, PsseTwoTerminalDcTransmissionLine::setVcmod, 0d));
        addField(fields, createNewField(STR_RCOMP, Double.class, PsseTwoTerminalDcTransmissionLine::getRcomp, PsseTwoTerminalDcTransmissionLine::setRcomp, 0d));
        addField(fields, createNewField(STR_DELTI, Double.class, PsseTwoTerminalDcTransmissionLine::getDelti, PsseTwoTerminalDcTransmissionLine::setDelti, 0d));
        addField(fields, createNewField(STR_MET, String.class, PsseTwoTerminalDcTransmissionLine::getMeter, PsseTwoTerminalDcTransmissionLine::setMeter, "I"));
        addField(fields, createNewField(STR_METER, String.class, PsseTwoTerminalDcTransmissionLine::getMeter, PsseTwoTerminalDcTransmissionLine::setMeter, "I"));
        addField(fields, createNewField(STR_DCVMIN, Double.class, PsseTwoTerminalDcTransmissionLine::getDcvmin, PsseTwoTerminalDcTransmissionLine::setDcvmin, 0d));
        addField(fields, createNewField(STR_CCCITMX, Integer.class, PsseTwoTerminalDcTransmissionLine::getCccitmx, PsseTwoTerminalDcTransmissionLine::setCccitmx, 20));
        addField(fields, createNewField(STR_CCCACC, Double.class, PsseTwoTerminalDcTransmissionLine::getCccacc, PsseTwoTerminalDcTransmissionLine::setCccacc, 1d));

        return fields;
    }

    @Override
    public void setModel(PssePowerFlowModel model) {
        super.setModel(model);
        rectifier.setModel(model);
        inverter.setModel(model);
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
