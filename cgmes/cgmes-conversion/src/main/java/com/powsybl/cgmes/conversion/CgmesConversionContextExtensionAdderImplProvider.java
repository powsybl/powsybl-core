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
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
@AutoService(ExtensionAdderProvider.class)
public class CgmesConversionContextExtensionAdderImplProvider implements
        ExtensionAdderProvider<Network, CgmesConversionContextExtension, CgmesConversionContextExtensionAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return CgmesConversionContextExtension.NAME;
    }

    @Override
    public Class<CgmesConversionContextExtensionAdderImpl> getAdderClass() {
        return CgmesConversionContextExtensionAdderImpl.class;
    }

    @Override
    public CgmesConversionContextExtensionAdderImpl newAdder(Network extendable) {
        return new CgmesConversionContextExtensionAdderImpl(extendable);
    }

}
