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
public class ReorientedBranchCharacteristics extends AbstractBranchParameters {

    public ReorientedBranchCharacteristics(double r, double x, double g1, double b1, double g2, double b2) {
        this(r, x, g1, b1, g2, b2, true);
    }

    public ReorientedBranchCharacteristics(double r, double x, double g1, double b1, double g2, double b2, boolean reorient) {
        super(r,
            x,
            reorient ? g2 : g1,
            reorient ? b2 : b1,
            reorient ? g1 : g2,
            reorient ? b1 : b2);
    }
}
