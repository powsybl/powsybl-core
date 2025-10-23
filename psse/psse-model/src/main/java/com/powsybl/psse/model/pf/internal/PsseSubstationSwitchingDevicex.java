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
public class PsseSubstationSwitchingDevicex {

    public PsseSubstationSwitchingDevicex() {

    }

    public PsseSubstationSwitchingDevicex(int isub, PsseSubstationSwitchingDevice switchingDevice) {
        this.isub = isub;
        this.switchingDevice = switchingDevice;
    }

    private int isub;
    private PsseSubstationSwitchingDevice switchingDevice;

    public static PsseSubstationSwitchingDevicex fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseSubstationSwitchingDevicex psseSubstationSwitchingDevicex = new PsseSubstationSwitchingDevicex();
        psseSubstationSwitchingDevicex.setIsub(parseIntFromRecord(rec, headers, "isub"));
        psseSubstationSwitchingDevicex.setSwitchingDevice(PsseSubstationSwitchingDevice.fromRecord(rec, version, headers));
        return psseSubstationSwitchingDevicex;
    }

    public static String[] toRecord(PsseSubstationSwitchingDevicex psseSubstationSwitchingDevicex, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "isub" -> String.valueOf(psseSubstationSwitchingDevicex.getIsub());
                case "ni", "inode" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getNi());
                case "nj", "jnode" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getNj());
                case "ckt", "swdid" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getCkt());
                case "name" -> psseSubstationSwitchingDevicex.getSwitchingDevice().getName();
                case "type" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getType());
                case "stat", "status" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getStatus());
                case "nstat" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getNstat());
                case "x", "xpu" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getX());
                case "rate1" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getRate1());
                case "rate2" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getRate2());
                case "rate3" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getRate3());
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

    public PsseSubstationSwitchingDevice getSwitchingDevice() {
        return switchingDevice;
    }

    public void setSwitchingDevice(PsseSubstationSwitchingDevice switchingDevice) {
        this.switchingDevice = switchingDevice;
    }
}
