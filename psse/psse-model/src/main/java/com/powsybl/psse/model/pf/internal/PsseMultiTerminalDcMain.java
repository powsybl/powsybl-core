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
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_MDC;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_NAME;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_NCONV;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_NDCBS;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_NDCLN;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_VCMOD;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_VCONV;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_VCONVN;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseMultiTerminalDcMain {

    private static final Map<String, PsseFieldDefinition<PsseMultiTerminalDcMain, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES = {STR_NAME, STR_NCONV, STR_NDCBS, STR_NDCLN, STR_MDC, STR_VCONV, STR_VCMOD, STR_VCONVN};

    private String name;
    private int nconv;
    private int ndcbs;
    private int ndcln;
    private int mdc = defaultIntegerFor(STR_MDC, FIELDS);
    private int vconv;
    private double vcmod = defaultDoubleFor(STR_VCMOD, FIELDS);
    private int vconvn = defaultIntegerFor(STR_VCONVN, FIELDS);

    public static String[] getFieldNames() {
        return FIELD_NAMES;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseMultiTerminalDcMain fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseMultiTerminalDcMain::new);
    }

    public static void toRecord(PsseMultiTerminalDcMain multiTerminalDcMain, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        Util.toRecord(multiTerminalDcMain, headers, FIELDS, row, unexpectedHeaders);
    }

    public static String[] toRecord(PsseMultiTerminalDcMain multiTerminalDcMain, String[] headers) {
        return Util.toRecord(multiTerminalDcMain, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseMultiTerminalDcMain, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseMultiTerminalDcMain, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_NAME, String.class, PsseMultiTerminalDcMain::getName, PsseMultiTerminalDcMain::setName));
        addField(fields, createNewField(STR_NCONV, Integer.class, PsseMultiTerminalDcMain::getNconv, PsseMultiTerminalDcMain::setNconv));
        addField(fields, createNewField(STR_NDCBS, Integer.class, PsseMultiTerminalDcMain::getNdcbs, PsseMultiTerminalDcMain::setNdcbs));
        addField(fields, createNewField(STR_NDCLN, Integer.class, PsseMultiTerminalDcMain::getNdcln, PsseMultiTerminalDcMain::setNdcln));
        addField(fields, createNewField(STR_MDC, Integer.class, PsseMultiTerminalDcMain::getMdc, PsseMultiTerminalDcMain::setMdc, 0));
        addField(fields, createNewField(STR_VCONV, Integer.class, PsseMultiTerminalDcMain::getVconv, PsseMultiTerminalDcMain::setVconv));
        addField(fields, createNewField(STR_VCMOD, Double.class, PsseMultiTerminalDcMain::getVcmod, PsseMultiTerminalDcMain::setVcmod, 0.0));
        addField(fields, createNewField(STR_VCONVN, Integer.class, PsseMultiTerminalDcMain::getVconvn, PsseMultiTerminalDcMain::setVconvn, 0));

        return fields;
    }

    public String getName() {
        return name;
    }

    public int getNconv() {
        return nconv;
    }

    public int getNdcbs() {
        return ndcbs;
    }

    public int getNdcln() {
        return ndcln;
    }

    public int getMdc() {
        return mdc;
    }

    public int getVconv() {
        return vconv;
    }

    public double getVcmod() {
        return vcmod;
    }

    public int getVconvn() {
        return vconvn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNconv(int nconv) {
        this.nconv = nconv;
    }

    public void setNdcbs(int ndcbs) {
        this.ndcbs = ndcbs;
    }

    public void setNdcln(int ndcln) {
        this.ndcln = ndcln;
    }

    public void setMdc(int mdc) {
        this.mdc = mdc;
    }

    public void setVconv(int vconv) {
        this.vconv = vconv;
    }

    public void setVcmod(double vcmod) {
        this.vcmod = vcmod;
    }

    public void setVconvn(int vconvn) {
        this.vconvn = vconvn;
    }
}
