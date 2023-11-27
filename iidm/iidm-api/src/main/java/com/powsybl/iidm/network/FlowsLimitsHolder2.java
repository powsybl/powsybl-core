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

public interface FlowsLimitsHolder2 {

    List<OperationalLimitsGroup> getOperationalLimitsGroups2();

    Optional<String> getDefaultIdOperationalLimitsGroups2();

    Optional<OperationalLimitsGroup> getOperationalLimitsGroup2(String id);

    Optional<OperationalLimitsGroup> getDefaultOperationalLimitsGroup2();

    OperationalLimitsGroup newOperationalLimitsGroup2(String id);

    void setDefaultOperationalLimitsGroup2To(String id);

    void removeOperationalLimitsGroup2(String id);

    void cancelDefaultOperationalLimitsGroup2();

    default Optional<CurrentLimits> getCurrentLimits2() {
        return getDefaultOperationalLimitsGroup2().flatMap(OperationalLimitsGroup::getCurrentLimits);
    }

    default CurrentLimits getNullableCurrentLimits2() {
        return getCurrentLimits2().orElse(null);
    }

    default Optional<ActivePowerLimits> getActivePowerLimits2() {
        return getDefaultOperationalLimitsGroup2().flatMap(OperationalLimitsGroup::getActivePowerLimits);
    }

    default ActivePowerLimits getNullableActivePowerLimits2() {
        return getActivePowerLimits2().orElse(null);
    }

    default Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return getDefaultOperationalLimitsGroup2().flatMap(OperationalLimitsGroup::getApparentPowerLimits);
    }

    default ApparentPowerLimits getNullableApparentPowerLimits2() {
        return getApparentPowerLimits2().orElse(null);
    }

    CurrentLimitsAdder newCurrentLimits2();

    ActivePowerLimitsAdder newActivePowerLimits2();

    ApparentPowerLimitsAdder newApparentPowerLimits2();

}
