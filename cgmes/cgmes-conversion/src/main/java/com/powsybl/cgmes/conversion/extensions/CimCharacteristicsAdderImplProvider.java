/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class CimCharacteristicsAdderImplProvider implements ExtensionAdderProvider<Network, CimCharacteristics, CimCharacteristicsAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public Class<? super CimCharacteristicsAdderImpl> getAdderClass() {
        return CimCharacteristicsAdderImpl.class;
    }

    @Override
    public CimCharacteristicsAdderImpl newAdder(Network extendable) {
        return new CimCharacteristicsAdderImpl(extendable);
    }
}
