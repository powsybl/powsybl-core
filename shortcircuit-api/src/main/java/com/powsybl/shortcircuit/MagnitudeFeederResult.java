/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class MagnitudeFeederResult extends AbstractFeederResult {

    private final double current;

    public MagnitudeFeederResult(String connectableId, double current) {
        super(connectableId);
        this.current = current;
    }

    /**
     * The three-phase current magnitude [in A].
     */
    public double getCurrent() {
        return current;
    }
}
