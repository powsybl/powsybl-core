/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class CgmesMetadataAdderImplProvider implements
        ExtensionAdderProvider<Network, CgmesMetadata, CgmesMetadataAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return CgmesMetadata.NAME;
    }

    @Override
    public Class<? super CgmesMetadataAdderImpl> getAdderClass() {
        return CgmesMetadataAdderImpl.class;
    }

    @Override
    public CgmesMetadataAdderImpl newAdder(Network extendable) {
        return new CgmesMetadataAdderImpl(extendable);
    }
}
