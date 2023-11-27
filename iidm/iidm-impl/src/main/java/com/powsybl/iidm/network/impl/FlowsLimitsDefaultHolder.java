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

interface FlowsLimitsDefaultHolder extends FlowsLimitsHolder {

    OperationalLimitsGroupsImpl getOperationalLimitsHolder();

    @Override
    default List<OperationalLimitsGroup> getOperationalLimitsGroups() {
        return getOperationalLimitsHolder().getAllOperationalLimitsGroup();
    }

    @Override
    default Optional<String> getDefaultIdOperationalLimitsGroups() {
        return getOperationalLimitsHolder().getDefaultId();
    }

    @Override
    default Optional<OperationalLimitsGroup> getOperationalLimitsGroup(String id) {
        return getOperationalLimitsHolder().getOperationalLimitsGroup(id);
    }

    @Override
    default Optional<OperationalLimitsGroup> getDefaultOperationalLimitsGroup() {
        return getOperationalLimitsHolder().getDefaultOperationalLimitsGroup();
    }

    @Override
    default OperationalLimitsGroup newOperationalLimitsGroup(String id) {
        return getOperationalLimitsHolder().newOperationalLimitsGroup(id);
    }

    @Override
    default void setDefaultOperationalLimitsGroupTo(String id) {
        getOperationalLimitsHolder().setDefaultTo(id);
    }

    @Override
    default void removeOperationalLimitsGroup(String id) {
        getOperationalLimitsHolder().removeOperationalLimitsGroup(id);
    }

    @Override
    default void cancelDefaultOperationalLimitsGroup() {
        getOperationalLimitsHolder().cancelDefault();
    }

    @Override
    default CurrentLimitsAdder newCurrentLimits() {
        return getOperationalLimitsHolder().newCurrentLimits();
    }

    @Override
    default ActivePowerLimitsAdder newActivePowerLimits() {
        return getOperationalLimitsHolder().newActivePowerLimits();
    }

    @Override
    default ApparentPowerLimitsAdder newApparentPowerLimits() {
        return getOperationalLimitsHolder().newApparentPowerLimits();
    }
}
