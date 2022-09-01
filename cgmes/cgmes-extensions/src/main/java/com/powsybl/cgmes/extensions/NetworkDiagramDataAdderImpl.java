/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;


import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class NetworkDiagramDataAdderImpl extends AbstractExtensionAdder<Network, NetworkDiagramData> implements NetworkDiagramDataAdder {

    protected NetworkDiagramDataAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    protected NetworkDiagramData createExtension(Network extendable) {
        return new NetworkDiagramDataImpl();
    }
}
