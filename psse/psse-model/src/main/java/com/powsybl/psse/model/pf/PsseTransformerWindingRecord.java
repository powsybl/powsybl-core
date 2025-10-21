/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.Optional;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseTransformerWindingRecord {

    private PsseTransformerWinding winding;
    private PsseRates windingRates;

    public static PsseTransformerWindingRecord fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseTransformerWindingRecord transformerWindingRecord = new PsseTransformerWindingRecord();
        transformerWindingRecord.winding = PsseTransformerWinding.fromRecord(rec, version, headers);
        transformerWindingRecord.windingRates = PsseRates.fromRecord(rec, version, headers);
        return transformerWindingRecord;
    }

    public static String[] toRecord(PsseTransformerWindingRecord psseTransformerWindingRecord, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            Optional<String> optionalValue = psseTransformerWindingRecord.getWinding().headerToString(headers[i]);
            if (optionalValue.isEmpty()) {
                optionalValue = psseTransformerWindingRecord.getWindingRates().headerToString(headers[i]);
                if (optionalValue.isEmpty()) {
                    throw new PsseException("Unsupported header: " + headers[i]);
                }
            }
            row[i] = optionalValue.get();
        }
        return row;
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
