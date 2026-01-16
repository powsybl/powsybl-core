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
public class PsseSubstationEquipmentTerminalx {

    private static final Map<String, PsseFieldDefinition<PsseSubstationEquipmentTerminalx, ?>> FIELDS = createFields();

    private int isub;
    private PsseSubstationEquipmentTerminal equipmentTerminal;

    private static Map<String, PsseFieldDefinition<PsseSubstationEquipmentTerminalx, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseSubstationEquipmentTerminalx, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_ISUB, Integer.class, PsseSubstationEquipmentTerminalx::getIsub, PsseSubstationEquipmentTerminalx::setIsub));

        return fields;
    }

    public static PsseSubstationEquipmentTerminalx fromRecord(CsvRecord rec, String[] headers) {
        PsseSubstationEquipmentTerminalx psseSubstationEquipmentTerminalx = Util.fromRecord(rec.getFields(), headers, FIELDS, PsseSubstationEquipmentTerminalx::new);
        psseSubstationEquipmentTerminalx.setEquipmentTerminal(PsseSubstationEquipmentTerminal.fromRecord(rec, headers));
        return psseSubstationEquipmentTerminalx;
    }

    public static String[] toRecord(PsseSubstationEquipmentTerminalx psseSubstationEquipmentTerminalx, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = Util.toRecord(psseSubstationEquipmentTerminalx, headers, FIELDS, unexpectedHeaders);
        PsseSubstationEquipmentTerminal.toRecord(psseSubstationEquipmentTerminalx.getEquipmentTerminal(), headers, recordValues, unexpectedHeaders);
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    public PsseSubstationEquipmentTerminalx() {

    }

    public PsseSubstationEquipmentTerminalx(int isub, PsseSubstationEquipmentTerminal equipmentTerminal) {
        this.isub = isub;
        this.equipmentTerminal = equipmentTerminal;
    }

    public int getIsub() {
        return isub;
    }

    public void setIsub(int isub) {
        this.isub = isub;
    }

    public PsseSubstationEquipmentTerminal getEquipmentTerminal() {
        return equipmentTerminal;
    }

    public void setEquipmentTerminal(PsseSubstationEquipmentTerminal equipmentTerminal) {
        this.equipmentTerminal = equipmentTerminal;
    }
}
