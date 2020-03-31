/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

import java.util.List;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface RegulatingControlList extends Extension<Network> {

    @Override
    default String getName() {
        return "regulatingControlList";
    }

    RegulatingControlAdder newRegulatingControl();

    RegulatingControl getRegulatingControl(String id);

    List<RegulatingControl> getRegulatingControls();
}
