/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public final class LimitViolationHelper {

    private LimitViolationHelper() {
    }

    private static VoltageLevel getVoltageLevel(LimitViolation limitViolation, Network network) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(limitViolation);

        Identifiable<?> identifiable = network.getIdentifiable(limitViolation.getSubjectId());
        if (identifiable == null) {
            Bus bus = network.getBusView().getBus(limitViolation.getSubjectId());
            if (bus != null) {
                return bus.getVoltageLevel();
            }
        }
        if (limitViolation.getLimitType() == LimitViolationType.LOW_VOLTAGE_ANGLE || limitViolation.getLimitType() == LimitViolationType.HIGH_VOLTAGE_ANGLE) {
            VoltageAngleLimit limit = network.getVoltageAngleLimit(limitViolation.getSubjectId());
            if (limit != null) {
                return limit.getTerminalFrom().getVoltageLevel();
            } else {
                throw new PowsyblException("Limit from limit violation is not in the network.");
            }
        }
        if (identifiable instanceof Branch<?> branch) {
            return branch.getTerminal(limitViolation.getSideAsTwoSides()).getVoltageLevel();
        } else if (identifiable instanceof Injection<?> injection) {
            return injection.getTerminal().getVoltageLevel();
        } else if (identifiable instanceof VoltageLevel voltageLevel) {
            return voltageLevel;
        } else if (identifiable instanceof Bus bus) {
            return bus.getVoltageLevel();
        } else {
            throw new IllegalStateException("Unexpected identifiable type: " + identifiable.getClass());
        }
    }

    public static Optional<Country> getCountry(LimitViolation limitViolation, Network network) {
        VoltageLevel voltageLevel = getVoltageLevel(limitViolation, network);

        return voltageLevel.getSubstation().flatMap(Substation::getCountry);
    }

    public static String getVoltageLevelId(LimitViolation limitViolation, Network network) {
        return getVoltageLevelId(limitViolation, network, false);
    }

    public static String getVoltageLevelId(LimitViolation limitViolation, Network network, boolean name) {
        VoltageLevel voltageLevel = getVoltageLevel(limitViolation, network);

        return name ? voltageLevel.getNameOrId() : voltageLevel.getId();
    }

    public static double getNominalVoltage(LimitViolation limitViolation, Network network) {
        VoltageLevel voltageLevel = getVoltageLevel(limitViolation, network);

        return voltageLevel.getNominalV();
    }
}
