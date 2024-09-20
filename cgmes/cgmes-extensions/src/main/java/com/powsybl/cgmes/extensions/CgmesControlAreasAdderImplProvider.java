/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
@AutoService(ExtensionAdderProvider.class)
public class CgmesControlAreasAdderImplProvider implements ExtensionAdderProvider<Network, CgmesControlAreas, CgmesControlAreasAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return CgmesControlAreas.NAME;
    }

    @Override
    public Class<? super CgmesControlAreasAdderImpl> getAdderClass() {
        return CgmesControlAreasAdderImpl.class;
    }

    @Override
    public CgmesControlAreasAdderImpl newAdder(Network extendable) {
        return new CgmesControlAreasAdderImpl(extendable);
    }

}
