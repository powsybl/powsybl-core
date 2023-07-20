/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Optional;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public interface VoltageAngleLimit extends OperationalLimits {

    @Override
    default LimitType getLimitType() {
        return LimitType.VOLTAGE_ANGLE;
    }

    /**
     * Return the mandatory name.
     */
    String getName();

    /**
     * If the Reference terminal is associated to a bus REFERENCE and the terminal OTHER is associated to bus OTHER,
     * the limit violation is checked on the difference between Bus OTHER and Bus REFERENCE (OTHER minus REFERENCE)
     */
    Terminal getReferenceTerminal();

    /**
     * If the Reference terminal is associated to a bus REFERENCE and the terminal OTHER is associated to bus OTHER,
     * the limit violation is checked on the difference between Bus OTHER and Bus REFERENCE (OTHER minus REFERENCE)
     */
    Terminal getOtherTerminal();

    /**
     * Get the low voltage angle limit value
     */
    Optional<Double> getLowLimit();

    /**
     * Get the high voltage angle limit value
     */
    Optional<Double> getHighLimit();
}
