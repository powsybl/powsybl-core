/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;


/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractTapChangerStepAdder<A extends AbstractTapChangerStepAdder<A>>  {

    protected double alpha = Double.NaN;

    protected double rho = Double.NaN;

    protected double r = Double.NaN;

    protected double x = Double.NaN;

    protected double g = Double.NaN;

    protected double b = Double.NaN;

    public A setAlpha(double alpha) {
        this.alpha = alpha;
        return (A) this;
    }

    public A setRho(double rho) {
        this.rho = rho;
        return (A) this;
    }

    public A setR(double r) {
        this.r = r;
        return (A) this;
    }

    public A setX(double x) {
        this.x = x;
        return (A) this;
    }

    public A setG(double g) {
        this.g = g;
        return (A) this;
    }

    public A setB(double b) {
        this.b = b;
        return (A) this;
    }

    protected void checkValues(Validable parent) {
        if (Double.isNaN(rho)) {
            throw new ValidationException(parent, "step rho is not set");
        }
        if (Double.isNaN(r)) {
            throw new ValidationException(parent, "step r is not set");
        }
        if (Double.isNaN(x)) {
            throw new ValidationException(parent, "step x is not set");
        }
        if (Double.isNaN(g)) {
            throw new ValidationException(parent, "step g is not set");
        }
        if (Double.isNaN(b)) {
            throw new ValidationException(parent, "step b is not set");
        }
    }
}
