/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network;
import java.util.Optional;

/**
 * @author Pauline Jean-Marie {@literal <pauline.jean-marie at artelys.com>}
 */
public interface OperationalLimitsGroup {

    String getId();

    Optional<CurrentLimits> getCurrentLimits();

    Optional<ActivePowerLimits> getActivePowerLimits();

    Optional<ApparentPowerLimits> getApparentPowerLimits();

    CurrentLimitsAdder newCurrentLimits();

    ActivePowerLimitsAdder newActivePowerLimits();

    ApparentPowerLimitsAdder newApparentPowerLimits();

    void removeCurrentLimits();

    void removeActivePowerLimits();

    void removeApparentPowerLimits();

    boolean isEmpty();
}
