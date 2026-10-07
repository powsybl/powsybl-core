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
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_NAME;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseMultiTerminalDcConverterx {

    private static final Map<String, PsseFieldDefinition<PsseMultiTerminalDcConverterx, ?>> FIELDS = createFields();

    private String name;
    private PsseMultiTerminalDcConverter converter;

    private static Map<String, PsseFieldDefinition<PsseMultiTerminalDcConverterx, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseMultiTerminalDcConverterx, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_NAME, String.class, PsseMultiTerminalDcConverterx::getName, PsseMultiTerminalDcConverterx::setName));

        return fields;
    }

    public static PsseMultiTerminalDcConverterx fromRecord(CsvRecord rec, String[] headers) {
        PsseMultiTerminalDcConverterx multiTerminalDcConverterx = Util.fromRecord(rec.getFields(), headers, FIELDS, PsseMultiTerminalDcConverterx::new);
        multiTerminalDcConverterx.setConverter(PsseMultiTerminalDcConverter.fromRecord(rec, headers));
        return multiTerminalDcConverterx;
    }

    public static String[] toRecord(PsseMultiTerminalDcConverterx multiTerminalDcConverterx, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = Util.toRecord(multiTerminalDcConverterx, headers, FIELDS, unexpectedHeaders);
        PsseMultiTerminalDcConverter.toRecord(multiTerminalDcConverterx.getConverter(), headers, recordValues, unexpectedHeaders);
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    public PsseMultiTerminalDcConverterx() {
    }

    public PsseMultiTerminalDcConverterx(String name, PsseMultiTerminalDcConverter converter) {
        this.name = name;
        this.converter = converter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PsseMultiTerminalDcConverter getConverter() {
        return converter;
    }

    public void setConverter(PsseMultiTerminalDcConverter converter) {
        this.converter = converter;
    }
}
