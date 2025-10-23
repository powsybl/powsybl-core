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

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class ZCorr35First {

    private int i;
    private ZCorr35Points points = new ZCorr35Points();

    public static ZCorr35First fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        ZCorr35First zCorr35First = new ZCorr35First();
        zCorr35First.setI(parseIntFromRecord(rec, headers, "i"));
        zCorr35First.setPoints(ZCorr35Points.fromRecord(rec, version, headers));
        return zCorr35First;
    }

    public static String[] toRecord(ZCorr35First zCorr35First, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "i" -> String.valueOf(zCorr35First.getI());
                case "t1" -> String.valueOf(zCorr35First.getPoints().getT1());
                case "ref1" -> String.valueOf(zCorr35First.getPoints().getRef1());
                case "imf1" -> String.valueOf(zCorr35First.getPoints().getImf1());
                case "t2" -> String.valueOf(zCorr35First.getPoints().getT2());
                case "ref2" -> String.valueOf(zCorr35First.getPoints().getRef2());
                case "imf2" -> String.valueOf(zCorr35First.getPoints().getImf2());
                case "t3" -> String.valueOf(zCorr35First.getPoints().getT3());
                case "ref3" -> String.valueOf(zCorr35First.getPoints().getRef3());
                case "imf3" -> String.valueOf(zCorr35First.getPoints().getImf3());
                case "t4" -> String.valueOf(zCorr35First.getPoints().getT4());
                case "ref4" -> String.valueOf(zCorr35First.getPoints().getRef4());
                case "imf4" -> String.valueOf(zCorr35First.getPoints().getImf4());
                case "t5" -> String.valueOf(zCorr35First.getPoints().getT5());
                case "ref5" -> String.valueOf(zCorr35First.getPoints().getRef5());
                case "imf5" -> String.valueOf(zCorr35First.getPoints().getImf5());
                case "t6" -> String.valueOf(zCorr35First.getPoints().getT6());
                case "ref6" -> String.valueOf(zCorr35First.getPoints().getRef6());
                case "imf6" -> String.valueOf(zCorr35First.getPoints().getImf6());
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

    public ZCorr35Points getPoints() {
        return points;
    }

    public void setPoints(ZCorr35Points points) {
        this.points = points;
    }
}
