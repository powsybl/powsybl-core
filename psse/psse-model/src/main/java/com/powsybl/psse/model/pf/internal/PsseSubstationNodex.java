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
public class PsseSubstationNodex {

    public PsseSubstationNodex() {
    }

    public PsseSubstationNodex(int isub, PsseSubstationNode node) {
        this.isub = isub;
        this.node = node;
    }

    private int isub;
    private PsseSubstationNode node;

    public static PsseSubstationNodex fromRecord(CsvRecord rec, String[] headers) {
        PsseSubstationNodex psseSubstationNodex = new PsseSubstationNodex();
        psseSubstationNodex.setIsub(parseIntFromRecord(rec, headers, "isub"));
        psseSubstationNodex.setNode(PsseSubstationNode.fromRecord(rec, headers));
        return psseSubstationNodex;
    }

    public static String[] toRecord(PsseSubstationNodex psseSubstationNodex, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "isub" -> String.valueOf(psseSubstationNodex.getIsub());
                case "ni", "inode" -> String.valueOf(psseSubstationNodex.getNode().getNi());
                case "name" -> psseSubstationNodex.getNode().getName();
                case "i", "ibus" -> String.valueOf(psseSubstationNodex.getNode().getI());
                case "stat", "status" -> String.valueOf(psseSubstationNodex.getNode().getStatus());
                case "vm" -> String.valueOf(psseSubstationNodex.getNode().getVm());
                case "va" -> String.valueOf(psseSubstationNodex.getNode().getVa());
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

    public PsseSubstationNode getNode() {
        return node;
    }

    public void setNode(PsseSubstationNode node) {
        this.node = node;
    }
}
