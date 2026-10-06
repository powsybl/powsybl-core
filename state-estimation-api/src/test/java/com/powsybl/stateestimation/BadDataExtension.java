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
 * Stands in for the kind of result an implementation might add later, a chi-square statistic from
 * a bad data test, to check that it can be attached without this module changing.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public class BadDataExtension extends AbstractExtension<StateEstimationResult> {

    private final double chiSquare;

    public BadDataExtension(double chiSquare) {
        this.chiSquare = chiSquare;
    }

    @Override
    public String getName() {
        return "badData";
    }

    public double getChiSquare() {
        return chiSquare;
    }
}
