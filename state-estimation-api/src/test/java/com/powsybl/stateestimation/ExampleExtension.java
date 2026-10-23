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
 * A stand-in for a result this module does not model, used to check that an implementation can
 * attach its own without the interface changing. The value it carries is arbitrary.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public class ExampleExtension extends AbstractExtension<StateEstimationResult> {

    private final double value;

    public ExampleExtension(double value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return "example";
    }

    public double getValue() {
        return value;
    }
}
