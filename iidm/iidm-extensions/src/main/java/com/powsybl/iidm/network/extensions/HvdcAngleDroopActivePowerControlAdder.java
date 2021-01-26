/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.HvdcLine;

/**
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public interface HvdcAngleDroopActivePowerControlAdder extends ExtensionAdder<HvdcLine, HvdcAngleDroopActivePowerControl> {

    @Override
    default Class<HvdcAngleDroopActivePowerControl> getExtensionClass() {
        return HvdcAngleDroopActivePowerControl.class;
    }

    HvdcAngleDroopActivePowerControlAdder withP0(float p0);

    HvdcAngleDroopActivePowerControlAdder withDroop(float droop);

    HvdcAngleDroopActivePowerControlAdder withEnabled(boolean enabled);
}
