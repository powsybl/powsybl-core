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
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseSubstationEquipmentTerminal {

    private int i;
    private int ni;
    private String type;
    private String id;
    private int j = 0;
    private int k = 0;

    public static PsseSubstationEquipmentTerminal fromRecord(CsvRecord rec, String[] headers) {
        PsseSubstationEquipmentTerminal psseSubstationEquipmentTerminal = new PsseSubstationEquipmentTerminal();
        psseSubstationEquipmentTerminal.setI(parseIntFromRecord(rec, headers, "i", "ibus"));
        psseSubstationEquipmentTerminal.setNi(parseIntFromRecord(rec, headers, "ni", "inode"));
        psseSubstationEquipmentTerminal.setType(parseStringFromRecord(rec, headers, "type"));
        psseSubstationEquipmentTerminal.setId(parseStringFromRecord(rec, "1 ", headers, "id", "eqid"));
        psseSubstationEquipmentTerminal.setJ(parseIntFromRecord(rec, 0, headers, "j", "jbus"));
        psseSubstationEquipmentTerminal.setK(parseIntFromRecord(rec, 0, headers, "k", "kbus"));
        return psseSubstationEquipmentTerminal;
    }

    public static String[] toRecord(PsseSubstationEquipmentTerminal psseSubstationEquipmentTerminal, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "i", "ibus" -> String.valueOf(psseSubstationEquipmentTerminal.getI());
                case "ni", "inode" -> String.valueOf(psseSubstationEquipmentTerminal.getNi());
                case "type" -> psseSubstationEquipmentTerminal.getType();
                case "id", "eqid" -> psseSubstationEquipmentTerminal.getId();
                case "j", "jbus" -> String.valueOf(psseSubstationEquipmentTerminal.getJ());
                case "k", "kbus" -> String.valueOf(psseSubstationEquipmentTerminal.getK());
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public PsseSubstationEquipmentTerminal copy() {
        PsseSubstationEquipmentTerminal copy = new PsseSubstationEquipmentTerminal();
        copy.i = this.i;
        copy.ni = this.ni;
        copy.type = this.type;
        copy.id = this.id;
        copy.j = this.j;
        copy.k = this.k;
        return copy;
    }
}
