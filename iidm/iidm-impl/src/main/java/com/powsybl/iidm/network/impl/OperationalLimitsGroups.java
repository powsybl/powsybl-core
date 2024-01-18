/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ActivePowerLimitsAdder;
import com.powsybl.iidm.network.ApparentPowerLimitsAdder;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.OperationalLimitsGroup;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface OperationalLimitsGroups {

    OperationalLimitsGroup newOperationalLimitsGroup(String id);

    void setSelectedOperationalLimitsGroup(String id);

    Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup();

    Optional<String> getSelectedOperationalLimitsGroupId();

    Collection<OperationalLimitsGroup> getAllOperationalLimitsGroup();

    Optional<OperationalLimitsGroup> getOperationalLimitsGroup(String id);

    void removeOperationalLimitsGroup(String id);

    void cancelSelectedOperationalLimitsGroup();

    CurrentLimitsAdder newCurrentLimits();

    ActivePowerLimitsAdder newActivePowerLimits();

    ApparentPowerLimitsAdder newApparentPowerLimits();
}
