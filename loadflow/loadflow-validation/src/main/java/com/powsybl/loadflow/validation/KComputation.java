/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

/**
 * Based on a list of (value, target, coeff), computes the balancing ratio k such that
 * <pre>
 *   sum(values) - sum(target) = k * sum(coeff)
 * </pre>
 *
 * and compute the variance of values around the theoretical value target + k*coeff.
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class KComputation {

    private double sumP = 0;
    private double sumTargetP = 0;
    private double sum = 0;
    private double cumSumVarK = 0;

    /**
     * Register values for a generator:
     * @param p       Computed power generation
     * @param targetP Target power generation
     * @param value   Assumed coefficient for that generator (for example Pmax, targetP, ...)
     */
    public void addGeneratorValues(double p, double targetP, double value) {
        sumP += p;
        sumTargetP += targetP;
        sum += value;
        cumSumVarK += Math.pow(p - targetP, 2) / value;
    }

    /**
     * Estimated value of coefficient, based on registered values.
     */
    public double getK() {
        return (sumP - sumTargetP) / sum;
    }

    /**
     * Compute variance of distribution around the theoretical values.
     */
    public double getVarK() {
        return (cumSumVarK / sum) / Math.max(1E-12, getK() * getK()) - 1;
    }

}
