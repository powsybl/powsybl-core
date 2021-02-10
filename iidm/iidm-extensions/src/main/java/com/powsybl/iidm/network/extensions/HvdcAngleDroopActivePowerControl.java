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
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public interface HvdcAngleDroopActivePowerControl extends Extension<HvdcLine> {
    @Override
    default String getName() {
        return "hvdcAngleDroopActivePowerControl";
    }

    float getP0();

    float getDroop();

    boolean isEnabled();

    HvdcAngleDroopActivePowerControl setP0(float p0);

    HvdcAngleDroopActivePowerControl setDroop(float droop);

    HvdcAngleDroopActivePowerControl setEnabled(boolean enabled);

}
