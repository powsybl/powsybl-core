/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.Optional;

interface FlowsLimitsDefaultHolder2 extends FlowsLimitsHolder2 {

    OperationalLimitsGroupsImpl getOperationalLimitsHolder2();

    @Override
    default List<OperationalLimitsGroup> getOperationalLimitsGroups2() {
        return getOperationalLimitsHolder2().getAllOperationalLimitsGroup();
    }

    @Override
    default Optional<String> getDefaultIdOperationalLimitsGroups2() {
        return getOperationalLimitsHolder2().getDefaultId();
    }

    @Override
    default Optional<OperationalLimitsGroup> getOperationalLimitsGroup2(String id) {
        return getOperationalLimitsHolder2().getOperationalLimitsGroup(id);
    }

    @Override
    default Optional<OperationalLimitsGroup> getDefaultOperationalLimitsGroup2() {
        return getOperationalLimitsHolder2().getDefaultOperationalLimitsGroup();
    }

    @Override
    default OperationalLimitsGroup newOperationalLimitsGroup2(String id) {
        return getOperationalLimitsHolder2().newOperationalLimitsGroup(id);
    }

    @Override
    default void setDefaultOperationalLimitsGroup2To(String id) {
        getOperationalLimitsHolder2().setDefaultTo(id);
    }

    @Override
    default void removeOperationalLimitsGroup2(String id) {
        getOperationalLimitsHolder2().removeOperationalLimitsGroup(id);
    }

    @Override
    default void cancelDefaultOperationalLimitsGroup2() {
        getOperationalLimitsHolder2().cancelDefault();
    }

    @Override
    default CurrentLimitsAdder newCurrentLimits2() {
        return getOperationalLimitsHolder2().newCurrentLimits();
    }

    @Override
    default ActivePowerLimitsAdder newActivePowerLimits2() {
        return getOperationalLimitsHolder2().newActivePowerLimits();
    }

    @Override
    default ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return getOperationalLimitsHolder2().newApparentPowerLimits();
    }
}
