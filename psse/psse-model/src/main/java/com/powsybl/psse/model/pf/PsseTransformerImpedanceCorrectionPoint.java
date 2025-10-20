/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;

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
//    public static PsseTransformerImpedanceCorrectionPoint fromRecord(NamedCsvRecord rec, PsseVersion version) {
//        double t = Double.parseDouble(rec.getField("t"));
//        if (version.getMajorNumber() <= 33) {
//            double f = Double.parseDouble(rec.getField("f"));
//            return new PsseTransformerImpedanceCorrectionPoint(t, f);
//        }
//        if (version.getMajorNumber() >= 35) {
//            double ref = Double.parseDouble(rec.getField("ref"));
//            double imf = Double.parseDouble(rec.getField("imf"));
//            return new PsseTransformerImpedanceCorrectionPoint(t, ref, imf);
//        }
//        throw new PsseException("Unexpected version " + version.getMajorNumber());
//    }
//
//    public static String[] toRecord(PsseTransformerImpedanceCorrectionPoint psseTransformerImpedanceCorrectionPoint, String[] headers) {
//        String[] row = new String[headers.length];
//        for (int i = 0; i < headers.length; i++) {
//            row[i] = switch (headers[i]) {
//                case "t" -> String.valueOf(psseTransformerImpedanceCorrectionPoint.getT());
//                case "f" -> String.valueOf(psseTransformerImpedanceCorrectionPoint.getF());
//                case "ref" -> String.valueOf(psseTransformerImpedanceCorrectionPoint.getRef());
//                case "imf" -> String.valueOf(psseTransformerImpedanceCorrectionPoint.getImf());
//                default -> throw new PsseException("Unsupported header: " + headers[i]);
//            };
//        }
//        return row;
//    }

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
