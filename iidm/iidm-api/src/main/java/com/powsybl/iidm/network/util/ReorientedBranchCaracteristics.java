/**
 * Copyright (c) 2021, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

/**
 * Utility class to reorient branch parameters
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class ReorientedBranchCaracteristics {

    private final double r;

    private final double x;

    private final double g1;

    private final double b1;

    private final double g2;

    private final double b2;

    public ReorientedBranchCaracteristics(double r, double x, double g1, double b1, double g2, double b2) {
        this(r, x, g1, b1, g2, b2, true);
    }

    public ReorientedBranchCaracteristics(double r, double x, double g1, double b1, double g2, double b2, boolean reorient) {
        this.r = r;
        this.x = x;
        this.g1 = reorient ? g2 : g1;
        this.b1 = reorient ? b2 : b1;
        this.g2 = reorient ? g1 : g2;
        this.b2 = reorient ? b1 : b2;
    }

    public double getR() {
        return r;
    }

    public double getX() {
        return x;
    }

    public double getG1() {
        return g1;
    }

    public double getB1() {
        return b1;
    }

    public double getG2() {
        return g2;
    }

    public double getB2() {
        return b2;
    }
}
