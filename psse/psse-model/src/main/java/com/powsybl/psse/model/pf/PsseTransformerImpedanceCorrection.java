/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.powsybl.psse.model.io.Util.parseIntFromRecord;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseTransformerImpedanceCorrection extends PsseVersioned {

    private final int i;
    private final List<PsseTransformerImpedanceCorrectionPoint> points;

    public static PsseTransformerImpedanceCorrection fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        int i = parseIntFromRecord(rec, headers, "i");
        PsseTransformerImpedanceCorrection psseTransformerImpedanceCorrection = new PsseTransformerImpedanceCorrection(i);
        int expextedPoints = version.getMajorNumber() <= 33 ? (headers.length - 1) / 2 : (headers.length - 1) / 3;
        for (int j = 0; j < expextedPoints; j++) {
            PsseTransformerImpedanceCorrectionPoint point = PsseTransformerImpedanceCorrectionPoint.fromRecord(rec, version, headers, String.valueOf(j + 1));
            if (point.isNotDefault()) {
                psseTransformerImpedanceCorrection.getPoints().add(point);
            }
        }
        return psseTransformerImpedanceCorrection;
    }

    public static String[] toRecord(PsseTransformerImpedanceCorrection psseTransformerImpedanceCorrection, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals("i")) {
                row[i] = String.valueOf(psseTransformerImpedanceCorrection.getI());
            } else {
                int j = Integer.parseInt(headers[i].substring(1));
                Optional<String> optionalValue = psseTransformerImpedanceCorrection.getPoints().get(i).headerToString(headers[i].substring(0, 1));
                if (optionalValue.isEmpty()) {
                    throw new PsseException("Unsupported header: " + headers[i]);
                }
                row[j] = optionalValue.get();
            }
        }
        return row;
    }

    public PsseTransformerImpedanceCorrection(int i) {
        this.i = i;
        this.points = new ArrayList<>();
    }

    public int getI() {
        return i;
    }

    public List<PsseTransformerImpedanceCorrectionPoint> getPoints() {
        return points;
    }

}
