/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion.translation;

import com.powsybl.iidm.network.*;

import java.util.Optional;

public interface NetworkElement<T> {

    String getId();

    Country getCountry1();

    Country getCountry2();

    Country getCountry();

    Double getNominalVoltage1();

    Double getNominalVoltage2();

    Double getNominalVoltage3();

    Double getNominalVoltage();

    Optional<T> getLimits(LimitType limitType, ThreeSides side);
}