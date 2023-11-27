/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.List;
import java.util.Optional;

public interface FlowsLimitsHolder1 {

    List<OperationalLimitsGroup> getOperationalLimitsGroups1();

    Optional<String> getDefaultIdOperationalLimitsGroups1();

    Optional<OperationalLimitsGroup> getOperationalLimitsGroup1(String id);

    Optional<OperationalLimitsGroup> getDefaultOperationalLimitsGroup1();

    OperationalLimitsGroup newOperationalLimitsGroup1(String id);

    void setDefaultOperationalLimitsGroup1To(String id);

    void removeOperationalLimitsGroup1(String id);

    void cancelDefaultOperationalLimitsGroup1();

    default Optional<CurrentLimits> getCurrentLimits1() {
        return getDefaultOperationalLimitsGroup1().flatMap(OperationalLimitsGroup::getCurrentLimits);
    }

    default CurrentLimits getNullableCurrentLimits1() {
        return getCurrentLimits1().orElse(null);
    }

    default Optional<ActivePowerLimits> getActivePowerLimits1() {
        return getDefaultOperationalLimitsGroup1().flatMap(OperationalLimitsGroup::getActivePowerLimits);
    }

    default ActivePowerLimits getNullableActivePowerLimits1() {
        return getActivePowerLimits1().orElse(null);
    }

    default Optional<ApparentPowerLimits> getApparentPowerLimits1() {
        return getDefaultOperationalLimitsGroup1().flatMap(OperationalLimitsGroup::getApparentPowerLimits);
    }

    default ApparentPowerLimits getNullableApparentPowerLimits1() {
        return getApparentPowerLimits1().orElse(null);
    }

    CurrentLimitsAdder newCurrentLimits1();

    ActivePowerLimitsAdder newActivePowerLimits1();

    ApparentPowerLimitsAdder newApparentPowerLimits1();

}
