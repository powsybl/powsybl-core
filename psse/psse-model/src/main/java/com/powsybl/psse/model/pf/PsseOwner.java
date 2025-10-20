/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

import static com.powsybl.psse.model.io.Util.defaultIfEmpty;
import static com.powsybl.psse.model.io.Util.getFieldFromMultiplePotentialHeaders;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseOwner {

    private int i;
    private String owname;

    public static PsseOwner fromRecord(NamedCsvRecord rec, PsseVersion version) {
        PsseOwner psseOwner = new PsseOwner();
        psseOwner.setI(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "i", "iowner")));
        psseOwner.setOwname(defaultIfEmpty(getFieldFromMultiplePotentialHeaders(rec, "owname", "owner"), "            "));
        return psseOwner;
    }

    public static String[] toRecord(PsseOwner psseOwner, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "i", "ibus" -> String.valueOf(psseOwner.getI());
                case "owname", "owner" -> psseOwner.getOwname();
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

    public String getOwname() {
        return owname;
    }

    public void setOwname(String owname) {
        this.owname = owname;
    }
}
