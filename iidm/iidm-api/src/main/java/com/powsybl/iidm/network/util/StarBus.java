/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import java.util.Objects;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.LegBase;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class StarBus {

    private final double u;
    private final double theta;

    public StarBus(ThreeWindingsTransformer twt) {
        Objects.requireNonNull(twt);
        Complex v1 = ComplexUtils.polar2Complex(getV(twt.getLeg1()), getTheta(twt.getLeg1()));
        Complex v2 = ComplexUtils.polar2Complex(getV(twt.getLeg2()), getTheta(twt.getLeg2()));
        Complex v3 = ComplexUtils.polar2Complex(getV(twt.getLeg3()), getTheta(twt.getLeg3()));
        Complex ytr1 = new Complex(twt.getLeg1().getR(), twt.getLeg1().getX()).reciprocal();
        Complex ytr2 = new Complex(twt.getLeg2().getR(), twt.getLeg2().getX()).reciprocal();
        Complex ytr3 = new Complex(twt.getLeg3().getR(), twt.getLeg3().getX()).reciprocal();

        double ratedU0 = twt.getLeg1().getRatedU();
        Complex a01 = new Complex(1, 0);
        Complex a1 = new Complex(twt.getLeg1().getRatedU() / ratedU0, 0);
        Complex a02 = new Complex(1, 0);
        Complex a2 = new Complex(twt.getLeg2().getRatedU() / ratedU0, 0);
        Complex a03 = new Complex(1, 0);
        Complex a3 = new Complex(twt.getLeg3().getRatedU() / ratedU0, 0);

        // IIDM model includes admittance to ground at star bus side in Leg1
        Complex ysh01 = new Complex(twt.getLeg1().getG(), twt.getLeg1().getB());
        Complex ysh02 = new Complex(0, 0);
        Complex ysh03 = new Complex(0, 0);
        Complex y01 = ytr1.negate().divide(a01.conjugate().multiply(a1));
        Complex y02 = ytr2.negate().divide(a02.conjugate().multiply(a2));
        Complex y03 = ytr3.negate().divide(a03.conjugate().multiply(a3));
        Complex y0101 = ytr1.add(ysh01).divide(a01.conjugate().multiply(a01));
        Complex y0202 = ytr2.add(ysh02).divide(a02.conjugate().multiply(a02));
        Complex y0303 = ytr3.add(ysh03).divide(a03.conjugate().multiply(a03));

        Complex v0 = y01.multiply(v1).add(y02.multiply(v2)).add(y03.multiply(v3)).negate().divide(y0101.add(y0202).add(y0303));

        u = v0.abs();
        theta = v0.getArgument();
    }

    private double getV(LegBase<?> leg) {
        return leg.getTerminal().isConnected() ? leg.getTerminal().getBusView().getBus().getV() : Double.NaN;
    }

    private double getTheta(LegBase<?> leg) {
        return leg.getTerminal().isConnected() ? Math.toRadians(leg.getTerminal().getBusView().getBus().getAngle()) : Double.NaN;
    }

    public double getU() {
        return u;
    }

    public double getTheta() {
        return theta;
    }

}
