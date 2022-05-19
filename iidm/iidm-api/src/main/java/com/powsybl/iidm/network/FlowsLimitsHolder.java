/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Optional;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface FlowsLimitsHolder {

    CurrentLimits getCurrentLimits(String id);

    /**
     * Get the active current limits.
     */
    Optional<CurrentLimits> getActiveCurrentLimits();

    /**
     * Define the active current limits by the ID.
     * If there is only one limits table (without ID), it is the active current limits
     */
    void setActiveCurrentLimits(String id);

    CurrentLimitsSet getCurrentLimitsSet();

    ActivePowerLimits getActivePowerLimits(String id);

    Optional<ActivePowerLimits> getActiveActivePowerLimits();

    void setActiveActivePowerLimits(String id);

    ActivePowerLimitsSet getActivePowerLimitsSet();

    ApparentPowerLimits getApparentPowerLimits(String id);

    Optional<ApparentPowerLimits> getActiveApparentPowerLimits();

    void setActiveApparentPowerLimits(String id);

    ApparentPowerLimitsSet getApparentPowerLimitsSet();

    CurrentLimitsAdder newCurrentLimits();

    ApparentPowerLimitsAdder newApparentPowerLimits();

    ActivePowerLimitsAdder newActivePowerLimits();
}
