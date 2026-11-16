/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public enum ValidationLevel {
    EQUIPMENT,
    STEADY_STATE_HYPOTHESIS;

    public static ValidationLevel min(ValidationLevel vl1, ValidationLevel vl2) {
        return vl1.compareTo(vl2) >= 0 ? vl2 : vl1;
    }

    public static final ValidationLevel MINIMUM_VALUE = EQUIPMENT;
}
