/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag.model;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

/**
 *
 *   v1,i1         v1',i1'            v2,i2
 *   +-->----Ratio--->--+----r+jx----<--+
 *                      |
 *                     g+jb
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TransformerModel {

    private static final double SQUARE_3 = Math.sqrt(3f);

    private final Complex z;
    private final Complex y;
    private final float ratio;

    public TransformerModel(float r, float x, float g, float b, float ratio) {
        this.z = new Complex(r, x); // z=r+jx
        this.y = new Complex(g, b); // y=g+jb
        this.ratio = ratio;
    }

    public StateVariable toSv2(StateVariable sv1) {
        Complex s1 = new Complex(-sv1.p, -sv1.q); // s1=p1+jq1
        Complex u1 = ComplexUtils.polar2Complex(sv1.u, Math.toRadians(sv1.theta));
        Complex v1 = u1.divide(SQUARE_3); // v1=u1/sqrt(3)
        Complex v1p = v1.multiply(ratio); // v1p=v1*rho
        Complex i1 = s1.divide(v1.multiply(3)).conjugate(); // i1=conj(s1/(3*v1))
        Complex i1p = i1.divide(ratio); // i1p=i1/rho
        Complex i2 = i1p.subtract(y.multiply(v1p)).negate(); // i2=-(i1p-y*v1p)
        Complex v2 = v1p.subtract(z.multiply(i2)); // v2=v1p-z*i2
        Complex s2 = v2.multiply(3).multiply(i2.conjugate()); // s2=3*v2*conj(i2)
        Complex u2 = v2.multiply(SQUARE_3);
        return new StateVariable(-s2.getReal(), -s2.getImaginary(), u2.abs(), Math.toDegrees(u2.getArgument()));
    }

    public StateVariable toSv1(StateVariable sv2) {
        Complex s2 = new Complex(-sv2.p, -sv2.q); // s2=p2+jq2
        Complex u2 = ComplexUtils.polar2Complex(sv2.u, Math.toRadians(sv2.theta));
        Complex v2 = u2.divide(SQUARE_3); // v2=u2/sqrt(3)
        Complex i2 = s2.divide(v2.multiply(3)).conjugate(); // i2=conj(s2/(3*v2))
        Complex v1p = v2.add(z.multiply(i2)); // v1'=v2+z*i2
        Complex i1p = i2.negate().add(y.multiply(v1p)); // i1'=-i2+v1'*y
        Complex i1 = i1p.multiply(ratio); // i1=i1p*ration
        Complex v1 = v1p.divide(ratio); // v1=v1p/ration
        Complex s1 = v1.multiply(3).multiply(i1.conjugate()); // s1=3*v1*conj(i1)
        Complex u1 = v1.multiply(SQUARE_3);
        return new StateVariable(-s1.getReal(), -s1.getImaginary(), u1.abs(), Math.toDegrees(u1.getArgument()));
    }

}
