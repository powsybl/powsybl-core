/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.HvdcLine;

/**
 * @author Paul Bui-Quang {@literal <paul.buiquang at rte-france.com>}
 */
public interface HvdcAngleDroopActivePowerControl extends Extension<HvdcLine> {

    String NAME = "hvdcAngleDroopActivePowerControl";

    @Override
    default String getName() {
        return NAME;
    }

    float getP0();

    float getDroop();

    boolean isEnabled();

    default HvdcAngleDroopActivePowerControl setP0(float p0) {
        return setP0(p0, false);
    }

    HvdcAngleDroopActivePowerControl setP0(float p0, boolean dryRun);

    default HvdcAngleDroopActivePowerControl setDroop(float droop) {
        return setDroop(droop, false);
    }

    HvdcAngleDroopActivePowerControl setDroop(float droop, boolean dryRun);

    default HvdcAngleDroopActivePowerControl setEnabled(boolean enabled) {
        return setEnabled(enabled, false);
    }

    HvdcAngleDroopActivePowerControl setEnabled(boolean enabled, boolean dryRun);

}
