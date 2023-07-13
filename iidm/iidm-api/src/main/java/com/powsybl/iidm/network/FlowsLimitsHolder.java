/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.util.LimitViolationUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface FlowsLimitsHolder {

    default Collection<OperationalLimits> getOperationalLimits() {
        return getCurrentLimits()
                .map(l -> Collections.singletonList((OperationalLimits) l))
                .orElseGet(Collections::emptyList);
    }

    Optional<CurrentLimits> getCurrentLimits();

    CurrentLimits getNullableCurrentLimits();

    Optional<ActivePowerLimits> getActivePowerLimits();

    ActivePowerLimits getNullableActivePowerLimits();

    Optional<ApparentPowerLimits> getApparentPowerLimits();

    ApparentPowerLimits getNullableApparentPowerLimits();

    CurrentLimitsAdder newCurrentLimits();

    ApparentPowerLimitsAdder newApparentPowerLimits();

    ActivePowerLimitsAdder newActivePowerLimits();

    default boolean isOverloaded(Terminal terminal) {
        return isOverloaded(terminal, 1.0f);
    }

    default boolean isOverloaded(Terminal terminal, float limitReduction) {
        return checkPermanentLimit(terminal, limitReduction, LimitType.CURRENT);
    }

    default boolean checkPermanentLimit(Terminal terminal, float limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, limitReduction, getValueForLimit(terminal, type), type);
    }

    default Optional<? extends LoadingLimits> getLimits(LimitType type) {
        switch (type) {
            case ACTIVE_POWER:
                return getActivePowerLimits();
            case APPARENT_POWER:
                return getApparentPowerLimits();
            case CURRENT:
                return getCurrentLimits();
            case VOLTAGE:
            default:
                throw new UnsupportedOperationException(String.format("Getting %s limits is not supported.", type.name()));
        }
    }

    default double getValueForLimit(Terminal t, LimitType type) {
        switch (type) {
            case ACTIVE_POWER:
                return t.getP();
            case APPARENT_POWER:
                return Math.sqrt(t.getP() * t.getP() + t.getQ() * t.getQ());
            case CURRENT:
                return t.getI();
            case VOLTAGE:
            default:
                throw new UnsupportedOperationException(String.format("Getting %s limits is not supported.", type.name()));
        }
    }

}
