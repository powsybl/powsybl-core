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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.checkForUnexpectedHeader;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_I;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class ZCorr35First {

    private static final Map<String, PsseFieldDefinition<ZCorr35First, ?>> FIELDS = createFields();

    private int i;
    private ZCorr35Points points = new ZCorr35Points();

    private static Map<String, PsseFieldDefinition<ZCorr35First, ?>> createFields() {
        Map<String, PsseFieldDefinition<ZCorr35First, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, ZCorr35First::getI, ZCorr35First::setI));

        return fields;
    }

    public static ZCorr35First fromRecord(CsvRecord rec, String[] headers) {
        ZCorr35First zCorr35First = Util.fromRecord(rec.getFields(), headers, FIELDS, ZCorr35First::new);
        zCorr35First.setPoints(ZCorr35Points.fromRecord(rec, headers));
        return zCorr35First;
    }

    public static String[] toRecord(ZCorr35First multiTerminalDcLinkx, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = Util.toRecord(multiTerminalDcLinkx, headers, FIELDS, unexpectedHeaders);
        ZCorr35Points.toRecord(multiTerminalDcLinkx.getPoints(), headers, recordValues, unexpectedHeaders);
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public ZCorr35Points getPoints() {
        return points;
    }

    public void setPoints(ZCorr35Points points) {
        this.points = points;
    }
}
