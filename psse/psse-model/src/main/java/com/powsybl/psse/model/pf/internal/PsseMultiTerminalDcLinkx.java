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
public class PsseMultiTerminalDcLinkx {

    public PsseMultiTerminalDcLinkx() {
    }

    public PsseMultiTerminalDcLinkx(String name, PsseMultiTerminalDcLink link) {
        this.name = name;
        this.link = link;
    }

    private String name;
    private PsseMultiTerminalDcLink link;

    public static PsseMultiTerminalDcLinkx fromRecord(CsvRecord rec, String[] headers) {
        PsseMultiTerminalDcLinkx psseMultiTerminalDcLinkx = new PsseMultiTerminalDcLinkx();
        psseMultiTerminalDcLinkx.setName(parseStringFromRecord(rec, headers, "name"));
        psseMultiTerminalDcLinkx.setLink(PsseMultiTerminalDcLink.fromRecord(rec, headers));
        return psseMultiTerminalDcLinkx;
    }

    public static String[] toRecord(PsseMultiTerminalDcLinkx psseMultiTerminalDcLinkx, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "name" -> psseMultiTerminalDcLinkx.getName();
                case "idc" -> String.valueOf(psseMultiTerminalDcLinkx.getLink().getIdc());
                case "jdc" -> String.valueOf(psseMultiTerminalDcLinkx.getLink().getJdc());
                case "dcckt" -> String.valueOf(psseMultiTerminalDcLinkx.getLink().getDcckt());
                case "met" -> String.valueOf(psseMultiTerminalDcLinkx.getLink().getMet());
                case "rdc" -> String.valueOf(psseMultiTerminalDcLinkx.getLink().getRdc());
                case "ldc" -> String.valueOf(psseMultiTerminalDcLinkx.getLink().getLdc());
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

    public PsseMultiTerminalDcLink getLink() {
        return link;
    }

    public void setLink(PsseMultiTerminalDcLink link) {
        this.link = link;
    }
}
