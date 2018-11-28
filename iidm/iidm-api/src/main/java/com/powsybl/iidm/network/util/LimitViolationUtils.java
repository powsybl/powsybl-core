/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.CurrentLimits;

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

    public static Branch.Overload checkTemporaryLimits(Branch branch, Branch.Side side, float limitReduction, double i) {
        Objects.requireNonNull(branch);
        Objects.requireNonNull(side);

        CurrentLimits limits = branch.getCurrentLimits(side);

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

    public static boolean checkPermanentLimit(Branch branch, Branch.Side side, float limitReduction, double i) {
        CurrentLimits limits = branch.getCurrentLimits(side);
        return limits != null &&
                !Double.isNaN(limits.getPermanentLimit()) &&
                !Double.isNaN(i) &&
                (i >= limits.getPermanentLimit() * limitReduction);
    }

}
