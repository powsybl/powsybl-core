/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
@AutoService(ExtensionAdderProvider.class)
public class PsseModelExtensionAdderImplProvider implements
        ExtensionAdderProvider<Network, PsseModelExtension, PsseModelExtensionAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return PsseModelExtension.NAME;
    }

    @Override
    public Class<PsseModelExtensionAdderImpl> getAdderClass() {
        return PsseModelExtensionAdderImpl.class;
    }

    @Override
    public PsseModelExtensionAdderImpl newAdder(Network extendable) {
        return new PsseModelExtensionAdderImpl(extendable);
    }
}
