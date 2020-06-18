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
public interface OperationalLimitsHolder {

    default List<OperationalLimits> getOperationalLimits() {
        return Collections.emptyList();
    }

    default CurrentLimits getCurrentLimits() {
        return null;
    }

    default ActivePowerLimits getActivePowerLimits() {
        return null;
    }

    default ApparentPowerLimits getApparentPowerLimits() {
        return null;
    }

    default VoltageLimits getVoltageLimits() {
        return null;
    }

    default CurrentLimitsAdder newCurrentLimits() {
        throw new UnsupportedOperationException();
    }

    default ApparentPowerLimitsAdder newApparentPowerLimits() {
        throw new UnsupportedOperationException();
    }

    default ActivePowerLimitsAdder newActivePowerLimits() {
        throw new UnsupportedOperationException();
    }

    default VoltageLimitsAdder newVoltageLimits() {
        throw new UnsupportedOperationException();
    }
}
