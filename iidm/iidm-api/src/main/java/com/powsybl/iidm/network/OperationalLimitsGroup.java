/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network;
import java.util.Optional;

import static com.powsybl.iidm.network.util.LoadingLimitsUtil.initializeFromLoadingLimits;

/**
 * @author Pauline Jean-Marie {@literal <pauline.jean-marie at artelys.com>}
 */
public interface OperationalLimitsGroup extends PropertiesHolder {

    String getId();

    Optional<CurrentLimits> getCurrentLimits();

    Optional<ActivePowerLimits> getActivePowerLimits();

    Optional<ApparentPowerLimits> getApparentPowerLimits();

    CurrentLimitsAdder newCurrentLimits();

    ActivePowerLimitsAdder newActivePowerLimits();

    ApparentPowerLimitsAdder newApparentPowerLimits();

    default CurrentLimitsAdder newCurrentLimits(CurrentLimits currentLimits) {
        CurrentLimitsAdder currentLimitsAdder = newCurrentLimits();
        return initializeFromLoadingLimits(currentLimitsAdder, currentLimits);
    }

    default ActivePowerLimitsAdder newActivePowerLimits(ActivePowerLimits activePowerLimits) {
        ActivePowerLimitsAdder activePowerLimitsAdder = newActivePowerLimits();
        return initializeFromLoadingLimits(activePowerLimitsAdder, activePowerLimits);
    }

    default ApparentPowerLimitsAdder newApparentPowerLimits(ApparentPowerLimits apparentPowerLimits) {
        ApparentPowerLimitsAdder apparentPowerLimitsAdder = newApparentPowerLimits();
        return initializeFromLoadingLimits(apparentPowerLimitsAdder, apparentPowerLimits);
    }

    void removeCurrentLimits();

    void removeActivePowerLimits();

    void removeApparentPowerLimits();

    boolean isEmpty();
}
