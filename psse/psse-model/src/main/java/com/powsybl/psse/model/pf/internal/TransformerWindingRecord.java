/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.internal;

import com.powsybl.psse.model.pf.PsseRates;
import com.powsybl.psse.model.pf.PsseTransformerWinding;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.checkForUnexpectedHeader;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_WDGRATE2;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_WDGRATE3;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_WDGRATE4;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_WDGRATE5;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_WDGRATE6;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_WDGRATE7;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_WDGRATE8;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_WDGRATE9;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class TransformerWindingRecord {

    private static final String[] FIELD_NAMES_32_33 = {STR_WINDV, STR_NOMV, STR_ANG, STR_RATA, STR_RATB, STR_RATC, STR_COD, STR_CONT, STR_RMA, STR_RMI, STR_VMA, STR_VMI, STR_NTP, STR_TAB, STR_CR, STR_CX, STR_CNXA};
    private static final String[] FIELD_NAMES_35 = {STR_WINDV, STR_NOMV, STR_ANG, STR_WDGRATE1, STR_WDGRATE2, STR_WDGRATE3, STR_WDGRATE4, STR_WDGRATE5, STR_WDGRATE6, STR_WDGRATE7, STR_WDGRATE8, STR_WDGRATE9, STR_WDGRATE10, STR_WDGRATE11, STR_WDGRATE12, STR_COD, STR_CONT, STR_NODE, STR_RMA, STR_RMI, STR_VMA, STR_VMI, STR_NTP, STR_TAB, STR_CR, STR_CX, STR_CNXA};

    private PsseTransformerWinding winding;
    private PsseRates windingRates;

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static TransformerWindingRecord fromRecord(CsvRecord rec, String[] headers) {
        TransformerWindingRecord transformerWindingRecord = new TransformerWindingRecord();
        transformerWindingRecord.setWinding(PsseTransformerWinding.fromRecord(rec, headers));
        transformerWindingRecord.setWindingRates(PsseRates.fromRecord(rec, headers));
        return transformerWindingRecord;
    }

    public static String[] toRecord(TransformerWindingRecord multiTerminalDcLinkx, String[] headers) {
        Set<String> unexpectedHeaders = new HashSet<>(List.of(headers));
        String[] recordValues = PsseTransformerWinding.toRecord(multiTerminalDcLinkx.getWinding(), headers, unexpectedHeaders);
        PsseRates.toRecord(multiTerminalDcLinkx.getWindingRates(), headers, recordValues, unexpectedHeaders);
        checkForUnexpectedHeader(unexpectedHeaders);
        return recordValues;
    }

    public PsseTransformerWinding getWinding() {
        return winding;
    }

    public void setWinding(PsseTransformerWinding winding) {
        this.winding = winding;
    }

    public PsseRates getWindingRates() {
        return windingRates;
    }

    public void setWindingRates(PsseRates windingRates) {
        this.windingRates = windingRates;
    }
}
