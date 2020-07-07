/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class Quadripole {
    public static final class PiModel {
        public final double r;
        public final double x;
        public final double g1;
        public final double b1;
        public final double g2;
        public final double b2;

        public PiModel(double r, double x, double g1, double b1, double g2, double b2) {
            this.r = r;
            this.x = x;
            this.g1 = g1;
            this.b1 = b1;
            this.g2 = g2;
            this.b2 = b2;
        }

        public static PiModel from(DanglingLine dl) {
            return new PiModel(dl.getR(), dl.getX(), dl.getG() / 2, dl.getB() / 2, dl.getG() / 2, dl.getB() / 2);
        }
    }

    final Complex a;
    final Complex b;
    final Complex c;
    final Complex d;

    private Quadripole(Complex a, Complex b, Complex c, Complex d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public static Quadripole from(PiModel pi) {
        Quadripole y1 = fromShuntAdmittance(pi.g1, pi.b1);
        Quadripole z = fromSeriesImpedance(pi.r, pi.x);
        Quadripole y2 = fromShuntAdmittance(pi.g2, pi.b2);
        return y1.cascade(z).cascade(y2);
    }

    public static Quadripole fromSeriesImpedance(double r, double x) {
        return new Quadripole(new Complex(1), new Complex(r, x), new Complex(0), new Complex(1));
    }

    public static Quadripole fromShuntAdmittance(double g, double b) {
        return new Quadripole(new Complex(1), new Complex(0), new Complex(g, b), new Complex(1));
    }

    public Quadripole cascade(Quadripole q2) {
        Quadripole q1 = this;
        Quadripole qr = new Quadripole(q1.a.multiply(q2.a).add(q1.b.multiply(q2.c)),
                q1.a.multiply(q2.b).add(q1.b.multiply(q2.d)), q1.c.multiply(q2.a).add(q1.d.multiply(q2.c)),
                q1.c.multiply(q2.b).add(q1.d.multiply(q2.d)));
        return qr;
    }

    public Quadripole.PiModel toPiModel() {
        // Y2 = (A - 1)/B
        // Y1 = (D - 1)/B
        Complex y1 = d.add(-1).divide(b);
        Complex y2 = a.add(-1).divide(b);

        double r = b.getReal();
        double x = b.getImaginary();
        double g1 = y1.getReal();
        double b1 = y1.getImaginary();
        double g2 = y2.getReal();
        double b2 = y2.getImaginary();
        return new PiModel(r, x, g1, b1, g2, b2);
    }
}
