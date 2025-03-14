/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public class BatteryShortCircuitImpl extends AbstractShortCircuitExtensionImpl<Battery, BatteryShortCircuitImpl> implements BatteryShortCircuit {
    public BatteryShortCircuitImpl(Battery battery, double directSubtransX, double directTransX,
                                     double stepUpTransformerX) {
        super(battery, directSubtransX, directTransX, stepUpTransformerX);
    }

    @Override
    protected BatteryShortCircuitImpl self() {
        return this;
    }
}
