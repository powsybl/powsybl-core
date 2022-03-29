/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 *
 * WARNING: this class is still in a beta version, it will change in the future
 */
public interface CgmesIidmMappingAdder extends ExtensionAdder<Network, CgmesIidmMapping> {

    CgmesIidmMappingAdder addTopologicalNode(String topologicalNodeId, String topologicalNodeName, Source source);

    @Override
    default Class<CgmesIidmMapping> getExtensionClass() {
        return CgmesIidmMapping.class;
    }
}
