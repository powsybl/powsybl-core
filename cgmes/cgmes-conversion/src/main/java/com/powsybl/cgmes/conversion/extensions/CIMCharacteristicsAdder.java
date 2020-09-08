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
public interface CIMCharacteristicsAdder extends ExtensionAdder<Network, CIMCharacteristics> {

    CIMCharacteristicsAdder setTopologyKind(CgmesTopologyKind cgmesTopologyKind);

    CIMCharacteristicsAdder setCimVersion(int cimVersion);

    @Override
    default Class<CIMCharacteristics> getExtensionClass() {
        return CIMCharacteristics.class;
    }
}
