/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.CurrentLimits;
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

        LoadingLimits limits = branch.getLimits(type, side).orElse(null);

        if (limits != null && !Double.isNaN(limits.getPermanentLimit()) && !Double.isNaN(i)) {
            String previousLimitName = null;
            double previousLimit = limits.getPermanentLimit();
            for (CurrentLimits.TemporaryLimit tl : limits.getTemporaryLimits()) { // iterate in ascending order
                if (i >= previousLimit * limitReduction && i < tl.getValue() * limitReduction) {
                    return new OverloadImpl(tl, previousLimitName, previousLimit);
                }
                previousLimitName = tl.getName();
                previousLimit = tl.getValue();
            }
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
        LoadingLimits limits = branch.getLimits(type, side).orElse(null);
        return limits != null &&
                !Double.isNaN(limits.getPermanentLimit()) &&
                !Double.isNaN(i) &&
                (i >= limits.getPermanentLimit() * limitReduction);
    }

}
