/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.LimitViolationUtils;

import java.util.Set;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public abstract class AbstractSecurityAnalysis implements SecurityAnalysis {

    private static LimitViolation checkPermanentLimit(Branch branch, Branch.Side side, double value) {

        Terminal t = branch.getTerminal(side);
        CurrentLimits limits = branch.getCurrentLimits(side);

        if (LimitViolationUtils.checkPermanentLimit(t, limits, 1, value)) {
            return new LimitViolation(branch.getId(),
                    LimitViolationType.CURRENT,
                    null,
                    Integer.MAX_VALUE,
                    branch.getCurrentLimits(side).getPermanentLimit(),
                    1,
                    value,
                    side);
        }
        return null;
    }

    protected static LimitViolation checkCurrentLimits(Branch branch, Branch.Side side, Set<Security.CurrentLimitType> currentLimitTypes, double value) {

        Terminal t = branch.getTerminal(side);
        CurrentLimits limits = branch.getCurrentLimits(side);

        Branch.Overload overload;
        overload = LimitViolationUtils.checkTemporaryLimits(t, limits, 1, value);
        if (currentLimitTypes.contains(Security.CurrentLimitType.TATL) && (overload != null)) {
            return new LimitViolation(branch.getId(),
                    LimitViolationType.CURRENT,
                    overload.getPreviousLimitName(),
                    overload.getTemporaryLimit().getAcceptableDuration(),
                    overload.getPreviousLimit(),
                    1,
                    value,
                    side);
        } else if (currentLimitTypes.contains(Security.CurrentLimitType.PATL)) {
            return checkPermanentLimit(branch, side, value);
        }

        return null;
    }

    protected static LimitViolation checkLimits(VoltageLevel vl, float value) {
        if (value < vl.getLowVoltageLimit()) {
            return new LimitViolation(vl.getId(), LimitViolationType.LOW_VOLTAGE, vl.getLowVoltageLimit(), 1, value);
        } else {
            return new LimitViolation(vl.getId(), LimitViolationType.HIGH_VOLTAGE, vl.getLowVoltageLimit(), 1, value);
        }
    }
}
