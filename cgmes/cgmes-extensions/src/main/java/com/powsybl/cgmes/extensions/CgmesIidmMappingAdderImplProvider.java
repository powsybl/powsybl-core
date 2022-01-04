/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class CgmesIidmMappingAdderImplProvider implements ExtensionAdderProvider<Network, CgmesIidmMapping, CgmesIidmMappingAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return CgmesIidmMapping.NAME;
    }

    @Override
    public Class<? super CgmesIidmMappingAdderImpl> getAdderClass() {
        return CgmesIidmMappingAdderImpl.class;
    }

    @Override
    public CgmesIidmMappingAdderImpl newAdder(Network extendable) {
        return new CgmesIidmMappingAdderImpl(extendable);
    }
}
