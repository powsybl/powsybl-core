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
 * A result this module does not model, here the chi-square statistic of a bad data test, used to
 * check that an implementation can attach its own without the interface changing.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public class ChiSquareExtension extends AbstractExtension<StateEstimationResult> {

    private final double chiSquare;

    public ChiSquareExtension(double chiSquare) {
        this.chiSquare = chiSquare;
    }

    @Override
    public String getName() {
        return "chiSquare";
    }

    public double getChiSquare() {
        return chiSquare;
    }
}
