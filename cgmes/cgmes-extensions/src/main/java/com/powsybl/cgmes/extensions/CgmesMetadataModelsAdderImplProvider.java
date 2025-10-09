/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@AutoService(ExtensionAdderProvider.class)
public class CgmesMetadataModelsAdderImplProvider implements
        ExtensionAdderProvider<Network, CgmesMetadataModels, CgmesMetadataModelsAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return CgmesMetadataModels.NAME;
    }

    @Override
    public Class<? super CgmesMetadataModelsAdderImpl> getAdderClass() {
        return CgmesMetadataModelsAdderImpl.class;
    }

    @Override
    public CgmesMetadataModelsAdderImpl newAdder(Network extendable) {
        return new CgmesMetadataModelsAdderImpl(extendable);
    }
}
