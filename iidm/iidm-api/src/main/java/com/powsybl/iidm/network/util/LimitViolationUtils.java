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

import java.util.Objects;

/**
 *
 * Helper methods for checking the occurence of overloads.
 *
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public final class LimitViolationUtils {

    private LimitViolationUtils() {
    }

    /**
     * @deprecated Since 4.3.0, use {@link #checkTemporaryLimits(Branch, Branch.Side, float, double, LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    public static Branch.Overload checkTemporaryLimits(Branch branch, Branch.Side side, float limitReduction, double i) {
        return checkTemporaryLimits(branch, side, limitReduction, i, LimitType.CURRENT);
    }

    public static Branch.Overload checkTemporaryLimits(Branch<?> branch, Branch.Side side, float limitReduction, double i, LimitType type) {
        Objects.requireNonNull(branch);
        Objects.requireNonNull(side);

        if (!Double.isNaN(i)) {
            return branch.getLimits(type, side)
                    .filter(l -> !Double.isNaN(l.getPermanentLimit()))
                    .map(limits -> {
                        String previousLimitName = null;
                        double previousLimit = limits.getPermanentLimit();
                        for (LoadingLimits.TemporaryLimit tl : limits.getTemporaryLimits()) { // iterate in ascending order
                            if (i >= previousLimit * limitReduction && i < tl.getValue() * limitReduction) {
                                return new OverloadImpl(tl, previousLimitName, previousLimit);
                            }
                            previousLimitName = tl.getName();
                            previousLimit = tl.getValue();
                        }
                        return null;
                    })
                    .orElse(null);
        }
        return null;
    }

    /**
     * @deprecated Since 4.3.0, use {@link #checkPermanentLimit(Branch, Branch.Side, float, double, LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    public static boolean checkPermanentLimit(Branch branch, Branch.Side side, float limitReduction, double i) {
        return checkPermanentLimit(branch, side, limitReduction, i, LimitType.CURRENT);
    }

    public static boolean checkPermanentLimit(Branch<?> branch, Branch.Side side, float limitReduction, double i, LimitType type) {
        return branch.getLimits(type, side)
                .map(l -> !Double.isNaN(l.getPermanentLimit()) &&
                        !Double.isNaN(i) &&
                        (i >= l.getPermanentLimit() * limitReduction))
                .orElse(false);
    }

}
