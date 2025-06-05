/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.extensions;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ThreeSides;

/**
 * @author Laurent Garnier {@literal <laurent.garnier_externe at rte-france.com>}
 */
class MeasurementTest {

    @Test
    void showUneasiness() {
        Measurement measurement = new MyMeasurement();

        measurement.setValue(1.0);
        assertFalse(measurement.isValid());
        measurement.setValid(true);
        assertTrue(measurement.isValid());

        assertThrows(PowsyblException.class, () -> measurement.setValue(Double.NaN));
        // so you have to mutate validity first ...

        measurement.setValid(false);
        measurement.setValue(Double.NaN);

        // ... but if you mutate validity first now ...
        assertThrows(PowsyblException.class, () -> measurement.setValid(true));
    }

    @Test
    void defaultSetValueAndValidity() {
        Measurement measurement = new MyMeasurement();

        measurement.setValueAndValidity(200.0d, true);
        assertEquals(200., measurement.getValue());
        assertTrue(measurement.isValid());

        measurement.setValueAndValidity(Double.NaN, false);
        assertFalse(measurement.isValid());
        assertTrue(Double.isNaN(measurement.getValue()));

    }

    private static class MyMeasurement implements Measurement {
        double d = Double.NaN;
        boolean valid = false;

        public String getId() {
            return "";
        }

        public Type getType() {
            return null;
        }

        public Set<String> getPropertyNames() {
            return Set.of();
        }

        public String getProperty(String name) {
            return "";
        }

        public Measurement putProperty(String name, String property) {
            return null;
        }

        public Measurement removeProperty(String name) {
            return null;
        }

        public Measurement setStandardDeviation(double standardDeviation) {
            return null;
        }

        public double getStandardDeviation() {
            return 0;
        }

        public double getValue() {
            return d;
        }

        public boolean isValid() {
            return valid;
        }

        public Measurement setValue(double value) {
            systematicCheck(value, this.valid);
            d = value;
            return this;
        }

        public Measurement setValid(boolean valid) {
            systematicCheck(d, valid);
            this.valid = valid;
            return this;
        }

        private void systematicCheck(double v, boolean valid) {
            if (Double.isNaN(v) && valid) {
                throw new PowsyblException("NaN is not valid");
            }
        }

        public ThreeSides getSide() {
            return null;
        }

        public void remove() {
            // dummy implementation
        }
    }
}
