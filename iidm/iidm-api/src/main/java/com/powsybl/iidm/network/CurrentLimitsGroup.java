/**
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Optional;

import static com.powsybl.iidm.network.util.LoadingLimitsUtil.initializeFromLoadingLimits;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public interface CurrentLimitsGroup extends PropertiesHolder {
    String getId();

    Optional<CurrentLimits> getCurrentLimits();

    CurrentLimitsAdder newCurrentLimits();

    default CurrentLimitsAdder newCurrentLimits(CurrentLimits currentLimits) {
        CurrentLimitsAdder currentLimitsAdder = newCurrentLimits();
        return initializeFromLoadingLimits(currentLimitsAdder, currentLimits);
    }

    void removeCurrentLimits();

    boolean isEmpty();
}
