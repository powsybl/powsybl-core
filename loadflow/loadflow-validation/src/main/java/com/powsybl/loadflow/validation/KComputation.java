/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class KComputation {

    private double sumP = 0;
    private double sumTargetP = 0;
    private double sum = 0;
    private double cumSumVarK = 0;

    public void addGeneratorValues(double p, double targetP, double value) {
        sumP +=  p;
        sumTargetP += targetP;
        sum += value;
        cumSumVarK += Math.pow(p - targetP, 2) / value;
    }

    public double getK() {
        return (sumP - sumTargetP) / sum;
    }

    public double getVarK() {
        return (cumSumVarK / sum) / Math.max(1E-12, getK() * getK()) - 1;
    }

}
