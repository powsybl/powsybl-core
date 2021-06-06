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
public class BranchReorientedParameters {

    public BranchReorientedParameters(double r, double x, double g1, double b1, double g2, double b2) {
        this.r = r;
        this.x = x;
        this.g1 = g1;
        this.b1 = b1;
        this.g2 = g2;
        this.b2 = b2;
        reorient = true;
    }

    public BranchReorientedParameters(double r, double x, double g1, double b1, double g2, double b2, boolean reorient) {
        this.r = r;
        this.x = x;
        this.g1 = g1;
        this.b1 = b1;
        this.g2 = g2;
        this.b2 = b2;
        this.reorient = reorient;
    }

    private final double r;

    private final double x;

    private final double g1;

    private final double b1;

    private final double g2;

    private final double b2;

    private final boolean reorient;

    public double getR() {
        return r;
    }

    public double getX() {
        return x;
    }

    public double getG1() {
        if (reorient) {
            return g2;
        } else {
            return g1;
        }
    }

    public double getB1() {
        if (reorient) {
            return b2;
        } else {
            return b1;
        }
    }

    public double getG2() {
        if (reorient) {
            return g1;
        } else {
            return g2;
        }
    }

    public double getB2() {
        if (reorient) {
            return b1;
        } else {
            return b2;
        }
    }
}
