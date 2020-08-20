/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class SwitchToSwitch extends IidmToCgmes {

    public SwitchToSwitch() {
        simpleUpdate("open", "cim:Switch.open", CgmesSubset.STEADY_STATE_HYPOTHESIS);
        simpleUpdate("open1", "cim:ACDCTerminal.connected",
                CgmesSubset.STEADY_STATE_HYPOTHESIS, ss -> ss.getAliases().stream().filter(alias -> ss.getAliasType(alias).map(type -> type.equals("Terminal1")).orElse(false)).findFirst().orElse(null));
        simpleUpdate("open2", "cim:ACDCTerminal.connected",
                CgmesSubset.STEADY_STATE_HYPOTHESIS, ss -> ss.getAliases().stream().filter(alias -> ss.getAliasType(alias).map(type -> type.equals("Terminal2")).orElse(false)).findFirst().orElse(null));
        //TODO TP profile should be updated as well
    }
}
