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

import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseMultiTerminalDcBusx {

    public PsseMultiTerminalDcBusx() {
    }

    public PsseMultiTerminalDcBusx(String name, PsseMultiTerminalDcBus bus) {
        this.name = name;
        this.bus = bus;
    }

    private String name;
    private PsseMultiTerminalDcBus bus;

    public static PsseMultiTerminalDcBusx fromRecord(CsvRecord rec, String[] headers) {
        PsseMultiTerminalDcBusx psseMultiTerminalDcBusx = new PsseMultiTerminalDcBusx();
        psseMultiTerminalDcBusx.setName(parseStringFromRecord(rec, headers, "name"));
        psseMultiTerminalDcBusx.setBus(PsseMultiTerminalDcBus.fromRecord(rec, headers));
        return psseMultiTerminalDcBusx;
    }

    public static String[] toRecord(PsseMultiTerminalDcBusx psseMultiTerminalDcBusx, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "name" -> psseMultiTerminalDcBusx.getName();
                case "idc" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getIdc());
                case "ib" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getIb());
                case "area" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getArea());
                case "zone" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getZone());
                case "dcname" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getDcname());
                case "idc2" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getIdc2());
                case "rgrnd" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getRgrnd());
                case "owner" -> String.valueOf(psseMultiTerminalDcBusx.getBus().getOwner());
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
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
