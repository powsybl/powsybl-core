/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.cgmes.conversion.extensions.CgmesSshControlAreasImpl.ControlArea;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public interface CgmesSshControlAreasAdder extends ExtensionAdder<Network, CgmesSshControlAreas> {

    CgmesSshControlAreasAdder addControlArea(ControlArea controlArea);

    @Override
    default Class<CgmesSshControlAreas> getExtensionClass() {
        return CgmesSshControlAreas.class;
    }
}
