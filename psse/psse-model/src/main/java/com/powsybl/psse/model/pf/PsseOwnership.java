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
import com.powsybl.psse.model.PsseVersioned;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.Optional;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseOwnership extends PsseVersioned {

    private int o1 = -1;
    private double f1 = 1;
    private int o2 = 0;
    private double f2 = 1;
    private int o3 = 0;
    private double f3 = 1;
    private int o4 = 0;
    private double f4 = 1;

    public static PsseOwnership fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseOwnership psseOwnership = new PsseOwnership();
        psseOwnership.setO1(parseIntFromRecord(rec, headers, "o1"));
        psseOwnership.setF1(parseDoubleFromRecord(rec, headers, "f1"));
        psseOwnership.setO2(parseIntFromRecord(rec, 0, headers, "o2"));
        psseOwnership.setF2(parseDoubleFromRecord(rec, 1d, headers, "f2"));
        psseOwnership.setO3(parseIntFromRecord(rec, headers, "o3"));
        psseOwnership.setF3(parseDoubleFromRecord(rec, 1d, headers, "f3"));
        psseOwnership.setO4(parseIntFromRecord(rec, headers, "o4"));
        psseOwnership.setF4(parseDoubleFromRecord(rec, 1d, headers, "f4"));
        return psseOwnership;
    }

    public static String[] toRecord(PsseOwnership psseOwnership, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            Optional<String> optionalValue = psseOwnership.headerToString(headers[i]);
            if (optionalValue.isEmpty()) {
                throw new PsseException("Unsupported header: " + headers[i]);
            }
            row[i] = optionalValue.get();
        }
        return row;
    }

    public Optional<String> headerToString(String header) {
        return switch (header) {
            case "o1" -> Optional.of(String.valueOf(getO1()));
            case "f1" -> Optional.of(String.valueOf(getF1()));
            case "o2" -> Optional.of(String.valueOf(getO2()));
            case "f2" -> Optional.of(String.valueOf(getF2()));
            case "o3" -> Optional.of(String.valueOf(getO3()));
            case "f3" -> Optional.of(String.valueOf(getF3()));
            case "o4" -> Optional.of(String.valueOf(getO4()));
            case "f4" -> Optional.of(String.valueOf(getF4()));
            default -> Optional.empty();
        };
    }

    public int getO1() {
        return o1;
    }

    public void setO1(int o1) {
        this.o1 = o1;
    }

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public int getO2() {
        return o2;
    }

    public void setO2(int o2) {
        this.o2 = o2;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public int getO3() {
        return o3;
    }

    public void setO3(int o3) {
        this.o3 = o3;
    }

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public int getO4() {
        return o4;
    }

    public void setO4(int o4) {
        this.o4 = o4;
    }

    public double getF4() {
        return f4;
    }

    public void setF4(double f4) {
        this.f4 = f4;
    }

    public PsseOwnership copy() {
        PsseOwnership copy = new PsseOwnership();
        copy.o1 = this.o1;
        copy.f1 = this.f1;
        copy.o2 = this.o2;
        copy.f2 = this.f2;
        copy.o3 = this.o3;
        copy.f3 = this.f3;
        copy.o4 = this.o4;
        copy.f4 = this.f4;
        return copy;
    }
}
