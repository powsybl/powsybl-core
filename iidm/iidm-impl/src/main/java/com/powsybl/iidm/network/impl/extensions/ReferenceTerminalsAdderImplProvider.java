/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ReferenceTerminals;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class ReferenceTerminalsAdderImplProvider implements ExtensionAdderProvider<Network,
        ReferenceTerminals, ReferenceTerminalsAdderImpl> {
    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return ReferenceTerminals.NAME;
    }

    @Override
    public Class<? super ReferenceTerminalsAdderImpl> getAdderClass() {
        return ReferenceTerminalsAdderImpl.class;
    }

    @Override
    public ReferenceTerminalsAdderImpl newAdder(Network extendable) {
        return new ReferenceTerminalsAdderImpl(extendable);
    }
}
