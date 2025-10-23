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

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseSubstationNode {

    private int ni;
    private String name;
    private int i;
    private int status = 1;
    private double vm = 1.0;
    private double va = 0.0;

    public static PsseSubstationNode fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseSubstationNode psseSubstationNode = new PsseSubstationNode();
        psseSubstationNode.setNi(parseIntFromRecord(rec, headers, "ni", "inode"));
        psseSubstationNode.setName(parseStringFromRecord(rec, "                                        ", headers, "name"));
        psseSubstationNode.setI(parseIntFromRecord(rec, headers, "i", "ibus"));
        psseSubstationNode.setStatus(parseIntFromRecord(rec, 1, headers, "stat", "status"));
        psseSubstationNode.setVm(parseDoubleFromRecord(rec, 1.0, headers, "vm"));
        psseSubstationNode.setVa(parseDoubleFromRecord(rec, 0.0, headers, "va"));
        return psseSubstationNode;
    }

    public static String[] toRecord(PsseSubstationNode psseSubstationNode, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "ni", "inode" -> String.valueOf(psseSubstationNode.getNi());
                case "name" -> psseSubstationNode.getName();
                case "i", "ibus" -> String.valueOf(psseSubstationNode.getI());
                case "stat", "status" -> String.valueOf(psseSubstationNode.getStatus());
                case "vm" -> String.valueOf(psseSubstationNode.getVm());
                case "va" -> String.valueOf(psseSubstationNode.getVa());
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
    }

    public int getNi() {
        return ni;
    }

    public void setNi(int ni) {
        this.ni = ni;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getVm() {
        return vm;
    }

    public void setVm(double vm) {
        this.vm = vm;
    }

    public double getVa() {
        return va;
    }

    public void setVa(double va) {
        this.va = va;
    }

    public PsseSubstationNode copy() {
        PsseSubstationNode copy = new PsseSubstationNode();
        copy.ni = this.ni;
        copy.name = this.name;
        copy.i = this.i;
        copy.status = this.status;
        copy.vm = this.vm;
        copy.va = this.va;
        return copy;
    }
}
