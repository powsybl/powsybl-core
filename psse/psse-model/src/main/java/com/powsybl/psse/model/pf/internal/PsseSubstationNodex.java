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
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ISUB;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseSubstationNodex {

    private static final Map<String, PsseFieldDefinition<PsseSubstationNodex, ?>> FIELDS = createFields();

    private int isub;
    private PsseSubstationNode node;

    private static Map<String, PsseFieldDefinition<PsseSubstationNodex, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseSubstationNodex, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_ISUB, Integer.class, PsseSubstationNodex::getIsub, PsseSubstationNodex::setIsub));

        return fields;
    }

    public static PsseSubstationNodex fromRecord(CsvRecord rec, String[] headers) {
        PsseSubstationNodex multiTerminalDcLinkx = Util.fromRecord(rec.getFields(), headers, FIELDS, PsseSubstationNodex::new);
        multiTerminalDcLinkx.setNode(PsseSubstationNode.fromRecord(rec, headers));
        return multiTerminalDcLinkx;
    }

    public static String[] toRecord(PsseSubstationNodex multiTerminalDcLinkx, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = Util.toRecord(multiTerminalDcLinkx, headers, FIELDS, unexpectedHeaders);
        PsseSubstationNode.toRecord(multiTerminalDcLinkx.getNode(), headers, recordValues, unexpectedHeaders);
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    public PsseSubstationNodex() {
    }

    public PsseSubstationNodex(int isub, PsseSubstationNode node) {
        this.isub = isub;
        this.node = node;
    }

    public int getIsub() {
        return isub;
    }

    public void setIsub(int isub) {
        this.isub = isub;
    }

    public PsseSubstationNode getNode() {
        return node;
    }

    public void setNode(PsseSubstationNode node) {
        this.node = node;
    }
}
