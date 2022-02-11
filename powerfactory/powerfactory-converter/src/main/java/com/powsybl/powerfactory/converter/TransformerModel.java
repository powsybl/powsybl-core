/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.converter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TransformerModel {

    private final double r;

    private final double x;

    private final double g;

    private final double b;

    public TransformerModel(double r, double x, double g, double b) {
        this.r = r;
        this.x = x;
        this.g = g;
        this.b = b;
    }

    /**
     * Create a transformer model from measures.
     *
     * @param shortCircuitVoltage short circuit voltage in %
     * @param copperLosses copper loss in KWh
     * @param openCircuitCurrent open circuit in %
     * @param coreLosses core (or iron) losses in KWh
     * @param ratedApparentPower rated apparent power in MVA
     * @param nominalVoltage nominal voltage in Kv
     *
     * @return a transformer model
     */
    public static TransformerModel fromMeasures(double shortCircuitVoltage, double copperLosses,
                                                double openCircuitCurrent, double coreLosses,
                                                double ratedApparentPower, double nominalVoltage) {
        // calculate leakage impedance from short circuit measures
        double vsc = shortCircuitVoltage / 100 * nominalVoltage;
        double isc = ratedApparentPower / vsc; // KA
        double r = copperLosses / 1000 / (isc * isc);
        double z = (nominalVoltage * shortCircuitVoltage / 100) / isc;
        double x = Math.sqrt(z * z - r * r);

        // calculate exciting branch admittance from open circuit measures
        double g = coreLosses / 1000 / (nominalVoltage * nominalVoltage);
        double ioc = ratedApparentPower / nominalVoltage * openCircuitCurrent / 100; // KA
        double y = ioc / nominalVoltage;
        double b = Math.sqrt(y * y - g * g);

        return new TransformerModel(r, x, g, b);
    }

    public double getR() {
        return r;
    }

    public double getX() {
        return x;
    }

    public double getG() {
        return g;
    }

    public double getB() {
        return b;
    }
}
