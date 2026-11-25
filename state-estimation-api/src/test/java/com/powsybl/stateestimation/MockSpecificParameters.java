/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.powsybl.commons.extensions.AbstractExtension;

/**
 * Settings of the kind an implementation adds on top of {@link StateEstimationParameters}: a
 * convergence tolerance and an iteration cap, which say nothing about what the estimate means.
 * Used to check that the specific parameters mechanism carries them.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public class MockSpecificParameters extends AbstractExtension<StateEstimationParameters> {

    public static final String NAME = "MockSpecificParameters";

    public static final double DEFAULT_TOLERANCE = 1e-4;
    public static final int DEFAULT_MAX_ITERATIONS = 30;

    private double tolerance = DEFAULT_TOLERANCE;
    private int maxIterations = DEFAULT_MAX_ITERATIONS;

    @Override
    public String getName() {
        return NAME;
    }

    public double getTolerance() {
        return tolerance;
    }

    public MockSpecificParameters setTolerance(double tolerance) {
        this.tolerance = tolerance;
        return this;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public MockSpecificParameters setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
        return this;
    }
}
