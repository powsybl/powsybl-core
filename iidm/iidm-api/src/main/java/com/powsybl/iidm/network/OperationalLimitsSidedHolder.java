/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collections;
import java.util.List;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface OperationalLimitsSidedHolder {

    default List<OperationalLimits> getOperationalLimits1() {
        return Collections.emptyList();
    }

    default CurrentLimits getCurrentLimits1() {
        return null;
    }

    default ActivePowerLimits getActivePowerLimits1() {
        return null;
    }

    default ApparentPowerLimits getApparentPowerLimits1() {
        return null;
    }

    default VoltageLimits getVoltageLimits1() {
        return null;
    }

    default CurrentLimits getCurrentLimits2() {
        return null;
    }

    default ActivePowerLimits getActivePowerLimits2() {
        return null;
    }

    default ApparentPowerLimits getApparentPowerLimits2() {
        return null;
    }

    default VoltageLimits getVoltageLimits2() {
        return null;
    }

    default CurrentLimits getCurrentLimits(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return getCurrentLimits1();
        } else if (side == Branch.Side.TWO) {
            return getCurrentLimits2();
        }
        throw new AssertionError("Unexpected side: " + side);
    }

    default ActivePowerLimits getActivePowerLimits(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return getActivePowerLimits1();
        } else if (side == Branch.Side.TWO) {
            return getActivePowerLimits2();
        }
        throw new AssertionError("Unexpected side: " + side);
    }

    default ApparentPowerLimits getApparentPowerLimits(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return getApparentPowerLimits1();
        } else if (side == Branch.Side.TWO) {
            return getApparentPowerLimits2();
        }
        throw new AssertionError("Unexpected side: " + side);
    }

    default VoltageLimits getVoltageLimits(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return getVoltageLimits1();
        } else if (side == Branch.Side.TWO) {
            return getVoltageLimits2();
        }
        throw new AssertionError("Unexpected side: " + side);
    }

    default CurrentLimitsAdder newCurrentLimits1() {
        throw new UnsupportedOperationException();
    }

    default ActivePowerLimitsAdder newActivePowerLimits1() {
        throw new UnsupportedOperationException();
    }

    default ApparentPowerLimitsAdder newApparentPowerLimits1() {
        throw new UnsupportedOperationException();
    }

    default VoltageLimitsAdder newVoltageLimits1() {
        throw new UnsupportedOperationException();
    }

    default List<OperationalLimits> getOperationalLimits2() {
        return Collections.emptyList();
    }

    default CurrentLimitsAdder newCurrentLimits2() {
        throw new UnsupportedOperationException();
    }

    default ActivePowerLimitsAdder newActivePowerLimits2() {
        throw new UnsupportedOperationException();
    }

    default ApparentPowerLimitsAdder newApparentPowerLimits2() {
        throw new UnsupportedOperationException();
    }

    default VoltageLimitsAdder newVoltageLimits2() {
        throw new UnsupportedOperationException();
    }
}
