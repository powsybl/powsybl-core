/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.powsybl.psse.model.PsseVersioned;

import java.util.Collections;
import java.util.List;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
@JsonPropertyOrder(alphabetic = false)
public class PsseTransformerImpedanceCorrectionTable extends PsseVersioned {

    private final int i;
    private final List<Entry> factors;

    public PsseTransformerImpedanceCorrectionTable(int i, List<Entry> factors) {
        this.i = i;
        this.factors = factors;
    }

    public int getI() {
        return i;
    }

    public List<Entry> getFactors() {
        return Collections.unmodifiableList(factors);
    }

    public static class Entry {
        private final double tap;
        private final double reFactor;
        private final double imFactor;

        public Entry(double tap, double reFactor, double imFactor) {
            this.tap = tap;
            this.reFactor = reFactor;
            this.imFactor = imFactor;
        }

        public double getTap() {
            return tap;
        }

        public double getReFactor() {
            return reFactor;
        }

        public double getImFactor() {
            return imFactor;
        }
    }
}
