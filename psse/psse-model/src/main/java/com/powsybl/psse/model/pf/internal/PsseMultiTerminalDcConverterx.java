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
public class PsseMultiTerminalDcConverterx {

    public PsseMultiTerminalDcConverterx() {
    }

    public PsseMultiTerminalDcConverterx(String name, PsseMultiTerminalDcConverter converter) {
        this.name = name;
        this.converter = converter;
    }

    private String name;
    private PsseMultiTerminalDcConverter converter;

    public static PsseMultiTerminalDcConverterx fromRecord(CsvRecord rec, String[] headers) {
        PsseMultiTerminalDcConverterx psseMultiTerminalDcConverterx = new PsseMultiTerminalDcConverterx();
        psseMultiTerminalDcConverterx.setName(parseStringFromRecord(rec, headers, "name"));
        psseMultiTerminalDcConverterx.setConverter(PsseMultiTerminalDcConverter.fromRecord(rec, headers));
        return psseMultiTerminalDcConverterx;
    }

    public static String[] toRecord(PsseMultiTerminalDcConverterx psseMultiTerminalDcConverterx, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "name" -> psseMultiTerminalDcConverterx.getName();
                case "ib" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getIb());
                case "n" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getN());
                case "angmx" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getAngmx());
                case "angmn" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getAngmn());
                case "rc" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getRc());
                case "xc" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getXc());
                case "ebas" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getEbas());
                case "tr" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getTr());
                case "tap" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getTap());
                case "tpmx" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getTpmx());
                case "tpmn" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getTpmn());
                case "tstp" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getTstp());
                case "setvl" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getSetvl());
                case "dcpf" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getDcpf());
                case "marg" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getMarg());
                case "cnvcod" -> String.valueOf(psseMultiTerminalDcConverterx.getConverter().getCnvcod());
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

    public PsseMultiTerminalDcConverter getConverter() {
        return converter;
    }

    public void setConverter(PsseMultiTerminalDcConverter converter) {
        this.converter = converter;
    }
}
