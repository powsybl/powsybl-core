/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.regulation.RegulationList;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class RegulationListAdderImplProvider<C extends Connectable<C>> implements ExtensionAdderProvider<C, RegulationList<C>, RegulationListAdderImpl<C>> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public Class<? super RegulationListAdderImpl<C>> getAdderClass() {
        return RegulationListAdderImpl.class;
    }

    @Override
    public RegulationListAdderImpl<C> newAdder(C extendable) {
        return new RegulationListAdderImpl<>(extendable);
    }
}
