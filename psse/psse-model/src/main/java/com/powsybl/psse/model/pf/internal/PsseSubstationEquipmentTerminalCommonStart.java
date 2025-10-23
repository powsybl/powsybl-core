/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.internal;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import de.siegmar.fastcsv.reader.CsvRecord;

import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseSubstationEquipmentTerminalCommonStart {

    private int i;
    private int ni;
    private String type;

    public static PsseSubstationEquipmentTerminalCommonStart fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseSubstationEquipmentTerminalCommonStart psseSubstationEquipmentTerminalCommonStart = new PsseSubstationEquipmentTerminalCommonStart();
        psseSubstationEquipmentTerminalCommonStart.setI(parseIntFromRecord(rec, headers, "i"));
        psseSubstationEquipmentTerminalCommonStart.setNi(parseIntFromRecord(rec, headers, "ni"));
        psseSubstationEquipmentTerminalCommonStart.setType(parseStringFromRecord(rec, headers, "type"));
        return psseSubstationEquipmentTerminalCommonStart;
    }

    public static String[] toRecord(PsseSubstationEquipmentTerminalCommonStart psseSubstationEquipmentTerminalCommonStart, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "i" -> String.valueOf(psseSubstationEquipmentTerminalCommonStart.getI());
                case "ni" -> String.valueOf(psseSubstationEquipmentTerminalCommonStart.getNi());
                case "type" -> psseSubstationEquipmentTerminalCommonStart.getType();
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
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
