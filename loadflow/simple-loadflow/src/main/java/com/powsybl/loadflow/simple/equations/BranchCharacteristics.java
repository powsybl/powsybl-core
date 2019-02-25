/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.simple.equations;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.TwoWindingsTransformer;

import java.util.Objects;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class BranchCharacteristics {

    protected final Branch branch;

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

    public BranchCharacteristics(Branch branch) {
        this.branch = Objects.requireNonNull(branch);
        init();
    }

    private void init() {

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

    public double r() {
        return r;
    }

    public double x() {
        return x;
    }

    public double z() {
        return z;
    }

    public double y() {
        return y;
    }

    public double ksi() {
        return ksi;
    }

    public double g1() {
        return g1;
    }

    public double g2() {
        return g2;
    }

    public double b1() {
        return b1;
    }

    public double b2() {
        return b2;
    }

    public double r1() {
        return r1;
    }

    public double r2() {
        return r2;
    }

    public double a1() {
        return a1;
    }

    public double a2() {
        return a2;
    }
}
