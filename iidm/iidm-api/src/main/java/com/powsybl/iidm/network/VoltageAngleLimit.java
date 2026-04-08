/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.OptionalDouble;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public interface VoltageAngleLimit extends OperationalLimits {

    @Override
    default LimitType getLimitType() {
        return LimitType.VOLTAGE_ANGLE;
    }

    /**
     * Return the mandatory name.
     */
    String getId();

    /**
     * A voltage angle limit is compared to the difference between the bus angle associated to the terminal from and
     * the bus angle associated to the terminal to. Difference = to - from.
     */
    Terminal getTerminalFrom();

    /**
     * A voltage angle limit is compared to the difference between the bus angle associated to the terminal from and
     * the bus angle associated to the terminal to. Difference = to - from.
     */
    Terminal getTerminalTo();

    /**
     * Get the low voltage angle limit value
     */
    OptionalDouble getLowLimit();

    /**
     * Get the high voltage angle limit value
     */
    OptionalDouble getHighLimit();
}
