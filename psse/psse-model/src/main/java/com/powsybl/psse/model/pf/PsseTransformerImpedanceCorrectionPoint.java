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
import com.powsybl.psse.model.Revision;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.Optional;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseTransformerImpedanceCorrectionPoint extends PsseVersioned {
    private double t;

    @Revision(until = 33)
    private double f;

    @Revision(since = 35)
    private double ref;

    @Revision(since = 35)
    private double imf;

    // TODO: check if needed
    public static PsseTransformerImpedanceCorrectionPoint fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        return fromRecord(rec, version, headers, "");
    }

    public static PsseTransformerImpedanceCorrectionPoint fromRecord(CsvRecord rec, PsseVersion version, String[] headers, String headerSuffix) {
        double t = parseDoubleFromRecord(rec, 0.0, headers, "t" + headerSuffix);
        if (version.getMajorNumber() <= 33) {
            double f = parseDoubleFromRecord(rec, 0.0, headers, "f" + headerSuffix);
            return new PsseTransformerImpedanceCorrectionPoint(t, f);
        }
        if (version.getMajorNumber() >= 35) {
            Double ref = parseDoubleFromRecord(rec, 0.0, headers, "ref" + headerSuffix);
            Double imf = parseDoubleFromRecord(rec, 0.0, headers, "imf" + headerSuffix);
            return new PsseTransformerImpedanceCorrectionPoint(t, ref, imf);
        }
        throw new PsseException("Unexpected version " + version.getMajorNumber());
    }

    public static String[] toRecord(PsseTransformerImpedanceCorrectionPoint psseTransformerImpedanceCorrectionPoint, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            Optional<String> optionalValue = psseTransformerImpedanceCorrectionPoint.headerToString(headers[i]);
            if (optionalValue.isEmpty()) {
                throw new PsseException("Unsupported header: " + headers[i]);
            }
            row[i] = optionalValue.get();
        }
        return row;
    }

    public Optional<String> headerToString(String header) {
        return switch (header) {
            case "t" -> Optional.of(String.valueOf(getT()));
            case "f" -> Optional.of(String.valueOf(getF()));
            case "ref" -> Optional.of(String.valueOf(getRef()));
            case "imf" -> Optional.of(String.valueOf(getImf()));
            default -> Optional.empty();
        };
    }

    public PsseTransformerImpedanceCorrectionPoint(double t, double f) {
        this.t = t;
        this.f = f;
    }

    public PsseTransformerImpedanceCorrectionPoint(double t, double ref, double imf) {
        this.t = t;
        this.ref = ref;
        this.imf = imf;
    }

    public double getT() {
        return t;
    }

    public double getF() {
        checkVersion("f");
        return f;
    }

    public double getRef() {
        checkVersion("ref");
        return ref;
    }

    public double getImf() {
        checkVersion("imf");
        return imf;
    }
}
