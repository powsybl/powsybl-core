/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface FlowsLimitsHolder {

    default Collection<OperationalLimits> getOperationalLimits() {
        return getCurrentLimits()
                .map(l -> Collections.singletonList((OperationalLimits) l))
                .orElseGet(Collections::emptyList);
    }

    Optional<CurrentLimits> getCurrentLimits();

    CurrentLimits getNullableCurrentLimits();

    Optional<ActivePowerLimits> getActivePowerLimits();

    ActivePowerLimits getNullableActivePowerLimits();

    Optional<ApparentPowerLimits> getApparentPowerLimits();

    ApparentPowerLimits getNullableApparentPowerLimits();

    CurrentLimitsAdder newCurrentLimits();

    ApparentPowerLimitsAdder newApparentPowerLimits();

    ActivePowerLimitsAdder newActivePowerLimits();
}
