/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;

/**
 * @author Jérémy LABOUS {@literal <jlabous at silicom.fr>}
 */
@AutoService(ExtensionAdderProvider.class)
public class CgmesModelExtensionAdderImplProvider implements
        ExtensionAdderProvider<Network, CgmesModelExtension, CgmesModelExtensionAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return CgmesModelExtension.NAME;
    }

    @Override
    public Class<CgmesModelExtensionAdderImpl> getAdderClass() {
        return CgmesModelExtensionAdderImpl.class;
    }

    @Override
    public CgmesModelExtensionAdderImpl newAdder(Network extendable) {
        return new CgmesModelExtensionAdderImpl(extendable);
    }
}
