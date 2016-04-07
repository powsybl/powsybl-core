/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag.model;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PowerFlow {

    public double p;
    public double q;

    public PowerFlow() {
        this(Double.NaN, Double.NaN);
    }

    public PowerFlow(double p, double q) {
        this.p = p;
        this.q = q;
    }

    public boolean isValid() {
        return !Double.isNaN(p) && !Double.isNaN(q);
    }

    @Override
    public String toString() {
        return "PF(p=" + p + ", q=" + q + ")";
    }
}
