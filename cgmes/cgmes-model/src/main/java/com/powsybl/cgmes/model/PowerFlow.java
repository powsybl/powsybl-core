/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class PowerFlow {
    public static final PowerFlow UNDEFINED = new PowerFlow(false, Double.NaN, Double.NaN);

    private PowerFlow(boolean defined, double p, double q) {
        this.defined = defined;
        this.p = p;
        this.q = q;
    }

    public PowerFlow(double p, double q) {
        defined = true;
        this.p = p;
        this.q = q;
    }

    public PowerFlow(PropertyBag b, String pname, String qname) {
        p = b.asDouble(pname);
        q = b.asDouble(qname);
        defined = b.containsKey(pname) && b.containsKey(qname);
    }

    public boolean defined() {
        return defined;
    }

    public double p() {
        return p;
    }

    public double q() {
        return q;
    }

    public PowerFlow sum(PowerFlow f) {
        if (!(defined && f.defined)) {
            throw new CgmesModelException("PowerFlow invalid operation sum");
        }
        return new PowerFlow(this.p + f.p, this.q + f.q);
    }

    private final double p;
    private final double q;
    private final boolean defined;
}
