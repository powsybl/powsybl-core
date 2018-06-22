/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class LimitViolationHelper {

    private LimitViolationHelper() {
    }

    private static VoltageLevel getVoltageLevel(LimitViolation limitViolation, Network network) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(limitViolation);

        Identifiable identifiable = network.getIdentifiable(limitViolation.getSubjectId());
        if (identifiable instanceof Branch) {
            Branch branch = (Branch) identifiable;
            return branch.getTerminal(limitViolation.getSide()).getVoltageLevel();
        } else if (identifiable instanceof Injection) {
            Injection injection = (Injection) identifiable;
            return injection.getTerminal().getVoltageLevel();
        } else if (identifiable instanceof VoltageLevel) {
            return (VoltageLevel) identifiable;
        } else if (identifiable instanceof Bus) {
            Bus bus = (Bus) identifiable;
            return bus.getVoltageLevel();
        } else {
            throw new AssertionError("Unexpected identifiable type: " + identifiable.getClass());
        }
    }

    public static Country getCountry(LimitViolation limitViolation, Network network) {
        VoltageLevel voltageLevel = getVoltageLevel(limitViolation, network);

        return voltageLevel.getSubstation().getCountry();
    }

    public static String getVoltageLevelId(LimitViolation limitViolation, Network network) {
        VoltageLevel voltageLevel = getVoltageLevel(limitViolation, network);

        return voltageLevel.getId();
    }

    public static double getNominalVoltage(LimitViolation limitViolation, Network network) {
        VoltageLevel voltageLevel = getVoltageLevel(limitViolation, network);

        return voltageLevel.getNominalV();
    }
}
