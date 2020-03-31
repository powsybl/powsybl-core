/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.regulation.RegulatingControlList;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class RegulatingControlListAdderImplProvider implements ExtensionAdderProvider<Network, RegulatingControlList, RegulatingControlListAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public Class<? super RegulatingControlListAdderImpl> getAdderClass() {
        return RegulatingControlListAdderImpl.class;
    }

    @Override
    public RegulatingControlListAdderImpl newAdder(Network extendable) {
        return new RegulatingControlListAdderImpl(extendable);
    }
}
