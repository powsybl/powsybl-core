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

    /**
     * Get the {@link LoadingLimits} corresponding to the given <code>limitType</code> (see {@link LimitType} for all types)
     * Throws an {@link UnsupportedOperationException} if a limit of the given type cannot be defined
     * @param limitType the type of limit we want
     * @return the limit corresponding to the <code>limitType</code> if any exist, otherwise an empty {@link Optional}
     */
    default Optional<? extends LoadingLimits> getLoadingLimits(LimitType limitType) {
        return switch (limitType) {
            case CURRENT -> getCurrentLimits();
            case ACTIVE_POWER -> getActivePowerLimits();
            case APPARENT_POWER -> getApparentPowerLimits();
            default -> throw new UnsupportedOperationException("Unsupported limitType for this element: " + limitType);
        };
    }

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
