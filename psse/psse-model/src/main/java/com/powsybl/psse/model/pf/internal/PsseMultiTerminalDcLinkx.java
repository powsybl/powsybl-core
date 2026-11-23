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
public class PsseMultiTerminalDcLinkx {

    private static final Map<String, PsseFieldDefinition<PsseMultiTerminalDcLinkx, ?>> FIELDS = createFields();

    private String name;
    private PsseMultiTerminalDcLink link;

    private static Map<String, PsseFieldDefinition<PsseMultiTerminalDcLinkx, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseMultiTerminalDcLinkx, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_NAME, String.class, PsseMultiTerminalDcLinkx::getName, PsseMultiTerminalDcLinkx::setName));

        return fields;
    }

    public static PsseMultiTerminalDcLinkx fromRecord(CsvRecord rec, String[] headers) {
        PsseMultiTerminalDcLinkx multiTerminalDcLinkx = Util.fromRecord(rec.getFields(), headers, FIELDS, PsseMultiTerminalDcLinkx::new);
        multiTerminalDcLinkx.setLink(PsseMultiTerminalDcLink.fromRecord(rec, headers));
        return multiTerminalDcLinkx;
    }

    public static String[] toRecord(PsseMultiTerminalDcLinkx multiTerminalDcLinkx, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = Util.toRecord(multiTerminalDcLinkx, headers, FIELDS, unexpectedHeaders);
        PsseMultiTerminalDcLink.toRecord(multiTerminalDcLinkx.getLink(), headers, recordValues, unexpectedHeaders);
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    public PsseMultiTerminalDcLinkx() {
    }

    public PsseMultiTerminalDcLinkx(String name, PsseMultiTerminalDcLink link) {
        this.name = name;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PsseMultiTerminalDcLink getLink() {
        return link;
    }

    public void setLink(PsseMultiTerminalDcLink link) {
        this.link = link;
    }
}
