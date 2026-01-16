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

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_I;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_NI;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_TYPE;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseSubstationEquipmentTerminalCommonStart {

    private static final Map<String, PsseFieldDefinition<PsseSubstationEquipmentTerminalCommonStart, ?>> FIELDS = createFields();

    private int i;
    private int ni;
    private String type;

    private static Map<String, PsseFieldDefinition<PsseSubstationEquipmentTerminalCommonStart, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseSubstationEquipmentTerminalCommonStart, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseSubstationEquipmentTerminalCommonStart::getI, PsseSubstationEquipmentTerminalCommonStart::setI));
        addField(fields, createNewField(STR_NI, Integer.class, PsseSubstationEquipmentTerminalCommonStart::getNi, PsseSubstationEquipmentTerminalCommonStart::setNi));
        addField(fields, createNewField(STR_TYPE, String.class, PsseSubstationEquipmentTerminalCommonStart::getType, PsseSubstationEquipmentTerminalCommonStart::setType));

        return fields;
    }

    public static PsseSubstationEquipmentTerminalCommonStart fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseSubstationEquipmentTerminalCommonStart::new);
    }

    public static String[] toRecord(PsseSubstationEquipmentTerminalCommonStart psseSubstationEquipmentTerminalCommonStart, String[] headers) {
        return Util.toRecord(psseSubstationEquipmentTerminalCommonStart, headers, FIELDS);
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getNi() {
        return ni;
    }

    public void setNi(int ni) {
        this.ni = ni;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
