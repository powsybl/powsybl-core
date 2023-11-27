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

interface FlowsLimitsDefaultHolder1 extends FlowsLimitsHolder1 {

    OperationalLimitsGroupsImpl getOperationalLimitsHolder1();

    @Override
    default Optional<String> getDefaultIdOperationalLimitsGroups1() {
        return getOperationalLimitsHolder1().getDefaultId();
    }

    @Override
    default List<OperationalLimitsGroup> getOperationalLimitsGroups1() {
        return getOperationalLimitsHolder1().getAllOperationalLimitsGroup();
    }

    @Override
    default Optional<OperationalLimitsGroup> getOperationalLimitsGroup1(String id) {
        return getOperationalLimitsHolder1().getOperationalLimitsGroup(id);
    }

    @Override
    default Optional<OperationalLimitsGroup> getDefaultOperationalLimitsGroup1() {
        return getOperationalLimitsHolder1().getDefaultOperationalLimitsGroup();
    }

    @Override
    default OperationalLimitsGroup newOperationalLimitsGroup1(String id) {
        return getOperationalLimitsHolder1().newOperationalLimitsGroup(id);
    }

    @Override
    default void setDefaultOperationalLimitsGroup1To(String id) {
        getOperationalLimitsHolder1().setDefaultTo(id);
    }

    @Override
    default void removeOperationalLimitsGroup1(String id) {
        getOperationalLimitsHolder1().removeOperationalLimitsGroup(id);
    }

    @Override
    default void cancelDefaultOperationalLimitsGroup1() {
        getOperationalLimitsHolder1().cancelDefault();
    }

    @Override
    default CurrentLimitsAdder newCurrentLimits1() {
        return getOperationalLimitsHolder1().newCurrentLimits();
    }

    @Override
    default ActivePowerLimitsAdder newActivePowerLimits1() {
        return getOperationalLimitsHolder1().newActivePowerLimits();
    }

    @Override
    default ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return getOperationalLimitsHolder1().newApparentPowerLimits();
    }
}
