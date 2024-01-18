/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface FlowsLimitsHolder {

    Collection<OperationalLimitsGroup> getOperationalLimitsGroups();

    Optional<String> getSelectedOperationalLimitsGroupId();

    Optional<OperationalLimitsGroup> getOperationalLimitsGroup(String id);

    Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup();

    OperationalLimitsGroup newOperationalLimitsGroup(String id);

    void setSelectedOperationalLimitsGroup(String id);

    void removeOperationalLimitsGroup(String id);

    void cancelSelectedOperationalLimitsGroup();

    default Optional<CurrentLimits> getCurrentLimits() {
        return getSelectedOperationalLimitsGroup().flatMap(OperationalLimitsGroup::getCurrentLimits);
    }

    default CurrentLimits getNullableCurrentLimits() {
        return getCurrentLimits().orElse(null);
    }

    default Optional<ActivePowerLimits> getActivePowerLimits() {
        return getSelectedOperationalLimitsGroup().flatMap(OperationalLimitsGroup::getActivePowerLimits);
    }

    default ActivePowerLimits getNullableActivePowerLimits() {
        return getActivePowerLimits().orElse(null);
    }

    default Optional<ApparentPowerLimits> getApparentPowerLimits() {
        return getSelectedOperationalLimitsGroup().flatMap(OperationalLimitsGroup::getApparentPowerLimits);
    }

    default ApparentPowerLimits getNullableApparentPowerLimits() {
        return getApparentPowerLimits().orElse(null);
    }

    CurrentLimitsAdder newCurrentLimits();

    ApparentPowerLimitsAdder newApparentPowerLimits();

    ActivePowerLimitsAdder newActivePowerLimits();
}
