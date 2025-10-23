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
import de.siegmar.fastcsv.reader.CsvRecord;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseFixedShunt {

    private static final String STRING_I = "i";
    private static final String STRING_ID = "id";
    private static final String STRING_STATUS = "status";

    private int i;
    private String id;
    private int status = 1;
    private double gl = 0;
    private double bl = 0;

    public static PsseFixedShunt fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseFixedShunt psseFixedShunt = new PsseFixedShunt();
        psseFixedShunt.setI(parseIntFromRecord(rec, headers, STRING_I, "ibus"));
        psseFixedShunt.setId(parseStringFromRecord(rec, "1", headers, STRING_ID, "shntid"));
        psseFixedShunt.setStatus(parseIntFromRecord(rec, 1, headers, STRING_STATUS, "stat"));
        psseFixedShunt.setGl(parseDoubleFromRecord(rec, 0d, headers, "gl"));
        psseFixedShunt.setBl(parseDoubleFromRecord(rec, 0d, headers, "bl"));
        return psseFixedShunt;
    }

    public static String[] toRecord(PsseFixedShunt psseFixedShunt, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case STRING_I, "ibus" -> String.valueOf(psseFixedShunt.getI());
                case STRING_ID, "shntid" -> psseFixedShunt.getId();
                case STRING_STATUS, "stat" -> String.valueOf(psseFixedShunt.getStatus());
                case "gl" -> String.valueOf(psseFixedShunt.getGl());
                case "bl" -> String.valueOf(psseFixedShunt.getBl());
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getGl() {
        return gl;
    }

    public void setGl(double gl) {
        this.gl = gl;
    }

    public double getBl() {
        return bl;
    }

    public void setBl(double bl) {
        this.bl = bl;
    }

    public PsseFixedShunt copy() {
        PsseFixedShunt copy = new PsseFixedShunt();
        copy.i = this.i;
        copy.id = this.id;
        copy.status = this.status;
        copy.gl = this.gl;
        copy.bl = this.bl;
        return copy;
    }
}
