/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class BranchUtil {

    private BranchUtil() {
    }

    static Terminal getTerminal(String voltageLevelId, Terminal terminal1, Terminal terminal2) {
        Objects.requireNonNull(voltageLevelId);
        boolean side1 = terminal1.getVoltageLevel().getId().equals(voltageLevelId);
        boolean side2 = terminal2.getVoltageLevel().getId().equals(voltageLevelId);
        if (side1 && side2) {
            throw new PowsyblException("Both terminals are connected to voltage level " + voltageLevelId);
        } else if (side1) {
            return terminal1;
        } else if (side2) {
            return terminal2;
        } else {
            throw new PowsyblException("No terminal connected to voltage level " + voltageLevelId);
        }
    }

    public static Branch.Side getSide(Terminal terminal, Terminal terminal1, Terminal terminal2) {
        Objects.requireNonNull(terminal);
        if (terminal1 == terminal) {
            return Branch.Side.ONE;
        } else if (terminal2 == terminal) {
            return Branch.Side.TWO;
        } else {
            throw new IllegalStateException("The terminal is not connected to this branch");
        }
    }

    static <T> T getFromSide(Branch.Side side, Supplier<T> getter1, Supplier<T> getter2) {
        Objects.requireNonNull(side);
        if (side == Branch.Side.ONE) {
            return getter1.get();
        } else if (side == Branch.Side.TWO) {
            return getter2.get();
        }
        throw new IllegalStateException("Unexpected side: " + side);
    }

    static int getOverloadDuration(Branch.Overload o1, Branch.Overload o2) {
        int duration1 = o1 != null ? o1.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        int duration2 = o2 != null ? o2.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        return Math.min(duration1, duration2);
    }

    static double getValueForLimit(Terminal t, LimitType type) {
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
