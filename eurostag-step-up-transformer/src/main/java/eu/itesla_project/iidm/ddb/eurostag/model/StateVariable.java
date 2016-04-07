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
public class StateVariable extends PowerFlow {

    public double u;
    public double theta;

    public StateVariable() {
        this(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public StateVariable(double p, double q, double u, double theta) {
        super(p, q);
        this.u = u;
        this.theta = theta;
    }

    public boolean isValid() {
        return super.isValid() && !Double.isNaN(u) && !Double.isNaN(theta);
    }

    @Override
    public String toString() {
        return "SV(p=" + p + ", q=" + q + ", u=" + u + ", theta=" + theta + ")";
    }
}
