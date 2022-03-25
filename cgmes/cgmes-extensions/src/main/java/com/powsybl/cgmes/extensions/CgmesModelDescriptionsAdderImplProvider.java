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
public class CgmesModelDescriptionsAdderImplProvider implements
        ExtensionAdderProvider<Network, CgmesModelDescriptions, CgmesModelDescriptionsAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return CgmesModelDescriptions.NAME;
    }

    @Override
    public Class<? super CgmesModelDescriptionsAdderImpl> getAdderClass() {
        return CgmesModelDescriptionsAdderImpl.class;
    }

    @Override
    public CgmesModelDescriptionsAdderImpl newAdder(Network extendable) {
        return new CgmesModelDescriptionsAdderImpl(extendable);
    }
}
