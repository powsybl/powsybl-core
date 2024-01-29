/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

public interface VoltageAngleLimitAdder extends OperationalLimitsAdder<VoltageAngleLimit, VoltageAngleLimitAdder> {

    VoltageAngleLimitAdder setId(String name);

    VoltageAngleLimitAdder from(Terminal from);

    VoltageAngleLimitAdder to(Terminal to);

    VoltageAngleLimitAdder setLowLimit(double lowLimit);

    VoltageAngleLimitAdder setHighLimit(double highLimit);
}
