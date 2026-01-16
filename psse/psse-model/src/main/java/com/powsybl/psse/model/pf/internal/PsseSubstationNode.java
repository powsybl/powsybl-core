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
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ISUB;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseSubstationNode {

    private static final Map<String, PsseFieldDefinition<PsseSubstationNode, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_NAME = {STR_NAME};
    private static final String[] FIELD_NAMES_COMMON = {STR_VM, STR_VA};
    private static final String[] FIELD_NAMES_START_RAW = {STR_NI};
    private static final String[] FIELD_NAMES_START_RAWX = {STR_ISUB, STR_INODE};
    private static final String[] FIELD_NAMES_MIDDLE_RAW = {STR_I, STR_STATUS};
    private static final String[] FIELD_NAMES_MIDDLE_RAWX = {STR_IBUS, STR_STAT};
    private static final String[] FIELD_NAMES_RAW = concatStringArrays(FIELD_NAMES_START_RAW, FIELD_NAMES_NAME,
        FIELD_NAMES_MIDDLE_RAW, FIELD_NAMES_COMMON);
    private static final String[] FIELD_NAMES_RAWX = concatStringArrays(FIELD_NAMES_START_RAWX, FIELD_NAMES_NAME,
        FIELD_NAMES_MIDDLE_RAWX, FIELD_NAMES_COMMON);

    private int ni;
    private String name;
    private int i;
    private int status = defaultIntegerFor(STR_STATUS, FIELDS);
    private double vm = defaultDoubleFor(STR_VM, FIELDS);
    private double va = defaultDoubleFor(STR_VA, FIELDS);

    public static String[] getFieldNamesRaw() {
        return FIELD_NAMES_RAW;
    }

    public static String[] getFieldNamesRawx() {
        return FIELD_NAMES_RAWX;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseSubstationNode fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseSubstationNode::new);
    }

    public static void toRecord(PsseSubstationNode multiTerminalDcLink, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        Util.toRecord(multiTerminalDcLink, headers, FIELDS, row, unexpectedHeaders);
    }

    public static String[] toRecord(PsseSubstationNode multiTerminalDcLink, String[] headers) {
        return Util.toRecord(multiTerminalDcLink, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseSubstationNode, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseSubstationNode, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_NI, Integer.class, PsseSubstationNode::getNi, PsseSubstationNode::setNi));
        addField(fields, createNewField(STR_INODE, Integer.class, PsseSubstationNode::getNi, PsseSubstationNode::setNi));
        addField(fields, createNewField(STR_NAME, String.class, PsseSubstationNode::getName, PsseSubstationNode::setName, STR_SPACES_40));
        addField(fields, createNewField(STR_I, Integer.class, PsseSubstationNode::getI, PsseSubstationNode::setI));
        addField(fields, createNewField(STR_IBUS, Integer.class, PsseSubstationNode::getI, PsseSubstationNode::setI));
        addField(fields, createNewField(STR_STAT, Integer.class, PsseSubstationNode::getStatus, PsseSubstationNode::setStatus, 1));
        addField(fields, createNewField(STR_STATUS, Integer.class, PsseSubstationNode::getStatus, PsseSubstationNode::setStatus, 1));
        addField(fields, createNewField(STR_VM, Double.class, PsseSubstationNode::getVm, PsseSubstationNode::setVm, 1.0));
        addField(fields, createNewField(STR_VA, Double.class, PsseSubstationNode::getVa, PsseSubstationNode::setVa, 0.0));

        return fields;
    }

    public int getNi() {
        return ni;
    }

    public void setNi(int ni) {
        this.ni = ni;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getVm() {
        return vm;
    }

    public void setVm(double vm) {
        this.vm = vm;
    }

    public double getVa() {
        return va;
    }

    public void setVa(double va) {
        this.va = va;
    }

    public PsseSubstationNode copy() {
        PsseSubstationNode copy = new PsseSubstationNode();
        copy.ni = this.ni;
        copy.name = this.name;
        copy.i = this.i;
        copy.status = this.status;
        copy.vm = this.vm;
        copy.va = this.va;
        return copy;
    }
}
