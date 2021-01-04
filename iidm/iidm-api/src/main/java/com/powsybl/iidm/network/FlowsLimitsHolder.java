/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface FlowsLimitsHolder {

    default Collection<OperationalLimits> getOperationalLimits() {
        return getCurrentLimits() != null ? Collections.singletonList(getCurrentLimits()) : Collections.emptyList();
    }

    CurrentLimits getCurrentLimits();

    default ActivePowerLimits getActivePowerLimits() {
        return null;
    }

    default ApparentPowerLimits getApparentPowerLimits() {
        return null;
    }

    CurrentLimitsAdder newCurrentLimits();

    default ApparentPowerLimitsAdder newApparentPowerLimits() {
        throw new UnsupportedOperationException();
    }

    default ActivePowerLimitsAdder newActivePowerLimits() {
        throw new UnsupportedOperationException();
    }
}
