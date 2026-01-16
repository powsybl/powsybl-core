/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.internal;

import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.checkForUnexpectedHeader;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ISUB;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseSubstationSwitchingDevicex {

    private static final Map<String, PsseFieldDefinition<PsseSubstationSwitchingDevicex, ?>> FIELDS = createFields();

    private int isub;
    private PsseSubstationSwitchingDevice switchingDevice;

    private static Map<String, PsseFieldDefinition<PsseSubstationSwitchingDevicex, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseSubstationSwitchingDevicex, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_ISUB, Integer.class, PsseSubstationSwitchingDevicex::getIsub, PsseSubstationSwitchingDevicex::setIsub));

        return fields;
    }

    public static PsseSubstationSwitchingDevicex fromRecord(CsvRecord rec, String[] headers) {
        PsseSubstationSwitchingDevicex psseSubstationSwitchingDevicex = Util.fromRecord(rec.getFields(), headers, FIELDS, PsseSubstationSwitchingDevicex::new);
        psseSubstationSwitchingDevicex.setSwitchingDevice(PsseSubstationSwitchingDevice.fromRecord(rec, headers));
        return psseSubstationSwitchingDevicex;
    }

    public static String[] toRecord(PsseSubstationSwitchingDevicex psseSubstationSwitchingDevicex, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = Util.toRecord(psseSubstationSwitchingDevicex, headers, FIELDS, unexpectedHeaders);
        PsseSubstationSwitchingDevice.toRecord(psseSubstationSwitchingDevicex.getSwitchingDevice(), headers, recordValues, unexpectedHeaders);
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    public PsseSubstationSwitchingDevicex() {

    }

    public PsseSubstationSwitchingDevicex(int isub, PsseSubstationSwitchingDevice switchingDevice) {
        this.isub = isub;
        this.switchingDevice = switchingDevice;
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
