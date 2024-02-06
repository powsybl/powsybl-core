/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.translation;

import com.powsybl.iidm.network.*;

import java.util.Optional;

public interface NetworkElementInterface {

    String getId();

    Country getCountry1();

    Country getCountry2();

    Country getCountry();

    VoltageLevel getVoltageLevel1();

    VoltageLevel getVoltageLevel2();

    VoltageLevel getVoltageLevel3();

    VoltageLevel getVoltageLevel();

    Optional<? extends LoadingLimits> getLoadingLimits(LimitType limitType, ThreeSides side);
}
