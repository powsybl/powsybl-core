package com.powsybl.iidm.network.extensions;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.powsybl.iidm.network.ThreeSides;

class MeasurementTest {

    @Test
    void defaultSetValueAndValidity() {
        Measurement measurement = new Measurement() {
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

            public Measurement setValue(double value) {
                return null;
            }

            public double getValue() {
                return 0;
            }

            public Measurement setStandardDeviation(double standardDeviation) {
                return null;
            }

            public double getStandardDeviation() {
                return 0;
            }

            public boolean isValid() {
                return false;
            }

            public Measurement setValid(boolean valid) {
                return null;
            }

            public ThreeSides getSide() {
                return null;
            }

            public void remove() {

            }
        };

        measurement.setValueAndValidity(200.0d, true);
        measurement.setValueAndValidity(Double.NaN, false);
    }
}
