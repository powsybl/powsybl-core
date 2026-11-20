/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.violations;

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

    /**
     * @return the voltage level where the violation is located, or an empty {@code Optional} if its subject is not
     * located in any voltage level (DC node or DC line). Such a violation has no country and no nominal voltage.
     * @throws IllegalStateException if the type of the subject of the violation is not supported.
     */
    public static Optional<VoltageLevel> getVoltageLevel(LimitViolation limitViolation, Network network) {
        return Optional.ofNullable(findVoltageLevel(limitViolation, network));
    }

    private static VoltageLevel findVoltageLevel(LimitViolation limitViolation, Network network) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(limitViolation);

        Identifiable<?> identifiable = network.getIdentifiable(limitViolation.getSubjectId());
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
        } else if (identifiable instanceof AcDcConverter<?> converter) {
            return converter.getTerminal1().getVoltageLevel();
        } else if (identifiable instanceof DcNode || identifiable instanceof DcLine) {
            return null;
        } else {
            throw new IllegalStateException("Unexpected identifiable type: " + identifiable.getClass());
        }
    }

    public static Optional<Country> getCountry(LimitViolation limitViolation, Network network) {
        return getVoltageLevel(limitViolation, network).flatMap(LimitViolationHelper::getCountry);
    }

    /**
     * @return the country of the substation of the voltage level, if any.
     */
    public static Optional<Country> getCountry(VoltageLevel voltageLevel) {
        return voltageLevel.getSubstation().flatMap(Substation::getCountry);
    }

    public static String getVoltageLevelId(LimitViolation limitViolation, Network network) {
        return getVoltageLevelId(limitViolation, network, false);
    }

    public static String getVoltageLevelId(LimitViolation limitViolation, Network network, boolean name) {
        return getVoltageLevel(limitViolation, network)
                .map(voltageLevel -> name ? voltageLevel.getNameOrId() : voltageLevel.getId())
                .orElse(null);
    }

    public static double getNominalVoltage(LimitViolation limitViolation, Network network) {
        return getVoltageLevel(limitViolation, network)
                .map(VoltageLevel::getNominalV)
                .orElse(Double.NaN);
    }
}
