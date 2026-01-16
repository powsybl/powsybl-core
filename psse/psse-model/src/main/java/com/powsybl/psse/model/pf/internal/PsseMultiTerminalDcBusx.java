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
public class PsseMultiTerminalDcBusx {

    private static final Map<String, PsseFieldDefinition<PsseMultiTerminalDcBusx, ?>> FIELDS = createFields();

    private String name;
    private PsseMultiTerminalDcBus bus;

    private static Map<String, PsseFieldDefinition<PsseMultiTerminalDcBusx, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseMultiTerminalDcBusx, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_NAME, String.class, PsseMultiTerminalDcBusx::getName, PsseMultiTerminalDcBusx::setName));

        return fields;
    }

    public static PsseMultiTerminalDcBusx fromRecord(CsvRecord rec, String[] headers) {
        PsseMultiTerminalDcBusx multiTerminalDcBusx = Util.fromRecord(rec.getFields(), headers, FIELDS, PsseMultiTerminalDcBusx::new);
        multiTerminalDcBusx.setBus(PsseMultiTerminalDcBus.fromRecord(rec, headers));
        return multiTerminalDcBusx;
    }

    public static String[] toRecord(PsseMultiTerminalDcBusx multiTerminalDcBusx, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = Util.toRecord(multiTerminalDcBusx, headers, FIELDS, unexpectedHeaders);
        PsseMultiTerminalDcBus.toRecord(multiTerminalDcBusx.getBus(), headers, recordValues, unexpectedHeaders);
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    public PsseMultiTerminalDcBusx() {
    }

    public PsseMultiTerminalDcBusx(String name, PsseMultiTerminalDcBus bus) {
        this.name = name;
        this.bus = bus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PsseMultiTerminalDcBus getBus() {
        return bus;
    }

    public void setBus(PsseMultiTerminalDcBus bus) {
        this.bus = bus;
    }
}
