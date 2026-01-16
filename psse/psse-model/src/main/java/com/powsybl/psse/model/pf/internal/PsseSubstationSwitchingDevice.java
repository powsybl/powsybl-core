/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.internal;

import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.concatStringArrays;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseSubstationSwitchingDevice {

    private static final Map<String, PsseFieldDefinition<PsseSubstationSwitchingDevice, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_COMMON_START = {"name", "type"};
    private static final String[] FIELD_NAMES_COMMON_NSTAT = {STR_NSTAT};
    private static final String[] FIELD_NAMES_COMMON_END = {"rate1", "rate2", "rate3"};
    private static final String[] FIELD_NAMES_START_RAW = {"ni", "nj", "ckt"};
    private static final String[] FIELD_NAMES_START_RAWX = {"isub", STR_INODE, "jnode", "swdid"};
    private static final String[] FIELD_NAMES_MIDDLE_RAW = {STR_STATUS};
    private static final String[] FIELD_NAMES_MIDDLE_RAWX = {STR_STAT};
    private static final String[] FIELD_NAMES_END_RAW = {"x"};
    private static final String[] FIELD_NAMES_END_RAWX = {"xpu"};
    private static final String[] FIELD_NAMES_RAW = concatStringArrays(FIELD_NAMES_START_RAW, FIELD_NAMES_COMMON_START,
        FIELD_NAMES_MIDDLE_RAW, FIELD_NAMES_COMMON_NSTAT, FIELD_NAMES_END_RAW, FIELD_NAMES_COMMON_END);
    private static final String[] FIELD_NAMES_RAWX = concatStringArrays(FIELD_NAMES_START_RAWX, FIELD_NAMES_COMMON_START,
        FIELD_NAMES_MIDDLE_RAWX, FIELD_NAMES_COMMON_NSTAT, FIELD_NAMES_END_RAWX, FIELD_NAMES_COMMON_END);

    private int ni;
    private int nj = defaultIntegerFor(STR_NJ, FIELDS);
    private String ckt;
    private String name;
    private int type = defaultIntegerFor(STR_TYPE, FIELDS);
    private int status = defaultIntegerFor(STR_STATUS, FIELDS);
    private int nstat = defaultIntegerFor(STR_NSTAT, FIELDS);
    private double x = defaultDoubleFor(STR_X, FIELDS);
    private double rate1 = defaultDoubleFor(STR_RATE1, FIELDS);
    private double rate2 = defaultDoubleFor(STR_RATE2, FIELDS);
    private double rate3 = defaultDoubleFor(STR_RATE3, FIELDS);

    public static String[] getFieldNamesRaw() {
        return FIELD_NAMES_RAW;
    }

    public static String[] getFieldNamesRawx() {
        return FIELD_NAMES_RAWX;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseSubstationSwitchingDevice fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseSubstationSwitchingDevice::new);
    }

    public static void toRecord(PsseSubstationSwitchingDevice psseSubstationSwitchingDevice, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        Util.toRecord(psseSubstationSwitchingDevice, headers, FIELDS, row, unexpectedHeaders);
    }

    public static String[] toRecord(PsseSubstationSwitchingDevice psseSubstationSwitchingDevice, String[] headers) {
        return Util.toRecord(psseSubstationSwitchingDevice, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseSubstationSwitchingDevice, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseSubstationSwitchingDevice, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_NI, Integer.class, PsseSubstationSwitchingDevice::getNi, PsseSubstationSwitchingDevice::setNi));
        addField(fields, createNewField(STR_INODE, Integer.class, PsseSubstationSwitchingDevice::getNi, PsseSubstationSwitchingDevice::setNi));
        addField(fields, createNewField(STR_NJ, Integer.class, PsseSubstationSwitchingDevice::getNj, PsseSubstationSwitchingDevice::setNj, 0));
        addField(fields, createNewField(STR_JNODE, Integer.class, PsseSubstationSwitchingDevice::getNj, PsseSubstationSwitchingDevice::setNj, 0));
        addField(fields, createNewField(STR_CKT, String.class, PsseSubstationSwitchingDevice::getCkt, PsseSubstationSwitchingDevice::setCkt, "1 "));
        addField(fields, createNewField(STR_SWDID, String.class, PsseSubstationSwitchingDevice::getCkt, PsseSubstationSwitchingDevice::setCkt, "1 "));
        addField(fields, createNewField(STR_NAME, String.class, PsseSubstationSwitchingDevice::getName, PsseSubstationSwitchingDevice::setName, STR_SPACES_40));
        addField(fields, createNewField(STR_TYPE, Integer.class, PsseSubstationSwitchingDevice::getType, PsseSubstationSwitchingDevice::setType, 1));
        addField(fields, createNewField(STR_STAT, Integer.class, PsseSubstationSwitchingDevice::getStatus, PsseSubstationSwitchingDevice::setStatus, 1));
        addField(fields, createNewField(STR_STATUS, Integer.class, PsseSubstationSwitchingDevice::getStatus, PsseSubstationSwitchingDevice::setStatus, 1));
        addField(fields, createNewField(STR_NSTAT, Integer.class, PsseSubstationSwitchingDevice::getNstat, PsseSubstationSwitchingDevice::setNstat, 1));
        addField(fields, createNewField(STR_X, Double.class, PsseSubstationSwitchingDevice::getX, PsseSubstationSwitchingDevice::setX, 0.0001));
        addField(fields, createNewField(STR_XPU, Double.class, PsseSubstationSwitchingDevice::getX, PsseSubstationSwitchingDevice::setX, 0.0001));
        addField(fields, createNewField(STR_RATE1, Double.class, PsseSubstationSwitchingDevice::getRate1, PsseSubstationSwitchingDevice::setRate1, 0.0));
        addField(fields, createNewField(STR_RATE2, Double.class, PsseSubstationSwitchingDevice::getRate2, PsseSubstationSwitchingDevice::setRate2, 0.0));
        addField(fields, createNewField(STR_RATE3, Double.class, PsseSubstationSwitchingDevice::getRate3, PsseSubstationSwitchingDevice::setRate3, 0.0));

        // This header seems to be expected, but no variable seems consistent with it
        addField(fields, createNewField(STR_RSETNAM, Double.class, device -> null, (device, name) -> { }));

        return fields;
    }

    public int getNi() {
        return ni;
    }

    public void setNi(int ni) {
        this.ni = ni;
    }

    public int getNj() {
        return nj;
    }

    public void setNj(int nj) {
        this.nj = nj;
    }

    public String getCkt() {
        return ckt;
    }

    public void setCkt(String ckt) {
        this.ckt = ckt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getNstat() {
        return nstat;
    }

    public void setNstat(int nstat) {
        this.nstat = nstat;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
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

    public PsseSubstationSwitchingDevice copy() {
        PsseSubstationSwitchingDevice copy = new PsseSubstationSwitchingDevice();
        copy.ni = this.ni;
        copy.nj = this.nj;
        copy.ckt = this.ckt;
        copy.name = this.name;
        copy.type = this.type;
        copy.status = this.status;
        copy.nstat = this.nstat;
        copy.x = this.x;
        copy.rate1 = this.rate1;
        copy.rate2 = this.rate2;
        copy.rate3 = this.rate3;
        return copy;
    }
}
