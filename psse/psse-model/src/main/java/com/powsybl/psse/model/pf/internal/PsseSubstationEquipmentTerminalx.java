/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.internal;

import com.powsybl.psse.model.PsseException;
import de.siegmar.fastcsv.reader.CsvRecord;

import static com.powsybl.psse.model.io.Util.parseIntFromRecord;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseSubstationEquipmentTerminalx {

    public PsseSubstationEquipmentTerminalx() {

    }

    public PsseSubstationEquipmentTerminalx(int isub, PsseSubstationEquipmentTerminal equipmentTerminal) {
        this.isub = isub;
        this.equipmentTerminal = equipmentTerminal;
    }

    private int isub;
    private PsseSubstationEquipmentTerminal equipmentTerminal;

    public static PsseSubstationEquipmentTerminalx fromRecord(CsvRecord rec, String[] headers) {
        PsseSubstationEquipmentTerminalx psseSubstationEquipmentTerminalx = new PsseSubstationEquipmentTerminalx();
        psseSubstationEquipmentTerminalx.setIsub(parseIntFromRecord(rec, headers, "isub"));
        psseSubstationEquipmentTerminalx.setEquipmentTerminal(PsseSubstationEquipmentTerminal.fromRecord(rec, headers));
        return psseSubstationEquipmentTerminalx;
    }

    public static String[] toRecord(PsseSubstationEquipmentTerminalx psseSubstationEquipmentTerminalx, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "isub" -> String.valueOf(psseSubstationEquipmentTerminalx.getIsub());
                case "i", "ibus" -> String.valueOf(psseSubstationEquipmentTerminalx.getEquipmentTerminal().getI());
                case "ni", "inode" -> String.valueOf(psseSubstationEquipmentTerminalx.getEquipmentTerminal().getNi());
                case "type" -> psseSubstationEquipmentTerminalx.getEquipmentTerminal().getType();
                case "id", "eqid" -> psseSubstationEquipmentTerminalx.getEquipmentTerminal().getId();
                case "j", "jbus" -> String.valueOf(psseSubstationEquipmentTerminalx.getEquipmentTerminal().getJ());
                case "k", "kbus" -> String.valueOf(psseSubstationEquipmentTerminalx.getEquipmentTerminal().getK());
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
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
