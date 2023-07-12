/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public interface VoltageAngleLimit extends OperationalLimits {

    public enum FlowDirection {
        FROM_TO,
        TO_FROM,
        BOTH_DIRECTIONS
    }

    @Override
    default LimitType getLimitType() {
        return LimitType.VOLTAGE_ANGLE;
    }

    /**
     * Get the Terminal from
     */
    Terminal getTerminalFrom();

    /**
     * Get the Terminal to
     */
    Terminal getTerminalTo();

    /**
     * Get the VoltageAngle limit value
     */
    double getLimit();

    /**
     * Get the flow direction
     */
    FlowDirection getFlowDirection();
}
