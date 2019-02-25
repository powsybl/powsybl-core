/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.simple.equations;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.TwoWindingsTransformer;

import java.util.Objects;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class FlowEquations {

    protected final Branch branch;

    protected double v1;
    protected double v2;
    protected double ph1;
    protected double ph2;
    protected double r;
    protected double x;
    protected double z;
    protected double y;
    protected double ksi;
    protected double g1;
    protected double g2;
    protected double b1;
    protected double b2;
    protected double r1;
    protected double r2;
    protected double a1;
    protected double a2;

    public FlowEquations(Branch branch) {
        this.branch = Objects.requireNonNull(branch);
        init();
    }

    private void init() {
        v1 = branch.getTerminal1().getBusView().getBus().getV();
        v2 = branch.getTerminal2().getBusView().getBus().getV();
        ph1 = branch.getTerminal1().getBusView().getBus().getAngle();
        ph2 = branch.getTerminal2().getBusView().getBus().getAngle();

        r2 = 1d;
        a2 = 0d;

        if (branch instanceof Line) {
            initLine((Line) branch);
        } else if (branch instanceof TwoWindingsTransformer) {
            initTransformer((TwoWindingsTransformer) branch);
        } else {
            throw new PowsyblException("Unsupported type of branch for flow equations for branch: " + branch.getId());
        }

        z = Math.hypot(r, x);
        y = 1 / z;
        ksi = Math.atan2(r, x);
    }

    private void initLine(Line line) {
        r = line.getR();
        x = line.getX();
        g1 = line.getG1();
        g2 = line.getG2();
        b1 = line.getB1();
        b2 = line.getB2();
        r1 = 1d;
        a1 = 0d;
    }

    private void initTransformer(TwoWindingsTransformer tf) {
        r = Transformers.getR(tf);
        x = Transformers.getX(tf);
        g1 = Transformers.getG1(tf);
        g2 = 0d;
        b1 = Transformers.getB1(tf);
        b2 = 0d;
        r1 = Transformers.getRatio(tf);
        a1 = Transformers.getAngle(tf);
    }

    public double q1() {
        return r1 * v1 * (-b1 * r1 * v1 + y * r1 * v1 * Math.cos(ksi) - y * r2 * v2 * Math.cos(ksi - a1 + a2 - ph1 + ph2));
    }

    public double  p2() {
        return r2 * v2 * (g2 * r2 * v2 - y * r1 * v1 * Math.sin(ksi + a1 - a2 + ph1 - ph2) + y * r2 * v2 * Math.sin(ksi));
    }

    public double p1() {
        return r1 * v1 * (g1 * r1 * v1 + y * r1 * v1 * Math.sin(ksi) - y * r2 * v2 * Math.sin(ksi - a1 + a2 - ph1 + ph2));
    }

    public double q2() {
        return r2 * v2 * (-b2 * r2 * v2 - y * r1 * v1 * Math.cos(ksi + a1 - a2 + ph1 - ph2) + y * r2 * v2 * Math.cos(ksi));
    }

    public double dq1dv1() {
        return r1 * (-2 * b1 * r1 * v1 + 2 * y * r1 * v1 * Math.cos(ksi) - y * r2 * v2 * Math.cos(ksi - a1 + a2 - ph1 + ph2));
    }

    public double dq1dv2() {
        return -y * r1 * r2 * v1 * Math.cos(ksi - a1 + a2 - ph1 + ph2);
    }

    public double dq1dph1() {
        return -y * r1 * r2 * v1 * v2 * Math.sin(ksi - a1 + a2 - ph1 + ph2);
    }

    public double dq1dph2() {
        return y * r1 * r2 * v1 * v2 * Math.sin(ksi - a1 + a2 - ph1 + ph2);
    }

    public double dp2dv1() {
        return -y * r1 * r2 * v2 * Math.sin(ksi + a1 - a2 + ph1 - ph2);
    }

    public double dp2dv2() {
        return r2 * (2 * g2 * r2 * v2 - y * r1 * v1 * Math.sin(ksi + a1 - a2 + ph1 - ph2) + 2 * y * r2 * v2 * Math.sin(ksi));
    }

    public double dp2dph1() {
        return -y * r1 * r2 * v1 * v2 * Math.cos(ksi + a1 - a2 + ph1 - ph2);
    }

    public double dp2dph2() {
        return y * r1 * r2 * v1 * v2 * Math.cos(ksi + a1 - a2 + ph1 - ph2);
    }

    public double dp1dv1() {
        return r1 * (2 * g1 * r1 * v1 + 2 * y * r1 * v1 * Math.sin(ksi) - y * r2 * v2 * Math.sin(ksi - a1 + a2 - ph1 + ph2));
    }

    public double dp1dv2() {
        return -y * r1 * r2 * v1 * Math.sin(ksi - a1 + a2 - ph1 + ph2);
    }

    public double dp1dph1() {
        return y * r1 * r2 * v1 * v2 * Math.cos(ksi - a1 + a2 - ph1 + ph2);
    }

    public double dp1dph2() {
        return -y * r1 * r2 * v1 * v2 * Math.cos(ksi - a1 + a2 - ph1 + ph2);
    }

    public double dq2dv1() {
        return -y * r1 * r2 * v2 * Math.cos(ksi + a1 - a2 + ph1 - ph2);
    }

    public double dq2dv2() {
        return r2 * (-2 * b2 * r2 * v2 - y * r1 * v1 * Math.cos(ksi + a1 - a2 + ph1 - ph2) + 2 * y * r2 * v2 * Math.cos(ksi));
    }

    public double dq2dph1() {
        return y * r1 * r2 * v1 * v2 * Math.sin(ksi + a1 - a2 + ph1 - ph2);
    }

    public double dq2dph2() {
        return -y * r1 * r2 * v1 * v2 * Math.sin(ksi + a1 - a2 + ph1 - ph2);
    }

    @Override
    public String toString() {
        return ImmutableMap.builder()
                .put("v1", v1)
                .put("v2", v2)
                .put("ph1", ph1)
                .put("ph2", ph2)
                .put("r", r)
                .put("x", x)
                .put("z", z)
                .put("y", y)
                .put("ksi", ksi)
                .put("g1", g1)
                .put("g2", g2)
                .put("b1", b1)
                .put("b2", b2)
                .put("a1", a1)
                .put("a2", a2)
                .put("r1", r1)
                .put("r2", r2)
                .build()
                .toString();
    }

}
