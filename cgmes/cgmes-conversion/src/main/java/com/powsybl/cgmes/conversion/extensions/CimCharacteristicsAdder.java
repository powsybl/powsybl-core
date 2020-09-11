/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.cgmes.conversion.elements.CgmesTopologyKind;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface CimCharacteristicsAdder extends ExtensionAdder<Network, CimCharacteristics> {

    CimCharacteristicsAdder setTopologyKind(CgmesTopologyKind cgmesTopologyKind);

    CimCharacteristicsAdder setCimVersion(int cimVersion);

    @Override
    default Class<CimCharacteristics> getExtensionClass() {
        return CimCharacteristics.class;
    }
}
