/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;

import java.util.Collection;
import java.util.Objects;

/**
 *
 * Helper methods for checking the occurence of overloads.
 *
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public final class LimitViolationUtils {

    public static final String PERMANENT_LIMIT_NAME = "permanent";

    private LimitViolationUtils() {
    }

    public static Branch.Overload checkTemporaryLimits(Branch<?> branch, Branch.Side side, float limitReduction, double i, LimitType type) {
        Objects.requireNonNull(branch);
        Objects.requireNonNull(side);

        if (!Double.isNaN(i)) {
            return branch.getLimits(type, side)
                    .filter(l -> !Double.isNaN(l.getPermanentLimit()))
                    .map(limits -> getOverload(limits, i, limitReduction))
                    .orElse(null);
        }
        return null;
    }

    private static OverloadImpl getOverload(LoadingLimits limits, double i, float limitReduction) {
        double permanentLimit = limits.getPermanentLimit();
        Collection<LoadingLimits.TemporaryLimit> temporaryLimits = limits.getTemporaryLimits();
        String previousLimitName = PERMANENT_LIMIT_NAME;
        double previousLimit = permanentLimit;
        for (LoadingLimits.TemporaryLimit tl : temporaryLimits) { // iterate in ascending order
            if (i >= previousLimit * limitReduction && i < tl.getValue() * limitReduction) {
                return new OverloadImpl(tl, previousLimitName, previousLimit);
            }
            previousLimitName = tl.getName();
            previousLimit = tl.getValue();
        }
        return null;
    }

    public static boolean checkPermanentLimit(Branch<?> branch, Branch.Side side, float limitReduction, double i, LimitType type) {
        return branch.getLimits(type, side)
                .map(l -> !Double.isNaN(l.getPermanentLimit()) &&
                        !Double.isNaN(i) &&
                        i >= l.getPermanentLimit() * limitReduction)
                .orElse(false);
    }

}
